# Architecture

```mermaid
flowchart TD
	subgraph Orchestrator
		O(TravelOrchestrator)
	end
	O --> A(AttractionAgent)
	O --> R(RestaurantAgent)
	O --> C(AccommodationAgent)
	O --> P(PlanAgent)
	O --> B(BudgetAgent)
	A -->|search| Internet[InternetSearchService]
	R -->|search| Internet
	C -->|search| Internet
	P --> Plan[Plan DTO]
```

Overview

- The system uses a central orchestrator (`TravelOrchestrator`) that exposes tool methods. An LLM interacting with the orchestrator can choose which tool (agent) to invoke.
- Each agent is a focused component responsible for one domain (attractions, restaurants, accommodations, planning, budget analysis).
- Agents call external services (e.g., `InternetSearchService`) and return typed DTOs. The orchestrator assembles these DTOs into a `Plan`.

Key flows

1. Parse user request: `TravelOrchestrator.parseUserQuery()` uses an LLM to extract structured requirements (`Requirements`) from free text.
2. Collect info in parallel: `collectTravelInfoInParallel()` runs Attraction/Restaurant/Accommodation agents concurrently, propagating SSE emitters to worker threads.
3. Plan generation: `PlanAgent.execute()` builds a single-plan prompt and asks the LLM to return a `Plan` entity.
4. Budget analysis and replan: `BudgetAgent.execute()` validates costs and triggers `replanWithAdjustedBudget()` when needed.

Integration points

- ChatClient: built via a `ChatClient.Builder` and used to call LLMs, map responses to entities, or collect textual content.
- Tools: methods annotated with `@Tool` allow the LLM to select and call agents programmatically.
- SSE: `SseEmitter` events are emitted during agent execution for UI progress updates.
