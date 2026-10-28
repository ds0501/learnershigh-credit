# 진행 현황
최종 갱신: 2026-10-28

## 현재 상태
- 된 것: mock 데이터로 학점 현황, 평가 점수 입력·삭제, 신규 과목 등록, 이수 상태 변경·과목 삭제를 시연할 수 있다. API 명세 문서(docs/api-contract.md) 완료
- 안 된 것: DB 테이블(V1)과 시연용 초기 데이터(V2), 백엔드 API (프론트는 아직 mock으로만 동작)
- 다음: DB 테이블 V1 (먼저 "미정"의 주의 상한(50%) 저장 위치를 정한다)

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
- [ ] DB 테이블 V1 / 초기 데이터 V2

## 지금 하는 것
- DB 테이블 V1 작성 준비

## 다음 할 일 (현재 기준)
백엔드 구현 순서. API 모양과 규칙은 docs/api-contract.md가 기준이다(예시 JSON은 mock 실제 응답이고 contract.test.ts로 확인됨).
1. DB 테이블 V1 (database/migrations/V1__…sql): PRD 7장의 credit_course_catalog, credit_student_course(student_id 포함), credit_assessment, credit_completion_rule, credit_graduation_rule. 착수 전에 "미정"의 "주의 상한(50%) 저장 위치"를 정한다
2. 시연용 초기 데이터 V2: 졸업 기준(192/174/18), 이수 기준(최소 출석률 66.67, 공통 최소 성취율 40), 표준 과목 30개(frontend/src/mocks/catalog.ts와 같은 순서, ID 1~30), 데모 학생 과목 20개(1학년 1학기 ID 1~10, 2학기 ID 11~20)와 평가 44개(ID 1~44, frontend/src/mocks/creditStore.ts와 같은 순서). 같은 순서로 넣어야 api-contract.md 예시의 ID(통합과학2 = 16, 새 평가 = 45 등)와 맞는다
3. 공통: 데모 학생 고정 처리, 오류 응답 형식(400 { message, fieldErrors }, 404 { message, fieldErrors: {} }), 성공 코드(200/201/204)
4. 계산 서비스: 성취율, 출석률, 위험 단계 7단계, 필요 평균(api-contract.md 5장). PRD 6장 테스트 케이스 7개를 백엔드 단위 테스트로 옮긴다(frontend/src/modules/credit/domain/completion.test.ts 참고)
5. 조회 API: GET summary, GET courses?gradeYear=&term=, GET required-courses, GET catalog?query=, GET courses/{courseId}
6. 과목 변경 API: POST courses(검증 포함), PATCH courses/{courseId}(이수 상태 변경), DELETE courses/{courseId}(평가도 함께 삭제)
7. 평가 API: POST courses/{courseId}/assessments, PUT·DELETE courses/{courseId}/assessments/{assessmentId}(그 과목의 평가가 아니면 404)
8. 연결 확인: VITE_USE_MOCK=false로 프론트를 띄워 화면 확인, api-contract.md 예시와 실제 응답 비교

## 막힌 것
- 없음

## 미정 (임의로 정하지 않음)
- 기존 러너스하이와의 연결 방식
- 인증 방식 (이번 주는 로그인 없이 데모 학생 1명으로 가정)
- 출석률 데이터 출처
- 브랜치와 PR 규칙
- 과목 목록(약 290개)의 출처와 이용 조건
- 필수 이수 학점 구조 (교육부 원문 확인 필요)
- 주의 상한(50%) 저장 위치: PRD 7장 credit_completion_rule에는 min_attendance, min_achievement만 있고 주의 상한 컬럼이 없음(프론트는 domain/rules.ts의 cautionBelowRate). DB V1 전에 컬럼 추가 여부를 정해야 함
- 생성 성공 상태 코드: mock에는 상태 코드가 없어 api-contract.md에 POST 201로 적음(클라이언트는 2xx를 모두 성공으로 처리해 200이어도 동작함). 확정 필요
- 이수 완료 과목 응답의 riskLevel·requiredAverage: 지금 API(mock)는 이수 상태와 관계없이 계산값을 돌려주고 화면이 수강 중 과목에만 보여 줌. API에서 뺄지는 정하지 않음
- 로그인 도입 후 다른 학생의 과목·평가 ID로 요청했을 때의 응답(404 또는 403)
- 성취율 소수 오차 처리 방식(비교할 때 허용 오차를 둘지, 소수 둘째 자리까지 반올림할지). 백엔드 구현 전에 정한다
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
