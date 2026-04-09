# 백엔드 아키텍처

## 현재 구조

- 루트 패키지: `com.projectj.api`
- 도메인 패키지:
  - `catalog`
  - `player`
  - `exploration`
  - `restaurant`
  - `storage`
  - `upgrade`
  - `common`
  - `config`

## 계층 책임

- `controller`
  - HTTP 입출력만 담당한다.
- `service`
  - 트랜잭션, 규칙 검증, 상태 전이를 담당한다.
- `repository`
  - 필요한 조회와 저장만 담당한다.
- `domain`
  - JPA 엔티티와 enum을 둔다.
- `dto`
  - 외부 요청과 응답 계약을 둔다.

## 서버 정본 범위

서버는 아래를 정본으로 가진다.

- 플레이어 상태
- 경제
- 인벤토리와 창고
- 도구 해금
- 업그레이드 구매 상태
- 지역 이동 규칙과 채집 규칙
- 자원 카탈로그

레시피와 재료 정본은 DB가 아니라 Google Sheets `레시피`, `재료` 시트다.

서버가 정본으로 갖지 않는 것은 아래와 같다.

- Unity 씬 배치
- 이미지 파일 배치
- 레시피 썸네일 리소스 자체

## 카탈로그 경계

- DB/Flyway가 관리하는 스키마
  - tools
  - regions
  - resources
  - ingredients
  - recipes
  - recipe_ingredients
  - resource_gather_rules
  - portal_rules
  - upgrades
  - upgrade_resource_costs
  - game_settings
- Google Sheets가 관리하는 정본 데이터
  - ingredient rows
  - recipe rows
  - recipe ingredient names
  - ingredient / recipe metadata

## 시트 동기화 구조

- Google Sheets `재료` 시트를 먼저 읽는다.
- 2행부터 `재료명`이 비어 있지 않은 행만 사용한다.
- `ingredientId`는 `재료` 시트의 `id` 열 값을 그대로 사용한다.
- Google Sheets `레시피` 시트를 읽는다.
- 2행부터 `레시피명`이 비어 있지 않은 행만 사용한다.
- `recipeId`는 `레시피` 시트의 `id` 열 값을 그대로 사용한다. 예: `food_001`, `food_041`
- `레시피` 시트의 `재료 1`~`재료 7`은 재료명 기준으로 읽고, 활성 `ingredients.ingredient_name`과 exact match 해야 한다.
- 같은 재료가 여러 칸에 있으면 수량을 누적하고, 첫 등장 순서 기준으로 구조화된 `ingredients` 배열을 만든다.
- sync 성공 시 `ingredients`, `recipes`, `recipe_ingredients`를 같은 트랜잭션으로 upsert 하고, 누락된 row는 `deleted_at`으로 soft delete 한다.
- 메모리 캐시는 DB sync가 성공한 뒤에만 새 스냅샷으로 교체한다.
- Google Sheets 인증과 HTTP 조회는 `GoogleSheetsCatalogClient`가 담당하고, 캐시/검증/도메인 매핑/DB sync는 `GoogleSheetsRecipeCatalogService`가 담당한다.

## 동시성

- 명령형 API는 player row를 `PESSIMISTIC_WRITE`로 잠근다.
- 같은 트랜잭션 안에서 inventory, storage, upgrade purchase, service 결과를 함께 갱신한다.
- 레시피 캐시는 읽기 전용 스냅샷으로 유지하고, 갱신 시 새 스냅샷으로 교체한다.

## 현재 고정 규칙

- 하루 루프와 phase 상태는 더 이상 서버 모델에 존재하지 않는다.
- `selected_recipe_id`는 DB 외래 키가 아닌 문자열 `recipeId`를 저장한다.
- `recipes` 테이블은 시트 정본을 upsert 해 캐시하고, 시트에 없는 `recipe_id`는 `deleted_at`으로 soft delete 한다.
- `ingredients`와 `recipe_ingredients`도 시트 정본을 upsert 해 캐시하고, 시트에 없는 row는 `deleted_at`으로 soft delete 한다.
- `BaseTimeEntity`를 쓰는 테이블은 `created_at`, `updated_at`, `deleted_at`을 공통으로 가진다.
- inventory slot 계산은 서로 다른 resource code 개수를 기준으로 한다.
- tool은 인벤토리 수량이 아니라 `player_tools`로 관리한다.
- portal 진입 가능 여부는 현재 지역, 도구, 평판으로 동적으로 계산한다.
- 레시피 재료명은 활성 `ingredients.ingredient_name`과 exact match 해야 한다.
- 영업 계산은 아직 `ingredientName -> resource.name` 브리지로 기존 `resources` 기반 소비 흐름을 유지한다.
- 창고 이동은 현재 snapshot 상태만 갱신하며 별도 `storage_logs` 정본이나 감사 로그 테이블을 두지 않는다.
- Unity 문서의 `Fishing Rod`, `Glow Moss`, `Wind Herb` 같은 표시명은 서버 코드값 `FishingRod`, `GlowMoss`, `WindHerb`와 구분한다.
