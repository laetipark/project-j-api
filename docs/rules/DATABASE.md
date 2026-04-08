# 데이터베이스 규칙

## 기본 원칙

- MySQL 8 기준으로 설계한다.
- 스키마 변경은 Flyway migration으로만 관리한다.
- 로컬 개발과 테스트 모두 Flyway를 기준으로 맞춘다.

## 모델링 원칙

- 카탈로그 테이블은 `code` unique를 기본으로 둔다.
- 업그레이드 가능 여부처럼 동적 상태는 결과 컬럼으로 저장하지 않는다.
- `player_region_unlocks` 같은 별도 해금 테이블은 만들지 않는다.
- 레시피/재료 마스터의 정본은 DB가 아니라 Google Sheets를 사용한다.

## 현재 구조

- `players.selected_recipe_id`는 nullable 문자열 컬럼이다.
- `selected_recipe_id`는 시트 `id` 열 기반 외부 `recipeId`를 저장한다. 예: `food_001`, `food_041`
- `ingredients`는 Google Sheets `재료` 시트를 동기화한 캐시 테이블이며, `ingredient_id`를 유니크 비즈니스 키로 사용한다.
- `recipes`는 Google Sheets `레시피` 시트를 동기화한 캐시 테이블이며, `recipe_id`를 유니크 비즈니스 키로 사용한다.
- `recipe_ingredients`는 sync 결과를 정규화한 관계 테이블이며 `(recipe_id, ingredient_id)`를 유니크 키로 사용한다.
- `BaseTimeEntity`를 쓰는 테이블은 `created_at`, `updated_at`, `deleted_at`을 공통으로 가진다.
- 시트 sync에서는 `ingredient_id`, `recipe_id`가 없으면 insert, 있으면 update, 시트에서 사라지면 `deleted_at`으로 soft delete 한다.
- `player_inventory`, `player_storage`는 수량이 0이 되면 hard delete 대신 `deleted_at`으로 soft delete 한다.
- soft-deleted inventory/storage row는 같은 `(player, resource)`가 다시 생기면 revive 해서 재사용한다.
- `player_tools`는 도구 해금 상태를 저장한다.
- inventory slot 계산은 서로 다른 자원 종류 수 기준으로 수행한다.
- day run과 gather/service/economy 로그 테이블은 제거되었다.
- 레시피 재료명은 활성 `ingredients.ingredient_name`과 exact match 해야 한다.

## SQL 작성 원칙

- 이름이 있는 제약 이름을 직접 붙인다.
- 초기값과 변하지 않는 기본값은 migration seed로 관리한다.
- 게임 기획과 함께 바뀌는 값은 코드 상수보다 migration이나 시트 정본에 둔다.
