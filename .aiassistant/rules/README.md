---
apply: always
---

# 규칙 라우터

이 파일은 `.aiassistant`용 짧은 진입점이다.

실제 프로젝트 규칙의 정본은 루트 `docs/` 디렉터리다. 작업 전에는 아래 순서로 읽는다.

1. `docs/README.md`
2. `docs/rules/HARNESS_ENGINEERING.md`
3. 작업에 맞는 세부 문서

필수 세부 문서:

- 백엔드 작업: `docs/rules/BACKEND.md`
- API/DTO 변경: `docs/rules/API.md`
- 스키마/Flyway 변경: `docs/rules/DATABASE.md`
- 테스트 추가/수정: `docs/rules/TESTING.md`
- 게임 규칙 확인: `docs/product/GAME_RULES.md`
- 커밋/브랜치 규칙: `docs/rules/GIT.md`

주의:

- 이 파일에 규칙을 중복 복사하지 않는다.
- 새 규칙은 `docs/`에 먼저 추가한다.
- 커밋 메시지는 `docs/rules/GIT.md` 정본만 따른다.
- 커밋 메시지 생성 시 `docs/rules/GIT.md`의 절대 출력 규칙과 입력 템플릿을 우선 적용한다.
