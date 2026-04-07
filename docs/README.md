# 프로젝트 규칙 인덱스

이 디렉터리는 `D:\project-j-api`의 규칙 정본이다.

하네스 엔지니어링 원칙에 따라:

- 짧은 진입 파일은 지도 역할만 한다.
- 실제 규칙과 설계 판단은 `docs/` 아래 문서에 둔다.
- 규칙은 주제별로 분리하고, 작업에 필요한 문서만 선택해서 읽는다.
- 코드보다 오래 남아야 하는 판단 기준은 문서에 먼저 기록한다.

## 읽는 순서

1. `docs/README.md`
2. `docs/rules/HARNESS_ENGINEERING.md`
3. 작업 성격에 맞는 세부 문서

## 문서 지도

- `docs/architecture/ARCHITECTURE.md`
  - 현재 서버 구조, 패키지 책임, 트랜잭션 경계, 동시성 기준
- `docs/product/GAME_RULES.md`
  - Jonggu Restaurant 서버 정본 규칙과 게임 도메인 제약
- `docs/rules/BACKEND.md`
  - Java/Spring 구현 규칙
- `docs/rules/API.md`
  - API 설계와 DTO/응답 규칙
- `docs/rules/DATABASE.md`
  - Flyway, MySQL, 엔티티, 카탈로그/상태 저장 원칙
- `docs/rules/TESTING.md`
  - 테스트 우선순위와 검증 기준
- `docs/rules/GIT.md`
  - 브랜치, 커밋, 커밋 메시지 규칙
- `docs/rules/HARNESS_ENGINEERING.md`
  - 에이전트 친화적 작업 방식과 문서 운영 방식

## 우선순위

규칙 충돌 시 우선순위는 아래와 같다.

1. 사용자의 현재 명시 요청
2. `docs/product/GAME_RULES.md`
3. `docs/architecture/ARCHITECTURE.md`
4. `docs/rules/*.md`
5. 루트의 짧은 라우터 파일들 (`AGENTS.md`, `CLAUDE.md`, `.aiassistant/rules/README.md`)

## 유지 원칙

- 새 규칙이 생기면 먼저 `docs/`에 넣고 라우터 파일은 링크만 갱신한다.
- 구현 세부보다 판단 기준을 기록한다.
- 오래된 규칙은 조용히 방치하지 말고 삭제하거나 대체 문서를 연결한다.
