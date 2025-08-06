# 테이블 명세서

## 1. 테이블 설명

### `users`

- **설명**: 서비스의 사용자 정보를 저장하는 테이블입니다. Supabase의 `auth.users` 테이블과 연동됩니다.
- **주요 컬럼**:
    - `id`: 사용자의 고유 UUID (auth.users.id와 연결)
    - `email`: 사용자 이메일
    - `nickname`: 사용자 닉네임

### `user_summary`

- **설명**: 사용자의 활동 요약 정보를 저장합니다. 포인트, 퀴즈 활동, 미션 달성률 등을 요약하여 보여줍니다.
- **주요 컬럼**:
    - `user_id`: 사용자 ID
    - `total_points`: 누적 획득 포인트
    - `used_points`: 사용한 포인트
    - `current_points`: 현재 보유 포인트
    - `quiz_activity_count`: 전체 퀴즈 참여 횟수
    - `correct_quiz_count`: 정답 퀴즈 수
    - `mission_completion_rate`: 미션 완료율

### `point_history`

- **설명**: 사용자의 포인트 획득 및 사용 내역을 기록하는 테이블입니다.
- **주요 컬럼**:
    - `user_id`: 사용자 ID
    - `point_change`: 포인트 변화량 (획득 시 양수, 사용 시 음수)
    - `reason`: 포인트 변경 사유 (예: `QUIZ_COMPLETION`, `RAFFLE_ENTRY`)

### `mission_place`

- **설명**: 미션이 진행되는 장소 정보를 저장합니다.
- **주요 컬럼**:
    - `title`: 장소 이름
    - `lat`, `lng`: 위도, 경도
    - `image_url`: 장소 이미지 URL

### `mission_detail`

- **설명**: 각 장소(`mission_place`)에 속한 상세 미션 정보를 저장합니다.
- **주요 컬럼**:
    - `mission_place_id`: 미션 장소 ID
    - `mission_goal`: 미션 목표
    - `reward`: 미션 보상

### `activity_history`

- **설명**: 사용자의 장소 방문 및 활동 내역을 기록합니다.
- **주요 컬럼**:
    - `user_id`: 사용자 ID
    - `place_id`: 방문한 장소 ID

### `quiz`

- **설명**: 미션에 포함된 퀴즈 정보를 저장합니다.
- **주요 컬럼**:
    - `mission_detail_id`: 퀴즈가 속한 미션 ID
    - `question_text`: 퀴즈 질문
    - `quiz_type`: 퀴즈 유형 (객관식, 주관식 등)
    - `quiz_choices`: 퀴즈 선택지 (JSON)
    - `quiz_answer`: 퀴즈 정답 (JSON)
    - `points_reward`: 정답 시 보상 포인트

### `user_quiz_activity`

- **설명**: 사용자의 퀴즈 풀이 활동을 기록합니다.
- **주요 컬럼**:
    - `user_id`: 사용자 ID
    - `quiz_id`: 푼 퀴즈 ID
    - `user_answer_text`/`user_answer_json`: 사용자가 제출한 답
    - `is_correct`: 정답 여부

### `raffle_product`

- **설명**: 래플(추첨) 이벤트의 상품 정보를 저장합니다.
- **주요 컬럼**:
    - `name`: 상품명
    - `point_cost`: 응모에 필요한 포인트

### `raffle_user_application`

- **설명**: 사용자의 래플 응모 내역을 기록합니다.
- **주요 컬럼**:
    - `user_id`: 사용자 ID
    - `raffle_product_id`: 응모한 상품 ID
    - `status`: 응모 상태 (`APPLIED`, `WON`, `LOST`)

---

## 2. 시나리오별 테이블 사용법

### 시나리오 1: 신규 사용자 가입

1. 사용자가 Supabase Auth를 통해 회원가입을 완료합니다.
2. `auth.users` 테이블에 새로운 레코드가 추가되면 `on_auth_user_created` 트리거가 실행됩니다.
3. `handle_new_user()` 함수가 호출되어 `public.users` 테이블에 해당 사용자의 정보를 복제하여 저장합니다.
4. (애플리케이션 로직) `user_summary` 테이블에 해당 유저의 초기 요약 정보를 생성합니다.

### 시나리오 2: 사용자가 퀴즈를 풀고 포인트를 획득

1. 사용자가 특정 `mission_detail`에 연결된 `quiz`를 풉니다.
2. 사용자가 답을 제출하면 `user_quiz_activity` 테이블에 레코드가 생성됩니다. (`is_correct`는 `false`로 시작)
3. (애플리케이션 로직) 제출된 답과 `quiz.quiz_answer`를 비교하여 정답 여부를 판단합니다.
4. **정답일 경우**:
    - `user_quiz_activity`의 `is_correct`를 `true`로 업데이트합니다.
    - `point_history`에 `reason`을 'QUIZ_COMPLETION'으로 하여 포인트 획득 내역을 추가합니다.
    - `user_summary`의 `total_points`, `current_points`, `correct_quiz_count`, `quiz_activity_count`를 업데이트합니다.
5. **오답일 경우**:
    - `user_summary`의 `quiz_activity_count`만 업데이트합니다.

### 시나리오 3: 사용자가 포인트를 사용하여 래플에 응모

1. 사용자가 `raffle_product` 목록에서 원하는 상품을 선택하고 응모합니다.
2. (애플리케이션 로직) 사용자의 `user_summary.current_points`가 `raffle_product.point_cost`보다 충분한지 확인합니다.
3. 포인트가 충분하면, `raffle_user_application` 테이블에 `status`를 'APPLIED'로 하여 응모 내역을 추가합니다.
4. `point_history`에 `reason`을 'RAFFLE_ENTRY'로 하여 포인트 사용 내역을 추가합니다.
5. `user_summary`의 `used_points`와 `current_points`를 업데이트합니다.

### 시나리오 4: 관리자가 래플 당첨자 선정

1. (관리자 기능) 특정 `raffle_product`에 대해 추첨을 진행합니다.
2. `raffle_user_application` 테이블에서 해당 상품에 응모한(`APPLIED`) 사용자 목록을 조회합니다.
3. (애플리케이션 로직) 당첨자를 선정하고, 해당 사용자의 `raffle_user_application` 레코드의 `status`를 'WON'으로 업데이트합니다.
4. 나머지 응모자들의 `status`는 'LOST'로 업데이트합니다.
