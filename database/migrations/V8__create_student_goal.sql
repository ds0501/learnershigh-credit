-- 학생부 관리 공통: 학생의 희망 학과와 목표 대학 (학생당 1개)
-- 학점 현황, 학과 로드맵 등 학생부 관리의 모든 탭이 같은 값을 쓴다.
-- student_id는 credit_student_course.student_id와 같은 타입(VARCHAR(100))이다.

CREATE TABLE student_goal (
    id                BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    student_id        VARCHAR(100)    NOT NULL,
    department_id     BIGINT UNSIGNED NOT NULL,
    -- 선택 입력. 없으면 NULL
    target_university VARCHAR(100)    NULL,
    updated_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_student_goal_student UNIQUE (student_id),
    CONSTRAINT fk_student_goal_department
        FOREIGN KEY (department_id) REFERENCES credit_department (id)
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
