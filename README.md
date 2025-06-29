# 🎁 기프티콘 매니저 (Gifticon Manager)

모바일 기프트콘을 스마트하게 관리하는 안드로이드 앱

## 📱 주요 기능

- **📸 스마트 등록**: 카메라로 기프티콘 촬영하여 자동 등록
- **🤖 AI 정보 추출**: OpenAI GPT-4 Vision으로 만료일/브랜드 자동 인식
- **⏰ 만료일 알림**: 푸시 알림으로 만료 전 미리 알림
- **💰 잔액 관리**: 사용 내역 기록 및 잔액 추적
- **🔍 바코드 스캔**: QR/바코드 자동 인식 및 분류

## 🛠 기술 스택

### Android App
- **언어**: Kotlin
- **UI**: Jetpack Compose + Material Design 3
- **데이터베이스**: Room (SQLite)
- **네트워킹**: Retrofit + OkHttp
- **카메라**: CameraX
- **이미지**: Glide
- **아키텍처**: MVVM + Clean Architecture

### Backend
- **플랫폼**: Vercel Functions
- **언어**: TypeScript
- **AI API**: OpenAI GPT-4o-2024-08-06
- **이미지 처리**: 바코드/OCR 인식

## 📂 프로젝트 구조

```
gifticon-manager/
├── android-app/          # Android 앱
├── backend/              # Vercel Functions 백엔드  
├── docs/                 # 문서
└── README.md
```

## 🚀 시작하기

### Android 앱 실행
```bash
cd android-app
./gradlew assembleDebug
```

### 백엔드 개발 서버
```bash
cd backend
npm install
vercel dev
```

## 📄 라이선스

MIT License

## 👥 기여하기

이슈와 PR을 환영합니다! 