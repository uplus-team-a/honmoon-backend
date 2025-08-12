# Honmoon API 가이드

## 인증

### 1. 테스트 토큰 (개발용)

```bash
POST /api/auth/test-token
Authorization: Basic {basicToken}
```

### 2. 이메일 매직 링크

```bash
# 회원가입 (redirectUrl은 선택사항, 기본값: https://honmoon.site)
POST /api/auth/signup/email?redirectUrl=https://honmoon.site/auth/success
Body: {"email": "...", "name": "..."}

# 로그인 (redirectUrl은 선택사항, 기본값: https://honmoon.site)
POST /api/auth/login/email/by-user?redirectUrl=https://honmoon.site/dashboard
Body: {"userId": "..."}

# 콜백(옵션 A: 302 리다이렉트)
GET /api/auth/email/callback?token=...&purpose=signup|login&redirectUrl=...

# 교환(옵션 B: JSON 교환)
POST /api/auth/email/exchange
Body: {"token": "...", "purpose": "signup|login"}
```

### 3. Google OAuth (서버 주도, 프론트 콜백 지정 가능)

```bash
# 1) 인증 URL 발급: redirectAfter(최종 이동 경로), frontendCallbackUrl(프론트 콜백) 지정
GET /api/auth/google/url?scope=openid%20email%20profile&redirectAfter=/my-profile&frontendCallbackUrl=https://honmoon.site/auth/google/callback

# 2) 구글 → 서버 콜백 → 프론트 콜백으로 302 리다이렉트 (code/state/redirectAfter 전달)
GET /api/auth/google/callback?code=...&state=...

# 3) 프론트 콜백에서 code/state를 서버로 교환하여 세션 발급
POST /api/auth/google/exchange
Body: {"code": "...", "state": "..."}
```

### 4. 현재 사용자 확인 & 로그아웃 + 이메일/비밀번호

```bash
GET /api/auth/me
POST /api/auth/logout
POST /api/auth/login/email/password                  # 이메일/비번 로그인 → 세션 토큰 발급
POST /api/auth/password/set                          # (세션) 비밀번호 설정/변경
```

## 미션

```bash
GET /api/missions/{id}                              # 미션 상세
GET /api/missions/{id}/detail                       # 미션 상세(별칭)
POST /api/missions/{id}/submit-answer               # 텍스트 답변 제출
POST /api/missions/{id}/submit-image-answer         # 이미지 답변 제출
POST /api/missions/{id}/image/upload-url            # 이미지 업로드 URL 발급
POST /api/missions/{id}/submit-quiz                 # (통합) 퀴즈 제출(텍스트/객관식/이미지)
POST /api/missions/{id}/submit-quiz/me              # (통합) 내 계정으로 퀴즈 제출
```

### 미션 타입별 호출/응답 가이드

- QUIZ_TEXT_INPUT

  - 호출: `POST /api/missions/{id}/submit-quiz?textAnswer=정답`
  - 응답: `isCorrect`, `pointsEarned`, `explanation`, `hint`, `aiResult`(reasoning 포함)

- QUIZ_MULTIPLE_CHOICE

  - 호출: `POST /api/missions/{id}/submit-quiz?selectedChoiceIndex={index}` (0-based)
  - 응답: 위와 동일 (정답/오답 reasoning 포함)

- QUIZ_IMAGE_UPLOAD

  - 호출(통합): `POST /api/missions/{id}/submit-quiz?uploadedImageUrl={이미지URL}`
  - 또는: `POST /api/missions/{id}/submit-image-answer` (body.imageUrl)
  - 응답: `aiResult`에 `extractedText`, `confidence`, `reasoning` 포함

- PHOTO_UPLOAD

  - 호출: `POST /api/missions/{id}/submit-quiz?uploadedImageUrl={이미지URL}`
  - 응답: `aiResult.reasoning = "이미지 업로드 완료"`

- PLACE_VISIT

  - 호출: `POST /api/missions/{id}/submit-quiz` (입력값 없음)
  - 응답: `aiResult.reasoning = "장소 방문 완료"`

- SURVEY
  - 호출: `POST /api/missions/{id}/submit-quiz?textAnswer={설문 응답}`
  - 응답: `aiResult.reasoning = "설문 응답 접수"`

## 미션 장소

```bash
GET /api/mission-places                             # 장소 목록
GET /api/mission-places/{id}                        # 장소 상세
GET /api/mission-places/search?title=...            # 장소 검색
GET /api/mission-places/nearby?lat=...&lng=...&radius=...  # 근처 장소
GET /api/mission-places/{id}/missions               # 장소별 미션 목록
```

## 사용자

```bash
GET /api/users/me                                   # 내 프로필
GET /api/users/me/profile/summary                   # 내 프로필(간략): 포인트 요약 + 최근 활동/포인트 10건
GET /api/users/me/profile/detail                    # 내 프로필(상세): 포인트 요약 + 전체 활동/포인트 내역
PUT /api/users/me/profile-image?imageUrl=...        # 내 프로필 이미지 업데이트
PATCH /api/users/me                                  # 내 프로필 정보 수정 (nickname, profileImageUrl)
```

## 활동 내역

```bash
GET /api/user-activities/{id}                       # 활동 상세
GET /api/user-activities/me                         # 내 활동 목록
GET /api/user-activities/place/{placeId}            # 장소별 활동 목록
GET /api/user-activities/me/recent?limit=10         # 내 최근 활동
POST /api/user-activities?placeId=...&description=...  # 활동 기록 생성 (세션 사용자 기준)
```

## 퀴즈 제출

```bash
POST /api/missions/{missionId}/submit-quiz?textAnswer=...
POST /api/missions/{missionId}/submit-quiz?selectedChoiceIndex=...
POST /api/missions/{missionId}/submit-quiz?uploadedImageUrl=...
POST /api/missions/{missionId}/submit-quiz/me?textAnswer=...
POST /api/missions/{missionId}/submit-quiz/me?selectedChoiceIndex=...
POST /api/missions/{missionId}/submit-quiz/me?uploadedImageUrl=...
```

응답 공통 필드: `isCorrect`, `pointsEarned`, `explanation`, `hint`, `aiResult({isCorrect, confidence, reasoning, extractedText?})`

## 포인트

```bash
// 삭제됨: 포인트 내역/요약은 /api/users/me/profile/* 에 통합
```

## 래플

```bash
GET /api/raffle-products                            # 래플 상품 목록
GET /api/raffle-products/{id}                       # 래플 상품 상세
GET /api/raffle-products/search?name=...            # 래플 상품 검색
GET /api/raffle-products/by-points?minPoints=...&maxPoints=...  # 포인트별 래플 상품
GET /api/raffle-products/{id}/applicants-count      # 래플 상품 응모자 수

GET /api/raffle-applications/{id}                   # 래플 응모 상세
GET /api/raffle-applications/me                     # 내 래플 응모 내역
GET /api/raffle-applications/product/{productId}    # 래플 상품별 응모자 목록
GET /api/raffle-applications/me/product/{productId} # 내 응모 상태

POST /api/raffle-applications?raffleProductId=...   # 래플 응모 (세션 사용자)
POST /api/raffle-applications/me?raffleProductId=... # 내 래플 응모
POST /api/raffle-applications/{productId}/draw?winnerCount=1  # 래플 당첨자 선정
```

## 이미지 업로드

```bash
POST /api/missions/{missionId}/image/upload-url?fileName=...  # 미션 이미지 업로드 URL
```

업로드 절차:

1. 위 API로 presigned URL 발급받기
2. 발급받은 URL로 파일을 PUT으로 업로드
3. 최종 URL: `https://storage.googleapis.com/{bucket}/missions/{fileName}`

## 공통 사항

- 모든 보호된 API는 `Authorization: Bearer {token}` 헤더 필요
- API 응답 형식: `{"success": true/false, "data": {...}}`
- Swagger UI: `/swagger-ui/index.html`
- OpenAPI: `/v3/api-docs`
