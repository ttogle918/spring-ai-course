# ch14-multi-agent

```mermaid
flowchart LR
	User[사용자] --> Orchestrator(TravelOrchestrator)
	Orchestrator --> AttractionAgent[AttractionAgent]
	Orchestrator --> RestaurantAgent[RestaurantAgent]
	Orchestrator --> AccommodationAgent[AccommodationAgent]
	Orchestrator --> PlanAgent[PlanAgent]
	Orchestrator --> BudgetAgent[BudgetAgent]
	PlanAgent --> Plan[Plan DTO]
```

This module demonstrates a tool-based multi-agent orchestration pattern using Spring AI.

- **Purpose**: Coordinate multiple specialist agents (Attraction, Restaurant, Accommodation, Plan, Budget) to answer travel planning requests and generate structured travel plans.
- **Key components**: TravelOrchestrator, AttractionAgent, RestaurantAgent, AccommodationAgent, PlanAgent, BudgetAgent.
- **Patterns**: Tool-based agent methods (@Tool), SSE progress events, parallel information collection, LLM-driven parsing and entity mapping.

See the detailed docs:

- **Architecture**: [Architecture](architecture.md)
- **Agents**: [Agents reference](agents.md)
- **Run examples**: [Run & examples](run-examples.md)

Highlights:

- The orchestrator exposes tool methods that an LLM can call to delegate work to expert agents.
- Agents use curated system and user prompt templates and attempt to return JSON-serializable entities.
- The orchestrator uses an InheritableThreadLocal to propagate SSE emitters to worker threads for real-time progress.

Terminology

- `TravelOrchestrator`: 중앙 조율자(엔트리포인트) — 사용자의 질의를 파싱하고 적절한 `@Tool` 메서드(에이전트)를 호출합니다.
- `Plan`: 코드 내 DTO(여행 일정) — 문서에서는 영어 `Plan`과 한국어 `일정`을 병기합니다.
- Agent(예: `AttractionAgent`): 특정 도메인(관광지/맛집/숙소)을 담당하는 컴포넌트.

Learning notes

- Design: prefer small, single-responsibility agents and keep orchestration logic lightweight.
- Prompting: enforce strict output formats (JSON) and include repair prompts for robustness.
- Observability: log prompts/responses (mask secrets), collect token metrics, and monitor latencies.

What you should know

- Agent responsibilities: agents are domain specialists; keep their surface area minimal and return typed DTOs when possible.
- Tool-based orchestration: the orchestrator exposes `@Tool` methods so an LLM can programmatically invoke agents — design tools to be idempotent and side-effect safe.
- Concurrency & SSE: worker threads run in parallel; use `InheritableThreadLocal` carefully to propagate `SseEmitter` instances and avoid blocking I/O in threads.
- Prompt engineering: prefer short, deterministic system prompts, include examples of expected JSON, and add repair prompts for malformed outputs.
- Testing: unit-test agent logic and use integration tests that mock `ChatClient` responses for deterministic behavior.

Example walkthrough

1. User submits free-text request (e.g., "Plan a 3-day, budget-friendly trip to Jeju").
2. `TravelOrchestrator.parseUserQuery()` extracts structured `Requirements` via an LLM call.
3. Orchestrator calls agents in parallel (`AttractionAgent`, `RestaurantAgent`, `AccommodationAgent`) to collect DTOs.
4. `PlanAgent` assembles collected DTOs and requests a final `Plan` entity from the LLM.
5. `BudgetAgent` validates costs and triggers a replan if the budget is exceeded.

Run the app (example):

```bash
cd ch14-multi-agent
../gradlew bootRun
# then open http://localhost:8080/travel-multi-agent
```

Extra notes & recommended reading

- Use `ChatClient.entity(...)` to convert LLM responses directly into DTOs and centralize JSON repair logic in a helper.
- Be mindful of cost: batch or stub LLM calls in unit tests and add caching for repeated searches.
- Security: never log raw user inputs or API keys; sanitize logs and redact tokens.
- Observability: add metrics for per-agent latency and per-request token consumption to identify expensive steps.
- When extending to multiple LLMs (see `ch14-multi-agent-with-multi-llm`), abstract provider-specific parsing and rate-limiting into a small adapter layer.
