# Jonggu Restaurant API

Unity 프로토타입 `Jonggu Restaurant`용 Spring Boot API 서버입니다. 서버는 플레이어 진행, 인벤토리/창고, 도구 해금, 업그레이드, 탐험, 영업을 담당합니다.

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
3. Google Sheets 연동을 쓰려면 서비스 계정 JSON을 준비하고, 해당 서비스 계정 이메일을 스프레드시트에 공유합니다.

```powershell
.\mvnw.cmd spring-boot:run
```

운영 프로필로 실행할 때는 아래처럼 `prod` 프로필을 지정합니다.

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=prod"
```

## 마이그레이션

Flyway가 애플리케이션 시작 시 자동 실행됩니다.

- 스키마 정의: [src/main/resources/db/migration/V1__init_schema.sql](/D:/project-j-api/src/main/resources/db/migration/V1__init_schema.sql)
- 초기 시드: [src/main/resources/db/migration/V2__seed_catalog.sql](/D:/project-j-api/src/main/resources/db/migration/V2__seed_catalog.sql)
- 탐험맵 갱신: [src/main/resources/db/migration/V3__exploration_map_graph.sql](/D:/project-j-api/src/main/resources/db/migration/V3__exploration_map_graph.sql)
- 현재 스키마는 처음부터 `deleted_at` 기반 soft delete, 레시피/재료 시트 정본, `recipes`/`ingredients`/`recipe_ingredients` 동기화 구조를 반영합니다.

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
- `POST /api/v1/players/{playerId}/recipes/select`
- `POST /api/v1/players/{playerId}/service/run`
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

## 탐험맵 API 계약

- `bootstrap.regions`는 `Beach`, `Sea`, `DeepForest`, `WindHill`, `Shortcut`, `AbandonedMine`을 포함합니다.
- `bootstrap.portalRules`는 `requiredToolCode`, `requiredReputation`, `requiredUpgradeCode`를 반환합니다.
- `Shortcut` 포털은 `unlock_shortcut` 구매 전 `PORTAL_UPGRADE_REQUIRED`로 막히며, `unlock_shortcut`은 `Gold 30`을 소비하는 `PORTAL_UNLOCK` 업그레이드입니다.
- player snapshot은 `purchasedUpgradeCodes`를 포함합니다.
- `Sea`의 어망 채집은 `POST /api/v1/players/{playerId}/gathers`에서 `regionCode=Sea`, `resourceCode=Fish`로 요청합니다.

## Google Sheets 카탈로그 연동

레시피와 재료는 같은 Google Spreadsheet 안의 `레시피`, `재료` 탭을 정본으로 사용합니다. 서버는 `GOOGLE_SHEETS_ID` 하나로 같은 스프레드시트를 가리키고, 필요하면 `GOOGLE_SHEETS_INGREDIENT_GID`, `GOOGLE_SHEETS_RECIPE_GID`로 각 탭 gid를 선택합니다. 서버는 시작 시 한 번 동기화하고, 이후 `Asia/Seoul` 기준 매시 `06분 02초`에 다시 가져와 DB와 메모리 캐시를 갱신합니다.

- 공통 스프레드시트: `GOOGLE_SHEETS_ID`
- 재료 탭 선택: `GOOGLE_SHEETS_INGREDIENT_SHEET_NAME=재료` 또는 `GOOGLE_SHEETS_INGREDIENT_GID`
- 레시피 탭 선택: `GOOGLE_SHEETS_RECIPE_SHEET_NAME=레시피` 또는 `GOOGLE_SHEETS_RECIPE_GID`
- `*_SHEET_NAME`이 있으면 이름 기준으로 먼저 찾고, 비어 있으면 `*_GID` 기준으로 찾습니다.
- 기본 갱신 시각: `GOOGLE_SHEETS_REFRESH_CRON`
- 조회 API: `GET /api/v1/catalog/google-sheets/recipes`
- 수동 갱신 API: `POST /api/v1/catalog/google-sheets/recipes/refresh`

시트 파싱 규칙은 아래와 같습니다.

- `재료` 시트는 `id`, `난이도`, `재료명`, `수급처`, `획득처`, `획득방식`, `획득도구`, `구매가격`, `판매가격`, `메모` 헤더를 사용합니다.
- `레시피` 시트는 `id`, `난이도`, `레시피명`, `수급처`, `조리법`, `재료 1`~`재료 7`, `가격`, `메모` 헤더를 사용합니다.
- 2행부터 `재료명` 또는 `레시피명`이 비어 있지 않은 행만 사용합니다.
- `recipeId`는 `레시피` 시트의 `id` 열 값을 서버가 그대로 사용합니다. 예: `food_001`, `food_041`
- `ingredientId`는 `재료` 시트의 `id` 열 값을 서버가 그대로 사용합니다. 예: `ingredient_001`, `ingredient_014`
- `레시피` 시트의 `재료 1`~`재료 7`은 재료명 기준으로 적고, 서버가 활성 `ingredientName`과 exact match로 resolve 합니다.
- 같은 재료가 여러 칸에 있으면 수량을 누적해 구조화된 `ingredients` 배열로 응답합니다.
- 레시피 응답의 `ingredients` 항목은 `ingredientId`, `ingredientName`, `quantity`를 포함합니다.
- `난이도`, `가격`, `구매가격`, `판매가격`은 셀 값에서 숫자만 추출합니다. 빈 값은 `0`으로 처리합니다.
- Unity 레시피 이미지 파일명은 시트 `id` 값과 정확히 일치해야 합니다.
- sync 성공 시 `ingredients`, `recipes`, `recipe_ingredients`가 DB에 upsert 되고, 시트에서 사라진 row는 `deleted_at`으로 soft delete 됩니다.

## 테스트

```powershell
.\mvnw.cmd test
```

현재 테스트는 아래 규칙을 검증합니다.

- Google Sheets 헤더 기반 레시피 매핑
- Google Sheets 헤더 기반 재료 매핑
- 시트 `id` 열 기반 `recipeId` 그대로 사용 규칙
- 시트 `id` 열 기반 `ingredientId` 그대로 사용 규칙
- `재료`/`레시피` 시트 -> DB upsert / soft delete 동기화
- 레시피 선택과 영업 계산
- inventory slot 규칙
- storage 이동
- upgrade 구매
- Beach 중심 portal 이동 조건
- Shortcut 구매 해금 조건
- Sea 어망 채집 계약
- 탐험 지역별 Hub 즉시 복귀

## 검증 포인트

- `players.selected_recipe_id`는 외래 키가 아닌 문자열 `recipeId`를 저장합니다.
- `bootstrap`은 `ingredients` 카탈로그와 구조화된 `recipes.ingredients`를 함께 반환합니다.
- `recipes`, `ingredients`, `recipe_ingredients`는 시트 정본 기준으로 upsert 되며, 시트에서 사라진 행은 `deleted_at`으로 soft delete 됩니다.
- `BaseTimeEntity`를 쓰는 테이블은 `created_at`, `updated_at`, `deleted_at`을 함께 가집니다.
- 하루 루틴과 phase 전이, day run 정산 흐름은 제거되었습니다.
- 인벤토리 슬롯은 수량 합이 아니라 서로 다른 resource 종류 수로 계산합니다.
- 도구는 `player_tools`로 관리하며 인벤토리 슬롯을 차지하지 않습니다.
- 업그레이드 가능 여부는 현재 골드, 자원, 도구, 선행 업그레이드 상태로 동적으로 계산합니다.
- `Shortcut` 해금은 별도 지역 해금 테이블 없이 `player_upgrade_purchases`의 `unlock_shortcut` 구매 상태로 계산합니다.
