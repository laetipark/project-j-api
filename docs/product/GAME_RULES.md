# Jonggu Restaurant 서버 규칙

## 하루 루프

1. 허브에서 준비
2. 오전 탐험
3. 허브 복귀
4. 메뉴 선택
5. 영업 진행
6. 정산
7. 다음 날 진행

## phase 규칙

- `morning_explore`
- `afternoon_service`
- `settlement`

## 지역

- `Hub`
- `Beach`
- `DeepForest`
- `AbandonedMine`
- `WindHill`

## 자원

- `Fish`
- `Shell`
- `Seaweed`
- `Mushroom`
- `Herb`
- `GlowMoss`
- `WindHerb`

## 도구

- `Rake`
- `FishingRod`
- `Sickle`
- `Lantern`

## 핵심 도메인 규칙

- 도구는 영구 해금형이며 인벤토리 슬롯을 차지하지 않는다.
- 인벤토리는 재료 전용이며 기본 8 슬롯이다.
- 창고는 `Hub`에서만 사용한다.
- `serviceCapacity` 기본값은 3이다.
- `AbandonedMine` 진입에는 `Lantern`이 필요하다.
- `WindHillShortcut`은 평판 6 이상일 때만 연다.
- 업그레이드 가능 여부는 저장된 status가 아니라 현재 골드, 재료, 도구, 인벤토리 상태로 계산한다.
- 외부 API 식별자는 DB 숫자 ID보다 `code`를 우선 사용한다.

## V1 시드 기준

- 초기 카탈로그는 소규모 핵심 세트로 유지한다.
- 이후 확장 값은 같은 스키마에 시드 추가로 반영한다.
- 레시피 가격과 평판 보상은 테이블 컬럼으로 조정 가능해야 한다.
