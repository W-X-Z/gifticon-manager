# AdMob 설정 및 배포 가이드

## 현재 설정 상태

### ✅ 완료된 설정
1. **AdMob 의존성 추가**: `build.gradle.kts`에 `play-services-ads:22.6.0` 추가
2. **AndroidManifest.xml 설정**: AdMob 앱 ID 메타데이터 추가
3. **Application 클래스 초기화**: `GifticonApplication.kt`에 AdMob 초기화 코드 추가
4. **광고 배너 컴포넌트**: `HomeScreen.kt`에 광고 배너 추가
5. **광고 유틸리티 클래스**: `AdMobUtil.kt` 생성

### 📍 광고 배치 위치
- **위치**: 첫 번째 기프티콘 섹션 하단 (LazyColumn의 첫 번째 아이템)
- **형태**: 가로로 긴 배너 광고 (BANNER 크기)
- **디자인**: 카드 형태로 감싸서 앱 디자인과 통일성 유지

## 🚀 플레이스토어 배포 준비사항

### 1. AdMob 계정 설정
1. [AdMob 콘솔](https://admob.google.com/)에 로그인
2. 새 앱 등록 (Android)
3. 배너 광고 단위 생성
4. 실제 광고 ID 획득

### 2. 코드 수정 필요사항

#### AdMobUtil.kt 수정
```kotlin
// 실제 광고 ID로 변경
const val PROD_BANNER_AD_UNIT_ID = "ca-app-pub-실제광고ID"
const val PROD_APP_ID = "ca-app-pub-실제앱ID"

fun getBannerAdUnitId(): String {
    return if (BuildConfig.DEBUG) {
        TEST_BANNER_AD_UNIT_ID
    } else {
        PROD_BANNER_AD_UNIT_ID
    }
}

fun getAppId(): String {
    return if (BuildConfig.DEBUG) {
        TEST_APP_ID
    } else {
        PROD_APP_ID
    }
}
```

#### AndroidManifest.xml 수정
```xml
<meta-data
    android:name="com.google.android.gms.ads.APPLICATION_ID"
    android:value="실제앱ID" />
```

### 3. 빌드 설정
1. **Release 빌드**: `BuildConfig.DEBUG = false`로 설정
2. **ProGuard 설정**: 광고 관련 클래스 난독화 제외
3. **서명**: 릴리즈용 키스토어로 서명

### 4. 테스트 체크리스트
- [ ] 개발 환경에서 테스트 광고 표시 확인
- [ ] 광고 로드 실패 시 앱 크래시 없음 확인
- [ ] 광고 클릭 시 정상 동작 확인
- [ ] 네트워크 없을 때 광고 로드 실패 처리 확인

## 📱 추가 고려사항

### 1. 광고 정책 준수
- **광고 위치**: 사용자 경험을 해치지 않는 위치
- **광고 밀도**: 과도한 광고 배치 금지
- **콘텐츠 정책**: AdMob 정책 준수

### 2. 성능 최적화
- **지연 로딩**: 필요할 때만 광고 로드
- **메모리 관리**: 광고 객체 적절한 해제
- **네트워크 효율성**: 불필요한 광고 요청 최소화

### 3. 사용자 경험
- **로딩 상태**: 광고 로딩 중 적절한 표시
- **오류 처리**: 광고 로드 실패 시 자연스러운 처리
- **접근성**: 광고 접근성 고려

## 🔧 향후 개선사항

### 1. 광고 타입 확장
- 전면 광고 (앱 시작 시)
- 보상형 광고 (프리미엄 기능 해제)
- 네이티브 광고 (더 자연스러운 통합)

### 2. 광고 분석
- 광고 수익 추적
- 사용자 참여도 분석
- A/B 테스트를 통한 최적화

### 3. 개인화
- 사용자 선호도 기반 광고
- 지역별 광고 타겟팅
- 시간대별 광고 최적화

## ⚠️ 주의사항

1. **테스트 광고 ID**: 배포 전 반드시 실제 광고 ID로 변경
2. **정책 준수**: AdMob 정책 및 플레이스토어 정책 준수
3. **사용자 경험**: 광고가 앱 사용을 방해하지 않도록 주의
4. **법적 고지**: 개인정보 처리방침에 광고 관련 내용 포함

## 📞 지원

광고 관련 문제 발생 시:
1. AdMob 콘솔의 도움말 섹션 확인
2. Google AdMob 개발자 문서 참조
3. AdMob 지원팀 문의 