# 진행 현황
최종 갱신: 2026-11-10

## 현재 상태
- 된 것: api-contract.md의 API 11개(4.1~4.11) 전부 실제 백엔드로 구현 완료(학점 현황 쪽). 과목 삭제 500 오류(파생 삭제 쿼리 트랜잭션 누락) 수정함. 화면의 "참고용 예측" 안내 문구는 전부 삭제(CLAUDE.md 규칙도 같이 바꿈) — WORKLOG 2026-11-10 항목들
- 된 것(학과 로드맵, 신규): docs/dept_subject_2depth.xlsx(대교협 2028학년도 권역별 대학별 권장과목) 기반으로 학과 검색 → 추천 선택과목 기능 추가. DB 테이블 credit_department·credit_department_subject(V6) + 실데이터 92개 학과·2035개 추천행 시딩(V7), GET /api/student/credit/departments?query=·GET /departments/{id} 구현, 프론트 /credit/roadmap 화면(검색, 전체공통/다수공통/학교별다름 3단, 요구 대학 토글, 이미 등록한 과목 강조)까지 전부 실제 API로 연결 완료 — WORKLOG 2026-11-10 "학과 로드맵 DB·API·프론트 연결"
- 안 된 것(확인 못 함): 브라우저 확장(claude-in-chrome)이 연결되지 않아 화면에서 직접 눌러서(학점 현황 전체 기능, 학과 로드맵 검색) 확인하지 못함. curl/build/test로만 확인함
- 안 된 것(법적 확인 필요): 학과 로드맵의 "참고: 시도교육청 예시" 섹션(서울시교육청교육연구정보원 자료)은 원본에 "무단전재·게시 금지" 조건이 있어 승인 전까지 실제 배포 빌드에서는 빼야 함(지금은 개발용으로만 노출 중, frontend 코드에 TODO 남김) — "미정" 참고
- 다음: 브라우저에서 화면 전체를 직접 눌러 확인(확장 연결되면). 교육연구정보원 승인 여부 확인. 그 다음 "미정" 목록 정리

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
- [x] GET courses/{courseId}(과목 상세), PATCH·DELETE courses/{courseId}(상태변경·삭제), POST·PUT·DELETE assessments(평가 추가·수정·삭제) + frontend 나머지 전부 forceReal — WORKLOG 2026-11-10 "과목 상세·상태변경·삭제, 평가 추가·수정·삭제 구현 (남은 API 전부)"
- [x] 학과 로드맵: DB 테이블(V6)·실데이터 시딩(V7, 92개 학과·2035개 추천행) — WORKLOG 2026-11-10 "학과 로드맵 DB·API·프론트 연결"
- [x] 학과 로드맵: GET /departments?query=·GET /departments/{id} API + /credit/roadmap 화면 연결 — WORKLOG 2026-11-10 "학과 로드맵 DB·API·프론트 연결"

## 지금 하는 것
- 없음 (학점 현황·학과 로드맵 1차 구현 완료, 다음 사람은 아래 "다음 할 일"부터 시작)

## 다음 할 일 (현재 기준)
1. 브라우저에서 화면 전체를 직접 눌러 확인(브라우저 확장이 연결 안 돼 아직 못 함): 학점 현황 전체 기능(점수 입력/수정/삭제, 이수 상태 변경, 과목 등록/삭제), 학과 로드맵 검색·추천·토글이 화면에서 실제로 동작하는지
2. 서울시교육청교육연구정보원에 "학과 로드맵"의 시도교육청 예시 자료 이용 승인 문의(위 "미정" 참고). 승인 전까지 배포 빌드에서는 그 섹션을 빼야 함
3. "미정" 목록 정리(특히 api-contract.md·contractScenario.ts의 catalogId 예시와 실제 DB 순서 불일치, SPECIALIZED 과목 유형 프론트 대응)

## 막힌 것
- 없음

## 미정 (임의로 정하지 않음)
- 학과 로드맵의 "참고: 시도교육청 예시"(서울시교육청교육연구정보원 자료) 이용 승인 — 원본에 "무단전재·게시 금지, 교사의 학생 지도 목적만 예외" 조건이 있어 지금은 개발용으로만 노출 중. 승인 안 나면 그 섹션(DB 컬럼은 그대로 두고 frontend에서만 숨김) 제거
- 학과 로드맵 검색 범위: 지금은 대교협 자료가 있는 92개 학과만 검색됨(학과카드 시트 기준). 참고_교육청예시에만 있는 나머지 33개 학과를 검색 대상에 포함할지는 정하지 않음
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
