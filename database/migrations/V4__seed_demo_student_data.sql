-- 데모 학생(1명 고정) 1학년 1학기(이수 완료)·2학기(수강 중) 과목 20개 + 평가 44개, 졸업/이수 기준값.
-- frontend/src/mocks/creditStore.ts의 시연 데이터와 같은 값·순서. 빈 테이블 기준이라 student_course id는 1~20, assessment id는 1~44로 고정된다(AUTO_INCREMENT).
-- catalog_id는 이름으로 조회한다(실제 DB catalog 순서가 frontend mock의 데모 catalog 순서와 다름 — PROGRESS.md "미정" 참고).
-- '체육'은 카탈로그에 '체육1'/'체육2'만 있어 정확히 일치하지 않아 직접 입력(catalog_id NULL, custom_name '체육')으로 둔다.

-- 졸업 학점 기준 (PRD 1장: 2025학년도 고1부터 전면 시행)
INSERT INTO credit_graduation_rule (effective_year, total_credits, subject_credits, creative_credits)
VALUES (2025, 192, 174, 18);

-- 이수 기준 (PRD 6장. 2026학년도부터 적용되는 기준값 — 전문교과도 선택과목과 같게 출석률만)
INSERT INTO credit_completion_rule (course_type, effective_year, min_attendance, min_achievement, caution_below_rate)
VALUES
    ('COMMON', 2026, 66.67, 40.00, 50.00),
    ('GENERAL', 2026, 66.67, NULL, NULL),
    ('CAREER', 2026, 66.67, NULL, NULL),
    ('FUSION', 2026, 66.67, NULL, NULL),
    ('SPECIALIZED', 2026, 66.67, NULL, NULL),
    ('CREATIVE', 2026, 66.67, NULL, NULL);

INSERT INTO credit_student_course
    (student_id, catalog_id, custom_name, course_type, credits, grade_year, term, status, written_weight, performance_weight, grade_display, absent_count, total_sessions)
VALUES
    ('demo-student', (SELECT id FROM credit_course_catalog WHERE name = '공통국어1'), NULL, 'COMMON', 4, 1, 1, 'COMPLETED', 60, 40, 'ACHIEVEMENT_AND_RANK', 0, NULL),
    ('demo-student', (SELECT id FROM credit_course_catalog WHERE name = '공통수학1'), NULL, 'COMMON', 4, 1, 1, 'COMPLETED', 60, 40, 'ACHIEVEMENT_AND_RANK', 0, NULL),
    ('demo-student', (SELECT id FROM credit_course_catalog WHERE name = '공통영어1'), NULL, 'COMMON', 4, 1, 1, 'COMPLETED', 60, 40, 'ACHIEVEMENT_AND_RANK', 0, NULL),
    ('demo-student', (SELECT id FROM credit_course_catalog WHERE name = '한국사1'), NULL, 'COMMON', 3, 1, 1, 'COMPLETED', 60, 40, 'ACHIEVEMENT_AND_RANK', 0, NULL),
    ('demo-student', (SELECT id FROM credit_course_catalog WHERE name = '통합사회1'), NULL, 'COMMON', 4, 1, 1, 'COMPLETED', 60, 40, 'ACHIEVEMENT_AND_RANK', 0, NULL),
    ('demo-student', (SELECT id FROM credit_course_catalog WHERE name = '통합과학1'), NULL, 'COMMON', 4, 1, 1, 'COMPLETED', 60, 40, 'ACHIEVEMENT_AND_RANK', 0, NULL),
    ('demo-student', (SELECT id FROM credit_course_catalog WHERE name = '과학탐구실험1'), NULL, 'COMMON', 1, 1, 1, 'COMPLETED', 0, 100, 'ACHIEVEMENT_AND_RANK', 0, NULL),
    ('demo-student', NULL, '체육', 'GENERAL', 2, 1, 1, 'COMPLETED', 0, 100, 'THREE_LEVEL', 0, NULL),
    ('demo-student', (SELECT id FROM credit_course_catalog WHERE name = '미술'), NULL, 'GENERAL', 3, 1, 1, 'COMPLETED', 0, 100, 'THREE_LEVEL', 0, NULL),
    ('demo-student', (SELECT id FROM credit_course_catalog WHERE name = '창의적 체험활동'), NULL, 'CREATIVE', 3, 1, 1, 'COMPLETED', 0, 0, NULL, 0, NULL),
    ('demo-student', (SELECT id FROM credit_course_catalog WHERE name = '공통국어2'), NULL, 'COMMON', 4, 1, 2, 'IN_PROGRESS', 60, 40, 'ACHIEVEMENT_AND_RANK', 0, NULL),
    ('demo-student', (SELECT id FROM credit_course_catalog WHERE name = '공통수학2'), NULL, 'COMMON', 4, 1, 2, 'IN_PROGRESS', 60, 40, 'ACHIEVEMENT_AND_RANK', 0, NULL),
    ('demo-student', (SELECT id FROM credit_course_catalog WHERE name = '공통영어2'), NULL, 'COMMON', 4, 1, 2, 'IN_PROGRESS', 60, 40, 'ACHIEVEMENT_AND_RANK', 0, NULL),
    ('demo-student', (SELECT id FROM credit_course_catalog WHERE name = '한국사2'), NULL, 'COMMON', 3, 1, 2, 'IN_PROGRESS', 60, 40, 'ACHIEVEMENT_AND_RANK', 0, NULL),
    ('demo-student', (SELECT id FROM credit_course_catalog WHERE name = '통합사회2'), NULL, 'COMMON', 4, 1, 2, 'IN_PROGRESS', 60, 40, 'ACHIEVEMENT_AND_RANK', 0, NULL),
    ('demo-student', (SELECT id FROM credit_course_catalog WHERE name = '통합과학2'), NULL, 'COMMON', 4, 1, 2, 'IN_PROGRESS', 60, 40, 'ACHIEVEMENT_AND_RANK', 0, NULL),
    ('demo-student', (SELECT id FROM credit_course_catalog WHERE name = '과학탐구실험2'), NULL, 'COMMON', 1, 1, 2, 'IN_PROGRESS', 0, 100, 'ACHIEVEMENT_AND_RANK', 0, NULL),
    ('demo-student', (SELECT id FROM credit_course_catalog WHERE name = '운동과 건강'), NULL, 'GENERAL', 2, 1, 2, 'IN_PROGRESS', 0, 100, 'THREE_LEVEL', 0, NULL),
    ('demo-student', (SELECT id FROM credit_course_catalog WHERE name = '음악'), NULL, 'GENERAL', 3, 1, 2, 'IN_PROGRESS', 0, 100, 'THREE_LEVEL', 0, NULL),
    ('demo-student', (SELECT id FROM credit_course_catalog WHERE name = '창의적 체험활동'), NULL, 'CREATIVE', 3, 1, 2, 'IN_PROGRESS', 0, 0, NULL, 0, NULL);

INSERT INTO credit_assessment
    (student_course_id, kind, title, weight_percent, max_score, score, task_id, evaluated_on)
VALUES
    (1, 'WRITTEN', '중간고사', 30, 100, 82, NULL, '2026-04-24'),
    (1, 'WRITTEN', '기말고사', 30, 100, 76, NULL, '2026-07-03'),
    (1, 'PERFORMANCE', '독서 감상문', 40, 100, 85, NULL, '2026-06-12'),
    (2, 'WRITTEN', '중간고사', 30, 100, 68, NULL, '2026-04-23'),
    (2, 'WRITTEN', '기말고사', 30, 100, 61, NULL, '2026-07-02'),
    (2, 'PERFORMANCE', '수학 탐구 보고서', 40, 100, 70, NULL, '2026-06-05'),
    (3, 'WRITTEN', '중간고사', 30, 100, 74, NULL, '2026-04-27'),
    (3, 'WRITTEN', '기말고사', 30, 100, 79, NULL, '2026-07-06'),
    (3, 'PERFORMANCE', '영어 말하기', 40, 100, 80, NULL, '2026-05-29'),
    (4, 'WRITTEN', '중간고사', 30, 100, 88, NULL, '2026-04-28'),
    (4, 'WRITTEN', '기말고사', 30, 100, 84, NULL, '2026-07-07'),
    (4, 'PERFORMANCE', '역사 신문 만들기', 40, 100, 90, NULL, '2026-06-19'),
    (5, 'WRITTEN', '중간고사', 30, 100, 71, NULL, '2026-04-29'),
    (5, 'WRITTEN', '기말고사', 30, 100, 66, NULL, '2026-07-08'),
    (5, 'PERFORMANCE', '모둠 토론', 40, 100, 75, NULL, '2026-06-10'),
    (6, 'WRITTEN', '중간고사', 30, 100, 58, NULL, '2026-04-30'),
    (6, 'WRITTEN', '기말고사', 30, 100, 63, NULL, '2026-07-09'),
    (6, 'PERFORMANCE', '실험 보고서', 40, 100, 72, NULL, '2026-06-17'),
    (7, 'PERFORMANCE', '탐구 계획서', 50, 100, 90, NULL, '2026-05-15'),
    (7, 'PERFORMANCE', '탐구 발표', 50, 100, 86, NULL, '2026-07-01'),
    (8, 'PERFORMANCE', '체력 측정', 50, 100, 88, NULL, '2026-05-08'),
    (8, 'PERFORMANCE', '구기 실기', 50, 100, 92, NULL, '2026-06-26'),
    (9, 'PERFORMANCE', '드로잉', 50, 100, 80, NULL, '2026-05-22'),
    (9, 'PERFORMANCE', '작품 감상문', 50, 100, 84, NULL, '2026-06-30'),
    (11, 'WRITTEN', '중간고사', 30, 100, 80, NULL, '2026-10-14'),
    (11, 'PERFORMANCE', '논설문 쓰기', 20, 100, 75, NULL, '2026-09-25'),
    (11, 'WRITTEN', '기말고사', 30, 100, NULL, NULL, '2026-12-16'),
    (12, 'WRITTEN', '중간고사', 30, 100, 50, NULL, '2026-10-13'),
    (12, 'PERFORMANCE', '수행평가 1', 20, 100, 40, NULL, '2026-09-30'),
    (12, 'WRITTEN', '기말고사', 30, 100, NULL, NULL, '2026-12-15'),
    (13, 'WRITTEN', '중간고사', 30, 100, 70, NULL, '2026-10-15'),
    (13, 'PERFORMANCE', '영어 에세이', 20, 100, 75, NULL, '2026-09-24'),
    (14, 'WRITTEN', '중간고사', 30, 100, 85, NULL, '2026-10-16'),
    (14, 'PERFORMANCE', '역사 인물 탐구', 20, 100, 75, NULL, '2026-10-02'),
    (15, 'WRITTEN', '중간고사', 30, 100, 65, NULL, '2026-10-19'),
    (15, 'PERFORMANCE', '사회 문제 보고서', 20, 100, 75, NULL, '2026-09-29'),
    (16, 'WRITTEN', '중간고사', 30, 100, 25, NULL, '2026-10-20'),
    (16, 'PERFORMANCE', '실험 보고서', 20, 100, 50, NULL, '2026-10-06'),
    (17, 'PERFORMANCE', '탐구 계획서', 50, 100, 90, NULL, '2026-09-18'),
    (17, 'PERFORMANCE', '탐구 발표', 50, 100, 86, NULL, '2026-10-23'),
    (18, 'PERFORMANCE', '체력 측정', 40, 100, 80, NULL, '2026-09-11'),
    (18, 'PERFORMANCE', '건강 계획서', 40, 100, 90, NULL, '2026-10-08'),
    (19, 'PERFORMANCE', '가창', 40, 100, 76, NULL, '2026-09-17'),
    (19, 'PERFORMANCE', '감상문', 40, 100, 82, NULL, '2026-10-21');
