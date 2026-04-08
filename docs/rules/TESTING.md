# 테스트 규칙

## 기본 원칙

- 규칙이 있는 기능은 통합 테스트를 우선한다.
- H2 MySQL 호환 모드와 Flyway로 테스트 환경을 맞춘다.
- 상태 변화는 snapshot이나 저장소 조회로 검증한다.

## 최소 검증 대상

- Google Sheets 헤더 기반 레시피 매핑
- Google Sheets 헤더 기반 재료 매핑
- 시트 `id` 열 값을 `recipeId`로 그대로 사용하는 규칙
- 시트 `id` 열 값을 `ingredientId`로 그대로 사용하는 규칙
- `재료`/`레시피` 시트 -> DB upsert / soft delete 동기화
- 레시피 선택과 영업 계산
- inventory slot 규칙
- storage 이동
- upgrade 구매
- portal 이동 조건

## 테스트 작성 원칙

- 각 테스트는 한 규칙을 명확하게 드러내야 한다.
- 반환값 자체보다 계산 규칙을 검증한다.
- 외부 Google API를 직접 호출하지 말고 고정 fixture `RecipeCatalogService`로 대체한다.
- 가능한 한 end-to-end에 가까운 서비스 호출로 검증한다.
