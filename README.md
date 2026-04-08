# Jonggu Restaurant API

Unity 프로토타입 `Jonggu Restaurant`용 Spring Boot API 서버입니다. 서버는 플레이어 진행, 인벤토리/창고, 도구 해금, 업그레이드, 하루 루프, 채집/영업/경제 로그를 담당합니다.

## 기술 스택

- Java 21
- Spring Boot 3.5.x
- Maven
- Spring Web
- Spring Data JPA
- MySQL 8
- Flyway
- Bean Validation
- springdoc-openapi
- JUnit 5 + Spring Boot Test

## 실행 방법

1. MySQL 8을 준비합니다.
2. 개발 실행은 루트의 `.env.development`, 운영 실행은 `.env.production`을 사용합니다.
   샘플은 `.env.development.sample`, `.env.production.sample`을 참고하면 됩니다.
   환경 변수 키와 예시 값은 각 `.sample` 파일에만 둡니다.

`GOOGLE_CREDENTIALS_PATH`는 OAuth 클라이언트 시크릿이 아니라 서비스 계정 JSON 파일 경로여야 하며, 해당 서비스 계정 이메일을 스프레드시트에 공유해야 합니다.

3. 개발 서버를 실행합니다.

```powershell
.\mvnw.cmd spring-boot:run
```

운영 프로필로 실행할 때는 아래처럼 `prod` 프로필을 지정합니다.

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=prod"
```

## 마이그레이션 방법

Flyway는 애플리케이션 시작 시 자동 실행됩니다.

- 스키마 정의: [src/main/resources/db/migration/V1__init_schema.sql](/D:/project-j-api/src/main/resources/db/migration/V1__init_schema.sql)
- 초기 시드: [src/main/resources/db/migration/V2__seed_catalog.sql](/D:/project-j-api/src/main/resources/db/migration/V2__seed_catalog.sql)

테이블 주도 값은 레시피 가격, 평판 보상, 포탈 조건, 업그레이드 비용, 시작 상태를 포함하며 이후 SQL/Flyway 수정으로 조정할 수 있습니다.

## Swagger

- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- OpenAPI JSON: [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

## 주요 엔드포인트

- `GET /api/v1/bootstrap`
- `GET /api/v1/catalog/google-sheets/recipes`
- `POST /api/v1/catalog/google-sheets/recipes/refresh`
- `POST /api/v1/players`
- `GET /api/v1/players/{playerId}/snapshot`
- `POST /api/v1/players/{playerId}/travel`
- `POST /api/v1/players/{playerId}/gathers`
- `POST /api/v1/players/{playerId}/exploration/skip`
- `POST /api/v1/players/{playerId}/recipes/select`
- `POST /api/v1/players/{playerId}/service/run`
- `POST /api/v1/players/{playerId}/service/skip`
- `POST /api/v1/players/{playerId}/day/next`
- `POST /api/v1/players/{playerId}/storage/deposit`
- `POST /api/v1/players/{playerId}/storage/withdraw`
- `POST /api/v1/players/{playerId}/upgrades/{upgradeCode}/purchase`

## 응답 형식

모든 응답은 아래 공통 envelope를 사용합니다.

```json
{
  "success": true,
  "data": {},
  "error": null,
  "timestamp": "2026-04-08T12:00:00Z"
}
```

실패 시 `error.code`, `error.message`, `error.fieldErrors`를 함께 반환합니다.

## Google Sheets 레시피 연동

레시피 시트는 백그라운드에서 주기적으로 가져오고 마지막 성공 스냅샷을 API로 제공합니다. 필요하면 수동 갱신 API로 즉시 다시 읽어올 수 있습니다.

- 대상 시트: `GOOGLE_SHEETS_RECIPE_GID` 또는 `GOOGLE_SHEETS_RECIPE_SHEET_NAME`
- 기본 크롤링 시각: 매시 `06분 02초` (`GOOGLE_SHEETS_REFRESH_CRON=2 6 * * * *`, Asia/Seoul)
- 조회 API: `GET /api/v1/catalog/google-sheets/recipes`
- 수동 갱신 API: `POST /api/v1/catalog/google-sheets/recipes/refresh`

응답에는 아래 값이 포함됩니다.

- 시트 탭 정보 (`sheetGid`, `sheetTitle`)
- 마지막 동기화 시각 (`syncedAt`)
- A-K 열의 레시피 기본 정보와 재료 목록
- AO-AP 열의 가격/메모

## 테스트

```powershell
.\mvnw.cmd test
```

현재 통합 테스트는 아래 규칙을 검증합니다.

- phase 전이
- service 계산
- inventory slot 규칙
- storage 이동
- upgrade 구매
- portal 접근 조건

## 검증 포인트

- `players.current_phase`는 enum으로 강하게 관리됩니다.
- 인벤토리 슬롯은 수량 합이 아니라 서로 다른 resource 종류 수로 계산됩니다.
- 도구는 `player_tools`에 영구 해금되며 인벤토리 슬롯을 차지하지 않습니다.
- 업그레이드 가능 여부는 저장된 status 없이 현재 상태로 동적 계산됩니다.
- `AbandonedMine`은 `Lantern`, `WindHillShortcut`은 `reputation >= 6` 조건을 사용합니다.
- 명령형 API는 player row를 `PESSIMISTIC_WRITE`로 읽어 동시 요청 충돌을 1차 방어합니다.
