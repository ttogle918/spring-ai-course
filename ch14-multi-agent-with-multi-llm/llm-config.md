# LLM configuration notes

```mermaid
flowchart LR
  OpenAI[openaiBuilder] --> ChatClient(OpenAI ChatClient)
  Gemini[geminiBuilder] --> ChatClient(Gemini ChatClient)
  Ollama[ollamaBuilder] --> ChatClient(Ollama ChatClient)
  note right of ChatClient: Qualifier로 빌더 선택 가능
```

`LlmConfig.java` registers multiple `ChatClient.Builder` beans:

- `@Qualifier("openaiBuilder")` → OpenAI builder
- `@Qualifier("geminiBuilder")` → Google Gemini builder
- `@Qualifier("ollamaBuilder")` → Ollama builder
- `@Primary` (no qualifier) → default builder (OpenAI in this project)

How to use

- To target a specific provider inside a component, inject the builder with the qualifier:

  - `public MyComponent(@Qualifier("geminiBuilder") ChatClient.Builder builder) { ... }`

- Build a `ChatClient` from the builder when you want to use that provider:

  - `ChatClient chatClient = builder.defaultSystem(SYSTEM_PROMPT).build();`

Provider considerations

- Different providers may return different metadata shapes (token usage, model names). If you rely on token accounting, implement provider-specific extraction or keep a best-effort reflection-based normalizer.
- Credentials and environment variables must be set per-provider (OpenAI API key, Google credentials, Ollama host, etc.).
