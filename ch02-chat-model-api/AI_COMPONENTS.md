# ch02 — AI 구성 요소 학습 노트

이 문서는 `Flux`, `ChatModel`, `ChatOptions` 등 본 챕터에서 자주 쓰이는 핵심 컴포넌트의 개념, 대표 특징, 설정 항목과 사용 용도를 정리합니다.

1) Reactor `Flux` (reactor.core.publisher.Flux)
- 목적: 0..N개의 비동기 스트림 데이터를 표현하는 Reactive 타입
- 대표 특징: 비동기, 논블로킹, 스트리밍(데이터 청크 단위 전송), 연산자(map, flatMap, filter 등)
- 사용 용도: 서버에서 클라이언트로 연속적으로 발생하는 이벤트(LLM 토큰 스트리밍, SSE, WebFlux 응답) 처리
- 주의사항: 백프레셔(처리 속도 차이 관리), 에러 처리(onErrorResume 등), 구독 시점에 실행이 시작됨(cold vs hot)
- 실무 팁: 스트리밍 텍스트는 `Flux<String>`으로 모델 응답 청크를 매핑하고, 클라이언트는 chunk 단위로 렌더링한다.

2) `ChatModel` (Spring AI의 추상화)
- 목적: 챗형 LLM 엔진(외부 모델)과 상호작용하는 추상화 레이어
- 대표 메서드
  - `call(Prompt)` : 전체 응답을 동기/단일 응답으로 반환(포괄적 응답이 필요할 때)
  - `stream(Prompt)` : `Flux<ChatResponse>` 형태로 스트리밍 응답을 반환(토큰/청크 단위)
- 사용 용도: LLM 호출 추상화, 테스트 더블(모킹) 적용이 쉬움
- 구현/설정 팁: 실제 엔드포인트(Provider)에 따라 타임아웃/리트라이/인증을 서비스 레이어에서 처리

3) `ChatOptions` (프롬프트 실행 옵션)
- 목적: 모델 동작을 제어하는 옵션 집합
- 자주 쓰는 옵션(예시)
  - `model` : 사용할 모델 이름(gpt-4o-mini, gpt-4o, gpt-5-mini 등)
  - `temperature` : 생성의 무작위성(0.0~1.0, 낮을수록 결정적)
  - `maxTokens` : 응답 토큰 상한(생성 길이 제어)
  - `top_p`, `presence_penalty`, `frequency_penalty` : 샘플링/반복 제어 (모델 및 SDK가 지원하면 사용)
- 사용 용도: 서비스 요구(요약, 대화, 코드 생성 등)에 맞추어 옵션을 조정하여 답변 스타일을 튜닝
- 실무 팁: 디버깅 시 `temperature=0.0`으로 안정적 결과를 확인한 뒤, UX 목적에 따라 값을 높게 둠

4) `Prompt` / `ChatResponse` / 메시지 타입
- `Prompt` : `SystemMessage`, `UserMessage` 등 메시지의 순서와 `ChatOptions`를 묶음
- `ChatResponse` : 모델 응답을 감싼 객체(메타데이터, 토큰 스트리밍, 최종 텍스트 등 포함)
- 메시지 타입: `SystemMessage`(지시문), `UserMessage`(사용자 입력), `AssistantMessage`(모델 응답)

5) 활용 패턴 예시
- 동기 응답: `chatModel.call(prompt)` → 컨트롤러에서 바로 결과 반환 (간단한 질의응답)
- 스트리밍 응답: `chatModel.stream(prompt)` → `Flux<ChatResponse>` map으로 텍스트 청크 추출 → `Flux<String>`을 SSE/응답 바디로 전달
- 에러/타임아웃: 외부 호출 래퍼에서 타임아웃, 재시도, 폴백 처리(예: `Mono.timeout`, `retryBackoff`) 권장

6) 학습 체크리스트
- Reactor 기초: `Flux` vs `Mono`, 주요 연산자, subscribe 흐름 이해
- 스트리밍 설계: cold vs hot publisher, 백프레셔 대응 방법
- Spring AI 추상화: `ChatModel.call()`과 `ChatModel.stream()`의 차이 이해
- 옵션 튜닝: `temperature`, `maxTokens`가 결과에 미치는 영향 실험
- 테스트: `ChatModel`을 모킹하여 서비스 계층 단위 테스트 작성

참고 링크
- Reactor 공식 문서: https://projectreactor.io/docs
- Spring AI (사용 중인 버전) 문서: 프로젝트 환경의 의존성 문서 참고

파일 위치: 이 파일은 `ch02-chat-model-api/AI_COMPONENTS.md`에 저장되어 있습니다.
