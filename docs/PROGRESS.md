# 진행 현황
최종 갱신: 2026-10-27

## 완료
- [x] GitHub 저장소 생성, .gitignore, docs/references 폴더
- [x] 문서 3종 (CLAUDE.md, PROGRESS.md, PRD.md)
- [x] 백엔드 뼈대 (실행 확인: MySQL 중지 상태에서 ./gradlew clean build 통과, docker compose up 후 ./gradlew bootRun에서 Flyway "Successfully validated 0 migrations" 로그, curl http://localhost:8080/api/health → 200 {"status":"ok"})
- [ ] 프론트 뼈대
- [ ] DB 테이블 V1 / 초기 데이터 V2
- [ ] 큰 틀 레이아웃
- [ ] 학점 현황 화면 (mock)
- [ ] 과목 등록 모달
- [ ] 이수 위험 계산 + 표시

## 지금 하는 것
- 프론트 뼈대 생성 준비 (저장소 초기 세팅, 문서 작성, 백엔드 뼈대는 완료)

## 다음 할 일 (현재 기준)
- 프론트 뼈대와 큰 틀 레이아웃
- DB 테이블 V1, 시연용 초기 데이터 V2 작성 (뼈대 이후 적용)

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

## 실행 방법
자세한 내용은 저장소 루트의 README.md를 본다. 요약:
- 처음 한 번: 저장소 루트에서 `cp .env.example .env` (.env는 커밋 금지)
- MySQL: 저장소 루트에서 `docker compose up -d` → `docker compose ps`에서 (healthy) 확인. 포트 3307, DB learnershigh_credit
- 백엔드: `cd backend` 후 `./gradlew bootRun` (Flyway 경로가 backend 폴더 기준 `../database/migrations`라서 backend에서 실행한다)
- 헬스 체크: `curl http://localhost:8080/api/health` → `{"status":"ok"}`
- 빌드/테스트: `cd backend` 후 `./gradlew build` (DB 없이 통과)
- 프론트: (뼈대 생성 후 채움)

## 작업 로그 (최신이 위)
형식:
### 날짜 — 제목
- 한 일:
- 바뀐 파일:
- 확인 방법과 결과:
- 결정과 이유:
- 막힌 점과 해결:
- 다음 할 일:

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
