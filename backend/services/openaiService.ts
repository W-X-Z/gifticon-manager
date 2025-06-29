import OpenAI from 'openai';
import { GifticonInfo, ProcessingResult } from '../types/gifticon';
import { ImageProcessor } from '../utils/imageProcessor';

export class OpenAIService {
  private openai: OpenAI;

  constructor(apiKey: string) {
    this.openai = new OpenAI({
      apiKey: apiKey,
    });
  }

  /**
   * 기프티콘 이미지를 분석하여 정보 추출
   */
  async analyzeGifticonImage(base64Image: string): Promise<ProcessingResult> {
    const startTime = Date.now();

    try {
      // 이미지 최적화
      const imageBuffer = ImageProcessor.base64ToBuffer(base64Image);
      const optimizedBuffer = await ImageProcessor.optimizeImage(imageBuffer);
      const optimizedBase64 = optimizedBuffer.toString('base64');
      const imageUrl = ImageProcessor.prepareForOpenAI(optimizedBase64);

      // OpenAI GPT-4 Vision API 호출
      const response = await this.openai.chat.completions.create({
        model: "gpt-4o-2024-08-06",
        messages: [
          {
            role: "user",
            content: [
              {
                type: "text",
                text: this.getAnalysisPrompt()
              },
              {
                type: "image_url",
                image_url: {
                  url: imageUrl,
                  detail: "high"
                }
              }
            ]
          }
        ],
        response_format: {
          type: "json_schema",
          json_schema: {
            name: "gifticon_analysis",
            strict: true,
            schema: {
              type: "object",
              properties: {
                brandName: { type: "string" },
                productName: { type: "string" },
                expiryDate: { type: "string" },
                amount: { type: "number" },
                balance: { type: "number" },
                barcodeNumber: { type: "string" },
                category: { type: "string" },
                confidence: { type: "number" }
              },
              required: ["brandName", "expiryDate", "confidence"],
              additionalProperties: false
            }
          }
        },
        max_tokens: 500,
        temperature: 0.1
      });

      const content = response.choices[0]?.message?.content;
      if (!content) {
        throw new Error('OpenAI API에서 응답을 받지 못했습니다.');
      }

      const parsedData = JSON.parse(content);
      const processingTime = Date.now() - startTime;

      const gifticon: GifticonInfo = {
        brandName: parsedData.brandName,
        productName: parsedData.productName || '',
        expiryDate: parsedData.expiryDate,
        amount: parsedData.amount || 0,
        balance: parsedData.balance || parsedData.amount || 0,
        barcodeNumber: parsedData.barcodeNumber || '',
        category: parsedData.category || '기타',
        purchaseDate: new Date().toISOString().split('T')[0]
      };

      return {
        gifticon,
        processingTime,
        extractedText: []
      };

    } catch (error) {
      const processingTime = Date.now() - startTime;
      console.error('OpenAI 이미지 분석 오류:', error);
      
      throw new Error(
        error instanceof Error 
          ? `이미지 분석 실패: ${error.message}`
          : '이미지 분석 중 알 수 없는 오류가 발생했습니다.'
      );
    }
  }

  /**
   * 기프티콘 분석을 위한 프롬프트
   */
  private getAnalysisPrompt(): string {
    return `
이 이미지는 한국의 모바일 기프트콘(기프티콘) 이미지입니다. 다음 정보를 정확하게 추출해주세요:

1. **brandName**: 브랜드명 (예: 스타벅스, 투썸플레이스, CGV, 등)
2. **productName**: 상품명 (예: 아메리카노, 팝콘콤보, 등)
3. **expiryDate**: 만료일 (YYYY-MM-DD 형식)
4. **amount**: 금액/수량 (숫자만)
5. **balance**: 잔액 (숫자만, 초기값은 amount와 동일)
6. **barcodeNumber**: 바코드 또는 쿠폰 번호
7. **category**: 카테고리 (카페, 영화, 편의점, 치킨, 패스트푸드, 뷰티, 쇼핑몰, 기타 중 하나)
8. **confidence**: 추출한 정보의 정확도 (0.0-1.0)

주의사항:
- 만료일은 반드시 YYYY-MM-DD 형식으로 변환
- 금액은 원화 기호(₩)를 제거하고 숫자만
- 브랜드명은 정확한 한글 표기로
- 정보가 불분명한 경우 confidence를 낮게 설정
- 바코드 번호가 보이지 않으면 빈 문자열로 설정

정확하고 구조화된 JSON 형식으로만 응답해주세요.
    `.trim();
  }
} 