# LLM 구성 메모 (한국어 번역)

```mermaid
flowchart LR
  OpenAI[openaiBuilder] --> ChatClient(OpenAI ChatClient)
  Gemini[geminiBuilder] --> ChatClient(Gemini ChatClient)
  Ollama[ollamaBuilder] --> ChatClient(Ollama ChatClient)
  note right of ChatClient: qualifier로 빌더 선택 가능
```

`LlmConfig.java`는 다음과 같은 `ChatClient.Builder` 빈을 등록합니다:

- `@Qualifier("openaiBuilder")` → OpenAI 빌더
- `@Qualifier("geminiBuilder")` → Google Gemini 빌더
- `@Qualifier("ollamaBuilder")` → Ollama 빌더
- `@Primary` (별도 qualifier 없음) → 기본 빌더(이 프로젝트에서는 OpenAI)

사용 방법

- 특정 공급자를 사용하려면 구성 요소에서 qualifier로 빌더를 주입하세요:

  - `public MyComponent(@Qualifier("geminiBuilder") ChatClient.Builder builder) { ... }`

- 빌더에서 `ChatClient`를 생성해 공급자별 클라이언트를 얻습니다:

  - `ChatClient chatClient = builder.defaultSystem(SYSTEM_PROMPT).build();`

공급자별 고려사항

- 공급자마다 반환하는 메타데이터(토큰 사용량, 모델 메타데이터 등)의 형상이 다를 수 있습니다. 토큰 계측에 의존하는 경우 공급자별로 추출 로직을 구현하거나 리플렉션 기반의 범용 정규화기를 사용하세요.
- 공급자별 자격증명(환경변수 등)을 올바르게 설정해야 합니다(OpenAI API 키, Google 자격증명, Ollama 호스트 등).
