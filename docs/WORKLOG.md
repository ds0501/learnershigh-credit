## 작업 로그 (최신이 위)
형식:
### 날짜 — 제목
- 한 일:
- 바뀐 파일:
- 확인 결과(확인하지 못한 것 포함):
- 결정과 이유:
- 막힌 점:
- 다음 할 일:

### 2026-10-28 — 졸업/이수 기준값과 데모 학생 과목·평가 시딩 (V4)
- 한 일: PROGRESS.md "다음 할 일" 1번(남은 시연용 초기 데이터)을 database/migrations/V4__seed_demo_student_data.sql로 작성. credit_graduation_rule 1행(2025, 192/174/18), credit_completion_rule 6행(course_type별, effective_year 2026, COMMON만 min_achievement 40·caution_below_rate 50, 나머지는 NULL), credit_student_course 20행(1학년 1학기 10개 COMPLETED + 2학기 10개 IN_PROGRESS), credit_assessment 44행. 전부 frontend/src/mocks/creditStore.ts의 시연 데이터와 같은 값
- 바뀐 파일: database/migrations/V4__seed_demo_student_data.sql(신규), docs/PROGRESS.md(완료, 다음 할 일)
- 확인 결과(확인하지 못한 것 포함):
  - creditStore.ts를 다시 한 줄씩 대조하다가 "운동과 건강"을 처음에 실제 2022 교육과정 분류(진로선택=CAREER)로 잘못 넣었던 걸 발견. creditStore.ts와 api-contract.md 예시는 둘 다 GENERAL로 되어 있어(기존에 이미 문서·테스트로 고정된 값) GENERAL로 고쳐서 다시 생성함
  - `./gradlew bootRun`으로 실제 적용 확인: `Current version: 3` → `Migrating to "4 - seed demo student data"` → `Successfully applied 1 migration ... now at version v4`
  - DB에서 직접 확인: credit_student_course 20행·credit_assessment 44행. id=16이 통합과학2(기대값과 일치), id=8의 custom_name='체육'·catalog_id=NULL(카탈로그에 '체육1/체육2'만 있어 정확히 일치하는 이름이 없어 직접 입력으로 처리) 확인
  - 성취율을 손으로 다시 계산해 PRD·api-contract.md 예시와 대조: 통합과학2(id=16) = (25×30+50×20)÷50 = 35%(예시와 일치), 공통수학2(id=12) = (50×30+40×20)÷50 = 46%(예시와 일치)
- 결정과 이유:
  - student_id는 'demo-student'로 고정(문서에 특정 문자열이 정해진 적이 없어 새로 정함)
  - credit_completion_rule.effective_year=2026(PRD 1장 "2026학년도부터" 적용 문구 기준), credit_graduation_rule.effective_year=2025(PRD 1장 "2025학년도 고1부터 전면 시행" 기준) — 둘이 다른 연도인 이유는 PRD에 적힌 시행 시점이 서로 다르기 때문
  - catalog_id는 이름으로 서브쿼리 조회(실제 DB catalog 순서가 frontend mock 순서와 달라 숫자를 하드코딩하면 틀림 — PROGRESS.md "미정" 참고). student_course_id·assessment id는 빈 테이블에 이 파일 하나로만 순서대로 넣은 것이라 1~20, 1~44로 고정됨을 믿고 하드코딩함(AUTO_INCREMENT)
- 막힌 점: 없음
- 다음 할 일: PROGRESS.md "다음 할 일" 2번(공통: 데모 학생 고정 처리, 오류 응답 형식, 성공 코드)부터 — 실제 컨트롤러/서비스 코드 작성 시작

### 2026-10-28 — 전문교과 이수 기준 반영
- 한 일: 사용자가 알려준 전문교과 이수 기준("2026학년도부터는 선택과목에 준하는 전문교과는 출석률 2/3 이상만으로 학점 취득 가능")을 PRD 6장 이수 기준 표에 추가(전문교과 행: 최소 출석률 66.67%, 최소 성취율 없음). 이전 WORKLOG "표준 과목 목록 실데이터 시딩" 항목에서 "미정"으로 남겨뒀던 것 중 이 부분을 해결함
- 바뀐 파일: docs/PRD.md(1장 출처, 6장 이수 기준 표, 12장 미정), docs/PROGRESS.md(다음 할 일, 미정). DB·코드 변경 없음
- 확인 결과(확인하지 못한 것 포함): 사용자가 알려준 내용을 그대로 반영. 교육부 원문으로 별도 대조는 하지 않음(확인 못 함, PRD 1장 "출처와 한계"에 이미 있는 제약과 같음)
- 결정과 이유:
  - 사용자가 함께 알려준 "특성화고·산업수요맞춤형고는 교과 174학점 중 전문교과 80학점 이상 + 보통교과 64학점 이상 편성·이수"는 졸업 기준 쪽 정보라 이번 항목과 분리해서 처리함. 이건 credit_graduation_rule에 반영하지 않고 PRD 12장 "미정"에 "이번 범위 밖(일반고만 다룸)"으로 적어 둠 — 지금 이 기능은 일반고 기준(192/174/18)만 구현 대상이고(CLAUDE.md, hscredit.kr 페이지의 "일반고 편성 기준" 안내와도 일치), 전문교과를 catalog에 넣은 건 일반고 학생도 전문교과를 선택할 수 있어서일 뿐 특성화고 졸업 기준을 구현하겠다는 뜻은 아니었음. 학교 유형 구분이 필요해지면 그때 다시 설계해야 함(지금 credit_graduation_rule에는 학교 유형 컬럼이 없음)
  - credit_completion_rule에 전문교과 행을 넣을 때 min_achievement는 NULL(선택과목과 같음)로 하면 됨이 이번에 확정됨
- 막힌 점: 없음
- 다음 할 일: 데모 학생 과목·평가 초기 데이터(PROGRESS.md "다음 할 일" 1번) — 이제 전문교과 이수 기준도 정해졌으니 credit_completion_rule 시딩에 포함 가능

### 2026-10-28 — 표준 과목 목록 실데이터 시딩
- 한 일: credit_course_catalog에 실제 과목 목록을 채움. 한국교육과정평가원 고교학점제 홈페이지(https://www.hscredit.kr/curriculum/subjectInfo)의 과목별 상세안내 표를 받아와 과목명·교과(군)·교과유형을 추출(총 290개). 그중 135개가 "전문교과"(특성화고·마이스터고 편성)였는데, 사용자에게 포함 여부를 물어 "다 포함한다(290개 전체)"로 결정됨. 지금 course_type CHECK 제약(COMMON/GENERAL/CAREER/FUSION/CREATIVE)에는 전문교과 자리가 없어 V2__add_specialized_course_type.sql로 SPECIALIZED 값을 추가(컬럼도 VARCHAR(10)→VARCHAR(20), SPECIALIZED가 11자라 안 들어감). V3__seed_course_catalog.sql로 290개 + 창의적 체험활동(공식 목록에는 없어 수동 추가, 기존 frontend mock과 같은 방식) 1개 = 291개를 INSERT
- 바뀐 파일: database/migrations/V2__add_specialized_course_type.sql(신규), database/migrations/V3__seed_course_catalog.sql(신규), docs/PRD.md(1장 출처, 7장 course_type 값 목록, 12장 미정), docs/api-contract.md(courseType 값 표에 SPECIALIZED 추가), docs/PROGRESS.md(완료, 다음 할 일, 미정)
- 확인 결과(확인하지 못한 것 포함):
  - 페이지를 curl로 받은 원본 HTML(487KB)에 표가 서버 렌더링돼 있어 `<tr data-type= data-group= data-name=>` 속성을 정규식으로 그대로 파싱(LLM 요약 대신 — 290행 전체가 정확한지 요약 모델에 맡기면 누락·변형 위험이 있어서 원본 HTML을 직접 파싱함). 행 수 291(헤더 포함) 확인, 중복 과목명 없음 확인, 홑따옴표·& 등 SQL 이스케이프 필요한 문자 없음 확인
  - Chrome 확장이 연결돼 있지 않아 브라우저로 직접 열어 눈으로 재확인은 못 함(확인 못 함). curl 원본과 WebFetch 요약(상위 20개 예시) 결과가 일치하는 것으로 교차 확인함
  - `cd backend && ./gradlew bootRun`으로 실제 Flyway 적용 확인: `Current version: 1` → `Migrating to "2 - add specialized course type"` → `Migrating to "3 - seed course catalog"` → `Successfully applied 2 migrations ... now at version v3`. DB에서 `SELECT COUNT(*) FROM credit_course_catalog` → 291, `GROUP BY course_type` → SPECIALIZED 135 / CAREER 64 / FUSION 38 / GENERAL 35 / COMMON 18 / CREATIVE 1(합계 291, 기대값과 일치)
  - 한글이 mysql CLI에서 "?"로 보여 데이터 손상처럼 보였으나 `--default-character-set=utf8mb4`로 다시 조회하니 정상 표시, `CHAR_LENGTH`/`LENGTH`로 바이트 수 확인(예: "공통국어1" 5자 13바이트, 한글 1자=3바이트 계산과 일치) → 클라이언트 인코딩 설정 문제였을 뿐 데이터는 정상
  - 저작권: 사이트에 "저작권자: 한국교육과정평가원", "본 내용은 일반계 고등학교(일반고) 편성 기준"이라는 안내는 있으나 재배포 조건을 명시한 라이선스 표기(공공누리 등)는 페이지에서 찾지 못함(확인 못 함)
- 결정과 이유:
  - 전문교과 135개 포함 여부는 사용자에게 직접 물어 결정함(임의로 정하지 않음). 포함하기로 하면서 기존 설계(course_type 5개)를 넘어서게 돼 스키마 변경이 필요해짐
  - V1은 이미 Flyway로 실제 DB에 적용된 파일이라 고치지 않고 V2(스키마 확장), V3(데이터)로 분리해서 추가함
  - 전문교과 새 값 이름은 "SPECIALIZED"로 정함(기존 값들처럼 영어 대문자 스네이크 없는 단어 한 개)
  - 창의적 체험활동은 이 공식 290개 목록에 없다(KICE 분류상 "과목"이 아니라 별도 교육과정 영역). 기존 frontend mock(catalog.ts)이 이미 창체를 수동으로 1개 추가해 둔 방식과 똑같이 맞춰서 291번째 행으로 추가함
  - 전문교과의 이수 기준(credit_completion_rule 행)은 지금 정하지 않고 "미정"에 남김 — PRD 6장이 공통/선택과목만 다루고 있어 임의로 선택과목과 같다고 가정하지 않음
- 막힌 점: Chrome 확장 미연결로 브라우저 직접 확인은 못 함(위 "확인 결과" 참고, curl+WebFetch 교차 확인으로 대신함)
- 다음 할 일: 데모 학생 과목 20개·평가 44개 시딩(이때 catalogId를 실제 DB 값으로 다시 찾아 써야 함 — PROGRESS.md "미정" 참고), 졸업·이수 기준 시딩

### 2026-10-28 — DB 테이블 V1 작성
- 한 일: database/migrations/V1__create_credit_tables.sql 작성. PRD 7장의 5개 테이블(credit_course_catalog, credit_completion_rule, credit_graduation_rule, credit_student_course, credit_assessment)을 그대로 옮기고, 각 enum성 컬럼(course_type, status, kind, grade_display)에 CHECK 제약, FK(catalog_id, student_course_id), 범위 체크(credits≥1, weight 0~100, score≤max_score 등)를 추가. credit_assessment.student_course_id는 ON DELETE CASCADE로 둬서 "과목 삭제 시 평가도 함께 삭제"(api-contract.md 4.8) 요구를 DB 레벨에서도 보장함
- 바뀐 파일: database/migrations/V1__create_credit_tables.sql(신규), docs/PROGRESS.md(완료 체크리스트, 다음 할 일, 미정, 막힌 것)
- 확인 결과(확인하지 못한 것 포함):
  - 이 머신에 Java 21이 없어 ./gradlew flywayMigrate / bootRun을 실행할 수 없었음(확인 못 함). 대신 docker compose up -d로 MySQL(8.4)을 띄우고 mysql 클라이언트로 SQL 파일을 직접 실행해 문법·제약조건을 확인: 5개 테이블 모두 생성됨(SHOW TABLES). 잘못된 course_type(INVALID) INSERT → chk_catalog_course_type 위반으로 거부. credits=0 INSERT → chk_student_course_credits 위반으로 거부. score>max_score INSERT → chk_assessment_score 위반으로 거부. catalog_id FK로 정상 student_course INSERT 성공. student_course 삭제 시 연결된 credit_assessment 행이 cascade로 같이 삭제됨을 확인(삭제 후 COUNT(*)=0)
  - 확인 후 테스트로 넣은 행은 전부 지우고 5개 테이블을 DROP해 스키마를 다시 빈 상태로 되돌림 — Flyway가 자기 schema_history 없이 이미 존재하는 테이블을 만나 실패하는 걸 막기 위함(다음에 Java 21 설치 후 ./gradlew bootRun을 돌리면 Flyway가 V1을 처음부터 깨끗하게 적용해야 함, 이 부분은 아직 확인 못 함)
- 결정과 이유:
  - "미정"에 있던 주의 상한(50%) 저장 위치를 지금 결정: credit_completion_rule에 caution_below_rate(DECIMAL(5,2), NULL) 컬럼 추가. CLAUDE.md "이수 기준은 설정값으로 둔다"와 맞고, 기존 min_attendance/min_achievement와 같은 테이블·같은 패턴(과목유형·연도별 1행)이라 분리할 이유가 없었음. "미정"에서 이 항목 지움
  - credit_completion_rule, credit_graduation_rule에는 PRD 7장에 없던 surrogate PK(id)를 추가함(자연키만으로는 JPA 엔티티 매핑이 번거로워서). 자연키는 각각 UNIQUE(course_type, effective_year), UNIQUE(effective_year)로 보존
  - credit_assessment.task_id는 FK를 걸지 않음(기존 수행평가 시스템은 이 DB 밖에 있어 참조 무결성을 보장할 수 없음, PRD에도 "이번 단계는 비워 둠"이라고 되어 있음)
  - enum성 값은 MySQL ENUM 타입 대신 VARCHAR + CHECK로 둠(컬럼 길이·허용값이 SQL에 그대로 보여 유지보수가 쉽고, MySQL 8.4는 CHECK를 강제함)
- 막힌 점: Java 21 미설치로 Flyway/Gradle 경로를 통한 확인은 못 함. 다음에 백엔드를 처음 실행하는 사람이 Java 21을 설치하고 ./gradlew bootRun → Flyway가 V1을 적용하는지 반드시 확인해야 함
- 다음 할 일: 시연용 초기 데이터 V2(PROGRESS.md "다음 할 일" 1번)

### 2026-10-28 — 표준 과목 목록에서 학점 제거(항상 학생 입력)
- 한 일: credit_course_catalog(표준 과목 목록)에서 credits를 빼고, credit_student_course.credits만 남김. 학점은 학교 교육과정 편성에 따라 달라 전국 공통 값이 없고, 기존 api-contract.md 4.5에도 "목록 과목도 요청 값을 쓴다(학교 편성에 맞춰 바꿀 수 있음)"고 이미 적혀 있어 catalog.credits는 저장·판정에 쓰인 적이 없던 값이었음. 과목 등록 모달에서 목록 과목을 골라도 학점을 자동 채우지 않고 항상 직접 입력하도록 바꿈(이전엔 선택 시 표준 학점으로 자동 채움 + 수정 가능이었음)
- 바뀐 파일:
  - docs/PRD.md: 7장 credit_course_catalog 표에서 credits 제거(이유 한 줄 추가), 3·4(시나리오)·F2 설명에서 "학점 자동 채움" 문구 수정
  - docs/api-contract.md: CatalogItem 필드 표·DB 대응표·GET /catalog 예시에서 credits 제거
  - frontend/src/modules/credit/types.ts: CatalogItem에서 credits 제거
  - frontend/src/mocks/catalog.ts: CatalogSeed/SEEDS/매핑에서 credits 제거
  - frontend/src/modules/credit/components/CourseRegisterModal.tsx: 검색 결과의 학점 표시, 선택 시 setCredits(item.credits) 자동 채움, 안내 문구의 "학점" 제거
  - (ERD 아티팩트: 저장소 밖 claude.ai 아티팩트라 커밋 대상 아님)
- 확인 결과: npm run build(tsc -b) 통과. npm test → 7개 파일 68개 모두 통과(수정 불필요, catalog 응답의 credits를 단언하는 테스트가 없었음). npm run lint 경고 0개. frontend/src/mocks/credit.ts의 findCatalogItem 사용처(과목 등록 처리)는 name·courseType만 쓰고 credits는 항상 input.credits(요청값)를 써서 이번 변경과 무관함을 코드로 확인
- 결정과 이유: 사용자가 "학점은 학교마다 과목별로 다를 수 있어 학생(학교) 입력이 맞다"고 판단, catalog에 남겨두면 추천값을 그대로 쓰고 넘어가는 오입력 위험과 안 쓰이는 컬럼이라는 정합성 문제가 있어 완전히 제거하는 쪽으로 결정(자동완성 편의는 포기). 이수 기준(credit_completion_rule)·졸업 기준(credit_graduation_rule)은 "전국 공통"이어도 effective_year 축이 있어(교육과정 개정마다 바뀜) 테이블 2개로 유지하기로 함(CLAUDE.md "이수 기준은 설정값으로 둔다"와 일치) — 이 부분은 테이블 구조를 바꾸지 않음
- 막힌 점: 없음
- 다음 할 일: "미정"의 주의 상한(50%) 저장 위치는 아직 못 정함 — DB 테이블 V1 착수 전에 정해야 함

### 2026-10-28 — 성취율 오차 안내와 일정, 형식 정리
- 한 일: api-contract.md 1장 표 아래에 성취율 끝자리 부동소수점 오차 안내 한 줄(예: 음악 78.99999999999999), 표의 "숫자" 칸에 "(끝자리 오차 안내는 아래 참고)" 추가. PROGRESS "미정"에 성취율 소수 오차 처리 방식 추가. PRD 13장 일정 표에 "진행 (2026-10-28 기준)" 칸 추가(커밋 날짜로 확인한 사실만), 다음 단계(테이블과 seed, 백엔드 API, 날짜 미정) 한 줄. WORKLOG 형식 설명의 항목 이름을 CLAUDE.md 규칙에 맞춤("확인 결과(확인하지 못한 것 포함)", "막힌 점")
- 바뀐 파일: docs/api-contract.md(1장), docs/PROGRESS.md(미정), docs/PRD.md(13장), docs/WORKLOG.md(형식 설명, 이 항목). 코드 변경 없음
- 확인 결과: 음악 78.99999999999999가 api-contract.md 4.2 예시에 실제로 있음을 확인. 예시 JSON과 contract 표시는 그대로 두었고 npm test → 7개 파일, 68개 모두 통과(contract.test.ts 포함). 13장 진행 칸은 git 커밋 날짜(10-27: 문서·백엔드 뼈대, 10-28: 프론트 뼈대부터 API 명세까지)와 요일(10-27 화, 10-28 수)로 확인
- 결정과 이유: 13장은 원래 목표 칸을 지우지 않고 진행 칸을 덧붙임(계획과 실제를 같이 보이게). 목요일 행은 아직 오지 않아 "아직 하지 않음"
- 막힌 점: 없음
- 다음 할 일: "미정"의 주의 상한(50%) 저장 위치를 정하고 DB 테이블 V1

### 2026-10-28 — PROGRESS 정리, 작업 로그를 WORKLOG로 이동
- 한 일: PROGRESS.md의 "작업 로그" 섹션(62~324줄)을 그대로 docs/WORKLOG.md로 옮기고 PROGRESS에는 안내 한 줄만 둠. PROGRESS 맨 위에 "현재 상태" 3줄, "완료" 체크리스트는 항목당 한 줄(+ WORKLOG 항목 이름), "지금 하는 것" 한 줄. "다음 할 일", "막힌 것", "미정", "실행 방법"은 그대로. CLAUDE.md "작업 기록" 규칙을 PROGRESS(상태만)와 WORKLOG(로그, 항목당 20줄 안팎)로 나눔. PRD 최종 갱신 2026-10-28, 4장 시연 시나리오를 현재 구현 5단계로 바꿈(5단계 예시는 한국사2)
- 바뀐 파일: docs/WORKLOG.md(신규), docs/PROGRESS.md, CLAUDE.md(작업 기록 섹션), docs/PRD.md(머리말, 4장). 코드 변경 없음
- 확인 결과:
  - 옮긴 내용: 이전 PROGRESS(git HEAD)의 62줄~끝 263줄과 WORKLOG.md 263줄(이 항목 추가 전)을 diff로 비교 → 차이 없음. 바이트 비교에서는 줄바꿈만 다름(git 저장본 LF, 작업 폴더 CRLF). CR을 빼면 바이트까지 같음
  - 체크리스트 괄호 안의 실행 확인 내용은 모두 WORKLOG의 해당 항목에 같은 내용이 있어서 지움
  - PRD 4장 5단계 숫자: 임시 테스트로 mock을 실제 호출해 확인(한국사2(ID 14, 3학점)를 PATCH로 이수 완료 → 이수 완료 32 → 35, 수강 중 32 → 29, 남은 128 그대로, 남은 필수 과목 7 → 6(한국사2 빠짐), 교과 29 → 32). 임시 테스트 파일은 확인 후 지움(커밋 안 함)
  - 확인 못 함: 없음(문서만 바뀌어 빌드·테스트는 다시 돌리지 않음)
- 결정과 이유: WORKLOG.md는 옮긴 섹션을 한 글자도 바꾸지 않으려고 제목(#)을 따로 붙이지 않음(첫 줄이 "## 작업 로그 (최신이 위)"). "완료"에서 DB 테이블 V1 미완료 항목은 맨 아래로 옮김
- 막힌 점: 없음
- 다음 할 일: "미정"의 주의 상한(50%) 저장 위치를 정하고 DB 테이블 V1

### 2026-10-28 — API 명세 문서 추가
- 한 일:
  1) docs/api-contract.md 새로 작성: 공통 규칙(경로 접두사, camelCase, 문자열 ID, 날짜, 성공 코드 200/201/204, 오류 400 { message, fieldErrors }·404), 값과 화면 문구 대응(riskLevel, courseType, status, gradeDisplay, kind), 응답 객체 표(CreditSummary, SemesterCourses, RequiredCourses, CourseItem, CourseDetail, Assessment, CatalogItem), 엔드포인트 11개(GET summary, GET courses?gradeYear=&term=, GET required-courses, GET catalog?query=, POST courses, GET/PATCH/DELETE courses/{courseId}, POST courses/{courseId}/assessments, PUT·DELETE courses/{courseId}/assessments/{assessmentId})마다 요청·응답 예시, 검증 규칙과 오류 문구, 실패 응답. 계산 규칙(성취율·출석률·필요 평균 식, 7단계 판정 순서, 저장하지 않고 응답 때 계산, API는 이수 상태와 관계없이 계산값을 주고 화면·배너는 수강 중 과목만 씀)과 구현 참고 파일, DB 대응 표(응답 필드 ← PRD 7장 테이블·컬럼, 학생 구분은 credit_student_course.student_id, 데모 학생 1명 고정)
  2) 예시는 지어내지 않고 mock을 실제로 호출한 결과를 붙임: frontend/src/mocks/contractScenario.ts에 호출 순서 22단계(조회 → 등록 → 상태 변경 → 평가 추가·수정·삭제 → 과목 삭제)를 두고 그 결과 JSON을 문서에 넣음. 문서의 각 예시에 <!-- contract:id 상태코드 -->, <!-- contract-request:id --> 표시를 붙임
  3) frontend/src/mocks/contract.test.ts 추가: 문서의 요청 예시를 그대로 mock에 보내고, 상태 코드와 응답 본문이 문서 예시와 키·값까지 같은지 비교. 문서의 예시 표시와 호출 순서가 1:1인지도 확인
  4) PRD 8장을 "API 요약"으로 줄이고 상세는 api-contract.md로 연결(예시·검증 문구는 api-contract.md로 옮기고 8장에서 지움)
  5) types.ts 주석 3곳을 api-contract.md와 맞춤(파일 머리 설명, requiredAverage 조건, CourseDetail 설명). 필드 변경은 없음
- 바뀐 파일: docs/api-contract.md(신규), docs/PRD.md(8장), docs/PROGRESS.md, frontend/src/mocks/contractScenario.ts(신규), frontend/src/mocks/contract.test.ts(신규), frontend/src/modules/credit/types.ts(주석만)
- 확인 방법과 결과:
  1) npm test → 7개 파일, 68개 모두 통과(새로 2개: 예시 표시와 호출 순서 1:1, 22단계 응답 비교). 처음 돌렸을 때 "update-assessment-not-found: 요청 예시가 문서에 없음"으로 실패 → 문서에 그 요청 예시를 추가해 통과
  2) 테스트가 실제로 어긋남을 잡는지 확인: 문서의 47.85714285714286을 48로 일부러 바꿔 돌리면 course-detail-after-add에서 실패, 되돌리면 통과
  3) npm run build 통과(테스트 파일 포함 타입 검사), npm run lint 경고 0개
  4) types.ts와 문서 대조(직접 읽어서): CreditSummary, CourseItem, SemesterCourses, RequiredCourses, Assessment, AssessmentInput, CatalogItem, CourseRegistrationInput, CourseDetail의 필드 이름·타입·선택 여부가 문서 3장·4장 표와 모두 같음. 다른 점: PATCH 요청 본문 { status }는 types.ts에 이름 붙은 타입이 없고 api.ts에서 바로 만듦(동작에는 차이 없음). 오류 본문 { message, fieldErrors }는 types.ts가 아니라 src/api/errors.ts의 ApiError로 다룸
  확인 못 함: 실제 백엔드 응답(아직 없음)
- 결정과 이유:
  - 학기별 과목 조회는 mock·PRD대로 ?gradeYear=1&term=2 (사용자 결정)
  - 예시 JSON은 mock 응답을 그대로 쓰되 평가 객체만 id를 앞에 두도록 키 순서를 바꿈(테스트는 키 순서를 보지 않음)
  - mock에는 HTTP 상태 코드가 없어서 문서에 조회·수정 200, 생성 201, 삭제 204로 적고 201은 "미정"에 적음
  - 요청 문구의 "위험·주의 표시와 필요 평균은 수강 중 과목에만"은 mock 확인 결과 화면 동작이고, API 응답은 이수 완료 과목에도 riskLevel·requiredAverage를 그대로 줌 → 문서에 그대로 적고 "미정"에 적음
  - PRD 7장 credit_student_course에 student_id가 이미 있어 7장은 고치지 않음. 평가는 과목 FK로 학생이 정해짐
  - 주의 상한(50%)을 둘 컬럼이 PRD 7장에 없음 → 문서에는 설정값으로만 적고 "미정"에 적음
  - "미정"에서 과목 상세 API, API 오류 응답 형식 두 항목을 지움(문서로 확정)
- 막힌 점과 해결: 문서 초안의 "출석률 미달 RISK에는 필요 평균을 넣지 않는다"가 코드와 다름(성취율도 40 미만이면 넣음)을 코드 대조로 찾아 고침
- 다음 할 일: "다음 할 일"의 백엔드 구현 순서 1번(DB 테이블 V1)부터

### 2026-10-28 — 평가 수정 경로를 과목 아래로 통일
- 한 일: 평가 수정 API를 PUT /student/credit/assessments/{assessmentId}에서 PUT /student/credit/courses/{courseId}/assessments/{assessmentId}로 바꿈(평가 추가·삭제와 같은 모양). 그 과목의 평가가 아니면 404 "평가를 찾을 수 없습니다.", 없는 과목이면 404 "과목을 찾을 수 없습니다.". mock 핸들러, api.ts의 updateAssessment(courseId, assessmentId, input), 점수 입력 폼, PRD 8장 API 표를 고침
- 바뀐 파일: frontend/src/mocks/credit.ts, frontend/src/mocks/index.ts(주석 예시), frontend/src/mocks/assessmentUpdate.test.ts(신규), frontend/src/mocks/courseStatus.test.ts, frontend/src/modules/credit/api.ts, frontend/src/modules/credit/types.ts(주석), frontend/src/modules/credit/components/AssessmentForm.tsx, docs/PRD.md(8장 표), docs/PROGRESS.md
- 확인 방법과 결과:
  1) npm test → 6개 파일, 66개 모두 통과. 새 테스트: 통합과학2 실험 보고서 50 → 100점 수정 시 35% → 55% 정상 / 다른 과목 경로로 수정하면 404이고 값이 바뀌지 않음 / 없는 과목 404 / 득점 > 만점 400. 과목 삭제 테스트는 옛 PUT 경로로 404를 보던 것을 "저장소에서 그 과목의 평가가 지워졌는지" 직접 보도록 바꿈
  2) npm run build 통과, npm run lint 경고 0개
  3) headless Edge(CDP) 12개 항목 모두 통과: 통합과학2 패널에서 실험 보고서 "수정" → 폼 "평가 수정: 실험 보고서"(득점 50) → 100으로 저장 → 패널 "현재 성취율 55% 수강 중", 목록 "100 / 100", 폼 "평가 추가"로, 표 "통합과학2 공통 4 55% 수강 중", 배너 "이수 위험 과목 0개 · 주의 1개". 페이지 안에서 화면이 쓰는 api 모듈을 불러 공통영어2 경로로 통합과학2 평가 수정 → "404 평가를 찾을 수 없습니다.", 값은 100점 그대로(방금 화면에서 저장한 100점이 보여 같은 mock 저장소임을 확인). 새로고침 → 35% 이수 위험
  4) 같은 서버에서 이전 확인 스크립트 세 개도 다시 돌려 모두 통과: 점수 입력·평가 삭제 45개, 과목 등록 39개, 과목 상태 변경·삭제 32개
  확인 후 개발 서버 종료(5173 포트 비어 있음). 확인 못 함: 실제 API
- 결정과 이유: 백엔드 구현 전에 평가 API 경로를 모두 /courses/{courseId}/assessments 아래로 통일(사용자 결정). 수정도 삭제처럼 과목과 평가가 맞는지 확인함
- 막힌 점과 해결: 없음
- 다음 할 일: API 명세 문서(docs/api-contract.md), DB 테이블 V1과 시연용 데이터 V2, 백엔드 API

### 2026-10-28 — 과목 상태 변경과 삭제(mock) 추가
- 한 일:
  1) mock 저장소에 updateCourseStatus, removeCourse(그 과목의 평가도 함께 삭제) 추가. mock API PATCH /student/credit/courses/{courseId}(본문 { status }, 성공 시 바뀐 과목 하나, 상태 값이 틀리면 400 { message, fieldErrors: { status } }), DELETE /student/credit/courses/{courseId}(성공 시 본문 없음 204) 추가. 없는 과목은 둘 다 404. 클라이언트에 PATCH 메서드 추가, api.ts에 changeCourseStatus, deleteCourse 추가
  2) 과목 행을 누르면 열리는 패널의 과목 이름 아래에 "이수 완료로 변경" / "수강 중으로 되돌리기" 버튼과 "과목 삭제" 버튼. 과목 삭제는 2단계("이 과목을 삭제할까요? 입력한 평가도 함께 삭제됩니다. [삭제] [취소]"). 상태를 바꾸면 패널은 열린 채로 패널과 학점 현황 전체(요약, 과목 표, 남은 필수 과목, 경고 배너)를 다시 불러오고, 삭제하면 패널을 닫고 학점 현황을 다시 불러옴. 패널 정보 줄에 이수 상태 표시
  3) 창체 행도 클릭(또는 Enter)하면 패널이 열림. 창체 패널은 성취율·평가 목록·평가 추가 폼을 숨기고 상태 변경과 삭제만 보임(정보 줄도 "창체 · 3학점 · 수강 중", 비율 없음)
  4) 이수 완료로 바꾼 과목의 패널에는 필요 평균 안내와 면책 문구를 보이지 않게 함(배지가 "이수 완료"라서). 과목 표 아래 안내 문구를 "과목을 누르면 평가 점수 입력, 이수 상태 변경, 과목 삭제를 할 수 있습니다."로 바꿈
  5) PRD: 5장에 F2-1(과목 이수 상태 변경과 삭제) 추가, F1에 경고 배너는 수강 중 과목만 센다는 것, F2에 지난 작업에서 임의로 정했던 것(목록 과목은 유형만 잠그고 학점 조정 가능, 창체 처리, 미래 학기 이수 완료 허용, 지필 비율 10% 단위와 직접 입력, 직접 입력 유형 4개, 기본값, 이름 기준 중복 판정) 추가, 8장 API 표에 PATCH·DELETE /courses/{courseId}, GET /catalog 설명에 최대 10개·빈 검색어는 빈 배열, 9장에 시연용 표준 과목 목록 30개의 성격. PROGRESS "미정"의 "과목 등록 모달에서 임의로 정한 것" 항목 전체를 삭제(모두 PRD로 옮김). 이번 작업의 임의 결정도 PRD에만 적고 "미정"에는 추가하지 않음
- 바뀐 파일: frontend/src/mocks/(creditStore.ts, credit.ts, courseStatus.test.ts 신규), frontend/src/api/client.ts, frontend/src/modules/credit/(api.ts, CreditPage.tsx, components/AssessmentDrawer.tsx, AssessmentDrawer.module.css, CourseTable.tsx), docs/PRD.md(5장 F1·F2·F2-1, 8장, 9장), docs/PROGRESS.md
- 확인 방법과 결과:
  1) npm test → 5개 파일, 62개 모두 통과(새로 9개): 통합과학2 이수 완료로 변경 시 이수 완료 32 → 36, 수강 중 32 → 28, 남은 128, 교과 29 → 33, 필수 과목 7 → 6, 배너 위험 0 · 주의 1 / 되돌리면 처음 숫자 / 창체 상태 변경 시 창체 3 → 6 / 잘못된 상태 값 400 / 등록한 과목 삭제 시 학점·필수 과목이 등록 전으로 / 과목 삭제 시 그 과목 평가도 삭제(PUT이 404) / 이수 완료 과목 삭제 시 이수 완료·교과 학점 감소 / 없는 과목 PATCH·DELETE 404(이미 지운 과목 포함)
  2) npm run build 통과, npm run lint 경고 0개
  3) npm run dev(mock) 후 headless Edge(CDP) 32개 항목 모두 통과:
     - 통합과학2 행 → 패널의 과목 이름 아래 "이수 완료로 변경", "과목 삭제", 정보 줄 "공통 · 4학점 · 수강 중"
     - 이수 완료로 변경 → 이수 완료 36, 수강 중 28, 남은 128, 교과 33 / 174, 남은 필수 6과목(통합과학2 빠짐), 표 "통합과학2 공통 4 35% 이수 완료", 배너 "이수 위험 과목 0개 · 주의 1개", 버튼 "수강 중으로 되돌리기", 패널에 필요 평균 안내 없음
     - 되돌리기 → 이수 완료 32, 수강 중 32, 남은 128, 교과 29, 필수 7과목(통합과학2 이수 위험), 표 35% 이수 위험, 배너 위험 1 · 주의 1
     - 과목 삭제 → "이 과목을 삭제할까요?"와 [삭제] [취소] → 취소 → 패널과 버튼 원래대로, 표에 통합과학2 그대로(10행)
     - 신규 과목 등록으로 확률과 통계 등록(11행, 수강 중 36, 남은 124) → 그 행의 패널에서 과목 삭제 → 패널 닫힘, 10행, 학기 합계 32학점, 수강 중 32, 남은 128
     - 창의적 체험활동 행 → 패널 열림, 상태 변경·과목 삭제 버튼만 있고 성취율·평가 목록·평가 추가 폼 없음, 정보 줄 "창체 · 3학점 · 수강 중". 이수 완료로 변경 → 창체 6 / 18, 이수 완료 35
     - 새로고침 → 처음 숫자, 필수 7과목, 배너 위험 1 · 주의 1, 10행
     같은 서버에서 지난 확인 스크립트(점수 입력·평가 삭제 45개, 과목 등록 39개)도 다시 돌려 모두 통과
  스크린샷: 패널(수강 중), 이수 완료로 바꾼 뒤, 과목 삭제 확인, 과목 삭제 후, 창체 패널을 찍어 눈으로 봄(로컬 임시 폴더, 커밋 안 함). 확인 후 개발 서버 종료(5173 포트 비어 있음)
  확인 못 함: 상태 변경·삭제가 실패했을 때 오류 문구(mock에서는 실패하지 않음), 다른 학기(1학년 1학기 이수 완료 과목)를 수강 중으로 되돌리는 화면 동작(단위 테스트의 상태 변경으로만 확인), 좁은 화면(모바일) 배치, 실제 API
- 결정과 이유:
  - DELETE /courses/{courseId}의 성공 응답은 기존 평가 삭제(DELETE …/assessments/{id})와 같은 방식으로 맞춤: mock 핸들러가 null을 돌려주고 PRD에는 "204, 본문 없음"으로 적음. 클라이언트는 204면 본문을 읽지 않음(평가 삭제 때 만든 처리 그대로)
  - 상태 변경은 되돌릴 수 있어 확인 단계 없이 바로 적용, 확인 단계는 삭제에만 둠
  - 이수 완료 과목 패널에 필요 평균 안내를 계속 보여주면 배지("이수 완료")와 어긋나 보여서, 위험·주의 표시와 필요 평균 안내는 수강 중 과목에만 보이게 함(계산 결과 riskLevel은 그대로)
  - 위 결정은 모두 PRD(F1, F2, F2-1, 8장)에 적고 "미정"에는 추가하지 않음
- 막힌 점과 해결: 패널 코드를 고치는 긴 셸 명령이 중간에 끊겨(heredoc 따옴표 오류) 아무것도 바뀌지 않음 → git status로 확인한 뒤 파일 편집 도구로 하나씩 고침
- 다음 할 일: API 계약 문서(docs/api-contract.md), DB 테이블 V1과 시연용 데이터 V2, 백엔드 API와 계산 서비스

### 2026-10-28 — 신규 과목 등록 모달(mock) 추가
- 한 일:
  1) 시연용 표준 과목 목록 30개(공통 14, 일반선택 10, 진로선택 3, 융합선택 2, 창체 1. 현재 mock 과목 모두 포함)를 frontend/src/mocks/catalog.ts에 만들고 mock API GET /student/credit/catalog?query=(과목명 부분 일치, 공백 무시, 최대 10개) 추가. 시연용이라 공식 목록과 다를 수 있다는 주석을 남김
  2) 과목 등록 규칙을 순수 함수로 frontend/src/modules/credit/domain/courseRegistration.ts에 만듦: setWrittenWeight(지필 0~100 정수, 수행 = 100 − 지필), validateCourseDraft(과목명 필수, 같은 학기 같은 과목명 중복 불가, 학점 1 이상 정수, 창체가 아니면 비율 합 100). 모달과 mock 저장 함수가 같은 함수를 씀
  3) mock API POST /student/credit/courses: 목록 과목(catalogId)과 직접 입력(customName, courseType) 모두 받음. 검증 실패 시 기존과 같은 400 { message, fieldErrors }. 메모리 저장소에 과목 추가(insertCourse), 저장소 과목에 catalogId와 gradeDisplay 필드 추가
  4) 학점 현황 제목 줄 오른쪽에 "+ 신규 과목 등록" 버튼, 목업 구성의 모달(학기 칩 6개, 과목명 검색과 후보 목록, 자동 입력 안내 "표준 과목 정보로 교과(군)·유형·학점을 자동 입력했어요 · 교과(군) ○○", "목록에 없는 과목 직접 입력" 전환, 과목 유형 칩, 학점 −/+, 이수 상태 칩, 성적 산출 방식(지필 −/+와 숫자 칸, 수행 비율, 비율 막대), 성적 표기 방식 칩 3개). 필드 옆 오류 문구와 하단 "입력값을 확인하세요. 등록되지 않았습니다." 등록하면 모달이 닫히고 등록한 학기 탭(?term=)으로 이동, 요약·필수 과목·경고 배너·과목 표를 다시 불러옴. 평가가 없는 새 과목은 "미입력"으로 보임. Escape, 바깥 클릭, 취소, ×로 닫힘
  5) PRD: F2 성적 표기 방식 문구를 화면과 같은 세 선택지("성취도(A~E)만", "성취도 + 5등급 함께 표기", "A·B·C 3단계")로 맞춤, 7장 grade_display 값과 화면 문구 대응 추가, 8장에 GET /catalog 응답 예시, POST /courses 요청 예시(목록 과목에 credits 추가, 직접 입력 예시 새로 추가), 검증 규칙과 성공·실패 응답 설명 추가
- 바뀐 파일: frontend/src/mocks/(catalog.ts·courseRegistration.test.ts 신규, credit.ts, creditStore.ts), frontend/src/modules/credit/domain/(courseRegistration.ts·courseRegistration.test.ts 신규, types.ts: CourseStatus·GradeDisplay를 domain으로 옮김), frontend/src/modules/credit/(types.ts, api.ts, labels.ts, CreditPage.tsx, CreditPage.module.css, components/CourseRegisterModal.tsx·CourseRegisterModal.module.css 신규), docs/PRD.md(5장 F2, 7장, 8장), docs/PROGRESS.md
- 확인 방법과 결과:
  1) npm test → 4개 파일, 53개 모두 통과(새로 24개). 비율 연동(70→30, 0→100, 범위 밖 값 보정, 합계 항상 100), 검증(과목명, 중복(공백·대소문자 무시), 학점, 비율 합, 창체, 학기), mock 등록 후 요약(수강 중 36·남은 124 등), 이수 완료 등록 시 이수 완료·교과·창체 학점, 공통+수강 중은 필수 과목에 포함 / 공통+이수 완료는 제외, 직접 입력 등록, 중복 400과 실패 시 저장 안 됨, 다른 학기에는 같은 과목 등록 가능, 과목 검색
  2) npm run build 통과, npm run lint 경고 0개
  3) npm run dev(mock) 후 headless Edge(CDP) 39개 항목 모두 통과:
     - "+ 신규 과목 등록" 버튼 → 모달 열림, 학기 기본값 1학년 2학기, 표기 방식 3개 문구("성취도(A~E)만 | 성취도 + 5등급 함께 표기 | A·B·C 3단계")
     - "공통국어" 입력 → 후보 공통국어1, 공통국어2. 공통국어2 선택 → 안내 문구와 "교과(군) 국어", 유형 "공통" 선택·잠김, 학점 4, + 누르면 5(목록 과목도 학점 조정 가능)
     - 지필 + → 70 / 수행 30, 지필 칸에 0 → 수행 100, 0에서 − → 0 유지
     - 1학년 2학기에 이미 있는 공통국어2 등록 → 과목명 아래 "같은 학기에 이미 등록된 과목입니다.", 모달 유지
     - "확률" 검색 → 확률과 통계(일반선택) 선택, 수강 중으로 등록 → 모달 닫힘, 표에 "확률과 통계 일반선택 4 미입력 미입력" 행 추가(10 → 11행), 수강 중 32 → 36, 남은 학점 128 → 124, 학기 합계 36학점, 주소 ?term=1-2
     - 확률과 통계를 같은 학기에 다시 등록 → 같은 오류 문구, 모달 유지, 행 수 그대로
     - 2학년 1학기 선택 → 직접 입력 모드 → "AI 기초", 진로선택, 학점 2, 지필 0(수행 100), A·B·C 3단계로 등록 → 모달 닫힘, 2학년 1학기 탭으로 이동(?term=2-1), 표에 "AI 기초 진로선택 2 미입력 미입력", 수강 중 38, 남은 122
     - 새로고침 → 2학년 1학기 "등록된 과목이 없습니다", 수강 중 32, 남은 128, 1학년 2학기 10과목(확률과 통계 없음)
     같은 서버에서 지난번 점수 입력·삭제 확인 스크립트(45개)도 다시 돌려 모두 통과
  스크린샷: 검색 후보, 자동 입력, 중복 오류, 등록 후 표, 직접 입력 화면을 찍어 눈으로 봄(로컬 임시 폴더, 커밋 안 함). 확인 후 개발 서버 종료(5173 포트 비어 있음)
  확인 못 함: 화면에서 공통과목을 등록했을 때 남은 필수 과목 목록이 바뀌는 모습(단위 테스트로만 확인), 화면에서 이수 완료로 등록했을 때 이수 완료·교과·창체 학점이 바뀌는 모습(단위 테스트로만 확인), 화면에서 창체를 골랐을 때 성적 산출 영역이 숨는 모습, 키보드만으로 후보를 고르는 동작(방향키 이동은 만들지 않았고 Tab으로만 이동), 좁은 화면(모바일) 배치, 실제 API
- 결정과 이유: "미정"의 "과목 등록 모달에서 임의로 정한 것" 항목에 모아 적음(창체 처리, 지필 10% 단위와 직접 입력, 목록 과목은 유형만 잠그고 학점은 조정 가능, 미래 학기 이수 완료 허용, 이름 기준 중복 판정, 기본값, catalog 응답 모양, 시연용 목록). 그 밖에:
  - 같은 학기 중복은 모달에서 미리 막지 않고 저장할 때 mock(서버 역할)이 확인해 400으로 돌려주고, 모달은 그 fieldErrors를 필드 옆에 보여줌(모달은 다른 학기의 과목 목록을 갖고 있지 않아서)
  - 검색은 과목을 고르기 전, 검색어가 있을 때만 요청. 고른 뒤 입력칸을 고치면 선택이 풀리고 다시 검색
  - CourseStatus, GradeDisplay 타입을 domain/types.ts로 옮기고 API 타입(types.ts)에서 다시 내보냄(domain이 API 타입에 의존하지 않게)
- 막힌 점과 해결: 브라우저 확인 스크립트를 셸 heredoc 한 번에 쓰다가 명령이 길어 파일이 중간에 잘림 → 본문을 별도 파일로 써서 합침. 화면 문제는 없었고 39개가 첫 실행에 통과
- 다음 할 일: DB 테이블 V1과 시연용 데이터 V2, 백엔드 API와 계산 서비스

### 2026-10-28 — 평가 삭제 추가
- 한 일: 점수 입력 패널의 평가 목록 각 행에 "수정" 옆 "삭제" 추가. 브라우저 confirm 창 대신 2단계 방식: "삭제"를 누르면 그 행이 "(평가 이름) 정말 삭제할까요? [삭제] [취소]"로 바뀜. 삭제하면 패널(목록, 성취율, 위험 표시, 필요 평균 안내)과 학점 현황(요약, 경고 배너, 필수 과목 배지, 과목 표)을 다시 불러와 다시 계산된 값으로 바뀜. 수정 중인 평가를 지우면 폼이 "평가 추가"로 돌아감. mock API DELETE /student/credit/courses/{courseId}/assessments/{assessmentId}(성공 시 본문 없음, 그 과목의 평가가 아니면 404) 추가, PRD 8장 API 표에 추가
- 바뀐 파일: frontend/src/mocks/creditStore.ts(removeAssessment), frontend/src/mocks/credit.ts(DELETE 핸들러), frontend/src/mocks/credit.test.ts(삭제 테스트 3개), frontend/src/api/client.ts(DELETE 메서드, 204 처리), frontend/src/modules/credit/api.ts(deleteAssessment), frontend/src/modules/credit/components/AssessmentDrawer.tsx, AssessmentDrawer.module.css, docs/PRD.md(8장), docs/PROGRESS.md
- 확인 방법과 결과:
  1) npm test → 29개 모두 통과. 새 테스트: 통합과학2에서 수행평가 2를 삭제하면 48% → 35% 이수 위험, 필요 평균 45, 배너 위험 1 · 주의 1로 복귀 / 다른 과목의 평가 ID로 삭제하면 404 / 공통영어2의 평가 2건을 모두 삭제하면 성취율 null, NO_DATA
  2) npm run build 통과, npm run lint 경고 0개
  3) npm run dev(mock) 후 headless Edge(CDP) 확인 45개 모두 통과(기존 32개 + 삭제 13개):
     - "삭제" 클릭 → 행이 "수행평가 2 정말 삭제할까요?"와 [삭제] [취소]로 바뀜. [취소] → 원래 행, 평가 3건 그대로
     - "수정" 클릭으로 폼이 "평가 수정: 수행평가 2"가 된 상태에서 삭제 → 평가 2건, 패널 "현재 성취율 35% 이수 위험", "남은 평가에서 평균 45점 이상 받아야 이수 기준(40%)을 넘어요", 폼 제목 "평가 추가", 표 "통합과학2 공통 4 35% 이수 위험", 배너 "이수 위험 과목 1개 · 주의 1개"와 45점 안내 (추가 전 값으로 복귀)
     - 공통영어2의 평가 2건을 모두 삭제 → 패널 "등록된 평가가 없습니다", 성취율 "미입력", 표 "공통영어2 공통 4 미입력 미입력"(성취율, 배지)
     - 새로고침 → 통합과학2 35% 이수 위험, 배너 1 · 1, 공통영어2 72%로 처음 상태
  스크린샷을 찍어 확인 행과 미입력 화면을 눈으로 봄(로컬 임시 폴더, 커밋 안 함). 확인 후 개발 서버 종료(5173 포트 비어 있음)
  확인 못 함: 삭제가 실패했을 때 오류 문구가 나오는 모습(mock은 존재하는 평가 삭제에 실패하지 않음), 좁은 화면 배치, 실제 API
- 결정과 이유:
  - 확인 행은 행 전체(6칸)를 한 칸으로 합쳐 문구와 버튼을 보여줌. 다른 행의 수정/삭제는 그대로 둠
  - 삭제 API는 PRD 8장 PUT(/assessments/{id})과 달리 요청대로 과목 경로 아래(/courses/{courseId}/assessments/{assessmentId})에 둠. 과목이 맞지 않으면 404
  - 실제 API 모드에서 204 응답은 본문을 읽지 않고 undefined로 돌려주게 클라이언트를 고침
- 막힌 점과 해결: 없음
- 다음 할 일: DB 테이블 V1과 시연용 데이터 V2, 백엔드 API와 계산 서비스, 과목 등록 모달

### 2026-10-28 — 미정 항목 정리
- 한 일: 확정된 4개를 PRD에 반영하고 PROGRESS "미정"에서 지움(나머지 미정 항목은 그대로)
  - 큰 틀 이름 "학생부 관리" 확정: PRD 3장의 "정식 이름은 확정되지 않았다" 문구와 12장 미정 항목 제거
  - "주의" 기준 확정(공통과목 성취율 40% 이상 50% 미만, 40% 미만은 이수 위험): PRD F4의 "임시/미정" 표기 제거, 6장에 위험 단계 표 추가, 9장 시연 데이터 표 아래에 상태 기준 한 줄 추가, 12장 미정 항목 제거
  - 판정 순서: PRD F4에 순서 7단계를 적음(출석률 미달 → 이수 위험, 창체 → 정상, 평가 없음 → 미입력, 선택과목 → 정상, 40% 미만 → 이수 위험, 50% 미만 → 주의, 그 밖 → 정상). 코드(domain/completion.ts의 riskLevel)와 같은 순서
  - PRD 9장에 1학년 1학기 이수 완료 과목 표 추가(공통 7과목 24학점 + 체육 2 + 미술 3 + 창체 3)
- 바뀐 파일: docs/PRD.md(3장, 5장 F4, 6장, 9장, 12장), frontend/src/modules/credit/domain/rules.ts(주석의 "임시 값(미정)" 삭제), docs/PROGRESS.md
- 확인 방법과 결과: 코드에서 40, 50을 검색함(grep). 판정에 쓰는 40과 50은 frontend/src/modules/credit/domain/rules.ts의 COMPLETION_RULES에만 있고, 화면 문구의 "이수 기준(40%)"도 이 값을 읽어 씀(labels.ts). 테스트 파일과 mock 시연 데이터의 점수 외에는 없어서 코드를 옮길 것은 없었음. 주석만 바뀌어 다시 빌드하지는 않음(아래 평가 삭제 작업에서 npm test·build·lint를 다시 돌림)
- 결정과 이유: PRD 9장의 1학년 1학기 과목 표는 mock(frontend/src/mocks/creditStore.ts)에 들어 있는 과목 그대로 옮김
- 막힌 점과 해결: 없음
- 다음 할 일: 평가 삭제 기능, DB 테이블 V1과 시연용 데이터 V2

### 2026-10-28 — 이수 위험 계산과 점수 입력(mock) 추가
- 한 일:
  1) 계산 모듈: frontend/src/modules/credit/domain/에 화면·API와 무관한 순수 함수로 성취율, 출석률, 위험 단계(NORMAL/CAUTION/RISK/NO_DATA), 필요 평균을 PRD 6장대로 구현. 이수 기준(최소 출석률 66.67%, 공통 성취율 40%)과 "주의" 상한(임시 50%), 졸업 학점(192/174/18)은 domain/rules.ts 설정 객체 한 곳에만 둠. 평가 입력 검증(득점 0~만점, 종류별 반영 비율 합 ≤ 그 종류의 반영 비율)도 domain/validation.ts에 두고 폼과 mock이 같이 씀
  2) Vitest 5.0.5를 개발 의존성으로 추가하고 `npm test` 스크립트 추가. PRD 6장 테스트 케이스 7개를 그대로 옮기고, 추가 규칙·검증·mock 데이터 테스트를 더함(총 26개)
  3) mock을 평가 데이터 기반으로 바꿈: src/mocks/creditStore.ts 메모리 저장소(과목 + 평가 원본만 있음)에서 summary, courses, required-courses 응답을 계산 모듈로 만듦. 성취율 숫자는 하드코딩하지 않음. 1학년 1학기 이수 완료 10과목(교과 29 + 창체 3) 추가. 평가 추가(POST)·수정(PUT) mock과 점수 입력 패널용 과목 상세(GET /courses/{courseId}) mock 추가. mock 클라이언트가 메서드와 경로 변수(:courseId)를 다루도록 바꿈
  4) 점수 입력 화면: 과목 표의 행(창체 제외)을 누르면 오른쪽 패널이 열림. 현재 성취율과 상태, 위험 과목이면 필요 평균 안내, 면책 문구, 평가 목록(미입력 표시, 수정 버튼), 평가 추가/수정 폼(종류, 이름, 반영 비율, 만점, 득점, 날짜). 저장하면 요약·경고 배너·필수 과목 배지·과목 표·패널이 다시 계산되어 바뀜. Escape, 바깥 클릭, 닫기 버튼으로 닫힘
  5) 성취율은 정수로 반올림해 표시하고 판정은 반올림 전 값으로 함. 평가가 없는(또는 모두 미입력인) 과목은 성취율 자리에 "미입력"
  6) PRD 8장에 GET /required-courses 응답 예시 추가({ remainingCount, courses })
- 바뀐 파일: frontend/package.json, frontend/package-lock.json(vitest), frontend/src/modules/credit/domain/(rules.ts, types.ts, completion.ts, validation.ts, completion.test.ts 신규), frontend/src/mocks/(creditStore.ts·credit.test.ts 신규, credit.ts, index.ts), frontend/src/api/(client.ts, errors.ts 신규, useAsync.ts), frontend/src/modules/credit/(types.ts, api.ts, labels.ts, CreditPage.tsx, CreditPage.module.css, components/AssessmentDrawer.tsx·AssessmentDrawer.module.css·AssessmentForm.tsx 신규, components/CourseTable.tsx, CourseTable.module.css, RiskAlertBanner.tsx), frontend/src/styles/tokens.css(--color-overlay 추가), docs/PRD.md(8장), README.md, docs/PROGRESS.md
- 확인 방법과 결과:
  1) frontend에서 npm test → 2개 파일, 26개 테스트 모두 통과. PRD 6장 케이스 1~7 포함(1: 30% 이수 위험, 2: 45% 주의, 3: 미입력, 4: NULL 제외, 5: 필요 평균 45, 6: 선택과목 출석률 60% 이수 위험, 7: 수행 100% 정상 계산). mock 데이터 테스트에서 요약 숫자, 1학년 2학기 9과목 성취율·상태, 1학년 1학기 합계, 통합과학2 평가 추가 후 48% 주의 확인
  2) npm run build → 통과(타입 오류 없음). npm run lint(oxlint) → 경고 0개. 출력이 비어 있어서 일부러 debugger 문을 넣은 임시 파일로 린트가 실제로 잡는 것을 확인한 뒤 파일을 지움
  3) npm run dev(mock) 후 Node 스크립트로 headless Edge를 CDP로 조종해 32개 항목 확인, 모두 통과:
     - 기존 숫자: 32 / 192학점 이수, 교과 29 / 174학점, 창의적 체험활동 3 / 18학점, 남은 학점 128학점, 표 "통합과학2 공통 4 35% 이수 위험", "공통수학2 공통 4 46% 주의", 배너 "이수 위험 과목 1개 · 주의 1개"와 "남은 평가에서 평균 45점 이상 받아야 이수 기준(40%)을 넘어요"
     - 1학년 1학기 탭: 10과목 모두 "이수 완료", 학기 합계 32학점
     - 통합과학2 행 클릭 → 패널 열림, "현재 성취율 35% 이수 위험", 필요 평균 45점 안내, 면책 문구, 평가 2건
     - 수행, 수행평가 2, 반영 20, 만점 100, 득점 120 입력 후 저장 → "득점은 만점(100점) 이하여야 합니다."와 "입력값을 확인하세요. 저장되지 않았습니다." 표시, 평가 2건 그대로, 패널과 표 모두 35% 이수 위험 그대로
     - 득점을 80으로 바꿔 저장 → 패널 "현재 성취율 48% 주의", 평가 3건, 표 "통합과학2 공통 4 48% 주의", 배너 "이수 위험 과목 0개 · 주의 2개", 필수 과목 배지 "통합과학2 주의", 폼 칸 비워짐
     - Escape로 패널 닫힘
     - 새로고침 → 통합과학2 35% 이수 위험, 배너 "이수 위험 과목 1개 · 주의 1개"로 처음 상태 복귀
  스크린샷: 1학년 1학기 표, 패널 열림, 득점 오류, 저장 후 화면을 찍어 눈으로 봄(로컬 임시 폴더에만 있고 커밋하지 않음). 전체 페이지를 찍는 방식이라 어두운 덮개가 창 높이까지만 덮인 것처럼 찍히는데, 실제 창에서는 덮개가 화면 전체에 고정됨
  확인 후 개발 서버 종료(5173 포트 비어 있음 확인)
  확인 못 함: "미입력" 과목이 화면 표에 나오는 모습(시연 데이터에 평가가 하나도 없는 과목이 없음. 판정은 단위 테스트로만 확인), 필요 평균이 100을 넘을 때의 "남은 평가로는 기준 도달이 어렵습니다" 문구가 화면에 나오는 모습(문구 함수만 있고 시연 데이터로는 안 나옴), 반영 비율 초과 오류가 화면에 나오는 모습(검증 함수는 단위 테스트로 확인), 평가 수정(PUT)을 화면에서 저장하는 동작, 좁은 화면(모바일) 배치, 실제 API(백엔드 없음)
- 결정과 이유:
  - 1학년 2학기 평가 구성: 9과목 모두 PRD 9장 성취율이 정확히 나오도록 점수를 정함. 공통수학2와 통합과학2는 지필 60/수행 40이고 입력된 반영 비율 합 50%(지필 중간 30 + 수행 20). 통합과학2는 수행을 20%만 써서 수행 20%를 더 넣을 수 있음. 공통국어2·공통수학2에는 득점이 비어 있는 기말고사(30%)를 넣어 미입력 평가 표시를 볼 수 있게 함. 과학탐구실험2, 운동과 건강, 음악은 수행 100%
  - 1학년 1학기: 공통 7과목(공통국어1, 공통수학1, 공통영어1 각 4, 한국사1 3, 통합사회1, 통합과학1 각 4, 과학탐구실험1 1 = 24) + 일반선택 2과목(체육 2, 미술 3) + 창체 3. 이름과 점수는 시연용 임의 값
  - 과목 ID: 1학년 1학기 1~10, 2학기 11~20 (PRD 8장 required-courses 예시의 ID도 이에 맞춤)
  - 위험 단계 판정 순서: 출석률 미달 → 이수 위험(모든 유형), 창체 → 정상(평가 없음), 성취율 없음 → 미입력, 공통과목만 성취율로 위험/주의 판정. 출석률을 먼저 보는 것은 PRD F4와 6장 테스트 6 사이의 해석이라 "미정"에 적음
  - 필요 평균은 성취율 미달로 이수 위험인 과목에만 계산해 응답에 넣음(PRD 8장 예시처럼). 화면에서는 "이상 받아야"라서 소수점은 올림해 정수로 표시
  - 경고 배너 개수는 수강 중인 과목만 셈(이수 완료 과목은 이미 이수했으므로)
  - 이수 완료 과목도 행을 누르면 패널이 열리고 점수를 고칠 수 있음(막을 근거가 PRD에 없음). 창체 행은 평가가 없어 열리지 않음
  - 점수 입력 패널은 모달 대신 오른쪽 드로어로 함. 과목 표를 가리지 않고 저장 결과(배너, 표)를 옆에서 바로 볼 수 있어서
  - 검증은 폼에서 먼저 하고 mock 저장 함수에서도 같은 함수로 다시 함(서버 검증 흉내). 실패하면 400 오류로 항목별 문구를 돌려줌
  - 저장 후 다시 불러올 때 화면이 "불러오는 중"으로 깜빡이지 않게 useAsync가 다시 불러오는 동안 이전 데이터를 보여주도록 바꿈(학기를 바꿀 때는 기존처럼 로딩 표시)
  - mock 응답은 structuredClone으로 복사해서 넘김(화면이 저장소 객체를 직접 잡지 않게)
  - ApiError를 src/api/errors.ts로 분리(mocks와 api/client 사이 순환 import를 피하려고)
  - 판정용 상수 COMMON_MIN_ACHIEVEMENT(labels.ts)는 지우고 domain/rules.ts의 COMPLETION_RULES를 쓰게 함
- 막힌 점과 해결: 브라우저 확인 첫 실행에서 3개 실패 → 화면 값은 맞았고(출력에 "48% 주의", "통합과학2 주의"가 찍힘), 확인 스크립트를 셸 heredoc으로 만들면서 정규식의 역슬래시가 빠져 공백 정규화가 안 된 것이 원인. 스크립트를 고쳐 새 페이지에서 처음부터 다시 돌려 32개 모두 통과
- 다음 할 일: DB 테이블 V1과 시연용 데이터 V2(mock 시연 데이터를 그대로 옮김), 백엔드 API와 계산 서비스(domain/ 규칙과 같게), 과목 등록 모달

### 2026-10-28 — 학점 현황 화면(mock) 추가
- 한 일: frontend/src/modules/credit/에 학점 현황 화면을 만듦. 왼쪽은 졸업 학점 진행바(이수 완료/수강 중/남은 학점), 교과·창체 학점, 남은 필수 이수 과목 체크리스트(배지: 수강 중/주의/이수 위험)이고, 오른쪽은 경고 배너, 학기 탭(1-1~3-2), 선택 학기 과목 표(과목명, 유형, 학점, 현재 성취율, 상태)와 학기 합계. "주간 학습" 열은 만들지 않음. 위험 표시가 있는 세 곳(배너, 필수 과목, 과목 표) 아래에 "참고용 예측이며 학교의 공식 판정이 아닙니다" 표시. 로딩/빈 상태/오류(다시 시도 버튼) 상태. PRD 9장 시연 데이터로 mock 작성. API 함수 getSummary, getCourses(gradeYear, term), getRequiredCourses(mock 또는 /api/student/credit/...)
- 바뀐 파일: frontend/src/api/client.ts(query 파라미터, mock 지연 300ms), frontend/src/api/useAsync.ts(신규), frontend/src/mocks/index.ts, frontend/src/mocks/credit.ts(신규), frontend/src/modules/credit/(CreditPage.tsx, CreditPage.module.css, types.ts, api.ts, labels.ts, components/ 아래 StatusBadge, Disclaimer, StateMessage, SummaryCard, RequiredCourseList, RiskAlertBanner, SemesterTabs, CourseTable와 각 .module.css), frontend/src/styles/tokens.css(--color-risk-border, --color-caution-border 추가), docs/PROGRESS.md
- 확인 방법과 결과:
  1) frontend에서 npm run build → 통과(타입 오류 없음). npx oxlint → 경고 0개(처음에는 useAsync에서 경고 3개가 나와 구조를 고침)
  2) npm run dev 후 Node 스크립트로 headless Edge를 CDP로 조종해 /credit을 열고 화면 텍스트를 확인함. 18개 항목 모두 통과: 로딩 문구 표시, "32 / 192학점 이수", "교과 29 / 174학점", "창의적 체험활동 3 / 18학점", 이수 완료 32·수강 중 32·남은 학점 128, 표의 "통합과학2 공통 4 35% 이수 위험", "공통수학2 공통 4 46% 주의", "남은 필수 이수 과목 7과목"(목록 7개), 필수 과목별 배지, 배너 "이수 위험 과목 1개 · 주의 1개", 면책 문구 3곳, "주간 학습" 없음, "학기 합계 32학점"
  3) 같은 스크립트로 "2학년 1학기" 탭을 클릭 → "등록된 과목이 없습니다" 표시, 표 사라짐, 주소가 /credit?term=2-1로 바뀜. 새로고침 후에도 2학년 1학기 선택 유지. 1학년 2학기를 다시 누르면 표가 다시 나옴
  4) 오류 상태: VITE_USE_MOCK=false로 개발 서버를 5174 포트에 따로 띄우고 백엔드는 끈 상태에서 /credit을 엶 → 세 영역 모두 "데이터를 불러오지 못했습니다." + "다시 시도" 버튼. vite 로그에서 /api/student/credit/summary, /courses?gradeYear=1&term=2, /required-courses 세 경로로 프록시 요청이 나간 것 확인(백엔드 API가 없어 실제 응답은 확인 못 함)
  스크린샷: headless Edge로 1학년 2학기 화면과 2학년 1학기(빈 상태) 화면을 찍어 배치를 눈으로 봄(로컬 임시 폴더에만 있고 커밋하지 않음). 목업과 시각적으로 같은지에 대한 최종 판단은 사용자가 직접 확인 예정
  확인 후 개발 서버 두 개 종료(5173, 5174 포트 비어 있음 확인). "다시 시도" 버튼을 눌러 재요청되는 동작은 확인 못 함. 화면 폭이 좁을 때(모바일) 배치는 확인 못 함
- 결정과 이유:
  - 선택한 학기를 주소 쿼리(?term=1-2)에 둠: 새로고침해도 탭이 유지되고 브라우저 확인도 쉬움. 쿼리가 없으면 데모 학생의 현재 학기(1학년 2학기)
  - required-courses 응답 모양은 PRD에 예시가 없어 { remainingCount, courses }로 임시로 정하고 "미정"에 적음
  - mock에는 1학년 2학기 과목만 넣음. 1학년 1학기 이수 완료 과목은 구성이 정해지지 않아 요약 숫자(32학점, 교과 29, 창체 3)에만 반영. 그래서 1학년 1학기 탭도 지금은 "등록된 과목이 없습니다"가 나옴
  - 배너는 목업처럼 오른쪽 패널 맨 위에 둠. 과목별 문구는 남은 필수 과목 데이터에서 만들어서 다른 학기 탭을 눌러도 유지됨. "주간 학습" 내용은 빼고 성취율과 필요 평균만 표시. 필요 평균(45점)은 PRD 8장 응답 예시의 requiredAverage 값을 mock에 넣은 것
  - 배너 문구의 "이수 기준(40%)"은 화면 표시용 상수(labels.ts의 COMMON_MIN_ACHIEVEMENT)이고, 판정은 데이터의 riskLevel을 그대로 씀
  - 창의적 체험활동은 성취율이 없어 "—"와 "수강 중"으로 표시(목업과 같음)
  - mock 응답에 300ms 지연을 줘서 로딩 상태가 실제로 보이게 함
  - 과목 등록 모달은 이번 범위가 아니라서 목업의 "신규 과목 등록" 버튼도 넣지 않음
- 막힌 점과 해결: 첫 화면 확인에서 1건 실패 → 화면 문제가 아니라 확인 스크립트가 표 칸 사이 탭 문자를 정규화하지 않아서였음. 스크립트를 고쳐 다시 돌려 18개 모두 통과
- 다음 할 일: DB 테이블 V1과 시연용 데이터 V2, 학점 현황 API 백엔드 구현, 과목 등록 모달

### 2026-10-28 — 참고 이미지를 git 추적에서 제외
- 한 일: .gitignore에 `docs/references/*`와 `!docs/references/.gitkeep` 추가, CLAUDE.md 규칙에 참고 이미지 커밋 금지 한 줄 추가, PROGRESS.md "미정"에서 참고 이미지 항목 삭제. 이미지 파일 4개는 삭제하거나 옮기지 않고 로컬 docs/references/에 그대로 둠
- 바뀐 파일: .gitignore, CLAUDE.md, docs/PROGRESS.md
- 확인 방법과 결과: git status --short에 이미지 4개가 나오지 않음(.gitignore만 수정으로 표시). git check-ignore -v로 4개 모두 .gitignore의 `docs/references/*` 규칙에 걸리는 것 확인. git ls-files docs/references → .gitkeep만 추적됨
- 결정과 이유: 참고 이미지(목업 2개, 기존 수행평가 캡처 2개)는 로컬에만 두고 커밋하지 않음. 기존 앱 캡처에 개인 이름이 보이기 때문
- 막힌 점과 해결: 없음
- 다음 할 일: DB 테이블 V1과 시연용 데이터 V2, 학점 현황 화면(mock).

### 2026-10-28 — 프론트 뼈대와 큰 틀 레이아웃 추가
- 한 일: create-vite(react-ts)로 frontend/ 생성(npm, workspaces 없음), react-router-dom 추가. 상단 아이콘 내비게이션 + 본문 레이아웃, 모듈 라우트 /credit, /tasks, /records, /grades (/와 없는 주소는 /credit으로 이동). /credit은 "학점 현황" 자리만, 나머지 셋은 "준비 중" 페이지. 디자인 값은 src/styles/tokens.css, 큰 틀 이름은 src/config/app.ts의 APP_NAME 하나. VITE_USE_MOCK을 보는 얇은 API 클라이언트(src/api/client.ts)와 빈 mock 목록(src/mocks/index.ts). 개발 서버 /api 프록시. PRD 11장 버전 기록, README 프론트 실행 방법 추가
- 바뀐 파일: frontend/ 전체(package.json, package-lock.json, vite.config.ts, index.html, tsconfig*.json, .oxlintrc.json, .gitignore, public/favicon.svg, src/main.tsx, src/router.tsx, src/vite-env.d.ts, src/config/app.ts, src/styles/tokens.css, src/styles/global.css, src/layout/*(AppLayout, PageHeader, ComingSoonPage, icons, navItems), src/modules/credit/CreditPage.*, src/modules/tasks/TasksPage.tsx, src/modules/records/RecordsPage.tsx, src/modules/grades/GradesPage.tsx, src/api/client.ts, src/mocks/index.ts), .env.example, README.md, docs/PRD.md, docs/PROGRESS.md
- 확인 방법과 결과:
  1) frontend에서 npm run build → 통과(tsc -b 타입 오류 없음, vite build 성공)
  2) npm run dev → "VITE v8.3.5 ready", curl http://localhost:5173/ → 200
  3) curl로 /credit, /tasks, /records, /grades → 모두 200 text/html. 이것만으로는 화면을 알 수 없어 headless Edge로 각 주소를 직접 열어 DOM을 확인함: 각각 h1이 학점 현황/수행평가/생기부 관리/성적 관리이고 해당 메뉴가 활성 상태. 주소를 직접 여는 것이 새로고침과 같은 상황이라 새로고침 시 라우트 유지도 이것으로 확인함(개발 서버 기준)
  4) MySQL과 백엔드(bootRun)를 켠 상태에서 curl http://localhost:5173/api/health → HTTP 200, {"status":"ok"}
  확인 후 개발 서버와 백엔드 종료(8080, 5173 포트 비어 있음 확인). 화면 스크린샷 확인은 못 함. 배포용 빌드(dist)를 서버에 올렸을 때의 새로고침은 확인 못 함. VITE_USE_MOCK=false로 실제 API를 부르는 경로는 아직 호출하는 화면이 없어 확인 못 함
- 결정과 이유:
  - 색상은 docs/references 이미지에서 픽셀 값을 뽑아 정함: 내비 #000000, 페이지 배경 #242629(기존 캡처), 패널 #1a1f25, 포인트 #4ff0f7, 진행바 #60e5f1/#2b7b82, 위험 #f38c8e(목업). 주의(노란색) #e8c25a는 목업에서 정확히 뽑지 못해 눈대중으로 정함
  - .env는 저장소 루트 하나만 쓰도록 vite.config의 envDir을 '..'로 둠(백엔드와 같은 .env를 공유해 파일이 둘로 나뉘지 않게)
  - 아이콘만 있는 내비라 각 아이콘에 title과 aria-label로 모듈 이름을 붙임. 오른쪽 사용자 자리는 로그인 전 단계라 "데모 학생"으로 고정
  - 공통 "준비 중" 페이지(src/layout/ComingSoonPage.tsx)를 만들고 세 모듈은 각자 폴더에서 이것을 씀(모듈 폴더 분리 규칙 유지)
  - 탭 제목(document.title)은 APP_NAME으로 main.tsx에서 설정함(이름을 한 곳에서만 정의하기 위해 index.html에는 넣지 않음)
  - 템플릿의 예제 파일(App.tsx, App.css, index.css, assets, icons.svg, README.md)은 삭제함
- 막힌 점과 해결: TaskStop으로 작업을 멈춘 뒤에도 java(8080)와 node(5173) 프로세스가 남음 → 포트를 점유한 프로세스를 직접 종료하고 포트가 빈 것을 확인함
- 다음 할 일: DB 테이블 V1과 시연용 데이터 V2, 학점 현황 화면(mock)

### 2026-10-27 — 백엔드 뼈대 추가
- 한 일: Spring Initializr로 backend/ 생성(Spring Boot 4.1.2, Java 21, Gradle Wrapper Kotlin DSL, 패키지 com.learnershigh, 도메인 패키지 common·credit). GET /api/health 컨트롤러와 @WebMvcTest 테스트 작성. application.yml(DB 접속은 환경 변수, ddl-auto none, Flyway 경로 ../database/migrations), docker-compose.yml(mysql:8.4, 포트 3307), .env.example, README.md 작성. PRD 11장에 설치 버전 기록
- 바뀐 파일: backend/ 전체(build.gradle.kts, settings.gradle.kts, gradlew, gradlew.bat, gradle/wrapper/*, .gitattributes, src/main/java/com/learnershigh/BackendApplication.java, common/controller/HealthController.java, credit/package-info.java, src/main/resources/application.yml, src/test/java/com/learnershigh/common/controller/HealthControllerTest.java), database/migrations/.gitkeep, docker-compose.yml, .env.example, README.md, docs/PRD.md, docs/PROGRESS.md
- 확인 방법과 결과:
  1) docker compose up -d → docker compose ps에서 (healthy), 컨테이너 안에서 SELECT VERSION() → 8.4.13, DATABASE() → learnershigh_credit
  2) MySQL 컨테이너를 멈춘 상태에서 ./gradlew clean build → BUILD SUCCESSFUL, 테스트 1개 통과(실패 0)
  3) MySQL을 다시 켜고 ./gradlew bootRun → Flyway "Database: jdbc:mysql://localhost:3307/learnershigh_credit (MySQL 8.4)", "Successfully validated 0 migrations", "Schema `learnershigh_credit` is up to date" 로그 확인. 폴더가 비어 있어 "No migrations found" 경고가 나옴(정상). 경로를 못 찾을 때 나오는 "Skipping filesystem location" 경고는 없었음. 이 실행으로 DB에 flyway_schema_history 테이블이 생성됨
  4) curl http://localhost:8080/api/health → HTTP 200, {"status":"ok"}
  확인 후 서버 종료. MySQL 컨테이너는 계속 실행 중으로 둠. 저장소 루트 등 backend 밖에서 실행하는 경우는 확인 못 함
- 결정과 이유:
  - Spring Boot 4.1.2: 작업 시점 start.spring.io의 기본(최신 안정) 버전
  - Boot 4는 테스트 스타터가 모듈별로 나뉘어 @WebMvcTest를 쓰려면 spring-boot-starter-webmvc-test가 필요해서, spring-boot-starter-test와 함께 추가함. Initializr가 넣은 data-jpa-test, flyway-test, validation-test는 지금 쓰지 않아 뺌
  - Initializr 기본 @SpringBootTest(contextLoads)는 DB가 있어야 돌아서 삭제함(DB 없이 build 통과 조건)
  - Flyway 경로는 FLYWAY_LOCATIONS 환경 변수로 바꿀 수 있게 하고 기본값을 filesystem:../database/migrations로 둠(bootRun은 backend 폴더에서 실행되므로)
  - docker-compose의 DB 사용자와 비밀번호는 .env의 DB_USERNAME, DB_PASSWORD를 그대로 써서 앱 설정과 값이 한 군데에서 맞춰지게 함. 값이 없으면 compose가 오류를 냄
  - JPA open-in-view는 false로 둠(요청에는 없었지만 컨트롤러 밖에서 지연 로딩이 일어나지 않게 하려는 기본 설정)
- 막힌 점과 해결: TaskStop으로 bootRun을 멈춘 뒤에도 java 프로세스가 남아 8080을 계속 점유함 → 프로세스를 직접 종료하고 8080이 비었는지 확인함
- 다음 할 일: 프론트 뼈대와 큰 틀 레이아웃, 그다음 DB 테이블 V1과 시연용 데이터 V2

### 2026-10-27 — PRD 추가
- 한 일: docs/PRD.md(PRD 초안)를 저장소에 추가, PROGRESS.md의 "문서 3종" 체크박스를 완료로 변경
- 바뀐 파일: docs/PRD.md, docs/PROGRESS.md
- 확인 방법과 결과: 문서만 바뀌어 실행해서 확인할 것은 없음. PRD의 제도 내용과 수치(192학점, 이수 기준 등)를 원문과 대조하는 것은 확인 못 함. push 결과는 이 항목을 쓰는 시점에는 확인 못 함
- 결정과 이유: PRD는 받은 초안 내용을 수정하지 않고 그대로 커밋함(PRD에 이 문서를 구현 기준으로 삼는다고 적혀 있어, 바뀌는 내용은 이후 PRD와 PROGRESS.md에 함께 반영하기 위해)
- 막힌 점과 해결: 없음
- 다음 할 일: 백엔드와 프론트 뼈대 생성, 큰 틀 레이아웃 (PRD 13장 화요일 목표)

### 2026-10-27 — 저장소 초기 세팅
- 한 일: GitHub 저장소(learnershigh-credit) 생성, .gitignore 작성, docs/references 폴더 생성, CLAUDE.md와 PROGRESS.md 작성 후 첫 커밋과 push
- 바뀐 파일: CLAUDE.md, docs/PROGRESS.md, .gitignore, docs/references/.gitkeep (커밋 cb2f0d2)
- 확인 방법과 결과: push 후 origin/main에 올라간 것을 확인함. GitHub 화면에서의 확인은 아직 안 함
- 결정과 이유: 큰 틀 이름은 "학생부 관리"를 임시로 사용하고 코드에서 상수 하나로만 정의하기로 함(정식 용어가 확정되지 않았기 때문)
- 막힌 점과 해결: 없음. push 때 LF→CRLF 경고가 나왔지만 내용에는 영향 없음
- 다음 할 일: PRD 추가, 백엔드와 프론트 뼈대 생성
