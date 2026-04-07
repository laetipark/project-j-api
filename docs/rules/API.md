# API 규칙

## 식별자

- 외부 API는 `code` 중심 식별자를 우선한다.
- `/players/{playerId}`는 공개 UUID 문자열을 사용한다.
- DB 숫자 ID는 외부 계약에 노출하지 않는다.

## 응답 형식

- 모든 응답은 `ApiResponse<T>` envelope를 사용한다.
- 성공 시 `success=true`, `data` 채움, `error=null`
- 실패 시 `success=false`, `data=null`, `error` 채움

## DTO 규칙

- Unity가 한 번에 화면을 그릴 수 있게 snapshot 응답을 우선 설계한다.
- 버튼 활성화 여부처럼 클라이언트가 즉시 써야 하는 계산값은 DTO로 내려준다.
- 내부 엔티티를 그대로 응답하지 않는다.

## 엔드포인트 설계 원칙

- phase와 지역 제약은 서비스 계층에서 강하게 검증한다.
- 실패 가능한 액션은 이유가 명확한 에러 코드나 결과 메시지를 남긴다.
- 로그성 데이터는 가능한 한 append-only로 쌓는다.
