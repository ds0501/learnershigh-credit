# 진행 현황
최종 갱신: 2026-10-28

## 완료
- [x] GitHub 저장소 생성, .gitignore, docs/references 폴더
- [x] 문서 3종 (CLAUDE.md, PROGRESS.md, PRD.md)
- [x] 백엔드 뼈대 (실행 확인: MySQL 중지 상태에서 ./gradlew clean build 통과, docker compose up 후 ./gradlew bootRun에서 Flyway "Successfully validated 0 migrations" 로그, curl http://localhost:8080/api/health → 200 {"status":"ok"})
- [x] 프론트 뼈대 (실행 확인: frontend에서 npm run build 통과(tsc 타입 검사 포함), npm run dev 후 http://localhost:5173 → 200)
- [ ] DB 테이블 V1 / 초기 데이터 V2
- [x] 큰 틀 레이아웃 (실행 확인: /credit, /tasks, /records, /grades 각각 200, headless Edge로 각 주소를 직접 열어 제목과 활성 메뉴가 맞게 그려지는 것 확인, 백엔드를 켠 상태에서 http://localhost:5173/api/health → 200 {"status":"ok"})
- [ ] 학점 현황 화면 (mock)
- [ ] 과목 등록 모달
- [ ] 이수 위험 계산 + 표시

## 지금 하는 것
- DB 테이블 V1 작성 준비 (저장소 초기 세팅, 문서 작성, 백엔드 뼈대, 프론트 뼈대와 큰 틀 레이아웃은 완료)

## 다음 할 일 (현재 기준)
- DB 테이블 V1, 시연용 초기 데이터 V2 작성 (PRD 7장, 9장)
- 학점 현황 화면 (mock 데이터는 src/mocks/, 자리는 frontend/src/modules/credit/CreditPage.tsx)

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

### 2026-10-28 — 참고 이미지를 git 추적에서 제외
- 한 일: .gitignore에 `docs/references/*`와 `!docs/references/.gitkeep` 추가, CLAUDE.md 규칙에 참고 이미지 커밋 금지 한 줄 추가, PROGRESS.md "미정"에서 참고 이미지 항목 삭제. 이미지 파일 4개는 삭제하거나 옮기지 않고 로컬 docs/references/에 그대로 둠
- 바뀐 파일: .gitignore, CLAUDE.md, docs/PROGRESS.md
- 확인 방법과 결과: git status --short에 이미지 4개가 나오지 않음(.gitignore만 수정으로 표시). git check-ignore -v로 4개 모두 .gitignore의 `docs/references/*` 규칙에 걸리는 것 확인. git ls-files docs/references → .gitkeep만 추적됨
- 결정과 이유: 참고 이미지(목업 2개, 기존 수행평가 캡처 2개)는 로컬에만 두고 커밋하지 않음. 기존 앱 캡처에 개인 이름이 보이기 때문
- 막힌 점과 해결: 없음
- 다음 할 일: DB 테이블 V1과 시연용 데이터 V2, 학점 현황 화면(mock). 새로 작업하는 사람은 참고 이미지를 저장소 밖에서 따로 받아 docs/references/에 넣어야 함

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
