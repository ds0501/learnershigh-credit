# 고교학점제 이수 관리 (러너스하이 신규 기능)

기능 정의는 [docs/PRD.md](docs/PRD.md), 진행 현황은 [docs/PROGRESS.md](docs/PROGRESS.md), 작업 규칙은 [CLAUDE.md](CLAUDE.md)를 본다.

## 필요한 것
- Java 21
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
