-- 성적 관리: 나이스 성적표(성적증명서) PDF 업로드 또는 수동 입력으로 쌓는 과목별 성적.
-- (student_id, grade_year, term, subject_name)은 하나만 있다(같은 학기 같은 과목 중복 저장 안 함).
-- matched_student_course_id가 있으면 고교학점제(credit) 모듈의 해당 과목 지필/수행 평가 점수를 덮어썼다는 뜻(docs/PROGRESS.md 참고).
-- applied_to_credit=false면 매칭 실패/모호/반영할 평가 없음 중 하나(unmatched_reason에 사유).

CREATE TABLE grade_upload (
    id                BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    student_id        VARCHAR(100) NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    uploaded_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    recognized_count  INT          NOT NULL DEFAULT 0,
    failed_count      INT          NOT NULL DEFAULT 0,
    INDEX idx_grade_upload_student (student_id)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE grade_record (
    id                         BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    student_id                 VARCHAR(100)    NOT NULL,
    grade_year                 TINYINT         NOT NULL,
    term                       TINYINT         NOT NULL,
    subject_name               VARCHAR(100)    NOT NULL,
    raw_score                  INT             NULL,
    subject_average            DECIMAL(5, 2)   NULL,
    std_dev                    DECIMAL(5, 2)   NULL,
    grade_level                TINYINT         NULL,
    source                     VARCHAR(10)     NOT NULL,
    upload_id                  BIGINT UNSIGNED NULL,
    matched_student_course_id  BIGINT UNSIGNED NULL,
    applied_to_credit          BOOLEAN         NOT NULL DEFAULT FALSE,
    unmatched_reason           VARCHAR(50)     NULL,
    created_at                 DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                 DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_grade_record_upload
        FOREIGN KEY (upload_id) REFERENCES grade_upload (id) ON DELETE SET NULL,
    CONSTRAINT fk_grade_record_student_course
        FOREIGN KEY (matched_student_course_id) REFERENCES credit_student_course (id) ON DELETE SET NULL,
    CONSTRAINT uq_grade_record_subject UNIQUE (student_id, grade_year, term, subject_name),
    CONSTRAINT chk_grade_record_grade_year CHECK (grade_year BETWEEN 1 AND 3),
    CONSTRAINT chk_grade_record_term CHECK (term IN (1, 2)),
    CONSTRAINT chk_grade_record_raw_score CHECK (raw_score IS NULL OR (raw_score BETWEEN 0 AND 100)),
    CONSTRAINT chk_grade_record_std_dev CHECK (std_dev IS NULL OR std_dev >= 0),
    CONSTRAINT chk_grade_record_grade_level CHECK (grade_level IS NULL OR (grade_level BETWEEN 1 AND 9)),
    CONSTRAINT chk_grade_record_source CHECK (source IN ('PDF', 'MANUAL')),
    CONSTRAINT chk_grade_record_unmatched_reason
        CHECK (unmatched_reason IS NULL OR unmatched_reason IN ('NOT_FOUND', 'AMBIGUOUS', 'NO_ASSESSMENT')),
    INDEX idx_grade_record_lookup (student_id, grade_year, term)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
