# 🎁 기프티콘 매니저 백엔드

Vercel Functions 기반의 기프티콘 이미지 분석 API

## 🚀 API 엔드포인트

### 1. 헬스 체크
```
GET /api/health
```
서버 상태 확인

### 2. 이미지 분석
```
POST /api/analyze
```
기프티콘 이미지를 분석하여 정보 추출

**요청 Body:**
```json
{
  "imageBase64": "data:image/jpeg;base64,/9j/4AAQSkZJRg...",
  "mimeType": "image/jpeg"
}
```

**응답:**
```json
{
  "success": true,
  "data": {
    "brandName": "스타벅스",
    "productName": "아메리카노",
    "expiryDate": "2024-12-31",
    "amount": 4500,
    "balance": 4500,
    "barcodeNumber": "1234567890123",
    "category": "카페",
    "purchaseDate": "2024-01-15"
  },
  "confidence": 0.95
}
```

## 🛠 개발 환경 설정

### 1. 의존성 설치
```bash
npm install
```

### 2. 환경 변수 설정
```bash
# env.example을 참고하여 .env 파일 생성
cp env.example .env
```

필수 환경 변수:
- `NODE_ENV`: 환경 설정 (development/production)

### 3. 로컬 서버 실행
```bash
npm run dev
```

### 4. 배포
```bash
npm run deploy
```

## 📝 API 사용법

### cURL 예시
```bash
curl -X POST https://your-api.vercel.app/api/analyze \
  -H "Content-Type: application/json" \
  -d '{
    "imageBase64": "data:image/jpeg;base64,/9j/4AAQSkZJRg...",
    "mimeType": "image/jpeg"
  }'
```

### JavaScript 예시
```javascript
const analyzeImage = async (imageBase64) => {
  const response = await fetch('/api/analyze', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      imageBase64,
      mimeType: 'image/jpeg'
    })
  });
  
  return await response.json();
};
```

## 🔧 기술 스택

- **런타임**: Node.js 18+
- **플랫폼**: Vercel Functions
- **언어**: TypeScript
- **이미지 분석**: 클라이언트 OCR 기반
- **이미지 처리**: Sharp

## 📋 지원 기능

- ✅ 이미지 크기 최적화 (최대 1024px)
- ✅ 다양한 이미지 형식 지원 (JPEG, PNG, WebP)
- ✅ 구조화된 JSON 응답
- ✅ 클라이언트 OCR 기반 텍스트 추출
- ✅ CORS 지원
- ✅ 에러 핸들링

## 🚨 주의사항

- 이미지 크기 제한: 5MB
- API 요청 제한: Vercel Functions 정책에 따름
- 지원 언어: 한국어 기프티콘에 최적화 