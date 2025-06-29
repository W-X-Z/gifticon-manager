import sharp from 'sharp';
import { Buffer } from 'node:buffer';

export class ImageProcessor {
  /**
   * Base64 이미지를 Buffer로 변환
   */
  static base64ToBuffer(base64String: string): Buffer {
    // data:image/jpeg;base64, 형태의 prefix 제거
    const base64Data = base64String.replace(/^data:image\/[a-z]+;base64,/, '');
    return Buffer.from(base64Data, 'base64');
  }

  /**
   * 이미지 크기 조정 및 최적화
   */
  static async optimizeImage(buffer: Buffer, maxWidth: number = 1024): Promise<Buffer> {
    return await sharp(buffer)
      .resize(maxWidth, null, {
        withoutEnlargement: true,
        fit: 'inside'
      })
      .jpeg({ quality: 85 })
      .toBuffer();
  }

  /**
   * 이미지 메타데이터 추출
   */
  static async getImageMetadata(buffer: Buffer) {
    return await sharp(buffer).metadata();
  }

  /**
   * 이미지 형식 검증
   */
  static validateImageFormat(mimeType: string): boolean {
    const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp'];
    return allowedTypes.includes(mimeType.toLowerCase());
  }

  /**
   * 이미지 크기 검증 (5MB 제한)
   */
  static validateImageSize(buffer: Buffer, maxSizeInMB: number = 5): boolean {
    const maxSizeInBytes = maxSizeInMB * 1024 * 1024;
    return buffer.length <= maxSizeInBytes;
  }

  /**
   * Base64 이미지를 OpenAI API 형식으로 변환
   */
  static prepareForOpenAI(base64String: string): string {
    // OpenAI API는 data:image/jpeg;base64, prefix가 필요
    if (base64String.startsWith('data:image/')) {
      return base64String;
    }
    return `data:image/jpeg;base64,${base64String}`;
  }
} 