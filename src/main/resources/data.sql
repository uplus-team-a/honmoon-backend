-- Users 테이블 샘플 데이터
INSERT INTO public.users (id, email, nickname, is_active, created_by, created_at, modified_by, modified_at) VALUES ('2c0cd5f3-2993-4b71-a8da-5beeb79ad43c', 'aekuya_@naver.com', 'aekuya_', true, 'anonymous', '2025-08-06 06:31:48.122090 +00:00', 'anonymous', '2025-08-06 06:31:48.122090 +00:00');
INSERT INTO public.users (id, email, nickname, is_active, created_by, created_at, modified_by, modified_at) VALUES ('63dbcdf7-598e-4111-89f4-1c6a6cc6f5e3', 'psy950115@naver.com', null, true, 'anonymous', '2025-08-06 06:31:48.122090 +00:00', 'anonymous', '2025-08-06 06:31:48.122090 +00:00');
INSERT INTO public.users (id, email, nickname, is_active, created_by, created_at, modified_by, modified_at) VALUES ('bce8f598-b083-4972-94f8-177c7af7386e', 'leejinku11@gmail.com', 'leejinku11', false, 'anonymous', '2025-08-06 06:31:48.122090 +00:00', 'anonymous', '2025-08-06 06:31:48.122090 +00:00');
INSERT INTO public.users (id, email, nickname, is_active, created_by, created_at, modified_by, modified_at) VALUES ('6f8e9e91-530f-410a-9796-a20c428297c9', 'consiconst@naver.com', '이진구', true, 'anonymous', '2025-08-06 06:31:48.122090 +00:00', 'anonymous', '2025-08-06 06:31:48.122090 +00:00');
INSERT INTO public.users (id, email, nickname, is_active, created_by, created_at, modified_by, modified_at) VALUES ('efaa7a69-b6d2-43d6-90f7-c079e156afd4', 'psy950115@gmail.com', '박상원', true, 'anonymous', '2025-08-06 06:31:48.122090 +00:00', 'anonymous', '2025-08-06 06:31:48.122090 +00:00');
INSERT INTO public.users (id, email, nickname, is_active, created_by, created_at, modified_by, modified_at) VALUES ('cf9f9ba7-d1fb-4240-9521-1b1e9a8d8808', 'sylvester7412@gmail.com', null, true, 'anonymous', '2025-08-06 06:31:48.122090 +00:00', 'anonymous', '2025-08-06 06:31:48.122090 +00:00');

-- User Summary 테이블 샘플 데이터
INSERT INTO user_summary (user_id, total_points, total_activities) VALUES ('2c0cd5f3-2993-4b71-a8da-5beeb79ad43c', 1500, 25);
INSERT INTO user_summary (user_id, total_points, total_activities) VALUES ('63dbcdf7-598e-4111-89f4-1c6a6cc6f5e3', 800, 12);
INSERT INTO user_summary (user_id, total_points, total_activities) VALUES ('6f8e9e91-530f-410a-9796-a20c428297c9', 2200, 35);
INSERT INTO user_summary (user_id, total_points, total_activities) VALUES ('efaa7a69-b6d2-43d6-90f7-c079e156afd4', 950, 18);
INSERT INTO user_summary (user_id, total_points, total_activities) VALUES ('cf9f9ba7-d1fb-4240-9521-1b1e9a8d8808', 300, 5);

-- Point History 테이블 샘플 데이터
INSERT INTO point_history (user_id, points, description) VALUES ('2c0cd5f3-2993-4b71-a8da-5beeb79ad43c', 100, '퀴즈 정답 보상');
INSERT INTO point_history (user_id, points, description) VALUES ('2c0cd5f3-2993-4b71-a8da-5beeb79ad43c', 200, '장소 방문 보상');
INSERT INTO point_history (user_id, points, description) VALUES ('63dbcdf7-598e-4111-89f4-1c6a6cc6f5e3', 150, '사진 업로드 보상');
INSERT INTO point_history (user_id, points, description) VALUES ('6f8e9e91-530f-410a-9796-a20c428297c9', 300, '미션 완료 보상');
INSERT INTO point_history (user_id, points, description) VALUES ('efaa7a69-b6d2-43d6-90f7-c079e156afd4', 50, '일일 출석 보상');

-- Mission Place 테이블 샘플 데이터
INSERT INTO mission_place (name, description, location) VALUES ('홍대 거리', '홍대의 상징적인 거리', '서울특별시 마포구 홍대로');
INSERT INTO mission_place (name, description, location) VALUES ('강남역', '강남의 중심지', '서울특별시 강남구 강남대로');
INSERT INTO mission_place (name, description, location) VALUES ('명동', '서울의 대표적인 관광지', '서울특별시 중구 명동길');
INSERT INTO mission_place (name, description, location) VALUES ('이태원', '다양한 문화가 공존하는 지역', '서울특별시 용산구 이태원로');
INSERT INTO mission_place (name, description, location) VALUES ('동대문', '패션의 중심지', '서울특별시 중구 장충단로');

-- Mission Detail 테이블 샘플 데이터
INSERT INTO mission_detail (title, description, points, mission_type, place_id, question, answer, choices, answer_explanation) VALUES 
('홍대 거리 퀴즈', '홍대에 대한 기본적인 지식을 테스트합니다', 100, 'QUIZ_MULTIPLE_CHOICE', 1, '홍대의 정식 명칭은?', '홍익대학교', '["홍익대학교", "홍대학교", "홍익대", "홍대"]', '홍대는 홍익대학교의 줄임말입니다');

INSERT INTO mission_detail (title, description, points, mission_type, place_id, question, answer, image_upload_instruction) VALUES 
('홍대 거리 사진', '홍대 거리의 특별한 장소를 사진으로 남겨보세요', 200, 'PHOTO_UPLOAD', 1, null, null, '홍대 거리의 상징적인 건물이나 장소를 촬영해주세요');

INSERT INTO mission_detail (title, description, points, mission_type, place_id, question, answer, choices, answer_explanation) VALUES 
('강남역 퀴즈', '강남역에 대한 퀴즈입니다', 150, 'QUIZ_MULTIPLE_CHOICE', 2, '강남역이 위치한 구는?', '강남구', '["강남구", "서초구", "마포구", "용산구"]', '강남역은 강남구에 위치해 있습니다');

INSERT INTO mission_detail (title, description, points, mission_type, place_id, question, answer) VALUES 
('명동 방문', '명동 거리를 직접 방문해보세요', 300, 'PLACE_VISIT', 3, null, null);

INSERT INTO mission_detail (title, description, points, mission_type, place_id, question, answer, image_upload_instruction) VALUES 
('이태원 문화 체험', '이태원의 다양한 문화를 사진으로 기록하세요', 250, 'PHOTO_UPLOAD', 4, null, null, '이태원의 독특한 문화나 건물을 촬영해주세요');

-- Activity History 테이블 샘플 데이터
INSERT INTO activity_history (user_id, place_id, description) VALUES ('2c0cd5f3-2993-4b71-a8da-5beeb79ad43c', 1, '홍대 거리 탐방');
INSERT INTO activity_history (user_id, place_id, description) VALUES ('63dbcdf7-598e-4111-89f4-1c6a6cc6f5e3', 2, '강남역 방문');
INSERT INTO activity_history (user_id, place_id, description) VALUES ('6f8e9e91-530f-410a-9796-a20c428297c9', 3, '명동 쇼핑');
INSERT INTO activity_history (user_id, place_id, description) VALUES ('efaa7a69-b6d2-43d6-90f7-c079e156afd4', 4, '이태원 문화 체험');
INSERT INTO activity_history (user_id, place_id, description) VALUES ('cf9f9ba7-d1fb-4240-9521-1b1e9a8d8808', 5, '동대문 패션 투어');

-- Raffle Product 테이블 샘플 데이터
INSERT INTO raffle_product (name, description, image_url) VALUES ('아이폰 15 Pro', '최신 아이폰 15 Pro 128GB', 'https://example.com/iphone15pro.jpg');
INSERT INTO raffle_product (name, description, image_url) VALUES ('에어팟 프로', '노이즈 캔슬링 무선 이어폰', 'https://example.com/airpodspro.jpg');
INSERT INTO raffle_product (name, description, image_url) VALUES ('갤럭시 워치', '삼성 갤럭시 워치 6', 'https://example.com/galaxywatch.jpg');
INSERT INTO raffle_product (name, description, image_url) VALUES ('아마존 기프트카드', '10만원 상당의 아마존 기프트카드', 'https://example.com/amazongift.jpg');
INSERT INTO raffle_product (name, description, image_url) VALUES ('스타벅스 기프트카드', '5만원 상당의 스타벅스 기프트카드', 'https://example.com/starbucksgift.jpg');

-- Raffle User Application 테이블 샘플 데이터
INSERT INTO raffle_user_application (user_id, raffle_product_id, application_date) VALUES ('2c0cd5f3-2993-4b71-a8da-5beeb79ad43c', 1, '2025-01-15 10:30:00+00:00');
INSERT INTO raffle_user_application (user_id, raffle_product_id, application_date) VALUES ('63dbcdf7-598e-4111-89f4-1c6a6cc6f5e3', 2, '2025-01-15 11:15:00+00:00');
INSERT INTO raffle_user_application (user_id, raffle_product_id, application_date) VALUES ('6f8e9e91-530f-410a-9796-a20c428297c9', 1, '2025-01-15 14:20:00+00:00');
INSERT INTO raffle_user_application (user_id, raffle_product_id, application_date) VALUES ('efaa7a69-b6d2-43d6-90f7-c079e156afd4', 3, '2025-01-15 16:45:00+00:00');
INSERT INTO raffle_user_application (user_id, raffle_product_id, application_date) VALUES ('cf9f9ba7-d1fb-4240-9521-1b1e9a8d8808', 4, '2025-01-15 18:30:00+00:00');

-- User Mission Activity 테이블 샘플 데이터
INSERT INTO user_mission_activity (user_id, mission_id, is_completed, points_earned, text_answer, selected_choice_index, uploaded_image_url, is_correct) VALUES 
('2c0cd5f3-2993-4b71-a8da-5beeb79ad43c', 1, true, 100, null, 0, null, true);

INSERT INTO user_mission_activity (user_id, mission_id, is_completed, points_earned, text_answer, selected_choice_index, uploaded_image_url, is_correct) VALUES 
('63dbcdf7-598e-4111-89f4-1c6a6cc6f5e3', 2, true, 200, null, null, 'https://example.com/hongdae_photo.jpg', null);

INSERT INTO user_mission_activity (user_id, mission_id, is_completed, points_earned, text_answer, selected_choice_index, uploaded_image_url, is_correct) VALUES 
('6f8e9e91-530f-410a-9796-a20c428297c9', 3, true, 150, null, 0, null, true);

INSERT INTO user_mission_activity (user_id, mission_id, is_completed, points_earned, text_answer, selected_choice_index, uploaded_image_url, is_correct) VALUES 
('efaa7a69-b6d2-43d6-90f7-c079e156afd4', 4, true, 300, null, null, null, null);

INSERT INTO user_mission_activity (user_id, mission_id, is_completed, points_earned, text_answer, selected_choice_index, uploaded_image_url, is_correct) VALUES 
('cf9f9ba7-d1fb-4240-9521-1b1e9a8d8808', 5, false, 0, null, null, null, null); 