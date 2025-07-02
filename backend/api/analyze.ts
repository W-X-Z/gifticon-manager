import { VercelRequest, VercelResponse } from '@vercel/node';
import { ImageProcessor } from '../utils/imageProcessor';
import { AnalyzeImageRequest, AnalyzeImageResponse, ApiResponse } from '../types/gifticon';

export default async function handler(req: VercelRequest, res: VercelResponse) {
  // CORS 헤더 설정
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'POST, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');

  // OPTIONS 요청 처리 (CORS Preflight)
  if (req.method === 'OPTIONS') {
    res.status(200).end();
    return;
  }

  // POST 요청만 허용
  if (req.method !== 'POST') {
    const response: ApiResponse = {
      success: false,
      error: 'POST 요청만 허용됩니다.',
      timestamp: new Date().toISOString()
    };
    return res.status(405).json(response);
  }

  try {
    // 요청 데이터 검증
    const { imageBase64, mimeType }: AnalyzeImageRequest = req.body;

    if (!imageBase64) {
      const response: ApiResponse = {
        success: false,
        error: 'imageBase64 파라미터가 필요합니다.',
        timestamp: new Date().toISOString()
      };
      return res.status(400).json(response);
    }

    // 이미지 형식 검증
    if (mimeType && !ImageProcessor.validateImageFormat(mimeType)) {
      const response: ApiResponse = {
        success: false,
        error: '지원하지 않는 이미지 형식입니다. (JPEG, PNG, WebP만 지원)',
        timestamp: new Date().toISOString()
      };
      return res.status(400).json(response);
    }

    // 이미지 크기 검증
    const imageBuffer = ImageProcessor.base64ToBuffer(imageBase64);
    if (!ImageProcessor.validateImageSize(imageBuffer)) {
      const response: ApiResponse = {
        success: false,
        error: '이미지 크기가 너무 큽니다. (최대 5MB)',
        timestamp: new Date().toISOString()
      };
      return res.status(400).json(response);
    }

    // OCR을 통한 텍스트 추출 (클라이언트에서 처리하므로 여기서는 기본 응답)
    const result = {
      brandName: "기프티콘",
      productName: undefined,
      expiryDate: "2024-12-31", // 클라이언트 OCR에서 추출
      amount: 0,
      balance: 0,
      barcodeNumber: undefined,
      category: "ETC",
      purchaseDate: undefined
    };

    // 성공 응답
    const response: AnalyzeImageResponse = {
      success: true,
      data: result,
      confidence: 0.85 // 기본값
    };

    res.status(200).json(response);

  } catch (error) {
    console.error('이미지 분석 API 오류:', error);

    const response: ApiResponse = {
      success: false,
      error: error instanceof Error ? error.message : '서버 내부 오류가 발생했습니다.',
      timestamp: new Date().toISOString()
    };

    res.status(500).json(response);
  }
} 