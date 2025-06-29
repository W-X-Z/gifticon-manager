# 🎁 기프티콘 매니저 - Android 앱

Kotlin + Jetpack Compose로 개발한 기프티콘 관리 앱

## 📱 주요 기능

- **스마트 등록**: 카메라로 기프티콘 촬영하여 AI 자동 분석
- **만료일 관리**: 만료 예정 기프티콘 알림
- **잔액 추적**: 사용 내역 기록 및 잔액 관리
- **카테고리 분류**: 브랜드별, 카테고리별 정리
- **검색 기능**: 브랜드명, 상품명으로 빠른 검색

## 🛠 기술 스택

- **언어**: Kotlin
- **UI**: Jetpack Compose + Material Design 3
- **아키텍처**: MVVM + Clean Architecture
- **데이터베이스**: Room (SQLite)
- **네트워킹**: Retrofit + OkHttp
- **의존성 주입**: Hilt
- **이미지 로딩**: Coil
- **카메라**: CameraX
- **바코드 인식**: ML Kit

## 📂 프로젝트 구조

```
app/src/main/java/com/gifticon/manager/
├── data/
│   ├── api/              # API 서비스 및 데이터 모델
│   ├── database/         # Room 데이터베이스
│   ├── model/            # 데이터 모델
│   └── repository/       # 리포지토리 패턴
├── di/                   # Hilt 의존성 주입 모듈
├── ui/
│   ├── navigation/       # Navigation Compose
│   ├── screen/           # UI 스크린
│   ├── theme/            # Material Design 테마
│   └── viewmodel/        # ViewModel
├── GifticonApplication.kt
└── MainActivity.kt
```

## 🚀 빌드 및 실행

### 요구사항
- Android Studio Flamingo 이상
- Kotlin 1.9.0 이상
- Android API 24 (Android 7.0) 이상

### 빌드 방법
1. **프로젝트 클론**
   ```bash
   git clone <repository-url>
   cd gifticon-manager/android-app
   ```

2. **Android Studio에서 열기**
   - Android Studio 실행
   - "Open an existing project" 선택
   - `android-app` 폴더 선택

3. **빌드 및 실행**
   ```bash
   ./gradlew assembleDebug
   ```
   또는 Android Studio에서 Run 버튼 클릭

### 환경 설정
- `NetworkModule.kt`에서 백엔드 API URL 설정
- 필요한 권한: 카메라, 저장소, 알림

## 📋 주요 화면

### 1. 홈 화면
- 보유 기프티콘 목록 표시
- 만료 예정 기프티콘 강조
- FAB로 새 기프티콘 추가

### 2. 기프티콘 추가 (개발 예정)
- 카메라 촬영 또는 갤러리 선택
- AI 자동 정보 추출
- 수동 편집 가능

### 3. 상세 화면 (개발 예정)
- 기프티콘 상세 정보
- 잔액 사용 기록
- 편집/삭제 기능

## 🔧 개발 진행 상황

- ✅ 프로젝트 구조 설정
- ✅ 데이터 모델 및 Room 데이터베이스
- ✅ API 서비스 연동
- ✅ 홈 화면 기본 구현
- ⏳ 카메라 촬영 기능
- ⏳ AI 이미지 분석 연동
- ⏳ 기프티콘 상세 화면
- ⏳ 알림 시스템
- ⏳ 검색 및 필터링

## 🧪 테스트

```bash
# 단위 테스트
./gradlew test

# UI 테스트 (기기 연결 필요)
./gradlew connectedAndroidTest
```

## 📦 패키징

### Debug APK
```bash
./gradlew assembleDebug
```

### Release APK
```bash
./gradlew assembleRelease
```

## 🔐 권한

앱에서 사용하는 권한:
- `CAMERA`: 기프티콘 촬영
- `READ_EXTERNAL_STORAGE`: 갤러리 이미지 접근
- `INTERNET`: API 통신
- `POST_NOTIFICATIONS`: 만료 알림 (Android 13+)

## 🤝 기여하기

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 라이선스

MIT License 