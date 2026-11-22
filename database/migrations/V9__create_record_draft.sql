-- 생기부 관리: 과목별 세특(세부능력 및 특기사항) 초안. 학생이 학기별로 작성·제출하고, 선생님 확인은 나중 단계에서 붙인다.
-- semester는 '학년-학기' 문자열('1-2' = 1학년 2학기). (student_id, semester, subject_name)은 하나만 있다.
-- source: NEW = 이번에 작성하는 초안, PAST = 이미 확정된 과거 기록을 옮겨 적은 것(status는 항상 CONFIRMED).
-- course_id는 학점 현황에 등록한 과목(credit_student_course)과의 연결. 과목을 지우면 초안은 남기고 연결만 끊는다(SET NULL).

CREATE TABLE record_draft (
    id                BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    student_id        VARCHAR(100)    NOT NULL,
    semester          VARCHAR(3)      NOT NULL,
    subject_name      VARCHAR(100)    NOT NULL,
    course_id         BIGINT UNSIGNED NULL,
    source            VARCHAR(10)     NOT NULL,
    -- 4칸 내용. 비어 있으면 빈 문자열
    motive            TEXT            NOT NULL,
    process           TEXT            NOT NULL,
    result            TEXT            NOT NULL,
    career_link       TEXT            NOT NULL,
    guideline_title   VARCHAR(200)    NULL,
    guideline_detail  TEXT            NULL,
    status            VARCHAR(20)     NOT NULL,
    teacher_comment   TEXT            NULL,
    submitted_at      DATETIME        NULL,
    created_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_record_draft_subject UNIQUE (student_id, semester, subject_name),
    CONSTRAINT fk_record_draft_course
        FOREIGN KEY (course_id) REFERENCES credit_student_course (id) ON DELETE SET NULL,
    CONSTRAINT chk_record_draft_semester CHECK (semester IN ('1-1', '1-2', '2-1', '2-2', '3-1', '3-2')),
    CONSTRAINT chk_record_draft_source CHECK (source IN ('NEW', 'PAST')),
    CONSTRAINT chk_record_draft_status CHECK (status IN ('DRAFT', 'SUBMITTED', 'REVISION_REQUESTED', 'CONFIRMED')),
    CONSTRAINT chk_record_draft_past_confirmed CHECK (source <> 'PAST' OR status = 'CONFIRMED'),
    INDEX idx_record_draft_lookup (student_id, semester)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
