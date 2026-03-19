# ch14-multi-agent

```mermaid
flowchart LR
	사용자 --> 조율자[TravelOrchestrator]
	조율자 --> 관광지에이전트[AttractionAgent]
	조율자 --> 맛집에이전트[RestaurantAgent]
	조율자 --> 숙소에이전트[AccommodationAgent]
	조율자 --> 일정에이전트[PlanAgent]
	조율자 --> 예산에이전트[BudgetAgent]
	일정에이전트 --> 계획[Plan DTO]
```

이 모듈은 Spring AI를 사용한 도구 기반 멀티 에이전트 조율 패턴을 보여줍니다.

- **목적**: 여러 전문 에이전트(관광지, 맛집, 숙소, 일정, 예산)를 조율하여 여행 계획 요청에 응답하고 구조화된 여행 일정을 생성합니다.
- **핵심 구성요소**: `TravelOrchestrator`, `AttractionAgent`, `RestaurantAgent`, `AccommodationAgent`, `PlanAgent`, `BudgetAgent`.
- **사용된 패턴**: `@Tool` 기반 에이전트 메서드, SSE 진행 이벤트, 병렬 정보 수집, LLM을 이용한 파싱 및 엔티티 매핑.

상세 문서:

- **아키텍처**: [architecture_ko.md](architecture_ko.md)
- **에이전트 참고**: [agents_ko.md](agents_ko.md)
- **실행 및 예제**: [run-examples_ko.md](run-examples_ko.md)

하이라이트:

- 조율자는 LLM이 호출할 수 있는 도구 메서드를 노출하여, LLM이 적절한 전문 에이전트를 선택하도록 합니다.
- 에이전트는 시스템/사용자 프롬프트 템플릿을 사용하며, JSON 직렬화 가능한 DTO를 반환하려고 시도합니다.
- 조율자는 `InheritableThreadLocal`을 사용해 비동기 작업 스레드에 SSE 발신자를 전달하여 실시간 진행 표시를 지원합니다.

용어 정리

- `TravelOrchestrator`: 중앙 조율자 — 사용자 질의를 파싱하고 적절한 `@Tool` 메서드를 호출합니다.
- `Plan`(`일정`): 코드 내 DTO(여행 일정) — 문서에서는 `Plan`과 '일정'을 병기합니다.
- `Agent`(예: `AttractionAgent`): 특정 도메인을 담당하는 컴포넌트.

학습 포인트 요약

- 설계: 단일 책임 원칙에 따라 작은 에이전트를 구성하고, 조율자는 가벼운 오케스트레이션만 수행합니다.
- 프롬프트: 출력 포맷(JSON)을 강제하고 수리 프롬프트를 포함해 견고성을 높이세요.
- 관찰성: 프롬프트/응답(민감정보 마스킹) 로그, 토큰 메트릭, 응답 지연 모니터링을 수집하세요.

알아야 할 내용

- 에이전트 책임: 에이전트는 도메인 전문가로 설계하고 인터페이스(메서드)를 최소화하며 가능한 경우 DTO를 반환하세요.
- 도구 기반 조율: 조율자는 `@Tool`로 메서드를 노출해 LLM이 에이전트를 호출하도록 합니다. 도구는 멱등성(idempotent)과 부작용 안전성을 고려하세요.
- 동시성 및 SSE: 워커 스레드는 병렬 실행됩니다. `SseEmitter`를 `InheritableThreadLocal`로 전달할 때 블로킹 I/O를 피하세요.
- 프롬프트 엔지니어링: 짧고 결정적인 시스템 프롬프트를 선호하고, 기대하는 JSON 예시와 수리 프롬프트를 포함하세요.
- 테스트: 에이전트 로직은 단위 테스트로 검증하고 `ChatClient`는 모킹해 통합 테스트를 안정적으로 만드세요.

예제 흐름

1. 사용자가 자유 텍스트(예: "제주 3일 예산형 여행 계획")을 전송합니다.
2. `TravelOrchestrator.parseUserQuery()`가 LLM 호출로 `Requirements`를 추출합니다.
3. 조율자는 `AttractionAgent`, `RestaurantAgent`, `AccommodationAgent`를 병렬로 호출해 DTO를 수집합니다.
4. `PlanAgent`는 수집한 DTO를 조합해 LLM에 `Plan` 엔티티 생성을 요청합니다.
5. `BudgetAgent`가 비용을 검증하고 초과 시 재계획(replan)을 트리거합니다.

실행 예시:

```bash
cd ch14-multi-agent
../gradlew bootRun
# 브라우저에서 http://localhost:8080/travel-multi-agent 열기
```

추가 팁

- `ChatClient.entity(...)`를 사용해 LLM 응답을 DTO로 바로 매핑하고, JSON 수리 로직은 중앙 헬퍼로 모으세요.
- 비용 관리: 단위 테스트에서는 LLM 호출을 스텁/모킹하고, 반복 검색에 캐시를 적용하세요.
- 보안: 원문 입력이나 API 키를 로깅하지 말고 로그를 마스킹하세요.
- 관찰성: 에이전트별 지연 시간과 요청별 토큰 사용량을 계측해 비용이 큰 단계를 식별하세요.
- 다중 LLM 확장: `ch14-multi-agent-with-multi-llm`을 참고해 프로바이더 어댑터 계층(파싱/레이트리밋)을 분리하세요.
