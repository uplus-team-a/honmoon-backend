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

# 콜백 (302 리다이렉트, Location 헤더 #token= 값 사용)
GET /api/auth/email/callback?token=...&purpose=signup|login&redirectUrl=...
```

### 3. Google OAuth

```bash
GET /api/auth/google/url?scope=openid%20email%20profile&redirectAfter=/
GET /api/auth/google/callback?code=...&state=...
```

### 4. 현재 사용자 확인 & 로그아웃

```bash
GET /api/auth/me
POST /api/auth/logout
```

## 미션

```bash
GET /api/missions/{id}                              # 미션 상세
POST /api/missions/{id}/submit-answer               # 텍스트 답변 제출
POST /api/missions/{id}/submit-image-answer         # 이미지 답변 제출
POST /api/missions/{id}/image/upload-url            # 이미지 업로드 URL 발급
```

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
GET /api/users/me/points                            # 내 포인트 현황
GET /api/users/me/quiz-stats                        # 내 퀴즈 통계
GET /api/users/me/mission-stats                     # 내 미션 통계
PUT /api/users/me/profile-image?imageUrl=...        # 내 프로필 이미지 업데이트
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
# 내 퀴즈 제출 (세션 사용자 기준)
POST /api/user-activities/missions/{missionId}/submit-quiz?textAnswer=...
POST /api/user-activities/missions/{missionId}/submit-quiz?selectedChoiceIndex=...
POST /api/user-activities/missions/{missionId}/submit-quiz?uploadedImageUrl=...

# (대체) 기존 /me 경로는 계속 지원
POST /api/user-activities/missions/{missionId}/submit-quiz/me?textAnswer=...
POST /api/user-activities/missions/{missionId}/submit-quiz/me?selectedChoiceIndex=...
POST /api/user-activities/missions/{missionId}/submit-quiz/me?uploadedImageUrl=...
```

## 포인트

```bash
GET /api/point-history/{id}                         # 포인트 내역 상세
GET /api/point-history/me                           # 내 포인트 내역
GET /api/point-history/me/earned                    # 내 획득 내역
GET /api/point-history/me/used                      # 내 사용 내역
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
