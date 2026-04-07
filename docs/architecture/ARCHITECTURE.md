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
  - `dayrun`
  - `common`
  - `config`

## 계층 책임

- `controller`
  - HTTP 입출력만 담당한다.
- `service`
  - 트랜잭션, 규칙 검증, 상태 전이를 담당한다.
- `repository`
  - 필요한 쿼리만 제공한다.
- `domain`
  - JPA 엔티티와 enum을 둔다.
- `dto`
  - 외부 응답/요청 계약을 둔다.

## 서버 정본 범위

서버는 아래를 정본으로 가진다.

- 플레이어 진행 상태
- 경제
- 인벤토리와 창고
- 도구 해금
- 업그레이드 구매
- day run
- 채집/영업/경제/창고 로그

서버는 아래를 정본으로 갖지 않는다.

- Unity 씬 위치
- 오브젝트 배치
- 위험 지대 좌표와 세부 파라미터

## 상태 전이

- `morning_explore`
- `afternoon_service`
- `settlement`

전이는 서비스 계층에서만 수행한다.

## 동시성

- 명령형 API는 player row를 `PESSIMISTIC_WRITE`로 읽는다.
- 같은 트랜잭션 안에서 inventory, storage, upgrade purchase, day run, 로그를 갱신한다.
- `@Version` 필드도 유지해 후속 확장 시 optimistic locking 여지도 남긴다.

## 테이블 주도 설계

아래 값은 코드 상수보다 DB/Flyway 시드를 우선한다.

- 레시피 가격
- 레시피 평판 보상
- 업그레이드 비용
- 포탈 조건
- 시작 상태
- 기본 해금 도구

## 절대 고정 규칙

- `players.current_phase`는 enum으로 강하게 관리한다.
- inventory slot은 서로 다른 resource 종류 수 기준이다.
- tool은 인벤토리 슬롯을 차지하지 않는다.
- `player_region_unlocks` 같은 별도 영구 해금 테이블은 만들지 않는다.
- 포탈 접근 가능 여부는 현재 상태로 계산한다.
