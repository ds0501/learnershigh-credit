-- 학과 로드맵: 희망 학과를 검색하면 추천 선택과목을 보여준다
-- 자료: 대교협 「2028학년도 권역별 대학별 권장과목」(docs/dept_subject_2depth.xlsx). 연도별 자료가 또 나오면
-- 기존 행을 덮어쓰지 않고 새 effective_year로 추가한다(credit_completion_rule과 같은 방식).
-- 선이수 조건은 저장하지 않는다(근거 미확인 자료라 제외).

CREATE TABLE credit_department (
    id                     BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    name                   VARCHAR(50)  NOT NULL,
    effective_year         INT          NOT NULL,
    field                  VARCHAR(30)  NULL,
    total_university_count INT          NOT NULL,
    low_sample             TINYINT(1)   NOT NULL DEFAULT 0,
    -- 서울시교육청교육연구정보원 자료(참고_교육청예시 시트). 이용 승인 전까지 서비스 코드에서 노출하지 않는다
    general_elective_text  VARCHAR(300) NULL,
    career_elective_text   VARCHAR(300) NULL,
    fusion_elective_text   VARCHAR(300) NULL,
    CONSTRAINT uq_department_name_year UNIQUE (name, effective_year),
    CONSTRAINT chk_department_total_university_count CHECK (total_university_count > 0),
    CONSTRAINT chk_department_field
        CHECK (field IS NULL OR field IN
            ('공학', '사회', '자연', '보건·의약학', '인문', '예술·체육', '교육', '계약학과 및 첨단학과', '자율전공'))
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE credit_department_subject (
    id                         BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    department_id              BIGINT UNSIGNED NOT NULL,
    catalog_id                 BIGINT UNSIGNED NOT NULL,
    required_university_count  INT            NOT NULL,
    total_university_count     INT            NOT NULL,
    ratio_percent              DECIMAL(5, 2)  NOT NULL,
    full_common                TINYINT(1)     NOT NULL DEFAULT 0,
    designation_type           VARCHAR(20)    NOT NULL,
    eduoffice_listed           TINYINT(1)     NOT NULL DEFAULT 0,
    required_universities      VARCHAR(255)   NOT NULL,
    CONSTRAINT fk_department_subject_department
        FOREIGN KEY (department_id) REFERENCES credit_department (id) ON DELETE CASCADE,
    CONSTRAINT fk_department_subject_catalog
        FOREIGN KEY (catalog_id) REFERENCES credit_course_catalog (id),
    CONSTRAINT chk_department_subject_designation_type
        CHECK (designation_type IN ('CORE', 'RECOMMENDED', 'UNSPECIFIED')),
    CONSTRAINT chk_department_subject_ratio CHECK (ratio_percent BETWEEN 0 AND 100),
    CONSTRAINT chk_department_subject_counts
        CHECK (total_university_count > 0 AND required_university_count BETWEEN 1 AND total_university_count),
    CONSTRAINT uq_department_subject UNIQUE (department_id, catalog_id),
    INDEX idx_department_subject_lookup (department_id, ratio_percent),
    INDEX idx_department_subject_catalog (catalog_id)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
