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
- [x] 과목 등록 모달 (mock) (실행 확인: npm test 53개 통과, npm run build·npm run lint 통과, headless Edge 39개 항목 통과(기존 점수 입력·삭제 45개도 다시 통과))
- [x] 이수 위험 계산 + 표시 (mock) (실행 확인: npm test 26개 통과(PRD 6장 케이스 7개 포함), npm run build·npm run lint 통과, headless Edge(CDP)로 32개 항목 확인)
- [x] 평가 점수 입력 (mock) (과목 행 클릭 → 오른쪽 패널. 위 headless 확인에 포함: 통합과학2에 수행 20% 80점 추가 → 48% 주의, 배너 위험 0 · 주의 2, 득점 > 만점은 저장 안 됨)
- [x] 평가 삭제 (mock) (실행 확인: npm test 29개 통과, headless Edge 45개 항목 통과. 통합과학2에 추가한 평가를 삭제하면 48% 주의 → 35% 이수 위험, 마지막 평가 삭제 시 미입력)
- [x] 과목 이수 상태 변경과 과목 삭제 (mock) (실행 확인: npm test 62개 통과, npm run build·npm run lint 통과, headless Edge 32개 항목 통과(기존 점수 입력·삭제 45개, 과목 등록 39개도 다시 통과))

## 지금 하는 것
- DB 테이블 V1 작성 준비 (저장소 초기 세팅, 문서, 백엔드·프론트 뼈대, 큰 틀 레이아웃, 학점 현황 화면(mock), 이수 위험 계산과 점수 입력·삭제(mock), 과목 등록 모달(mock), 과목 상태 변경·삭제(mock)는 완료. 프론트는 아직 mock 데이터로만 동작)

## 다음 할 일 (현재 기준)
- DB 테이블 V1, 시연용 초기 데이터 V2 작성 (PRD 7장, 9장). 표준 과목 목록 seed는 frontend/src/mocks/catalog.ts(시연용 30개)를 옮긴다. 평가 점수 seed는 frontend/src/mocks/creditStore.ts의 시연 데이터(1학년 1학기 10과목, 2학기 10과목과 평가)를 그대로 옮기면 PRD 9장 숫자가 나온다
- 백엔드 API 구현: summary, courses, required-courses, courses/{courseId}, catalog, 과목 등록 POST /courses, 과목 상태 변경 PATCH /courses/{courseId}, 과목 삭제 DELETE /courses/{courseId}, 평가 POST/PUT/DELETE. 응답 모양은 frontend/src/modules/credit/types.ts와 맞추고, 계산 규칙은 frontend/src/modules/credit/domain/(completion.ts, rules.ts, validation.ts, courseRegistration.ts)과 같게 서비스에 구현한다(테스트 케이스는 domain/completion.test.ts 참고)
- API 계약 문서 docs/api-contract.md 작성 (다음 작업에서 만들기로 함. 이번에는 만들지 않음)

## 막힌 것
- 없음

## 미정 (임의로 정하지 않음)
- 기존 러너스하이와의 연결 방식
- 인증 방식 (이번 주는 로그인 없이 데모 학생 1명으로 가정)
- 출석률 데이터 출처
- 브랜치와 PR 규칙
- 과목 목록(약 290개)의 출처와 이용 조건
- 필수 이수 학점 구조 (교육부 원문 확인 필요)
- 점수 입력 패널용 과목 상세 API: PRD 8장에 없어 임시로 GET /api/student/credit/courses/{courseId} (과목 정보 + writtenWeight, performanceWeight, assessments)를 mock에 만듦
- API 오류 응답 형식 (PRD 8장: 뼈대 생성 후 정함). mock은 임시로 검증 실패 시 400 + { message, fieldErrors: { 항목: 문구 } } 모양으로 오류를 냄
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

## 작업 로그 (최신이 위)
형식:
### 날짜 — 제목
- 한 일:
- 바뀐 파일:
- 확인 방법과 결과:
- 결정과 이유:
- 막힌 점과 해결:
- 다음 할 일:

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
