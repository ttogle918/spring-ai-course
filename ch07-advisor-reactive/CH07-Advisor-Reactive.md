# Chapter 07 (Reactive) — 모델 전·후처리 어드바이저 (요약)

이 문서는 `ch07-advisor-reactive` 모듈의 소스 코드를 기반으로 Reactive(리액티브) 환경에서의 Advisor 특징과 학습 포인트, 기존 `ch07-advisor`와의 차이점을 정리합니다.

## 개요
- 위치: `ch07-advisor-reactive` 모듈
- 핵심: Advisor는 동기/비동기 호출 전후 처리를 캡슐화합니다. Reactive 모듈은 Reactor의 `Flux`/비동기 스트림 경로를 중심으로 Advisor의 스트리밍 훅(`StreamAdvisor`)을 활용합니다.

## 주요 구성 요소
- Advisors: `AdvisorA`, `AdvisorB`, `AdvisorC` (각 파일: `src/main/java/com/example/demo/advisor`)
- 서비스: `AiService1` — `ChatClient.Builder.defaultAdvisors(...)`로 기본 Advisor 등록, `prompt().advisors(...)`로 호출 단위 Advisor 주입

## Reactive에서의 학습 포인트
- 스트리밍 훅: `StreamAdvisor.adviceStream(...)`는 `Flux<ChatClientResponse>`를 반환하여 스트리밍 응답을 비동기적으로 가로챌 수 있습니다. (`AdvisorA/AdvisorB/AdvisorC` 참고)
- 논블로킹 원칙: 스트림 경로에서는 `block()`/`blockLast()` 같은 블로킹 호출을 피해야 합니다. 블로킹은 Reactor 워크플로우를 중단시키고 성능 문제를 유발합니다.
- 백프레셔와 흐름 제어: Advisor가 반환하는 `Flux`는 소비자에게 데이터 흐름을 맞춰야 하므로, 필요 시 `onBackpressureBuffer()` 등으로 전략을 정의하세요.
- 동일한 전/후처리 패턴: `CallAdvisor` (동기 경로)와 `StreamAdvisor`(스트리밍 경로)를 동시에 구현해 호출 유형에 따라 적절히 동작하도록 구성할 수 있습니다.
- 우선순위 제어: `getOrder()`로 Advisor 체인 내 순서를 제어합니다(높은 우선순위부터 실행).

## ch07-advisor와의 차이점 요약
- 스트리밍 우선: `ch07-advisor-reactive`는 스트리밍 응답(`.stream()`)을 주요 시나리오로 다루며, `AiService1.advisorChain2`가 `Flux<String>`을 반환합니다.
- 블로킹 회피: non-reactive 모듈(`ch07-advisor`)의 `AdvisorC`는 예제에서 `flux.blockLast()`를 호출해 블로킹을 보여주지만, Reactive 모듈에서는 그러한 블로킹을 제거하고 비동기 흐름을 유지합니다.
- 경량화된 구현: Reactive 모듈의 Advisor 구현은 스트림을 즉시 반환해 소비자에게 스트리밍 데이터를 전달합니다. 이로써 낮은 지연과 높은 동시성을 기대할 수 있습니다.

## 구현상 권장사항
- 스트림 Advisor는 최대한 순수(비차단)하게 작성하세요. 부수작용이 필요하면 비동기 방식으로 처리하세요.
- 스트리밍 테스트: 다양한 메시지 크기와 지연 조건에서 Flux 흐름을 테스트해 메모리/백프레셔 이슈를 확인하세요.
- 로깅: 스트리밍에서 로깅을 남길 때는 메시지 볼륨에 주의하고, 샘플링하거나 요약 로그를 남기세요.

## 빠른 검증 체크리스트 (Reactive)
- [ ] `./gradlew :ch07-advisor-reactive:build`가 성공하는가?
- [ ] `AiService1.advisorChain2`에서 `.stream()` 호출이 `Flux<String>`을 반환하는가?
- [ ] 스트리밍 경로에서 블로킹 호출(`block`, `blockLast`)이 없는가? (코드 검토)
- [ ] 스트리밍 응답을 받아 UI(템플릿 `advisor-chain.html`)에서 실시간으로 표시되는가?
- [ ] 다양한 부하(동시 연결)에서 메모리 누수나 백프레셔 문제가 없는가?

## 참고 파일
- `AiService1.java` — 서비스 및 기본 Advisor 등록
- `AdvisorA.java`, `AdvisorB.java`, `AdvisorC.java` — Advisor 구현
- 템플릿: `src/main/resources/templates/advisor-chain.html`

---

원하시면 이 문서를 `README`로 병합하거나, 스트리밍 테스트 스크립트(간단한 Reactor 테스트 코드)를 생성해 드리겠습니다.
