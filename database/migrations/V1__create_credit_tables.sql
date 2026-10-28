-- 고교학점제 이수 관리 테이블. 설계 기준은 docs/PRD.md 7장, docs/api-contract.md다.
-- PK는 BIGINT UNSIGNED이고 API로 나갈 때는 문자열로 바뀐다(애플리케이션 책임).
-- 성취율, 이수 상태, 출석률 같은 계산 값은 저장하지 않는다.

-- 표준 과목 목록 (초기 데이터). 학점은 학교 편성에 따라 달라 두지 않는다.
CREATE TABLE credit_course_catalog (
    id            BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    subject_group VARCHAR(50)  NOT NULL,
    course_type   VARCHAR(10)  NOT NULL,
    CONSTRAINT chk_catalog_course_type
        CHECK (course_type IN ('COMMON', 'GENERAL', 'CAREER', 'FUSION', 'CREATIVE'))
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 이수 기준 설정값. 과목 유형·연도별로 한 행. 교육과정 개정으로 기준이 바뀔 수 있어 effective_year로 둔다.
-- caution_below_rate: "주의" 구간 상한(%). min_achievement가 있는 유형(지금은 공통)에만 의미가 있다.
CREATE TABLE credit_completion_rule (
    id                 BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    course_type        VARCHAR(10)   NOT NULL,
    effective_year     INT           NOT NULL,
    min_attendance     DECIMAL(5, 2) NOT NULL,
    min_achievement    DECIMAL(5, 2) NULL,
    caution_below_rate DECIMAL(5, 2) NULL,
    CONSTRAINT chk_completion_rule_course_type
        CHECK (course_type IN ('COMMON', 'GENERAL', 'CAREER', 'FUSION', 'CREATIVE')),
    CONSTRAINT uq_completion_rule UNIQUE (course_type, effective_year)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 졸업 학점 기준. 연도별로 한 행.
CREATE TABLE credit_graduation_rule (
    id              BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    effective_year  INT NOT NULL,
    total_credits   INT NOT NULL,
    subject_credits INT NOT NULL,
    creative_credits INT NOT NULL,
    CONSTRAINT uq_graduation_rule_year UNIQUE (effective_year)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 학생이 등록한 과목. 학점은 학교 편성에 따라 달라 항상 여기에 학생 입력값을 저장한다.
CREATE TABLE credit_student_course (
    id                  BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    student_id          VARCHAR(100) NOT NULL,
    catalog_id          BIGINT UNSIGNED NULL,
    custom_name         VARCHAR(100) NULL,
    course_type         VARCHAR(10)  NOT NULL,
    credits             INT          NOT NULL,
    grade_year          TINYINT      NOT NULL,
    term                TINYINT      NOT NULL,
    status              VARCHAR(20)  NOT NULL,
    written_weight      INT          NOT NULL,
    performance_weight  INT          NOT NULL,
    grade_display       VARCHAR(30)  NULL,
    absent_count        INT          NOT NULL DEFAULT 0,
    total_sessions      INT          NULL,
    CONSTRAINT fk_student_course_catalog
        FOREIGN KEY (catalog_id) REFERENCES credit_course_catalog (id),
    CONSTRAINT chk_student_course_course_type
        CHECK (course_type IN ('COMMON', 'GENERAL', 'CAREER', 'FUSION', 'CREATIVE')),
    CONSTRAINT chk_student_course_credits CHECK (credits >= 1),
    CONSTRAINT chk_student_course_grade_year CHECK (grade_year BETWEEN 1 AND 3),
    CONSTRAINT chk_student_course_term CHECK (term IN (1, 2)),
    CONSTRAINT chk_student_course_status CHECK (status IN ('IN_PROGRESS', 'COMPLETED')),
    CONSTRAINT chk_student_course_written_weight CHECK (written_weight BETWEEN 0 AND 100),
    CONSTRAINT chk_student_course_performance_weight CHECK (performance_weight BETWEEN 0 AND 100),
    CONSTRAINT chk_student_course_grade_display
        CHECK (grade_display IS NULL OR grade_display IN ('ACHIEVEMENT_ONLY', 'ACHIEVEMENT_AND_RANK', 'THREE_LEVEL')),
    CONSTRAINT chk_student_course_absent_count CHECK (absent_count >= 0),
    CONSTRAINT chk_student_course_total_sessions CHECK (total_sessions IS NULL OR total_sessions >= 0),
    INDEX idx_student_course_lookup (student_id, grade_year, term)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 평가 한 건. task_id는 기존 수행평가 시스템 연결용 문자열 ID(다른 시스템 소유라 FK를 걸지 않음). 이번 단계는 항상 비워 둔다.
CREATE TABLE credit_assessment (
    id                 BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    student_course_id  BIGINT UNSIGNED NOT NULL,
    kind               VARCHAR(20)  NOT NULL,
    title              VARCHAR(100) NOT NULL,
    weight_percent     INT          NOT NULL,
    max_score          INT          NOT NULL,
    score              INT          NULL,
    task_id            VARCHAR(50)  NULL,
    evaluated_on       DATE         NOT NULL,
    CONSTRAINT fk_assessment_student_course
        FOREIGN KEY (student_course_id) REFERENCES credit_student_course (id) ON DELETE CASCADE,
    CONSTRAINT chk_assessment_kind CHECK (kind IN ('WRITTEN', 'PERFORMANCE')),
    CONSTRAINT chk_assessment_weight_percent CHECK (weight_percent BETWEEN 0 AND 100),
    CONSTRAINT chk_assessment_max_score CHECK (max_score > 0),
    CONSTRAINT chk_assessment_score CHECK (score IS NULL OR (score >= 0 AND score <= max_score))
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
