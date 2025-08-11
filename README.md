## 프론트엔드 연동 가이드

아래 순서로 인증 → 이미지 업로드(필요 시) → 각 도메인 API를 호출하세요. 모든 보호된 API는 Authorization: Bearer 세션 토큰이 필요합니다.

### 1) 인증 (이메일 매직 링크)

- step 1. 매직 링크 요청: POST `/api/auth/login/email/by-user`
  - body: `{ "userId": "{UUID}" }`
  - 응답 `data.magicLink` 에 링크가 반환됨
- step 2. 백엔드 콜백 검증: GET `/api/auth/email/callback?token={TOKEN}&purpose=login`
  - 302 Location 헤더의 fragment(`#token=...`) 에서 `token` 값을 추출 → 이후 Authorization: `Bearer {token}` 로 사용
- 유틸: 현재 사용자 확인 GET `/api/auth/me` (Bearer 필요)

예시

```bash
curl -X POST "$HOST/api/auth/login/email/by-user" \
  -H 'Content-Type: application/json' \
  -d '{"userId":"2c0cd5f3-2993-4b71-a8da-5beeb79ad43c"}'

# magicLink 에서 token 추출 후
curl -i "$HOST/api/auth/email/callback?token=$TOKEN&purpose=login"
# Location 해시의 token 값을 Authorization: Bearer 로 사용
```

### 2) 이미지 업로드(필요 시)

- 프로필 이미지 업로드 URL 발급: POST `/api/user-summary/me/profile-image/upload-url?fileName={name}`
- 미션 이미지 업로드 URL 발급: POST `/api/missions/{missionId}/image/upload-url?fileName={name}`
- 발급된 `data.uploadUrl` 로 파일 바이트를 PUT 업로드
- 최종 사용 URL 구성
  - 프로필: `https://storage.googleapis.com/{bucket}/profiles/{data.fileName}`
  - 미션: `https://storage.googleapis.com/{bucket}/missions/{data.fileName}`

예시(업로드)

```bash
# presigned URL 발급 후
curl -X PUT "$UPLOAD_URL" \
  -H 'Content-Type: image/jpeg' \
  --data-binary @/absolute/path/to/file.jpg
```

### 3) 미션/장소

- 미션 상세: GET `/api/missions/{id}`
- 미션 답변 제출(텍스트): POST `/api/missions/{id}/submit-answer` body `{ "answer": "..." }`
- 미션 답변 제출(이미지): POST `/api/missions/{id}/submit-image-answer` body `{ "imageUrl": "..." }`
- 장소 상세/목록/검색/근처/장소별 미션:
  - GET `/api/mission-places/{id}`
  - GET `/api/mission-places`
  - GET `/api/mission-places/search?title=...`
  - GET `/api/mission-places/nearby?lat=..&lng=..&radius=..`
  - GET `/api/mission-places/{id}/missions`

### 4) 사용자 활동/퀴즈 제출

- 활동 상세/사용자별/장소별/생성/최근/내 활동:
  - GET `/api/user-activities/{id}`
  - GET `/api/user-activities/user/{userId}`
  - GET `/api/user-activities/place/{placeId}`
  - POST `/api/user-activities?userId={uuid}&placeId={id}&description=...`
  - GET `/api/user-activities/user/{userId}/recent?limit=10`
  - GET `/api/user-activities/me`, `/api/user-activities/me/recent?limit=10`
- 퀴즈 제출:
  - POST `/api/user-activities/missions/{missionId}/submit-quiz?userId={uuid}&textAnswer=...|selectedChoiceIndex=...|uploadedImageUrl=...`
  - POST `/api/user-activities/missions/{missionId}/submit-quiz/me?textAnswer=...|selectedChoiceIndex=...|uploadedImageUrl=...`

### 5) 래플

- 상품 상세/목록/검색/포인트범위/응모자수:
  - GET `/api/raffle-products/{id}`
  - GET `/api/raffle-products`
  - GET `/api/raffle-products/search?name=...`
  - GET `/api/raffle-products/by-points?minPoints=..&maxPoints=..`
  - GET `/api/raffle-products/{id}/applicants-count`
- 응모/내 응모/당첨자 선정/응모 상태:
  - POST `/api/raffle-applications?userId={uuid}&raffleProductId={id}`
  - POST `/api/raffle-applications/me?raffleProductId={id}`
  - POST `/api/raffle-applications/{productId}/draw?winnerCount=1`
  - GET `/api/raffle-applications/user/{userId}` `/api/raffle-applications/product/{productId}`
  - GET `/api/raffle-applications/user/{userId}/product/{productId}`
  - GET `/api/raffle-applications/me` `/api/raffle-applications/me/product/{productId}`

### 6) 포인트

- 상세/사용자별/획득/사용/내 내역:
  - GET `/api/point-history/{id}`
  - GET `/api/point-history/user/{userId}`
  - GET `/api/point-history/user/{userId}/earned`
  - GET `/api/point-history/user/{userId}/used`
  - GET `/api/point-history/me`
- 래플 응모 차감: POST `/api/point-history/use/raffle?userId={uuid}&raffleProductId={id}`

### 7) 사용자

- 사용자 프로필/포인트/퀴즈통계/미션통계:
  - GET `/api/users/{userId}`
  - GET `/api/users/{userId}/points`
  - GET `/api/users/{userId}/quiz-stats`
  - GET `/api/users/{userId}/mission-stats`
- 내 프로필/포인트/퀴즈통계/미션통계:
  - GET `/api/users/me`, `/api/users/me/points`, `/api/users/me/quiz-stats`, `/api/users/me/mission-stats`
- 프로필 업데이트/이미지 업데이트:
  - PATCH `/api/users/me` body `{ "nickname": "...", "profileImageUrl": "..." }`
  - PATCH `/api/users/me/profile-image?imageUrl=...`
  - PATCH `/api/users/{userId}/profile-image?imageUrl=...`

주의사항

### 1) 인증 URL 생성

- Endpoint: `GET /api/auth/google/url`
- Query
  - `scope` (optional, default: `profile email openid`)
  - `redirectAfter` (optional): 로그인 성공 후 프론트 내 이동 경로
- Response

````json
{
  "success": true,
  "data": {
    "provider": "google",
    "authorizationUrl": "https://accounts.google.com/o/oauth2/v2/auth?...",
    "state": "<state>"
  }
}
### 테스트용 세션 토큰 발급 (Basic)

- Endpoint: `POST /api/auth/test-token`
- 인증: Basic (`BASIC_AUTH_USERNAME`/`BASIC_AUTH_PASSWORD`)
- 응답 예시

```json
{
  "success": true,
  "data": {
    "token": "<server-session-token>",
    "expiresAt": "2025-01-01T00:00:00Z"
  }
}
````

받은 `token`을 `Authorization: Bearer <token>`로 설정해 다른 API를 테스트합니다.

````

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
````

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

불필요한 백엔드(yaml/환경설명 등)는 제거했습니다. 상세 스키마/예시는 Swagger UI(`/swagger-ui/index.html`) 또는 OpenAPI(`/v3/api-docs`)를 참고하세요.

# Honmoon Backend

- Swagger UI: `/swagger-ui/index.html`
- OpenAPI JSON: `/v3/api-docs`

## 프론트엔드 연동 가이드

### OAuth 회원가입/로그인 플로우 (Google)

1. 인증 URL 발급

- GET `/api/auth/google/url?scope=profile email openid&redirectAfter=/` → `authorizationUrl`, `state` 반환
- 프론트는 `authorizationUrl`로 즉시 리다이렉트

2. Google 콜백 → 백엔드에서 사용자 정보 및 세션 발급

- GET `/api/auth/google/callback?code=...&state=...`
- 응답: `appSessionToken`(서버 세션 토큰), `google`(프로필), `googleTokens`(원본 토큰)
  - 최초 로그인 시 자동 회원가입으로 간주(별도 가입 API 불필요)

3. 이후 인증이 필요한 모든 API 호출에 세션 사용

- 헤더: `Authorization: Bearer <appSessionToken>`
- 서버에서는 Spring Security를 통해 현재 세션 사용자 정보(`@CurrentUser`)를 주입받아 사용
- 별도 “로그인 상태 확인” API 없이도 보호된 API 호출이 가능

4. 로그아웃

- POST `/api/auth/logout` → 서버 세션 무효화

선택) 현재 사용자 프로필 조회

- GET `/api/auth/me` → 편의용 API (선택 사항). 백엔드는 `@CurrentUser`로 언제든지 접근 가능

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

### 기능별 API 연동 플로우 요약

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
  - 래플 응모 차감: POST `/api/point-history/use/raffle?userId={userId}&raffleProductId={id}`

- 래플(Raffle)

  - 상품 상세/목록/검색: 컨트롤러에 정의된 라우트 참고
  - 내 응모 목록: GET `/api/raffle-applications/me`
  - 내 응모: POST `/api/raffle-applications/me?raffleProductId={id}`
    - 차감 포인트: `raffle_product.point_cost`
  - 내 특정 상품 응모 여부: GET `/api/raffle-applications/me/product/{productId}`

- 사용자
  - 내 프로필: GET `/api/users/me` → `{ id, email, nickname, totalPoints, totalActivities, ... }`
  - 내 포인트 현황: GET `/api/users/me/points`
    - 응답: `{ currentPoints, totalEarned, totalUsed }`
  - 내 퀴즈 통계: GET `/api/users/me/quiz-stats`
    - 응답: `{ totalQuizzes, correctQuizzes, accuracy, totalPointsEarned }`
  - 내 미션 통계: GET `/api/users/me/mission-stats`
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

### 이메일 매직 링크 연동 (프론트)

- 회원가입 링크 전송: POST `https://honmoon-api.site/api/auth/signup/email` body: `{ email, name }`
- 로그인 링크 전송(사용자 ID): POST `https://honmoon-api.site/api/auth/login/email/by-user` body: `{ userId }`
- 콜백 처리: 프론트 콜백 라우트 `https://honmoon.site/auth/email/callback`
  - 서버가 이메일 내 버튼을 프론트 URL로 발송합니다. 예: `https://honmoon.site/auth/email/callback?token=...&purpose=login|signup`
  - 프론트는 첫 렌더링 시 `token`, `purpose`를 쿼리에서 읽어 백엔드 검증 API 호출 없이 그대로 진입할 수 있도록 구성하거나,
    필요 시 백엔드 검증 엔드포인트(`GET https://honmoon-api.site/api/auth/email/callback?token=...`)로 302 리다이렉트를 받아 `#token=...` 형태로 처리할 수 있습니다.

프론트 처리 가이드:

1. 이메일 버튼 클릭 → 프론트 라우트 `/auth/email/callback` 진입
2. `token`, `purpose` 쿼리 파라미터 추출
3. 옵션 A: 즉시 서버 검증 대신 `GET https://honmoon-api.site/api/auth/email/callback?token=...&purpose=...`로 이동해 302 응답의 `#token=` 값을 수신
4. 옵션 B: 바로 로컬 상태로 처리 후 필요한 API 호출부터 진행(권장: 옵션 A)
5. 이후 전역 헤더 `Authorization: Bearer <appSessionToken>` 설정

이메일 템플릿

- 메일 로고: `https://storage.googleapis.com/honmoon-bucket/image/honmmon.png`
- 버튼: “혼문에서 계속하기” → 프론트 콜백 URL 포함

SMTP 설정 (Gmail/Naver)

- application.yml

```yaml
spring:
  mail:
    host: ${MAIL_HOST}
    port: ${MAIL_PORT:587}
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
          connectiontimeout: ${MAIL_CONNECTION_TIMEOUT:5000}
          timeout: ${MAIL_TIMEOUT:5000}
          writetimeout: ${MAIL_WRITE_TIMEOUT:5000}
```

- env.properties 예시는 `src/main/resources/properties/env.properties.example` 참고(주석 포함). 운영에서는 안전하게 환경 변수로 주입하세요.
