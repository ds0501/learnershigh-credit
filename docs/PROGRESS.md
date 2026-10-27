# 진행 현황
최종 갱신: 2026-10-28

## 완료
- [x] GitHub 저장소 생성, .gitignore, docs/references 폴더
- [x] 문서 3종 (CLAUDE.md, PROGRESS.md, PRD.md)
- [x] 백엔드 뼈대 (실행 확인: MySQL 중지 상태에서 ./gradlew clean build 통과, docker compose up 후 ./gradlew bootRun에서 Flyway "Successfully validated 0 migrations" 로그, curl http://localhost:8080/api/health → 200 {"status":"ok"})
- [x] 프론트 뼈대 (실행 확인: frontend에서 npm run build 통과(tsc 타입 검사 포함), npm run dev 후 http://localhost:5173 → 200)
- [ ] DB 테이블 V1 / 초기 데이터 V2
- [x] 큰 틀 레이아웃 (실행 확인: /credit, /tasks, /records, /grades 각각 200, headless Edge로 각 주소를 직접 열어 제목과 활성 메뉴가 맞게 그려지는 것 확인, 백엔드를 켠 상태에서 http://localhost:5173/api/health → 200 {"status":"ok"})
- [x] 학점 현황 화면 (mock) (실행 확인: npm run build 통과, 개발 서버 + headless Edge(CDP)로 /credit 텍스트 18개 항목 확인, 2학년 1학기 탭 클릭 시 "등록된 과목이 없습니다", VITE_USE_MOCK=false + 백엔드 꺼짐에서 오류 상태 확인)
- [ ] 과목 등록 모달
- [ ] 이수 위험 계산 + 표시

## 지금 하는 것
- DB 테이블 V1 작성 준비 (저장소 초기 세팅, 문서, 백엔드·프론트 뼈대, 큰 틀 레이아웃, 학점 현황 화면(mock)은 완료)

## 다음 할 일 (현재 기준)
- DB 테이블 V1, 시연용 초기 데이터 V2 작성 (PRD 7장, 9장)
- 학점 현황 API 3개 백엔드 구현 (summary, courses, required-courses. 응답 모양은 frontend/src/modules/credit/types.ts와 맞춘다)
- 과목 등록 모달 (PRD F2)

## 막힌 것
- 없음

## 미정 (임의로 정하지 않음)
- 큰 틀의 정식 이름 ("학생부 관리"로 임시 사용 중)
- 기존 러너스하이와의 연결 방식
- 인증 방식 (이번 주는 로그인 없이 데모 학생 1명으로 가정)
- 출석률 데이터 출처
- 브랜치와 PR 규칙
- "주의" 상태 기준값 (임시: 성취율 40% 이상 50% 미만)
- 과목 목록(약 290개)의 출처와 이용 조건
- 필수 이수 학점 구조 (교육부 원문 확인 필요)
- GET /required-courses 응답 모양 (PRD 8장에 예시가 없어 임시로 { remainingCount, courses: 과목 목록(courses 응답의 과목과 같은 모양) }으로 정함)
- 1학년 1학기 이수 완료 과목 구성 (PRD 9장: 목업 숫자 교과 29, 창체 3에 맞춰 정한다고만 되어 있음. mock에는 요약 숫자만 넣고 과목은 없음)
- 배포 시 프론트 서빙 방식 (새로고침 시 index.html로 돌려주는 설정은 개발 서버에서만 확인함)

## 실행 방법
자세한 내용은 저장소 루트의 README.md를 본다. 요약:
- 처음 한 번: 저장소 루트에서 `cp .env.example .env` (.env는 커밋 금지)
- MySQL: 저장소 루트에서 `docker compose up -d` → `docker compose ps`에서 (healthy) 확인. 포트 3307, DB learnershigh_credit
- 백엔드: `cd backend` 후 `./gradlew bootRun` (Flyway 경로가 backend 폴더 기준 `../database/migrations`라서 backend에서 실행한다)
- 헬스 체크: `curl http://localhost:8080/api/health` → `{"status":"ok"}`
- 빌드/테스트: `cd backend` 후 `./gradlew build` (DB 없이 통과)
- 프론트: `cd frontend` 후 `npm install`(처음 한 번), `npm run dev` → http://localhost:5173. `/api`는 localhost:8080으로 프록시된다
- mock 모드: 저장소 루트 `.env`의 `VITE_USE_MOCK` (없거나 true면 mock, false면 실제 API). 바꾸면 개발 서버 재시작
- 프론트 빌드: `cd frontend` 후 `npm run build` (타입 검사 포함)

## 작업 로그 (최신이 위)
형식:
### 날짜 — 제목
- 한 일:
- 바뀐 파일:
- 확인 방법과 결과:
- 결정과 이유:
- 막힌 점과 해결:
- 다음 할 일:

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
