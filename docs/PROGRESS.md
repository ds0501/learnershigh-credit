# 진행 현황
최종 갱신: 2026-10-28

## 현재 상태
- 된 것: 학점 현황 요약(SummaryCard), 학기별 수강 과목 표, 남은 필수 이수 과목 패널이 이제 mock이 아니라 실제 백엔드 API를 쓴다. API 명세 문서(docs/api-contract.md) 완료. DB 테이블 V1~V5(표준 과목 291개, 졸업/이수 기준값, 데모 학생 과목 20개·평가 44개, 데모 학생 id="0") 모두 적용 완료. 백엔드: 공통 오류 응답(ApiException 계층+GlobalExceptionHandler), 계산 서비스(CreditCalculationService), GET summary·courses·required-courses·catalog(검색), POST courses(등록) 실제로 동작 — WORKLOG 2026-10-28 "GET /courses, GET /required-courses 구현...", "계산 서비스와 GET /summary 구현...", "공통 처리와 과목 검색·등록 API 구현"
- 안 된 것: GET courses/{courseId}(과목 상세), 과목 변경 API(이수 상태 변경·삭제), 평가 API(추가·수정·삭제)는 여전히 mock. **그래서 화면의 과목 표에는 실제 DB 과목이 보이지만, 그 과목을 눌러 점수를 입력/상태 변경/삭제하려 하면 mock 저장소에 그 id가 없어 오류가 난다** — 평가 점수 입력·삭제, 이수 상태 변경, 과목 삭제는 아직 mock 과목(원래 시연용으로 들어있던 20개)에서만 정상 동작함
- 다음: GET courses/{courseId} + 과목 변경 API + 평가 API를 실제 백엔드로 구현 (위 "안 된 것"의 간극을 없애는 것)

## 완료
각 항목의 실행 확인 상세는 docs/WORKLOG.md의 해당 날짜 항목에 있다.
- [x] GitHub 저장소, .gitignore, docs/references 폴더 — WORKLOG 2026-10-27 "저장소 초기 세팅"
- [x] 문서 3종 (CLAUDE.md, PROGRESS.md, PRD.md) — WORKLOG 2026-10-27 "PRD 추가"
- [x] 백엔드 뼈대 — WORKLOG 2026-10-27 "백엔드 뼈대 추가"
- [x] 프론트 뼈대 — WORKLOG 2026-10-28 "프론트 뼈대와 큰 틀 레이아웃 추가"
- [x] 큰 틀 레이아웃 — WORKLOG 2026-10-28 "프론트 뼈대와 큰 틀 레이아웃 추가"
- [x] 학점 현황 화면 (mock) — WORKLOG 2026-10-28 "학점 현황 화면(mock) 추가"
- [x] 이수 위험 계산 + 표시 (mock) — WORKLOG 2026-10-28 "이수 위험 계산과 점수 입력(mock) 추가"
- [x] 평가 점수 입력 (mock) — WORKLOG 2026-10-28 "이수 위험 계산과 점수 입력(mock) 추가"
- [x] 평가 삭제 (mock) — WORKLOG 2026-10-28 "평가 삭제 추가"
- [x] 과목 등록 모달 (mock) — WORKLOG 2026-10-28 "신규 과목 등록 모달(mock) 추가"
- [x] 과목 이수 상태 변경과 과목 삭제 (mock) — WORKLOG 2026-10-28 "과목 상태 변경과 삭제(mock) 추가"
- [x] API 명세 문서 (docs/api-contract.md) — WORKLOG 2026-10-28 "API 명세 문서 추가"
- [x] 표준 과목 목록에서 학점 제거(항상 학생 입력) — WORKLOG 2026-10-28 "표준 과목 목록에서 학점 제거"
- [x] DB 테이블 V1 — WORKLOG 2026-10-28 "DB 테이블 V1 작성"
- [x] 표준 과목 목록(credit_course_catalog) 실데이터 291개 시딩 — WORKLOG 2026-10-28 "표준 과목 목록 실데이터 시딩"
- [x] 졸업/이수 기준값 + 데모 학생 과목 20개·평가 44개 시딩(V4) — WORKLOG 2026-10-28 "졸업/이수 기준값과 데모 학생 과목·평가 시딩 (V4)"
- [x] 공통 오류 응답/성공 코드 + GET catalog 검색 + POST courses 등록 — WORKLOG 2026-10-28 "공통 처리와 과목 검색·등록 API 구현"
- [x] 계산 서비스(성취율·출석률·위험단계·필요평균) + GET summary + 첫 화면 요약 카드 실제 API 연결 — WORKLOG 2026-10-28 "계산 서비스와 GET /summary 구현, 첫 화면 요약 카드 실제 API 연결"
- [x] GET courses?gradeYear=&term=, GET required-courses + 과목 표·필수 과목 패널 실제 API 연결 — WORKLOG 2026-10-28 "GET /courses, GET /required-courses 구현, 과목 표·필수 과목 패널 실제 API 연결"

## 지금 하는 것
- GET courses/{courseId}(과목 상세) 설계 시작

## 다음 할 일 (현재 기준)
백엔드 구현 순서. API 모양과 규칙은 docs/api-contract.md가 기준이다(예시 JSON은 mock 실제 응답이고 contract.test.ts로 확인됨).
1. GET courses/{courseId}(과목 상세, 평가 목록 포함) — CourseItemAssembler·CreditCalculationService를 재사용. 이게 없어서 지금 화면에서 실제 DB 과목을 눌러 점수 입력 패널을 열면 mock에 없는 과목이라 오류가 남(위 "안 된 것" 참고)
2. 과목 변경 API: PATCH courses/{courseId}(이수 상태 변경), DELETE courses/{courseId}(평가도 함께 삭제) (POST courses는 이미 됨)
3. 평가 API: POST courses/{courseId}/assessments, PUT·DELETE courses/{courseId}/assessments/{assessmentId}(그 과목의 평가가 아니면 404)
4. 연결 확인: VITE_USE_MOCK=false로 프론트를 띄워 화면 전체 확인, api-contract.md 예시와 실제 응답 비교(단, api-contract.md의 catalogId 예시는 frontend mock 기준이라 실제 DB 값과 다를 수 있음 — "미정" 참고). summary·courses·required-courses는 이미 forceReal로 항상 실제 API를 쓰므로 VITE_USE_MOCK 값과 무관하게 동작함을 재확인

## 막힌 것
- 없음

## 미정 (임의로 정하지 않음)
- 기존 러너스하이와의 연결 방식
- 인증 방식 (이번 주는 로그인 없이 데모 학생 1명으로 가정)
- 출석률 데이터 출처
- 브랜치와 PR 규칙
- 필수 이수 학점 구조 (교육부 원문 확인 필요)
- 전문교과(SPECIALIZED)는 지금 DB(credit_course_catalog)에만 있고 frontend CourseType·COURSE_TYPE_LABEL에는 없음. 실제 백엔드 연동(VITE_USE_MOCK=false) 전에 프론트도 맞춰야 함
- 특성화고·산업수요맞춤형고 졸업 기준(교과 174학점 중 전문교과 80학점 이상 + 보통교과 64학점 이상)은 이번 범위에 넣지 않음 — credit_graduation_rule은 일반고 기준(192/174/18)만 다룸(PRD 12장)
- api-contract.md·contractScenario.ts의 catalogId 예시(예: "확률과 통계"=17)는 frontend mock(catalog.ts, 30개 demo 순서) 기준이고 실제 DB 순서(확률과 통계=18)와 다름. 데모 학생 과목을 실제 DB에 넣을 때는 이름으로 다시 조회해서 ID를 써야 함
- 이수 완료 과목 응답의 riskLevel·requiredAverage: 지금 API(mock)는 이수 상태와 관계없이 계산값을 돌려주고 화면이 수강 중 과목에만 보여 줌. API에서 뺄지는 정하지 않음
- 로그인 도입 후 다른 학생의 과목·평가 ID로 요청했을 때의 응답(404 또는 403)
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
- 프론트 단위 테스트: `cd frontend` 후 `npm test` (Vitest). 린트: `npm run lint`
- mock 데이터는 frontend/src/mocks/creditStore.ts의 메모리 저장소에 있다. 점수를 입력해도 새로고침하면 처음 상태로 돌아간다(정상)

## 작업 로그
작업 로그는 docs/WORKLOG.md로 옮겼다 (최신이 위).
