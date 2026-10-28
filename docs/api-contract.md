# API 명세: 고교학점제 이수 관리

백엔드가 그대로 구현할 수 있도록 현재 프론트 mock(frontend/src/mocks/)이 실제로 하는 동작을 적은 문서다.
PRD 8장은 이 문서의 요약이며, 상세 기준은 이 문서다. 동작을 바꾸면 이 문서, PRD 8장, mock을 함께 고친다.

- 예시는 시연 데이터(PRD 9장)로 mock을 실제로 호출한 결과를 그대로 붙인 것이다. 예시는 문서에 적힌 순서대로 호출한 결과라서, 앞 예시(과목 등록, 평가 추가 등)가 뒤 예시의 상태에 영향을 준다.
- 예시와 mock이 같은지는 테스트로 확인한다: frontend/src/mocks/contract.test.ts가 frontend/src/mocks/contractScenario.ts의 순서대로 mock을 호출해, 이 문서의 `<!-- contract:… -->` 표시가 붙은 요청 예시를 그대로 보내고 응답이 응답 예시와 키·값까지 같은지 비교한다. 이 표시는 지우지 않는다.

## 1. 공통 규칙

| 항목 | 규칙 |
|---|---|
| 경로 접두사 | `/api/student/credit` (아래 경로는 이 접두사 뒤의 경로) |
| JSON | 필드 이름은 camelCase |
| ID | 모든 ID는 문자열 (DB는 BIGINT, 응답할 때 문자열로 바꾼다) |
| 날짜 | `YYYY-MM-DD` 문자열 |
| 숫자 | 성취율·필요 평균은 반올림하지 않은 값(예: `47.85714285714286`). 화면이 정수로 반올림해 표시한다 |
| 학생 | 로그인이 없어 데모 학생 1명 고정. 요청에 학생 ID를 넣지 않고, 서버가 데모 학생으로 처리한다 |
| 성공 상태 코드 | 조회·수정 200, 생성(POST) 201, 삭제 204(본문 없음) |
| 검증 오류 | `400` + `{ "message": "입력값을 확인하세요.", "fieldErrors": { "필드 이름": "오류 문구" } }`. 실패하면 아무것도 저장하지 않는다 |
| 없는 대상 | `404` + `{ "message": "과목을 찾을 수 없습니다.", "fieldErrors": {} }` (평가는 "평가를 찾을 수 없습니다.") |

- 오류 본문은 항상 `message`와 `fieldErrors`를 가진다. `fieldErrors`의 키는 요청 본문의 필드 이름이고, 화면은 그 필드 옆에 문구를 그대로 보여 준다. 404의 `fieldErrors`는 빈 객체다.
- 프론트 클라이언트(frontend/src/api/client.ts)는 2xx면 성공으로 보고, 204면 본문을 읽지 않는다. 실패하면 본문의 `message`, `fieldErrors`를 읽는다.

## 2. 값(enum)과 화면 문구

### 위험 단계 `riskLevel`
| 값 | 뜻 | 화면 배지 (수강 중 과목) |
|---|---|---|
| `NORMAL` | 정상 | 수강 중 |
| `CAUTION` | 주의 (공통과목 성취율 40% 이상 50% 미만) | 주의 |
| `RISK` | 이수 위험 (출석률 미달, 또는 공통과목 성취율 40% 미만) | 이수 위험 |
| `NO_DATA` | 입력된 평가가 없음 | 미입력 |

이수 완료 과목은 `riskLevel`과 관계없이 배지가 "이수 완료"다.

### 과목 유형 `courseType`
| 값 | 화면 문구 |
|---|---|
| `COMMON` | 공통 |
| `GENERAL` | 일반선택 |
| `CAREER` | 진로선택 |
| `FUSION` | 융합선택 |
| `CREATIVE` | 창체 |

### 이수 상태 `status`
| 값 | 화면 문구 |
|---|---|
| `IN_PROGRESS` | 수강 중 |
| `COMPLETED` | 이수 완료 |

### 성적 표기 방식 `gradeDisplay`
| 값 | 화면 문구 |
|---|---|
| `ACHIEVEMENT_ONLY` | 성취도(A~E)만 |
| `ACHIEVEMENT_AND_RANK` | 성취도 + 5등급 함께 표기 |
| `THREE_LEVEL` | A·B·C 3단계 |

창체는 `null`. 표기 설정일 뿐 계산에는 쓰지 않는다.

### 평가 종류 `kind`
| 값 | 화면 문구 |
|---|---|
| `WRITTEN` | 지필 |
| `PERFORMANCE` | 수행 |

## 3. 응답 모양 (공통 객체)

프론트 타입: frontend/src/modules/credit/types.ts (이 장의 표와 같은 이름·필드)

### 요약 `CreditSummary` (GET /summary)
| 필드 | 타입 | 설명 |
|---|---|---|
| `graduationCredits` | number | 졸업 학점 (192) |
| `completedCredits` | number | 이수 완료 학점 |
| `inProgressCredits` | number | 수강 중 학점 |
| `remainingCredits` | number | 남은 학점 |
| `subjectCredits` | `{ earned: number, required: number }` | 교과 이수 학점 / 교과 기준(174) |
| `creativeCredits` | `{ earned: number, required: number }` | 창체 이수 학점 / 창체 기준(18) |
| `alert` | `{ riskCount: number, cautionCount: number }` | 경고 배너 (수강 중 과목만 셈) |

### 학기별 과목 `SemesterCourses` (GET /courses)
| 필드 | 타입 | 설명 |
|---|---|---|
| `gradeYear` | number | 요청한 학년 |
| `term` | number | 요청한 학기 |
| `totalCredits` | number | 그 학기 과목 학점 합 |
| `courses` | CourseItem[] | 그 학기 과목 |

### 남은 필수 과목 `RequiredCourses` (GET /required-courses)
| 필드 | 타입 | 설명 |
|---|---|---|
| `remainingCount` | number | `courses`의 개수 |
| `courses` | CourseItem[] | 이수하지 않은 공통과목 |

### 과목 `CourseItem`
GET /courses, GET /required-courses의 `courses` 항목, POST /courses와 PATCH /courses/{courseId}의 응답.

| 필드 | 타입 | 설명 |
|---|---|---|
| `id` | string | 과목 ID |
| `name` | string | 과목명 |
| `courseType` | courseType | 과목 유형 |
| `credits` | number | 학점 |
| `status` | status | 이수 상태 |
| `achievementRate` | number \| null | 현재 성취율(%). 입력된 평가가 없으면(창체 포함) `null` |
| `riskLevel` | riskLevel | 위험 단계 |
| `requiredAverage` | number (선택) | 남은 평가에서 필요한 평균(%). `RISK`인 공통과목의 성취율이 40 미만이고 남은 반영 비율이 있을 때만 필드가 있다(5장). 없으면 필드 자체를 넣지 않는다 |

### 과목 상세 `CourseDetail`
GET /courses/{courseId}의 응답. `CourseItem`의 모든 필드에 아래를 더한다.

| 필드 | 타입 | 설명 |
|---|---|---|
| `gradeYear` | number | 학년 (1~3) |
| `term` | number | 학기 (1~2) |
| `writtenWeight` | number | 지필 반영 비율(%). 창체는 0 |
| `performanceWeight` | number | 수행 반영 비율(%). 창체는 0 |
| `assessments` | Assessment[] | 평가 목록 (등록 순서) |

### 평가 `Assessment`
| 필드 | 타입 | 설명 |
|---|---|---|
| `id` | string | 평가 ID |
| `kind` | kind | 지필/수행 |
| `title` | string | 평가 이름 |
| `weightPercent` | number | 반영 비율(%) |
| `maxScore` | number | 만점 |
| `score` | number \| null | 득점. `null`이면 미입력(계산에서 제외) |
| `evaluatedOn` | string | 평가 날짜 YYYY-MM-DD |
| `taskId` | string \| null | 기존 수행평가 연결용. 이번 단계는 항상 `null` |

### 표준 과목 `CatalogItem`
| 필드 | 타입 | 설명 |
|---|---|---|
| `id` | string | 표준 과목 ID |
| `name` | string | 과목명 |
| `subjectGroup` | string | 교과(군) |
| `courseType` | courseType | 과목 유형 |
| `credits` | number | 기준 학점 |

## 4. 엔드포인트

| # | 메서드 | 경로 | 설명 |
|---|---|---|---|
| 4.1 | GET | `/summary` | 학점 현황 요약 |
| 4.2 | GET | `/courses?gradeYear=1&term=2` | 학기별 수강 과목 |
| 4.3 | GET | `/required-courses` | 남은 필수 과목 |
| 4.4 | GET | `/catalog?query=공통국어` | 표준 과목 검색 |
| 4.5 | POST | `/courses` | 과목 등록 |
| 4.6 | GET | `/courses/{courseId}` | 과목 상세(평가 목록 포함) |
| 4.7 | PATCH | `/courses/{courseId}` | 이수 상태 변경 |
| 4.8 | DELETE | `/courses/{courseId}` | 과목 삭제(평가도 함께) |
| 4.9 | POST | `/courses/{courseId}/assessments` | 평가 추가 |
| 4.10 | PUT | `/courses/{courseId}/assessments/{assessmentId}` | 평가 수정 |
| 4.11 | DELETE | `/courses/{courseId}/assessments/{assessmentId}` | 평가 삭제 |

### 4.1 GET /summary
학점 현황 요약(진행바, 교과/창체, 경고 배너).
- `completedCredits`: 이수 완료 과목 학점 합. `inProgressCredits`: 수강 중 과목 학점 합(창체 포함)
- `remainingCredits` = `graduationCredits` − `completedCredits` − `inProgressCredits` (수강 중 과목을 모두 이수한다고 가정)
- `subjectCredits.earned`: 이수 완료 과목 중 창체가 아닌 과목 학점 합. `creativeCredits.earned`: 이수 완료 창체 학점 합
- `required` 값과 `graduationCredits`는 졸업 학점 기준(credit_graduation_rule)
- `alert`: **수강 중인 과목만** 세어 `riskLevel`이 `RISK`인 수, `CAUTION`인 수

응답 200
<!-- contract:summary 200 -->
```json
{
  "graduationCredits": 192,
  "completedCredits": 32,
  "inProgressCredits": 32,
  "remainingCredits": 128,
  "subjectCredits": { "earned": 29, "required": 174 },
  "creativeCredits": { "earned": 3, "required": 18 },
  "alert": { "riskCount": 1, "cautionCount": 1 }
}
```

### 4.2 GET /courses?gradeYear=1&term=2
선택한 학기의 과목 목록. `gradeYear`(1~3), `term`(1~2) 둘 다 받는다. 그 학기에 과목이 없으면 `courses: []`, `totalCredits: 0`.
- `totalCredits`: 그 학기 과목 학점 합(이수 상태와 관계없이, 창체 포함)
- 과목 순서: 등록 순서

응답 200
<!-- contract:courses 200 -->
```json
{
  "gradeYear": 1,
  "term": 2,
  "totalCredits": 32,
  "courses": [
    { "id": "11", "name": "공통국어2", "courseType": "COMMON", "credits": 4, "status": "IN_PROGRESS", "achievementRate": 78, "riskLevel": "NORMAL" },
    { "id": "12", "name": "공통수학2", "courseType": "COMMON", "credits": 4, "status": "IN_PROGRESS", "achievementRate": 46, "riskLevel": "CAUTION" },
    { "id": "13", "name": "공통영어2", "courseType": "COMMON", "credits": 4, "status": "IN_PROGRESS", "achievementRate": 72, "riskLevel": "NORMAL" },
    { "id": "14", "name": "한국사2", "courseType": "COMMON", "credits": 3, "status": "IN_PROGRESS", "achievementRate": 81, "riskLevel": "NORMAL" },
    { "id": "15", "name": "통합사회2", "courseType": "COMMON", "credits": 4, "status": "IN_PROGRESS", "achievementRate": 69, "riskLevel": "NORMAL" },
    { "id": "16", "name": "통합과학2", "courseType": "COMMON", "credits": 4, "status": "IN_PROGRESS", "achievementRate": 35, "riskLevel": "RISK", "requiredAverage": 45 },
    { "id": "17", "name": "과학탐구실험2", "courseType": "COMMON", "credits": 1, "status": "IN_PROGRESS", "achievementRate": 88, "riskLevel": "NORMAL" },
    { "id": "18", "name": "운동과 건강", "courseType": "GENERAL", "credits": 2, "status": "IN_PROGRESS", "achievementRate": 85, "riskLevel": "NORMAL" },
    { "id": "19", "name": "음악", "courseType": "GENERAL", "credits": 3, "status": "IN_PROGRESS", "achievementRate": 78.99999999999999, "riskLevel": "NORMAL" },
    { "id": "20", "name": "창의적 체험활동", "courseType": "CREATIVE", "credits": 3, "status": "IN_PROGRESS", "achievementRate": null, "riskLevel": "NORMAL" }
  ]
}
```

### 4.3 GET /required-courses
아직 이수하지 않은 공통과목: `courseType`이 `COMMON`이고 `status`가 `COMPLETED`가 아닌 과목(모든 학기). `remainingCount`는 `courses`의 개수.

응답 200
<!-- contract:required-courses 200 -->
```json
{
  "remainingCount": 7,
  "courses": [
    { "id": "11", "name": "공통국어2", "courseType": "COMMON", "credits": 4, "status": "IN_PROGRESS", "achievementRate": 78, "riskLevel": "NORMAL" },
    { "id": "12", "name": "공통수학2", "courseType": "COMMON", "credits": 4, "status": "IN_PROGRESS", "achievementRate": 46, "riskLevel": "CAUTION" },
    { "id": "13", "name": "공통영어2", "courseType": "COMMON", "credits": 4, "status": "IN_PROGRESS", "achievementRate": 72, "riskLevel": "NORMAL" },
    { "id": "14", "name": "한국사2", "courseType": "COMMON", "credits": 3, "status": "IN_PROGRESS", "achievementRate": 81, "riskLevel": "NORMAL" },
    { "id": "15", "name": "통합사회2", "courseType": "COMMON", "credits": 4, "status": "IN_PROGRESS", "achievementRate": 69, "riskLevel": "NORMAL" },
    { "id": "16", "name": "통합과학2", "courseType": "COMMON", "credits": 4, "status": "IN_PROGRESS", "achievementRate": 35, "riskLevel": "RISK", "requiredAverage": 45 },
    { "id": "17", "name": "과학탐구실험2", "courseType": "COMMON", "credits": 1, "status": "IN_PROGRESS", "achievementRate": 88, "riskLevel": "NORMAL" }
  ]
}
```

### 4.4 GET /catalog?query=공통국어
표준 과목 검색. 과목명 부분 일치이며 공백은 무시한다(`확률과통계`로 `확률과 통계`를 찾는다). 최대 10개, 목록 순서대로. `query`가 비어 있거나 없으면 빈 배열.

응답 200
<!-- contract:catalog 200 -->
```json
[
  { "id": "1", "name": "공통국어1", "subjectGroup": "국어", "courseType": "COMMON", "credits": 4 },
  { "id": "2", "name": "공통국어2", "subjectGroup": "국어", "courseType": "COMMON", "credits": 4 }
]
```

### 4.5 POST /courses
과목 등록. 응답은 만든 과목 하나(`CourseItem`). 평가가 없으므로 `achievementRate`는 `null`, `riskLevel`은 `NO_DATA`(창체는 `NORMAL`).

요청 본문
| 필드 | 필수 | 설명 |
|---|---|---|
| `gradeYear`, `term` | 예 | 학년 1~3, 학기 1~2 |
| `catalogId` | 예(값은 null 가능) | 목록 과목이면 표준 과목 ID. 목록에 없는 과목(직접 입력)이면 `null` |
| `customName` | 직접 입력일 때 | 과목명 |
| `courseType` | 직접 입력일 때 | 과목 유형. 목록 과목이면 보내도 무시하고 목록 값을 쓴다 |
| `credits` | 예 | 학점. 목록 과목도 요청 값을 쓴다(학교 편성에 맞춰 바꿀 수 있음) |
| `status` | 예 | `IN_PROGRESS` / `COMPLETED`. 미래 학기도 `COMPLETED` 가능 |
| `writtenWeight`, `performanceWeight` | 예 | 지필·수행 반영 비율. 창체는 보낸 값과 관계없이 0/0으로 저장 |
| `gradeDisplay` | 창체가 아니면 예 | 성적 표기 방식. 창체는 `null`로 저장 |

- 목록 과목: 과목명과 유형은 표준 과목 값을 쓴다.
- 서버가 저장하는 값: `catalogId`(직접 입력이면 null), 과목명은 목록 과목이면 표준 과목 이름, 직접 입력이면 앞뒤 공백을 뺀 `customName`. 결석 수 0, 총 수업 수 null.

검증 (실패하면 400, 필드별 문구)
| 필드 | 조건 | 문구 |
|---|---|---|
| `term` | 학년 1~3 정수, 학기 1 또는 2 | 학기를 선택하세요. |
| `name` | 목록 과목인데 `catalogId`가 목록에 없음 | 과목 목록에서 찾을 수 없는 과목입니다. |
| `name` | 직접 입력인데 과목명이 비어 있음(공백만 포함) | 과목명을 입력하세요. |
| `name` | 같은 학년·학기에 같은 과목명이 이미 있음(공백·대소문자 무시) | 같은 학기에 이미 등록된 과목입니다. |
| `courseType` | 직접 입력인데 유형이 없거나 5개 값이 아님 | 과목 유형을 선택하세요. |
| `credits` | 1 이상 정수 | 학점은 1 이상의 정수여야 합니다. |
| `writtenWeight` | 창체가 아니면 두 비율이 0~100 정수이고 합 100 | 지필과 수행 반영 비율의 합은 100%여야 합니다. |
| `status` | `IN_PROGRESS` 또는 `COMPLETED` | 이수 상태를 선택하세요. |
| `gradeDisplay` | 창체가 아니면 3개 값 중 하나 | 성적 표기 방식을 선택하세요. |

목록 과목 등록: 요청
<!-- contract-request:register-catalog -->
```json
{
  "gradeYear": 1,
  "term": 2,
  "catalogId": "17",
  "credits": 4,
  "status": "IN_PROGRESS",
  "writtenWeight": 60,
  "performanceWeight": 40,
  "gradeDisplay": "ACHIEVEMENT_AND_RANK"
}
```

응답 201
<!-- contract:register-catalog 201 -->
```json
{
  "id": "21",
  "name": "확률과 통계",
  "courseType": "GENERAL",
  "credits": 4,
  "status": "IN_PROGRESS",
  "achievementRate": null,
  "riskLevel": "NO_DATA"
}
```

목록에 없는 과목(직접 입력) 등록: 요청
<!-- contract-request:register-custom -->
```json
{
  "gradeYear": 2,
  "term": 1,
  "catalogId": null,
  "customName": "AI 기초",
  "courseType": "CAREER",
  "credits": 2,
  "status": "IN_PROGRESS",
  "writtenWeight": 0,
  "performanceWeight": 100,
  "gradeDisplay": "THREE_LEVEL"
}
```

응답 201
<!-- contract:register-custom 201 -->
```json
{
  "id": "22",
  "name": "AI 기초",
  "courseType": "CAREER",
  "credits": 2,
  "status": "IN_PROGRESS",
  "achievementRate": null,
  "riskLevel": "NO_DATA"
}
```

같은 학기에 이미 있는 과목(공통국어2) 등록: 요청
<!-- contract-request:register-duplicate -->
```json
{
  "gradeYear": 1,
  "term": 2,
  "catalogId": "2",
  "credits": 4,
  "status": "IN_PROGRESS",
  "writtenWeight": 60,
  "performanceWeight": 40,
  "gradeDisplay": "ACHIEVEMENT_AND_RANK"
}
```

응답 400
<!-- contract:register-duplicate 400 -->
```json
{
  "message": "입력값을 확인하세요.",
  "fieldErrors": { "name": "같은 학기에 이미 등록된 과목입니다." }
}
```

여러 필드가 틀린 경우: 요청
<!-- contract-request:register-invalid -->
```json
{
  "gradeYear": 1,
  "term": 2,
  "catalogId": null,
  "customName": "",
  "credits": 0,
  "status": "IN_PROGRESS",
  "writtenWeight": 60,
  "performanceWeight": 50,
  "gradeDisplay": "ACHIEVEMENT_AND_RANK"
}
```

응답 400
<!-- contract:register-invalid 400 -->
```json
{
  "message": "입력값을 확인하세요.",
  "fieldErrors": { "courseType": "과목 유형을 선택하세요.", "name": "과목명을 입력하세요.", "credits": "학점은 1 이상의 정수여야 합니다.", "writtenWeight": "지필과 수행 반영 비율의 합은 100%여야 합니다." }
}
```

### 4.6 GET /courses/{courseId}
과목 상세와 평가 목록. 점수 입력 패널이 쓴다. 없는 과목은 404.

응답 200 (통합과학2)
<!-- contract:course-detail 200 -->
```json
{
  "id": "16",
  "name": "통합과학2",
  "courseType": "COMMON",
  "credits": 4,
  "status": "IN_PROGRESS",
  "achievementRate": 35,
  "riskLevel": "RISK",
  "requiredAverage": 45,
  "gradeYear": 1,
  "term": 2,
  "writtenWeight": 60,
  "performanceWeight": 40,
  "assessments": [
    { "id": "37", "kind": "WRITTEN", "title": "중간고사", "weightPercent": 30, "maxScore": 100, "score": 25, "evaluatedOn": "2026-10-20", "taskId": null },
    { "id": "38", "kind": "PERFORMANCE", "title": "실험 보고서", "weightPercent": 20, "maxScore": 100, "score": 50, "evaluatedOn": "2026-10-06", "taskId": null }
  ]
}
```

없는 과목: 응답 404
<!-- contract:course-detail-not-found 404 -->
```json
{
  "message": "과목을 찾을 수 없습니다.",
  "fieldErrors": {}
}
```

### 4.7 PATCH /courses/{courseId}
이수 상태 변경. 본문은 `{ "status": ... }` 하나. 응답은 바뀐 과목 하나(`CourseItem`). 이수 위험·주의 과목도 제한 없이 바꿀 수 있다. 없는 과목은 404.
- 응답의 `riskLevel`, `requiredAverage`는 이수 상태와 관계없이 계산한 값이다(아래 예시처럼 이수 완료여도 `RISK`가 올 수 있음). 화면은 이수 완료 과목에 위험·주의 표시와 필요 평균 안내를 보여 주지 않는다.

| 필드 | 조건 | 문구 |
|---|---|---|
| `status` | `IN_PROGRESS` 또는 `COMPLETED` | 이수 상태를 선택하세요. |

요청 (통합과학2를 이수 완료로)
<!-- contract-request:change-status -->
```json
{
  "status": "COMPLETED"
}
```

응답 200
<!-- contract:change-status 200 -->
```json
{
  "id": "16",
  "name": "통합과학2",
  "courseType": "COMMON",
  "credits": 4,
  "status": "COMPLETED",
  "achievementRate": 35,
  "riskLevel": "RISK",
  "requiredAverage": 45
}
```

잘못된 상태 값: 요청
<!-- contract-request:change-status-invalid -->
```json
{
  "status": "DONE"
}
```

응답 400
<!-- contract:change-status-invalid 400 -->
```json
{
  "message": "입력값을 확인하세요.",
  "fieldErrors": { "status": "이수 상태를 선택하세요." }
}
```

(문서의 다음 예시들은 통합과학2를 다시 `IN_PROGRESS`로 되돌린 상태에서 호출한 것이다.)
<!-- contract:change-status-back 200 -->

### 4.8 DELETE /courses/{courseId}
과목 삭제. 그 과목의 평가도 함께 삭제한다. 성공하면 204, 본문 없음. 없는 과목(이미 삭제한 과목 포함)은 404.

`DELETE /courses/22` (4.5에서 등록한 AI 기초): 응답 204, 본문 없음
<!-- contract:delete-course 204 -->

같은 과목을 다시 삭제: 응답 404
<!-- contract:delete-course-not-found 404 -->
```json
{
  "message": "과목을 찾을 수 없습니다.",
  "fieldErrors": {}
}
```

### 4.9 POST /courses/{courseId}/assessments
평가 추가. 응답은 만든 평가 하나(`Assessment`, `taskId`는 `null`). 없는 과목은 404.

요청 본문: `kind`, `title`, `weightPercent`, `maxScore`, `score`(미입력이면 `null`), `evaluatedOn`. 저장할 때 `title`의 앞뒤 공백을 뺀다.

검증 (4.10 수정도 같음)
| 필드 | 조건 | 문구 |
|---|---|---|
| `title` | 공백만 있으면 안 됨 | 평가 이름을 입력하세요. |
| `evaluatedOn` | `YYYY-MM-DD` 형식 | 날짜를 입력하세요. |
| `maxScore` | 0보다 큼 | 만점은 0보다 커야 합니다. |
| `score` | `null`이 아니면 0 이상 | 득점은 0 이상이어야 합니다. |
| `score` | `null`이 아니면 만점 이하 | 득점은 만점(`{만점}`점) 이하여야 합니다. |
| `weightPercent` | 0보다 큼 | 반영 비율은 0보다 커야 합니다. |
| `weightPercent` | 같은 종류 평가의 반영 비율 합(수정이면 자기 자신 제외) + 이 값 ≤ 과목의 그 종류 반영 비율(`writtenWeight` 또는 `performanceWeight`) | `{지필\|수행}` 반영 비율 합이 `{한도}`%를 넘습니다. (이미 `{사용}`% 사용, 남은 `{남은}`%) |

통합과학2에 수행평가 추가: 요청
<!-- contract-request:add-assessment -->
```json
{
  "kind": "PERFORMANCE",
  "title": "수행평가 2",
  "weightPercent": 20,
  "maxScore": 100,
  "score": 80,
  "evaluatedOn": "2026-10-28"
}
```

응답 201
<!-- contract:add-assessment 201 -->
```json
{
  "id": "45",
  "kind": "PERFORMANCE",
  "title": "수행평가 2",
  "weightPercent": 20,
  "maxScore": 100,
  "score": 80,
  "evaluatedOn": "2026-10-28",
  "taskId": null
}
```

추가 후 과목 상세(성취율 47.857…% → 주의, 이수 위험이 아니라 `requiredAverage` 없음): 응답 200
<!-- contract:course-detail-after-add 200 -->
```json
{
  "id": "16",
  "name": "통합과학2",
  "courseType": "COMMON",
  "credits": 4,
  "status": "IN_PROGRESS",
  "achievementRate": 47.85714285714286,
  "riskLevel": "CAUTION",
  "gradeYear": 1,
  "term": 2,
  "writtenWeight": 60,
  "performanceWeight": 40,
  "assessments": [
    { "id": "37", "kind": "WRITTEN", "title": "중간고사", "weightPercent": 30, "maxScore": 100, "score": 25, "evaluatedOn": "2026-10-20", "taskId": null },
    { "id": "38", "kind": "PERFORMANCE", "title": "실험 보고서", "weightPercent": 20, "maxScore": 100, "score": 50, "evaluatedOn": "2026-10-06", "taskId": null },
    { "id": "45", "kind": "PERFORMANCE", "title": "수행평가 2", "weightPercent": 20, "maxScore": 100, "score": 80, "evaluatedOn": "2026-10-28", "taskId": null }
  ]
}
```

득점이 만점보다 큼: 요청
<!-- contract-request:add-assessment-invalid-score -->
```json
{
  "kind": "WRITTEN",
  "title": "기말고사",
  "weightPercent": 30,
  "maxScore": 100,
  "score": 120,
  "evaluatedOn": "2026-12-16"
}
```

응답 400
<!-- contract:add-assessment-invalid-score 400 -->
```json
{
  "message": "입력값을 확인하세요.",
  "fieldErrors": { "score": "득점은 만점(100점) 이하여야 합니다." }
}
```

수행 반영 비율 초과(통합과학2 수행 40% 중 이미 40% 사용): 요청
<!-- contract-request:add-assessment-invalid-weight -->
```json
{
  "kind": "PERFORMANCE",
  "title": "수행평가 3",
  "weightPercent": 10,
  "maxScore": 100,
  "score": null,
  "evaluatedOn": "2026-11-20"
}
```

응답 400
<!-- contract:add-assessment-invalid-weight 400 -->
```json
{
  "message": "입력값을 확인하세요.",
  "fieldErrors": { "weightPercent": "수행 반영 비율 합이 40%를 넘습니다. (이미 40% 사용, 남은 0%)" }
}
```

### 4.10 PUT /courses/{courseId}/assessments/{assessmentId}
평가 수정. 본문과 검증은 4.9와 같다. 응답은 수정한 평가 하나(`Assessment`). 없는 과목이면 404 "과목을 찾을 수 없습니다.", 그 과목의 평가가 아니면 404 "평가를 찾을 수 없습니다.".

`PUT /courses/16/assessments/45`: 요청
<!-- contract-request:update-assessment -->
```json
{
  "kind": "PERFORMANCE",
  "title": "수행평가 2",
  "weightPercent": 20,
  "maxScore": 100,
  "score": 60,
  "evaluatedOn": "2026-10-28"
}
```

응답 200
<!-- contract:update-assessment 200 -->
```json
{
  "id": "45",
  "kind": "PERFORMANCE",
  "title": "수행평가 2",
  "weightPercent": 20,
  "maxScore": 100,
  "score": 60,
  "evaluatedOn": "2026-10-28",
  "taskId": null
}
```

다른 과목(13, 공통영어2) 경로로 같은 평가 수정 `PUT /courses/13/assessments/45`: 요청
<!-- contract-request:update-assessment-not-found -->
```json
{
  "kind": "PERFORMANCE",
  "title": "수행평가 2",
  "weightPercent": 20,
  "maxScore": 100,
  "score": 60,
  "evaluatedOn": "2026-10-28"
}
```

응답 404
<!-- contract:update-assessment-not-found 404 -->
```json
{
  "message": "평가를 찾을 수 없습니다.",
  "fieldErrors": {}
}
```

### 4.11 DELETE /courses/{courseId}/assessments/{assessmentId}
평가 삭제. 성공하면 204, 본문 없음. 없는 과목, 또는 그 과목의 평가가 아니면 404.

`DELETE /courses/16/assessments/45`: 응답 204, 본문 없음
<!-- contract:delete-assessment 204 -->

## 5. 계산 규칙 (서버가 구현)

성취율, 출석률, 위험 단계, 필요 평균은 **DB에 저장하지 않고 응답할 때마다 계산한다**. 기준값은 설정값(credit_completion_rule, credit_graduation_rule)에서 읽는다.

| 설정 | 값 | 프론트 구현 위치 |
|---|---|---|
| 최소 출석률 | 66.67% (모든 유형) | `COMPLETION_RULES.minAttendanceRate` |
| 최소 성취율 | 공통 40% (그 밖 유형은 없음) | `COMPLETION_RULES.minAchievementRate` |
| 주의 상한 | 50% (최소 성취율 이상 이 값 미만이면 주의) | `COMPLETION_RULES.cautionBelowRate` |
| 졸업 학점 | 192 (교과 174 + 창체 18) | `GRADUATION_RULES` |

- **성취율(%)** = Σ(득점 ÷ 만점 × 반영비율) ÷ Σ(득점이 있는 평가의 반영비율) × 100. 득점이 `null`인 평가는 분자·분모에서 모두 뺀다. 득점이 있는 평가가 없으면 `null`. 창체는 항상 `null`.
- **출석률(%)** = (총 수업 수 − 결석 수) ÷ 총 수업 수 × 100. 총 수업 수가 없으면(null 또는 0 이하) 100%로 본다.
- **위험 단계** (위에서부터 처음 맞는 것. 성취율은 반올림하기 전 값으로 판정):
  1. 출석률 < 최소 출석률 → `RISK` (모든 유형)
  2. 창체 → `NORMAL`
  3. 성취율 `null` → `NO_DATA`
  4. 최소 성취율이 없는 유형(일반선택·진로선택·융합선택) → `NORMAL`
  5. 성취율 < 40 → `RISK`
  6. 성취율 < 50 → `CAUTION`
  7. 그 밖 → `NORMAL`
- **필요 평균(%)** = (최소 성취율 × 100 − 성취율 × 득점이 있는 평가의 반영비율 합) ÷ (100 − 득점이 있는 평가의 반영비율 합).
  - `riskLevel`이 `RISK`이고, 최소 성취율이 있는 유형(공통)이며, 성취율이 최소 성취율(40) 미만일 때만 계산해 `requiredAverage`로 넣는다. 출석률 미달로 `RISK`여도 성취율이 40 이상이면 넣지 않는다.
  - 남은 반영비율(100 − 득점이 있는 평가의 반영비율 합)이 0 이하면 넣지 않는다.
  - 100을 넘어도 그대로 넣는다(화면이 "남은 평가로는 기준 도달이 어렵습니다"로 표시).
  - 예: 득점이 있는 평가의 반영비율 합 50, 성취율 35 → (4000 − 1750) ÷ 50 = 45.
- **이수 상태와의 관계**: API는 이수 상태와 관계없이 `riskLevel`, `requiredAverage`를 계산해 돌려준다. 화면은 위험·주의 표시와 필요 평균 안내를 **수강 중인 과목에만** 보여 주고, 경고 배너(`summary.alert`)도 **수강 중인 과목만** 센다.
- 화면 표시: 성취율은 정수로 반올림, 필요 평균은 올림(“평균 N점 이상”).

구현 참고 파일(프론트, 같은 규칙을 단위 테스트로 확인함)
- frontend/src/modules/credit/domain/rules.ts: 설정값
- frontend/src/modules/credit/domain/completion.ts: 성취율, 출석률, 위험 단계, 필요 평균 (테스트 completion.test.ts, PRD 6장 테스트 케이스 7개 포함)
- frontend/src/modules/credit/domain/validation.ts: 평가 입력 검증
- frontend/src/modules/credit/domain/courseRegistration.ts: 과목 등록 검증
- frontend/src/mocks/credit.ts: 각 API의 응답 조립(요약 합계, 필수 과목 조건, 404 처리 등)

## 6. DB와의 대응 (PRD 7장)

모든 테이블은 `credit_` 접두사, PK는 BIGINT(응답에서는 문자열). 계산 값은 저장하지 않는다.

학생 구분: `credit_student_course.student_id`(문자열)로 학생을 나눈다. 평가는 과목 FK(`student_course_id`)를 따라 학생이 정해지고, 표준 과목 목록과 기준 테이블은 학생과 무관하다. 지금은 로그인이 없어 모든 API가 데모 학생 1명(고정 `student_id`)의 데이터만 읽고 쓴다. 로그인 도입 후 다른 학생의 과목·평가 ID 요청을 어떻게 처리할지는 정하지 않았다(PROGRESS.md "미정").

### 과목 (`CourseItem`, `CourseDetail`) ← credit_student_course
| 응답 필드 | 출처 |
|---|---|
| `id` | `credit_student_course.id` |
| `name` | `catalog_id`가 있으면 `credit_course_catalog.name`, 없으면 `custom_name` |
| `courseType` | `credit_student_course.course_type` (목록 과목도 등록할 때 목록 값을 복사해 저장) |
| `credits` | `credit_student_course.credits` |
| `status` | `credit_student_course.status` |
| `gradeYear`, `term` | `grade_year`, `term` |
| `writtenWeight`, `performanceWeight` | `written_weight`, `performance_weight` |
| `achievementRate` | 계산 (그 과목의 credit_assessment) |
| `riskLevel` | 계산 (성취율 + 출석률(`absent_count`, `total_sessions`) + credit_completion_rule) |
| `requiredAverage` | 계산 |
| `assessments` | `credit_assessment` (`student_course_id` = 과목 ID) |
| (응답에 없음) | `student_id`, `catalog_id`, `grade_display`, `absent_count`, `total_sessions`는 저장만 하고 지금 응답에는 내보내지 않는다 |

### 평가 (`Assessment`) ← credit_assessment
| 응답 필드 | 출처 |
|---|---|
| `id` | `id` |
| `kind` | `kind` |
| `title` | `title` |
| `weightPercent` | `weight_percent` |
| `maxScore` | `max_score` |
| `score` | `score` (NULL이면 미입력) |
| `evaluatedOn` | `evaluated_on` |
| `taskId` | `task_id` (이번 단계는 항상 NULL) |

### 표준 과목 (`CatalogItem`) ← credit_course_catalog
| 응답 필드 | 출처 |
|---|---|
| `id` | `id` |
| `name` | `name` |
| `subjectGroup` | `subject_group` |
| `courseType` | `course_type` |
| `credits` | `credits` |

### 요약 (`/summary`)
| 응답 필드 | 출처 |
|---|---|
| `graduationCredits`, `subjectCredits.required`, `creativeCredits.required` | `credit_graduation_rule.total_credits`, `subject_credits`, `creative_credits` |
| `completedCredits`, `inProgressCredits`, `subjectCredits.earned`, `creativeCredits.earned` | 계산 (그 학생의 `credit_student_course`를 `status`, `course_type`별로 `credits` 합) |
| `remainingCredits` | 계산 |
| `alert` | 계산 (수강 중 과목의 위험 단계) |

### 설정 테이블
| 테이블 | 쓰는 곳 |
|---|---|
| `credit_completion_rule` (`course_type`, `effective_year`, `min_attendance`, `min_achievement`) | 최소 출석률, 공통 최소 성취율 |
| `credit_graduation_rule` (`effective_year`, `total_credits`, `subject_credits`, `creative_credits`) | 졸업 학점 |
