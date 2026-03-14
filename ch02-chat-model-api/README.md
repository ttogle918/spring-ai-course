# ch02 — Chat Model API

이 챕터는 간단한 채팅형 인터페이스와 이를 뒷받침하는 REST/스트리밍 API를 구현하며, 클라이언트와 서버 간의 데이터 흐름과 비동기 스트리밍 처리 방법을 익히는 것을 목표로 합니다.

핵심 포인트
- **REST API 설계**: 컨트롤러와 DTO(요청/응답 모델)의 설계 원칙
- **서비스 계층**: 비즈니스 로직을 분리하여 테스트 가능하게 만드는 방법
- **템플릿 연동**: `chat-model.html`, `chat-model-stream.html` 을 통해 서버 렌더링 UI와 실시간 스트리밍 UI 비교 학습
- **스트리밍 처리**: 서버에서 클라이언트로 연속적으로 데이터를 전달하는 패턴(SSE, WebSocket 또는 Reactive Streams 개념)
- **외부 모델 연동**: (교재 예시) 외부 챗 모델이나 API와 통합하는 기본 패턴 — 비동기 호출, 타임아웃, 에러 처리
- **테스트**: 컨트롤러와 서비스 단위 테스트 작성법

프로젝트에서 살펴볼 위치
- 컨트롤러: `src/main/java/.../controller` — API 엔드포인트 정의
- 서비스: `src/main/java/.../service` — 모델 호출 및 스트리밍 제어
- 템플릿: `src/main/resources/templates` — `chat-model.html`, `chat-model-stream.html`
- 정적 자원: `src/main/resources/static` — JS로 스트리밍/페칭 처리

빠른 실행
- 개발 모드: `gradlew.bat bootRun` (Windows) 또는 `./gradlew bootRun`
- API 호출 확인: 브라우저에서 `http://localhost:8080/chat-model` 또는 `http://localhost:8080/chat-model-stream`

학습 포인트(이 챕터에서 강조하는 내용)
- 동기 vs 비동기 응답의 차이와 사용자 경험(UX) 관점의 설계
- 스트리밍 응답의 구현 방식과 클라이언트 처리
- 외부 모델/서비스를 호출할 때의 안정성(타임아웃, 재시도, 폴백)

참고
- 스트리밍 구현은 프로젝트에 따라 SSE, WebSocket, 혹은 Reactive WebFlux 중 적절한 선택을 해야 합니다. 본 챕터는 개념 이해와 예제 중심입니다.
