# DB ERD

기준: database/migrations/V1~V2, V6에 적용된 실제 스키마 (V3~V5, V7은 시딩 데이터라 테이블 구조에 영향 없음).
계산값(성취율, 이수 상태, 출석률)은 저장하지 않는다 — CLAUDE.md 규칙.

```mermaid
erDiagram
    credit_course_catalog {
        BIGINT id PK
        VARCHAR name
        VARCHAR subject_group
        VARCHAR course_type
    }

    credit_completion_rule {
        BIGINT id PK
        VARCHAR course_type
        INT effective_year
        DECIMAL min_attendance
        DECIMAL min_achievement "nullable"
        DECIMAL caution_below_rate "nullable"
    }

    credit_graduation_rule {
        BIGINT id PK
        INT effective_year
        INT total_credits
        INT subject_credits
        INT creative_credits
    }

    credit_student_course {
        BIGINT id PK
        VARCHAR student_id
        BIGINT catalog_id FK "nullable"
        VARCHAR custom_name "nullable"
        VARCHAR course_type
        INT credits
        TINYINT grade_year
        TINYINT term
        VARCHAR status
        INT written_weight
        INT performance_weight
        VARCHAR grade_display "nullable"
        INT absent_count
        INT total_sessions "nullable"
    }

    credit_assessment {
        BIGINT id PK
        BIGINT student_course_id FK
        VARCHAR kind
        VARCHAR title
        INT weight_percent
        INT max_score
        INT score "nullable"
        VARCHAR task_id "nullable, 외부 시스템 ID, FK 아님"
        DATE evaluated_on
    }

    credit_department {
        BIGINT id PK
        VARCHAR name
        INT effective_year
        VARCHAR field "nullable"
        INT total_university_count
        TINYINT low_sample
        VARCHAR general_elective_text "nullable, 시도교육청 자료, 승인 전까지 화면 노출 보류"
        VARCHAR career_elective_text "nullable"
        VARCHAR fusion_elective_text "nullable"
    }

    credit_department_subject {
        BIGINT id PK
        BIGINT department_id FK
        BIGINT catalog_id FK
        INT required_university_count
        INT total_university_count
        DECIMAL ratio_percent
        TINYINT full_common
        VARCHAR designation_type
        TINYINT eduoffice_listed
        VARCHAR required_universities "콤마 구분 텍스트"
    }

    credit_course_catalog ||--o{ credit_student_course : "catalog_id"
    credit_student_course ||--o{ credit_assessment : "student_course_id, ON DELETE CASCADE"
    credit_department ||--o{ credit_department_subject : "department_id, ON DELETE CASCADE"
    credit_course_catalog ||--o{ credit_department_subject : "catalog_id"
```

## 참고
- `credit_completion_rule`, `credit_graduation_rule`은 다른 테이블과 FK 관계가 없는 독립 설정 테이블이다. `course_type`/`effective_year` 값으로 서비스 로직에서 참조한다.
- `credit_student_course.catalog_id`는 nullable이다 — 표준 과목 목록에 없는 과목(직접 입력, `custom_name`)을 등록할 수 있다.
- `credit_assessment.task_id`는 기존 수행평가 시스템 연결용 문자열이며 FK가 아니다(다른 시스템 소유, V1 주석 참고). 이번 범위에서는 항상 비워 둔다.
- course_type 허용값(V2 기준): COMMON, GENERAL, CAREER, FUSION, CREATIVE, SPECIALIZED.
- `credit_department`/`credit_department_subject`(학과 로드맵, docs/dept_subject_2depth.xlsx 기반)는 학생과 무관한 참고 데이터다. `effective_year`로 연도별 자료를 구분하며, 기존 행을 덮어쓰지 않고 새 연도를 추가하는 방식(credit_completion_rule과 같음)이다. `ratio_percent`가 100이면 전체 대학 공통, 50 이상이면 다수 대학 공통, 미만이면 대학별로 다른 과목이다(별도 구분 컬럼 없음). `general/career/fusion_elective_text`는 서울시교육청교육연구정보원 자료라 이용 승인 전까지 서비스 코드에서 노출하지 않는다(docs/PROGRESS.md "미정" 참고).
