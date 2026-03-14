# 예제 구현 (더미) — RAG 및 Agent 방식

다음 코드는 학습용 더미 구현 스니펫입니다. 실제 프로젝트에서는 보안, 비동기 처리, 예외처리, 의존성(벡터 DB, 인덱서, Spring AI SDK) 등을 반영해야 합니다.

1) RAG(검색 기반 응답) — 더미 Retriever + 서비스 통합

```java
// 간단 문서 객체
public class Document {
  private final String id;
  private final String text;
  public Document(String id, String text) { this.id = id; this.text = text; }
  public String getId() { return id; }
  public String getText() { return text; }
  @Override public String toString() { return text; }
}

// Retriever 인터페이스와 더미 구현
public interface Retriever { List<Document> search(String query, int k); }

public class DummyRetriever implements Retriever {
  private final List<Document> store;
  public DummyRetriever(List<Document> store) { this.store = store; }
  public List<Document> search(String query, int k) {
    return store.stream()
      .filter(d -> d.getText().toLowerCase().contains(query.toLowerCase()))
      .limit(k)
      .collect(Collectors.toList());
  }
}

// AiService에서 RAG 호출 예
// 의존성: DummyRetriever retriever, ChatModel chatModel
public String answerWithRag(String userQuery, Retriever retriever, ChatModel chatModel) {
  List<Document> docs = retriever.search(userQuery, 5);
  String context = docs.stream().map(Document::getText).collect(Collectors.joining("\n---\n"));

  SystemMessage system = SystemMessage.builder()
      .text("다음 컨텍스트를 참고해서 한국어로 응답하세요:\n" + context)
      .build();
  UserMessage user = UserMessage.builder().text(userQuery).build();

  Prompt prompt = Prompt.builder()
      .messages(system, user)
      .chatOptions(ChatOptions.builder().model("gpt-4o").temperature(0.2).build())
      .build();

  ChatResponse resp = chatModel.call(prompt);
  return resp.getResult().getOutput().getText();
}
```

2) Agent + Tool Calling — 더미 Tool 인터페이스와 간단한 Agent 클라이언트

```java
// 툴 인터페이스
public interface Tool { String name(); String run(String input); }

// 검색 툴(더미)
public class SearchTool implements Tool {
  private final Retriever retriever;
  public SearchTool(Retriever retriever) { this.retriever = retriever; }
  public String name() { return "search"; }
  public String run(String input) {
    List<Document> docs = retriever.search(input, 3);
    return docs.stream().map(Document::getText).collect(Collectors.joining("\n"));
  }
}

// 간단한 ConversationMemory(더미)
public class ConversationMemory {
  private final List<String> history = new ArrayList<>();
  public void addUser(String text) { history.add("USER: " + text); }
  public void addAssistant(String text) { history.add("ASSISTANT: " + text); }
  public String getContext() { return String.join("\n", history); }
}

// 더미 에이전트 클라이언트(개념적)
public class DummyAgentClient {
  private final ChatModel chatModel;
  private final List<Tool> tools;
  private final ConversationMemory memory;

  public DummyAgentClient(ChatModel chatModel, List<Tool> tools, ConversationMemory memory) {
    this.chatModel = chatModel; this.tools = tools; this.memory = memory;
  }

  // 매우 단순화된 루프: 질문 분석(키워드 매칭)→도구 호출→모델에 합성
  public String run(String userQuery) {
    memory.addUser(userQuery);

    // 예시: 'search:' 접두사가 있으면 검색 툴을 사용
    String toolOutput = "";
    if (userQuery.startsWith("search:")) {
      String q = userQuery.substring("search:".length()).trim();
      Tool search = tools.stream().filter(t -> t.name().equals("search")).findFirst().orElse(null);
      if (search != null) toolOutput = search.run(q);
    }

    // 합성 프롬프트
    SystemMessage system = SystemMessage.builder()
        .text("당신은 도우미입니다. 필요한 경우 아래 도구 출력을 참고하세요:\n" + toolOutput)
        .build();
    UserMessage user = UserMessage.builder().text(userQuery).build();

    Prompt prompt = Prompt.builder().messages(system, user)
        .chatOptions(ChatOptions.builder().model("gpt-4o").temperature(0.3).build())
        .build();

    ChatResponse resp = chatModel.call(prompt);
    String out = resp.getResult().getOutput().getText();
    memory.addAssistant(out);
    return out;
  }
}
```

참고: 위 코드는 교육용 더미 예제입니다. 실제로는 비동기(stream), 에러처리, 인증, 툴 권한관리, 입력 검증, 벡터 검색(유사도) 등을 구현해야 합니다.

파일 위치: `ch02-chat-model-api/AI_IMPLEMENTATIONS.md`
