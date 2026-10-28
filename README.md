# 고교학점제 이수 관리 (러너스하이 신규 기능)

기능 정의는 [docs/PRD.md](docs/PRD.md), 진행 현황은 [docs/PROGRESS.md](docs/PROGRESS.md), 작업 규칙은 [CLAUDE.md](CLAUDE.md)를 본다.

## 필요한 것
- Java 21
- Node 24 (npm)
- Docker (Docker Desktop)

## 실행 방법

### 1. 환경 변수 파일 만들기 (처음 한 번)
저장소 루트에서 `.env.example`을 복사해 `.env`를 만든다. `.env`는 커밋하지 않는다.
```
cp .env.example .env
```

### 2. MySQL 실행
저장소 루트에서 실행한다. MySQL 8.4가 호스트 포트 3307로 뜨고, DB 이름은 `learnershigh_credit`이다.
```
docker compose up -d
docker compose ps
```
STATUS가 `(healthy)`가 되면 준비된 것이다. 종료는 `docker compose stop`.

### 3. 백엔드 실행
`backend` 폴더에서 실행한다. Flyway 마이그레이션 경로(`../database/migrations`)가 이 위치 기준이다.
```
cd backend
./gradlew bootRun
```
- DB 접속 정보는 환경 변수 `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`로 받는다. 설정하지 않으면 `.env.example`과 같은 로컬 기본값을 쓴다.
- 다른 위치에서 실행하면 `FLYWAY_LOCATIONS`(예: `filesystem:/절대경로/database/migrations`)로 경로를 지정한다.
- 빌드와 테스트는 DB 없이 돈다: `./gradlew build`

### 4. 헬스 체크
```
curl http://localhost:8080/api/health
```
`{"status":"ok"}`가 나오면 정상이다.

### 5. 프론트 실행
`frontend` 폴더에서 실행한다.
```
cd frontend
npm install
npm run dev
```
- http://localhost:5173 으로 연다. 메뉴는 `/credit`(고교학점제 관리), `/tasks`, `/records`, `/grades`다.
- 개발 서버는 `/api` 요청을 http://localhost:8080 (백엔드)으로 넘긴다. 예: http://localhost:5173/api/health
- `VITE_USE_MOCK`은 저장소 루트의 `.env`에서 읽는다. 값이 `false`일 때만 실제 API를 쓰고, 없거나 `true`면 mock 데이터를 쓴다. 값을 바꾸면 개발 서버를 다시 시작한다.
- mock 모드에서는 백엔드 없이 화면을 볼 수 있다.
- 빌드(타입 검사 포함): `npm run build`
- 단위 테스트(Vitest, 이수 위험 계산과 mock 데이터): `npm test`
- 린트: `npm run lint`
- mock 모드의 데이터는 브라우저 메모리에만 있어서, 점수를 입력해도 새로고침하면 처음 시연 데이터로 돌아간다.
