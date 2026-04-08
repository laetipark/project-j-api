# API 규칙

## 공개 식별자

- 외부 API는 DB 숫자 ID 대신 공개 식별자를 우선 사용한다.
- `/players/{playerId}`는 UUID 문자열을 사용한다.
- 레시피는 `recipeId`를 사용하며, 값은 시트 `id` 열 값을 서버가 그대로 사용한 문자열이다. 예: `food_001`, `food_041`
- 재료는 `ingredientId`를 사용하며, 값은 시트 `id` 열 값을 서버가 그대로 사용한 문자열이다. 예: `ingredient_001`, `ingredient_014`
- DB 숫자 ID는 외부 계약으로 노출하지 않는다.

## 응답 형식

- 모든 응답은 `ApiResponse<T>` envelope를 사용한다.
- 성공 시 `success=true`, `data` 채움, `error=null`
- 실패 시 `success=false`, `data=null`, `error` 채움

## DTO 규칙

- Unity가 한 번에 그리기 쉬운 snapshot 중심 응답을 우선한다.
- 버튼 활성화나 즉시 계산이 필요한 값은 DTO에 포함한다.
- 내부 엔티티를 그대로 응답하지 않는다.

## 필드/제약 설계

- 지역 제약과 허브 제약은 서비스 계층에서 강하게 검증한다.
- phase, day run, next-day 흐름은 더 이상 API 계약에 포함하지 않는다.
- 레시피 선택 요청은 `recipeId`를 받는다.
- 영업 실행 응답은 `recipeId`와 갱신된 snapshot을 함께 반환한다.
- 실패 가능한 액션은 이유가 명확한 에러 코드와 메시지를 반환한다.

## Google Sheets 계약

- `GET /api/v1/catalog/google-sheets/recipes`는 현재 메모리 캐시 스냅샷을 반환한다.
- `POST /api/v1/catalog/google-sheets/recipes/refresh`는 `재료`/`레시피` 시트 재동기화를 즉시 시도하고 새 스냅샷을 반환한다.
- `bootstrap`은 `ingredients` 목록을 함께 반환한다.
- 재료 목록 응답 항목은 `ingredientId`, `ingredientName`, `difficulty`, `supplySource`, `acquisitionSource`, `acquisitionMethod`, `acquisitionTool`, `buyPrice`, `sellPrice`, `memo`를 포함한다.
- 레시피 목록 응답은 `recipeId`, `recipeName`, `supplySource`, `difficulty`, `cookingMethod`, `ingredients`, `price`, `memo`를 포함한다.
- 레시피 `ingredients` 항목은 `ingredientId`, `ingredientName`, `quantity` 구조를 사용한다.
