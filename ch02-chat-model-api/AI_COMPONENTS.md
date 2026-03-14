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

7) `ChatClient` vs `ChatModel` — 언제 무엇을 쓸까?
- 요약: `ChatModel`은 순수 모델 호출(LLM에 prompt 보내고 응답 받기)에 집중한 추상화입니다. 반면 `ChatClient`는 대화 상태 관리(메모리), 도구 호출(tool calling), 에이전트/플로우 조정(RAG, 햅틱 등)을 포함한 상위 레벨의 클라이언트 역할을 합니다.

- `ChatModel` 사용 시나리오
  - 단일 질의-응답(질문을 보내고 바로 응답을 받음)
  - 간단한 동기/스트리밍 호출: `call()` / `stream()`
  - 테스트에서 모킹하여 서비스 레이어의 동작 검증

- `ChatClient` 사용 시나리오
  - RAG(검색 기반 응답) 파이프라인: 검색 결과를 컨텍스트로 결합하고 모델에 전달
  - 대화 기억(Conversation Memory): 과거 메시지/상태 유지, 세션 관리
  - Tool Calling: 외부 도구(검색, 계산기, DB 쿼리 등)를 호출하고 그 결과를 모델과 합성
  - 에이전트 패턴: `create_agent()` 같은 고수준 API로 도구+메모리+정책을 묶어 복잡한 흐름을 자동화

- 왜 `ChatClient`가 필요한가
  - 단순 모델 호출만으로는 복잡한 멀티스텝 작업(검색→정제→응답)이나 외부 도구 연계, 상태 관리(대화 히스토리 유지)를 구현하기 어렵습니다.
  - `ChatClient`는 이러한 책임을 담당해 서비스 레이어를 단순화합니다.

8) LangChain / LangGraph 비교(간단 요약)
- 역할 비교
  - LangChain: 파이썬/JS 기반의 에이전트 프레임워크로, `use_model()` 수준의 모델 호출 기능과 `create_agent()` 수준의 에이전트/툴/메모리 통합 기능을 모두 제공합니다. RAG, 체인(Chains), 에이전트(Agents) 개념이 풍부합니다.
  - LangGraph: 그래프 기반의 워크플로우/오케스트레이션 툴로, 여러 컴포넌트(LLM, 도구, 변환기)를 노드로 연결해 복잡한 파이프라인을 시각적으로 설계할 수 있습니다.

- Spring AI (ChatModel/ChatClient)와의 매핑
  - `ChatModel` ≒ LangChain의 `use_model()` (단일 모델 호출 추상화)
  - `ChatClient` ≒ LangChain의 `create_agent()` / 에이전트(도구+메모리+정책 통합)
  - LangGraph는 오케스트레이션/시각적 워크플로우 관점에서 보완적이며, 복잡한 파이프라인을 설계할 때 유용

- 선택 가이드
  - 단순 Q&A나 프로토타입: `ChatModel`로 빠르게 시작
  - RAG, 장기 대화 메모리, 도구 호출이 필요한 고도화된 서비스: `ChatClient` 또는 LangChain 스타일의 에이전트 사용 권장
  - 파이프라인을 시각적으로 설계하거나 여러 서비스 연동이 많다면 LangGraph 같은 오케스트레이터 고려

9) 빠른 예시(개념)
- ChatModel (간단 호출)
  - `chatModel.call(prompt)` → 텍스트 응답

- ChatClient (에이전트/메모리/도구 포함)
  - `chatClient.createConversation(sessionId)` → conversation context 관리
  - `chatClient.send(userMessage)` → 내부에서 메모리 적용, 필요시 검색/도구 호출, 모델 호출을 조합하여 응답 반환

학습 체크포인트
- `ChatModel`으로 시작해 스트리밍/동기 호출 패턴을 익히기
- `ChatClient` 또는 에이전트 패턴으로 RAG와 도구 호출을 구현해보기
- LangChain 예제를 살펴보고 동일한 워크플로우를 Spring 기반으로 재구성해보기

10) LangChain 정식버전(Agent 중심) 변화와 의미

지난해 하반기 LangChain이 정식(주요) 릴리스를 내며 에이전트(Agent) 인터페이스를 더욱 표준화했고, `create_agent()`와 같은 상위 레벨 API로 툴(tool) 관리를 중앙화하는 방향으로 발전했습니다. 주요 변화와 의미는 다음과 같습니다.

- 과거(정식 이전):
  - 개발자는 모델 호출(`use_model()` 등)과 툴 호출(검색, 계산기, 외부 API 등)을 직접 연결하거나, Chains(체인)이나 Prompt 템플릿으로 수작업으로 조합하는 경우가 많았습니다.
  - 툴 호출 로직(언제 어떤 툴을 호출할지), 응답 합성, 예외 처리 등의 책임이 앱 코드에 분산되어 유지보수가 어려웠습니다.

- 정식 버전 이후(Agent 중심):
  - `create_agent()` 같은 고수준 API로 툴 등록, 라우팅 정책, 메모리, 안전성 제어(allowed tools) 등을 에이전트에 위임할 수 있게 되었습니다.
  - 에이전트는 입력을 해석하고 내부 정책에 따라 적절한 툴을 호출한 뒤, 툴 결과를 모델과 재조합해서 최종 응답을 생성합니다. 즉 도구 호출과 모델 호출을 명확히 분리하면서도 통합된 실행 흐름을 제공합니다.
  - 결과적으로 개발자는 각 툴을 구현하고 에이전트에 등록하면, 에이전트가 멀티스텝(검색→정제→응답) 흐름을 자동으로 관리합니다.

실무적 장점
- 중앙화된 툴 관리로 보안/권한 통제, 사용량 로깅, 정책 적용이 쉬워졌습니다.
- 에이전트 레벨에서 재시도, 타임아웃, 안전 필터링 등을 일관되게 적용할 수 있습니다.

11) LangChain 정식버전 이전 vs 이후의 차이(요약)
- 이전: 도구 통합이 분산, 체인/프롬프트 중심의 수동 오케스트레이션
- 이후: `create_agent()`로 툴 등록·관리·정책 중심의 자동 오케스트레이션

12) Spring AI와 LangChain의 유사성 및 차이

상세 설명
- 공통점
  - 두 접근법 모두 모델 호출을 추상화하는 레이어(`use_model()`/`ChatModel`)와, 더 높은 수준의 오케스트레이션(에이전트/클라이언트)을 갖추려는 설계 철학을 공유합니다.
  - RAG, 대화 메모리, 툴 호출 같은 고급 패턴을 지원하려는 목표가 같습니다.

- 차이점 (중요 포인트)
  - 런타임 환경 및 언어
    - LangChain: 주로 Python/JavaScript 에코시스템에서 풍부한 오픈소스 커넥터와 도구들을 제공.
    - Spring AI: Java/Spring 기반으로, 기존 스프링 애플리케이션 아키텍처(의존성 주입, 빈, Reactor)와 자연스럽게 통합됩니다.

  - 에코시스템과 커넥터
    - LangChain: 문서 로더(document loaders), 벡터 DB 커넥터, 다양한 서드파티 통합이 풍부합니다.
    - Spring AI: Spring 생태계(데이터, 보안, 트랜잭션 등)와의 통합이 강점이며, LangChain만큼의 커넥터 생태계는 아직 확장 중일 수 있습니다.

  - 개발 패턴
    - LangChain: 스크립트/파이프라인 중심, 연구·프로토타입에 적합한 빠른 실험성이 강점
    - Spring AI: 엔터프라이즈 수준의 애플리케이션에 쉽게 내재화시키기 좋고, 타입 안전성과 DI로 유지보수가 쉬움

  - 에이전트/툴 관리 방식
    - LangChain(정식): `create_agent()`로 툴을 중앙 등록하고 에이전트 정책으로 관리하는 기능이 강력
    - Spring AI: `ChatClient`나 유사한 상위 레벨 컴포넌트로 같은 패턴을 구현할 수 있으나, 구현 방식(예: API명, 툴 인터페이스)은 Spring AI 버전과 구현체에 따라 다름

  - 오케스트레이션 & 시각화
    - LangGraph: 그래프 기반 워크플로우 설계와 시각화에 특화되어 복잡한 파이프라인을 시각적으로 구성하기 쉬움
    - Spring AI: 코드 기반 구성으로 IDE와 CI/CD 파이프라인에 잘 녹아듭니다. 필요하면 LangGraph 같은 오케스트레이터와 연동할 수 있습니다.

비교 표(간단)

| 항목 | LangChain (정식 이전) | LangChain (정식/Agent 중심) | Spring AI (`ChatModel` / `ChatClient`) | LangGraph |
|---|---:|---:|---:|---:|
| 툴 관리 | 분산(수동 통합) | 중앙화된 `create_agent()` | `ChatClient`로 구현 가능하나 프레임워크별 차이 | 오케스트레이션/시각화 중심 |
| RAG 지원 | 체인/수동 결합 | 에이전트 내부 정책으로 통합 가능 | 서비스 레이어에서 구현(검색→프롬프트 결합) | 워크플로우에서 시각적 조합 가능 |
| 메모리/상태 관리 | 개발자 구현(체인) | 에이전트가 메모리 통합 지원 | `ChatClient`나 별도 컴포넌트로 구현(스프링 빈) | 상태 있는 파이프라인 표현 가능 |
| 에코시스템 | 풍부한 커넥터(파이썬 중심) | 더 표준화된 에이전트/플러그인 | Spring 에코(데이터·보안·DI) 강점 | 각종 노드로 서드파티 연결 가능 |
| 언어/런타임 | Python/JS 중심 | Python/JS 중심 | Java/Spring (Reactor 지원) | 언어 불문(오케스트레이터) |
| 적합 사례 | 빠른 프로토타이핑, 연구 | 제품화된 에이전트, 툴 중심 워크플로우 | 엔터프라이즈 앱, 스프링 서비스 통합 | 복잡 파이프라인 시각화·운영 |

요약 권고
- 빠르게 에이전트/툴 아키텍처를 실험하고 싶다면 LangChain(정식)에 있는 `create_agent()` 패턴을 학습하는 것이 좋습니다.
- 스프링 애플리케이션에 자연스럽게 통합하고 운영·유지보수가 목적이라면 `ChatClient` 기반의 구현을 Spring 스타일로 구성하는 편이 안정적입니다.
- LangGraph는 복잡한 오케스트레이션을 시각적으로 구성해야 할 때 보완적으로 고려하세요.

업데이트 완료: LangChain 정식 버전 변화 및 Spring AI 비교(자세한 설명 + 표)를 추가했습니다.

10) 예제 코드 — RAG(검색 기반 응답) 흐름 (ChatClient 사용 예, 개념적)

// 개념: 검색기(retriever)로 문서/스니펫을 가져와서 prompt에 합쳐 모델에 전달
```java
// 의존성 주입된 컴포넌트(예시)
@Autowired
private ChatClient chatClient; // 상위 레벨 클라이언트
@Autowired
private Retriever retriever; // 검색/인덱스 조회 컴포넌트

public String answerWithRag(String userQuery) {
  // 1) 검색
  List<Document> docs = retriever.search(userQuery, 5);

  // 2) 검색 결과를 컨텍스트로 결합
  String context = docs.stream()
    .map(Document::getText)
    .collect(Collectors.joining("\n---\n"));

  // 3) 시스템/사용자 메시지 생성
  SystemMessage system = SystemMessage.builder()
    .text("다음 컨텍스트를 참고해서 한국어로 응답하세요:\n" + context)
    .build();

  UserMessage user = UserMessage.builder().text(userQuery).build();

  // 4) Prompt와 옵션 만들기
  Prompt prompt = Prompt.builder()
    .messages(system, user)
    .chatOptions(ChatOptions.builder().model("gpt-4o").temperature(0.2).build())
    .build();

  // 5) ChatClient를 통해 호출 (ChatClient는 내부에서 메모리/RAG 정책을 적용할 수 있음)
  ChatResponse resp = chatClient.call(prompt);
  return resp.getResult().getOutput().getText();
}
```

11) 예제 코드 — Agent + Tool Calling (ChatClient에 툴 등록하여 자동 도구 호출)

// 개념: 외부 툴(검색, 계산기 등)을 에이전트에 등록하면 ChatClient가 필요시 도구를 호출
```java
// 툴 인터페이스(간단화된 예)
public interface Tool {
  String name();
  String run(String input);
}

// 예: 검색 툴 구현
public class SearchTool implements Tool {
  private final Retriever retriever;
  public SearchTool(Retriever retriever) { this.retriever = retriever; }
  public String name() { return "search"; }
  public String run(String input) { return retriever.search(input, 3).toString(); }
}

// 에이전트 빌드 및 실행(개념)
Tool searchTool = new SearchTool(retriever);
ChatClient agentClient = ChatClient.builder()
  .chatModel(chatModel)          // 하위 모델
  .tools(List.of(searchTool))    // 도구 등록
  .memory(new ConversationMemory())
  .build();

// 사용 예
String userQuestion = "서울 근처에서 채식 식당 추천해줘";
// agentClient는 내부 정책에 따라 필요시 searchTool을 호출하고 결과를 합성
AgentResult result = agentClient.createAgent().run(userQuestion);
String answer = result.getText();
```

참고: 위 코드는 개념적 스니펫입니다 — 실제 API(메서드/클래스명)는 사용 중인 Spring AI 버전과 라이브러리에 따라 다를 수 있으므로, 프로젝트의 SDK 문서를 참고해 API 호환 코드를 작성하세요.
