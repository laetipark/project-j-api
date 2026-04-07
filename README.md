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
2. 필요하면 `.env.development` 파일을 만들고 아래 값을 넣습니다.

```properties
JONGGU_DB_URL=jdbc:mysql://localhost:3306/jonggu_restaurant?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul&characterEncoding=utf8&connectionCollation=utf8mb4_unicode_ci
JONGGU_DB_USERNAME=root
JONGGU_DB_PASSWORD=
JONGGU_SERVER_PORT=8080
JONGGU_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173
```

3. 서버를 실행합니다.

```powershell
.\mvnw.cmd spring-boot:run
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
