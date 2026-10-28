-- 전문교과(특성화고·마이스터고 편성, 2022 개정 교육과정 과목 중 전문교과 I/II)를 위한 course_type 값 추가.
-- V1에서 COMMON/GENERAL/CAREER/FUSION/CREATIVE 5개만 허용했던 CHECK 제약을 SPECIALIZED를 포함하도록 넓힌다.
-- SPECIALIZED는 11자라 VARCHAR(10)에 안 들어가서 컬럼 크기도 20으로 늘린다.
-- V1은 이미 적용된 파일이라 고치지 않고 새 파일로 추가한다.

ALTER TABLE credit_course_catalog
    MODIFY COLUMN course_type VARCHAR(20) NOT NULL,
    DROP CHECK chk_catalog_course_type,
    ADD CONSTRAINT chk_catalog_course_type
        CHECK (course_type IN ('COMMON', 'GENERAL', 'CAREER', 'FUSION', 'CREATIVE', 'SPECIALIZED'));

ALTER TABLE credit_completion_rule
    MODIFY COLUMN course_type VARCHAR(20) NOT NULL,
    DROP CHECK chk_completion_rule_course_type,
    ADD CONSTRAINT chk_completion_rule_course_type
        CHECK (course_type IN ('COMMON', 'GENERAL', 'CAREER', 'FUSION', 'CREATIVE', 'SPECIALIZED'));

ALTER TABLE credit_student_course
    MODIFY COLUMN course_type VARCHAR(20) NOT NULL,
    DROP CHECK chk_student_course_course_type,
    ADD CONSTRAINT chk_student_course_course_type
        CHECK (course_type IN ('COMMON', 'GENERAL', 'CAREER', 'FUSION', 'CREATIVE', 'SPECIALIZED'));
