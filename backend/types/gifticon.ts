export interface GifticonInfo {
  id?: string;
  brandName: string;
  productName?: string;
  expiryDate: string;
  amount?: number;
  balance?: number;
  barcodeNumber?: string;
  category?: string;
  purchaseDate?: string;
  notes?: string;
}

export interface AnalyzeImageRequest {
  imageBase64: string;
  mimeType?: string;
}

export interface AnalyzeImageResponse {
  success: boolean;
  data?: GifticonInfo;
  error?: string;
  confidence?: number;
}

export interface ApiResponse<T = any> {
  success: boolean;
  data?: T;
  error?: string;
  timestamp: string;
}

export interface BarcodeInfo {
  type: string;
  value: string;
  format?: string;
}

export interface ProcessingResult {
  gifticon: GifticonInfo;
  barcode?: BarcodeInfo;
  extractedText?: string[];
  processingTime: number;
} 