## Google OAuth 설정 (Supabase 의존성 없이 백엔드 직접 연동)

- 서버 베이스 URL: `https://honmoon-api.site`
- 프론트 사이트 URL: `https://honmoon.site`

### 1) Google Cloud Console 설정

- `승인된 리디렉션 URI`에 다음을 등록하세요:
  - `https://honmoon-api.site/api/auth/google/callback`
- 클라이언트 ID와 Client Secret을 발급받습니다.

주의: 기존 Supabase 콜백 URL(`https://srrbifwkihsblsqoiefy.supabase.co/auth/v1/callback`)은 사용하지 않습니다. 만약 백엔드가 아닌 프론트 직접 콜백을 사용하고 싶다면, 프론트 라우트에 콜백을 만들고 그 URL을 등록하면 됩니다. 이 레포에서는 백엔드 콜백을 사용합니다.

### 2) 환경 변수 설정

- `properties/env.properties` (local 프로필) 또는 운영 환경 변수에 아래 값을 등록하세요.

```
GOOGLE_REDIRECT_URI=https://honmoon-api.site/api/auth/google/callback
```

### 3) API 개요

- `GET /api/auth/google/url` : 구글 인증 URL과 state 발급 (프론트는 이 URL로 리다이렉트)
- `GET /api/auth/google/callback` : 구글이 전달한 `code`, `state`로 토큰 교환 및 사용자 정보 조회
- 이메일 매직 링크(샘플):
  - `POST /api/auth/email/magic-link` : 입력한 이메일로 매직 링크 발급 (실제 메일 전송 로직 필요)
  - `GET /api/auth/email/callback` : 매직 링크 토큰 검증 (샘플 구현)

### 4) 프론트 연동

- 1. `GET /api/auth/google/url` 호출 → 응답의 `authorizationUrl`로 브라우저 리다이렉트
- 2. 인증 후 구글이 백엔드 콜백(`/api/auth/google/callback`) 호출
- 3. 백엔드는 구글 액세스 토큰, 사용자 정보 응답. 필요 시 앱 세션 토큰(JWT 등) 생성하여 `appSessionToken`에 담아 반환하면 됩니다.

## OAuth Google Login 연동 가이드 (프론트엔드)

### 1) 인증 URL 생성

- Endpoint: `GET /api/auth/google/url`
- Query
  - `scope` (optional, default: `profile email openid`)
  - `redirectAfter` (optional): 로그인 성공 후 프론트 내 이동 경로
- Response

```json
{
  "success": true,
  "data": {
    "provider": "google",
    "authorizationUrl": "https://accounts.google.com/o/oauth2/v2/auth?...",
    "state": "<state>"
  }
}
```

- 프론트는 `authorizationUrl`로 리다이렉트합니다.

### 2) OAuth 콜백 처리

- Endpoint: `GET /api/auth/google/callback?code=...&state=...`
- Response

```json
{
  "success": true,
  "data": {
    "provider": "google",
    "google": {
      "sub": "<google-user-id>",
      "email": "...",
      "emailVerified": true,
      "name": "...",
      "givenName": "...",
      "familyName": "...",
      "picture": "..."
    },
    "googleTokens": {
      "accessToken": "...",
      "idToken": "...",
      "refreshToken": "...",
      "expiresInSeconds": 3599,
      "scope": "profile email openid",
      "tokenType": "Bearer"
    },
    "appSessionToken": "<server-session-token>",
    "jwt": null
  }
}
```

- 이후 API 호출 시 헤더 `Authorization: Bearer <appSessionToken>` 를 포함하세요.

### 3) 현재 사용자 조회

- Endpoint: `GET /api/auth/me`
- Header: `Authorization: Bearer <appSessionToken>` 또는 Basic Auth
- Response

```json
{
  "success": true,
  "data": {
    "sub": "...",
    "email": "...",
    "name": "...",
    "picture": "...",
    "provider": "google" | "basic"
  }
}
```

### 4) 로그아웃

- Endpoint: `POST /api/auth/logout`
- Header: `Authorization: Bearer <appSessionToken>`
- 서버 세션 토큰을 무효화합니다.

### 인증 방식 요약

- Basic 인증: 관리자용. 개발/운영 콘솔 등에서 사용. `Authorization: Basic base64(username:password)`
- Bearer 세션 토큰: 일반 사용자용. OAuth 콜백에서 받은 `appSessionToken` 사용.

## 보안/환경설정

- Basic 계정은 환경 변수 혹은 `properties/env.properties`에 설정

- CORS는 `CORS_ALLOWED_ORIGINS` 로 `allowedOriginPatterns` 설정

## 주석

- 모든 컨트롤러 메서드는 `@CurrentUser principal: UserPrincipal?` 파라미터로 현재 인증 정보를 주입받을 수 있습니다.

# Honmoon Backend

- Swagger UI: `/swagger-ui/index.html`
- OpenAPI JSON: `/v3/api-docs`

## 프론트엔드 연동 가이드

### 1) OAuth 로그인/회원가입 연동 (Google)

- 인증 URL 생성

  - GET `/api/auth/google/url?scope=profile email openid&redirectAfter=/`
  - 응답: `{ provider, url, state }`
  - 프론트는 `url`로 리다이렉트

- OAuth 콜백 처리

  - GET `/api/auth/google/callback?code=...&state=...`
  - 서버가 `code`로 토큰 교환 후 사용자 정보 조회, 서버 세션 토큰 발급
  - 응답 예시
    ```json
    {
      "provider": "google",
      "google": {
        "sub": "...",
        "email": "...",
        "name": "...",
        "picture": "..."
      },
      "googleTokens": {
        "accessToken": "...",
        "idToken": "...",
        "refreshToken": "...",
        "expiresInSeconds": 3599,
        "scope": "...",
        "tokenType": "Bearer"
      },
      "appSessionToken": "<서버세션토큰>",
      "jwt": null
    }
    ```
  - 프론트는 `Authorization: Bearer <appSessionToken>`로 세션 유지

- 현재 사용자 프로필

  - GET `/api/auth/me`
  - 헤더: `Authorization: Bearer <appSessionToken>`
  - 응답: `{ sub, email, name, picture, provider }`

- 로그아웃
  - POST `/api/auth/logout`
  - 헤더: `Authorization: Bearer <appSessionToken>`
  - 응답: `true`

### 2) 기능별 API 연동 플로우

세션 유지: 모든 보호된 API는 `Authorization: Bearer <appSessionToken>` 필요.

- 미션

  - 상세 조회: GET `/api/missions/{id}` → `{ id, title, points, missionType, ... }`
  - 미션 퀴즈 제출(내 계정):
    - POST `/api/user-activities/missions/{missionId}/submit-quiz/me`
    - form: `textAnswer?`, `selectedChoiceIndex?`, `uploadedImageUrl?`
    - 결과: 활동 기록 생성 `{ id, isCorrect, pointsEarned, ... }`
    - 정답이면 포인트 적립 및 `UserSummary.totalActivities` 증가

- 활동(User Activity)

  - 내 활동 목록: GET `/api/user-activities/me`
  - 내 최근 활동: GET `/api/user-activities/me/recent?limit=10`
  - 특정 사용자 활동 목록: GET `/api/user-activities/user/{userId}`
  - 장소별 활동 목록: GET `/api/user-activities/place/{placeId}`

- 포인트(Point History)

  - 내 포인트 내역: GET `/api/point-history/me`
  - 특정 사용자 포인트 내역: GET `/api/point-history/user/{userId}`
  - 획득 내역만: GET `/api/point-history/user/{userId}/earned`
  - 사용 내역만: GET `/api/point-history/user/{userId}/used`
  - 퀴즈 보상 적립(테스트용): POST `/api/point-history/earn/quiz?userId={userId}&quizId={quizId}&points={points}`
  - 래플 응모 차감(내 계정은 아래 래플 API 사용 권장)

- 래플(Raffle)

  - 상품 상세/목록/검색: 컨트롤러에 정의된 라우트 참고
  - 내 응모 목록: GET `/api/raffle-applications/me`
  - 내 응모: POST `/api/raffle-applications/me?raffleProductId={id}`
    - 차감 포인트: `raffle_product.point_cost`
  - 내 특정 상품 응모 여부: GET `/api/raffle-applications/me/product/{productId}`

- 사용자 요약(User Summary)
  - 내 요약: GET `/api/user-summary/me` → `{ totalPoints, totalActivities, ... }`
  - 내 포인트 현황: GET `/api/user-summary/me/points`
    - 응답: `{ currentPoints, totalEarned, totalUsed }`
  - 내 퀴즈 통계: GET `/api/user-summary/me/quiz-stats`
    - 응답: `{ totalQuizzes, correctQuizzes, accuracy, totalPointsEarned }`
  - 내 미션 통계: GET `/api/user-summary/me/mission-stats`
    - 응답: `{ totalMissions, completedMissions, completionRate, totalPointsEarned }`

### 권장 호출 순서 예시

1. 로그인 버튼 클릭 → GET `/api/auth/google/url` → `url`로 리다이렉트
2. OAuth 콜백 수신 → GET `/api/auth/google/callback?code&state` → `appSessionToken` 수신
3. 전역 헤더 설정 `Authorization: Bearer <appSessionToken>`
4. 홈 진입 시 내 요약/포인트/최근활동 선조회
   - GET `/api/user-summary/me`
   - GET `/api/user-summary/me/points`
   - GET `/api/user-activities/me/recent?limit=10`
5. 미션 상세 진입 → GET `/api/missions/{id}` → 제출 → POST `/api/user-activities/missions/{id}/submit-quiz/me`
6. 래플 진입 → 목록/상세 조회 → POST `/api/raffle-applications/me?raffleProductId={id}` → GET `/api/raffle-applications/me/product/{id}`

### 에러 처리

- 공통 에러 응답 스펙: `Response<T>` 래핑
- 404: 리소스 없음, 400: 파라미터/검증 오류, 401: 인증 필요, 409: 중복 등
