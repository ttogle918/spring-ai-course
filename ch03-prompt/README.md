# ch03-prompt: 프롬프트 기법 정리

이 문서에서는 `src/main/java/com/example/demo/service` 폴더에 있는 각 서비스가 어떤 프롬프트 기법을 사용하는지 설명합니다. 각 항목에는 기법의 개요, 동작 방식, 장단점, 언제 사용하면 좋은지, 간단한 예시를 포함합니다.

**참고 파일 목록**
- [AiServiceZeroShotPrompt.java](src/main/java/com/example/demo/service/AiServiceZeroShotPrompt.java)
- [AiServiceFewShotPrompt.java](src/main/java/com/example/demo/service/AiServiceFewShotPrompt.java)
- [AiServicePromptTemplate.java](src/main/java/com/example/demo/service/AiServicePromptTemplate.java)
- [AiServiceRoleAssignmentPrompt.java](src/main/java/com/example/demo/service/AiServiceRoleAssignmentPrompt.java)
- [AiServiceMultiMessages.java](src/main/java/com/example/demo/service/AiServiceMultiMessages.java)
- [AiServiceDefaultMethod.java](src/main/java/com/example/demo/service/AiServiceDefaultMethod.java)
- [AiServiceChainOfThoughtPrompt.java](src/main/java/com/example/demo/service/AiServiceChainOfThoughtPrompt.java)
- [AiServiceStepBackPrompt.java](src/main/java/com/example/demo/service/AiServiceStepBackPrompt.java)
- [AiServiceSelfConsistency.java](src/main/java/com/example/demo/service/AiServiceSelfConsistency.java)

**AiServiceZeroShotPrompt**: 제로샷 프롬프트
- **개요:** 사전 예시 없이 단일 지시문(프롬프트)으로 모델에 답변을 요청합니다.
- **동작 방식:** `PromptTemplate`에 분류 지침과 변수(`{review}`)를 넣고, 온도(`temperature`)를 0으로 설정해 결정적인 답변을 요청합니다.
- **장점:** 간단하고 빠르며 추가 맥락(예시)이 필요 없음.
- **단점:** 복잡한 태스크나 모호한 지시문에서는 성능이 낮을 수 있음.
- **사용 시기:** 명확한 규칙(예: 레이블 반환)이 있고, 적은 토큰으로 빠른 결과를 원할 때.
- **파일:** [AiServiceZeroShotPrompt.java](src/main/java/com/example/demo/service/AiServiceZeroShotPrompt.java)

**AiServiceFewShotPrompt**: 퓨샷(Few-shot) 프롬프트
- **개요:** 입력과 함께 몇 개의 예시(prompt→response 페어)를 제공해 모델에게 형식과 기대 출력을 보여줍니다.
- **동작 방식:** 프롬프트에 여러 예시(JSON 변환 예시)를 포함한 뒤, 마지막에 실제 입력을 붙여 모델이 동일한 형식으로 응답하도록 유도합니다. 온도를 0으로 고정해 예측을 안정화합니다.
- **장점:** 모델에게 원하는 출력 형식을 명확히 보여줄 수 있어 정형화된 출력(JSON 등)에 강함.
- **단점:** 예시가 길어지면 토큰 비용이 증가하고, 예시가 잘못되면 편향된 출력이 나올 수 있음.
- **사용 시기:** 특정 출력 포맷(예: JSON)을 안정적으로 얻고 싶을 때.
- **파일:** [AiServiceFewShotPrompt.java](src/main/java/com/example/demo/service/AiServiceFewShotPrompt.java)

**AiServicePromptTemplate**: 프롬프트 템플릿과 시스템/사용자 메시지 구성
- **개요:** `PromptTemplate`과 `SystemPromptTemplate`을 사용해 메시지(시스템/사용자)를 구조화하고, 여러 방식으로 조합하여 LLM에 전달합니다.
- **동작 방식:** 템플릿을 미리 정의하고 `render` 또는 `createMessage`를 통해 값 바인딩을 수행한 뒤, `chatClient.prompt()`에 메시지로 전달합니다. 시스템 메시지로 역할·출력 형식(HTML/CSS 등)을 강제할 수 있습니다.
- **장점:** 재사용 가능한 템플릿으로 여러 엔드포인트에서 일관된 프롬프트를 적용할 수 있고, 시스템 메시지로 모델 행동을 제어하기 쉽습니다.
- **단점:** 템플릿 관리가 복잡해질 수 있으며, 동적 값 처리에 주의해야 합니다.
- **사용 시기:** 일관된 스타일/형식을 유지하면서 다양한 입력을 처리해야 할 때.
- **파일:** [AiServicePromptTemplate.java](src/main/java/com/example/demo/service/AiServicePromptTemplate.java)

**AiServiceRoleAssignmentPrompt**: 시스템 메시지를 이용한 역할 부여
- **개요:** 시스템 메시지를 사용하여 모델에 특정 역할(예: 여행 가이드)을 부여하고, 그 역할에 맞춘 응답을 스트리밍으로 받습니다.
- **동작 방식:** `.system(...)`에 역할/행동 지침을 넣고 `.user(...)`로 실제 요청을 전달합니다. 결과는 `stream()`으로 받아 스트리밍 처리합니다.
- **장점:** 역할 기반 응답 생성이 명확해지고, 대화 성격을 제어하기 쉽습니다. 스트리밍으로 빠르게 부분 응답을 처리 가능.
- **단점:** 시스템 메시지가 너무 구체적이면 모델의 창의성이 억제될 수 있음.
- **사용 시기:** 명확한 역할을 모델에 부여하고자 할 때(예: 도메인 전문가 흉내).
- **파일:** [AiServiceRoleAssignmentPrompt.java](src/main/java/com/example/demo/service/AiServiceRoleAssignmentPrompt.java)

**AiServiceMultiMessages**: 다중 메시지(대화 메모리) 관리
- **개요:** 이전 대화(메시지 리스트)를 그대로 모델에 전달하여 문맥을 유지하면서 응답을 얻는 방식입니다.
- **동작 방식:** `SystemMessage`, `UserMessage`, `AssistantMessage` 등 메시지 객체 목록을 `prompt().messages(...)`로 전달 후 `call()`로 동기 응답을 얻습니다. 응답은 대화 메모리에 저장해 다음 요청에 재사용합니다.
- **장점:** 사용자와의 이어지는 대화를 자연스럽게 처리할 수 있으며, 장기 문맥 유지에 유리합니다.
- **단점:** 문맥 길이가 길어질수록 토큰 사용량이 증가하고, 오래된 문맥이 방해가 될 수 있음.
- **사용 시기:** 채팅 애플리케이션이나 상태 유지가 필요한 대화형 시스템.
- **파일:** [AiServiceMultiMessages.java](src/main/java/com/example/demo/service/AiServiceMultiMessages.java)

**AiServiceDefaultMethod**: 기본 시스템 메시지 및 기본 옵션 설정
- **개요:** `ChatClient.Builder`에서 `defaultSystem`과 `defaultOptions`를 설정해 모든 요청에 공통 동작(예: 말투)과 기본 옵션(온도, maxTokens)을 적용하는 방식입니다.
- **동작 방식:** 빌더 수준에서 기본 시스템 메시지(친절한 톤 등)와 옵션을 지정해 `chatClient`를 생성한 뒤, 개별 요청에서는 최소한의 입력만 넘깁니다.
- **장점:** 애플리케이션 전반의 일관된 응답 톤과 기본 옵션을 중앙에서 관리할 수 있습니다.
- **단점:** 특정 요청만 다르게 동작시키려면 별도 옵션을 덮어써야 함.
- **사용 시기:** 전반적인 톤/스타일을 고정하고 싶은 경우.
- **파일:** [AiServiceDefaultMethod.java](src/main/java/com/example/demo/service/AiServiceDefaultMethod.java)

**AiServiceChainOfThoughtPrompt**: Chain-of-Thought (사고의 흐름 유도)
- **개요:** 문제를 푸는 과정을 단계별로 설명하도록 모델에게 유도하여 논리적 추론을 얻는 기법입니다.
- **동작 방식:** 프롬프트에 "한 걸음씩 생각해 봅시다." 같은 지침과 간단한 예시(추론 과정 포함)를 넣어 모델이 내부 사고(중간 계산)를 출력하게 합니다. 스트리밍으로 중간 결과를 받아 처리할 수도 있습니다.
- **장점:** 복잡한 추론 문제에서 정확도를 크게 향상시킬 수 있습니다.
- **단점:** 모델이 중간 사고를 노출하면 때때로 잘못된 추론도 길게 설명할 수 있으며, 출력 토큰이 늘어납니다.
- **사용 시기:** 계산, 논리 추론, 단계적 사고가 필요한 문제.
- **파일:** [AiServiceChainOfThoughtPrompt.java](src/main/java/com/example/demo/service/AiServiceChainOfThoughtPrompt.java)

**AiServiceStepBackPrompt**: Step-Back(문제 분해) 프롬프트
- **개요:** 사용자의 복잡한 질문을 여러 단계의 하위 질문으로 재구성한 뒤, 단계별로 답을 구해 최종 답을 얻는 기법입니다.
- **동작 방식:** 모델에 원 질문을 주고 여러 단계의 질문 리스트(JSON 배열)를 반환하도록 요청합니다. 반환된 단계별 질문을 순차적으로 다시 LLM에 물어 최종 답을 도출합니다. 이전 단계 답변을 문맥으로 제공해 누적적 추론을 수행합니다.
- **장점:** 복잡한 문제를 구조화하고 단계적으로 해결할 수 있어 어려운 질의에 강합니다.
- **단점:** 여러 번의 모델 호출로 비용과 지연이 증가합니다. 단계 생성이 부정확하면 전체 흐름이 깨질 수 있습니다.
- **사용 시기:** 긴 추론 과제, 복잡한 의사결정 지원.
- **파일:** [AiServiceStepBackPrompt.java](src/main/java/com/example/demo/service/AiServiceStepBackPrompt.java)

**AiServiceSelfConsistency**: Self-Consistency (여러 샘플링으로 다수결)
- **개요:** 동일한 질의에 대해 여러 번(샘플링) 모델 응답을 받아 다수결로 최종 결과를 결정하는 불확실성 완화 기법입니다.
- **동작 방식:** 같은 프롬프트를 여러 번(예: 5회) 호출하되 온도 값을 높게 설정해 다양한 응답을 얻습니다. 각 응답을 집계해 가장 많이 나온 선택을 최종 결과로 채택합니다.
- **장점:** 단일 샘플의 우연한 오류를 완화하고 더 안정적인 결정을 얻을 수 있습니다.
- **단점:** 여러 요청으로 비용과 지연이 증가합니다. 선택지가 미세하게 다를 경우 합의 도출이 어려울 수 있음.
- **사용 시기:** 분류/결정 문제에서 단일 답의 신뢰도가 낮을 때.
- **파일:** [AiServiceSelfConsistency.java](src/main/java/com/example/demo/service/AiServiceSelfConsistency.java)

---

- 각 기법에 대해 간단한 테스트 케이스(입력/기대 출력)를 `src/test`에 추가, 직접 실험하면서 차이를 확인
 
벤치마크 실행
- 이 저장소에는 간단한 벤치마크 러너 `PromptBenchmarks`(Spring `CommandLineRunner`)가 추가되어 있습니다.
- 실행 조건: 기본적으로 자동 실행되지 않습니다. 벤치마크를 실행하려면 JVM 시스템 프로퍼티 `runBenchmarks`를 설정하세요. (선택적으로 `runs`로 반복 횟수 지정)

예: 루트에서 (Windows)
```
.\gradlew.bat bootRun -DrunBenchmarks=true -Druns=3 -DoutputFile=build/bench-results.json
```
예: 루트에서 (Unix / Git Bash)
```
./gradlew :ch03-prompt:bootRun -DrunBenchmarks=true -Druns=3 -DoutputFile=ch03-prompt/build/bench-results.json

```

이 벤치마크는 각 서비스에 대해 동일한(내부 정의된) 샘플 입력을 사용해 응답 지연(밀리초)을 여러 번 측정하고 평균을 출력합니다. 출력은 콘솔에 출력되며 각 실행별 간단한 프리뷰를 보여줍니다.

주의사항
- 벤치마크는 실제 LLM 호출을 수행하므로 네트워크 및 사용량(비용)에 영향을 줄 수 있습니다. 안전하게 테스트하려면 `runs`를 1로 낮추고 로컬 환경 또는 테스트 계정을 사용하세요.

실행 방법(권장)

1) 권장: JAR을 만들어 직접 JVM 시스템 프로퍼티로 실행 (간단·확실)

Unix / Git Bash (모듈 디렉터리에서):
```
./gradlew bootJar
java -DrunBenchmarks=true -Druns=1 -DoutputFile=build/bench-results.json -jar build/libs/*.jar
```

Windows (PowerShell / cmd):
```
.\gradlew.bat bootJar
java -DrunBenchmarks=true -Druns=1 -DoutputFile=build/bench-results.json -jar build/libs\*.jar
```

2) 대안: Gradle `bootRun`으로 실행할 때 애플리케이션 JVM에 시스템 프로퍼티 전달

Gradle이 실행하는 JVM에 `-D`를 바로 전달하면 애플리케이션 JVM에는 전달되지 않습니다. `bootRun`으로 실행하면서 애플리케이션 JVM에 전달하려면 `spring-boot.run.jvmArguments`에 JVM 인수를 지정하세요.

Unix / Git Bash (모듈 디렉터리에서):
```
./gradlew bootRun -Dspring-boot.run.jvmArguments='-DrunBenchmarks=true -Druns=1 -DoutputFile=build/bench-results.json'
```

Windows (PowerShell / cmd):
```
.\gradlew.bat bootRun -Dspring-boot.run.jvmArguments="-DrunBenchmarks=true -Druns=1 -DoutputFile=build/bench-results.json"
```

또는 워크스페이스 루트에서 서브프로젝트를 지정할 때:
```
./gradlew :ch03-prompt:bootRun -Dspring-boot.run.jvmArguments='-DrunBenchmarks=true -Druns=1 -DoutputFile=ch03-prompt/build/bench-results.json'
```

주의사항
- `runs`는 JVM 시스템 프로퍼티로 전달해야 하므로 `application.properties`가 아니라 위와 같이 JVM 인수로 넘겨야 합니다.
- `outputFile`에 적은 경로는 `ch03-prompt` 모듈 기준입니다(예: `build/bench-results.json`).
- 안전 테스트: 먼저 `-Druns=1`으로 실행하여 LLM 호출 수/비용을 최소화하세요.


원하시면 `src/test` 기반 JUnit 테스트 사례 또는 더 정교한 CSV/JSON 결과 출력(토큰 사용량 포함)이 필요할 경우 추가로 구현해 드리겠습니다.

```
INFO  c.e.d.service.AiServiceMultiMessages.multiMessages(): [SystemMessage{textContent='    당신은 AI 비서입니다.
    제공되는 지난 대화 내용을 보고 우선적으로 답변해주세요.
', messageType=SYSTEM, metadata={messageType=SYSTEM}}]
INFO  c.e.d.service.AiServiceMultiMessages.multiMessages(): [SystemMessage{textContent='    당신은 AI 비서입니다.
    제공되는 지난 대화 내용을 보고 우선적으로 답변해주세요.
', messageType=SYSTEM, metadata={messageType=SYSTEM}}, UserMessage{content='지금부터 너의 이름을 자비스라고 부를게.', metadata={messageType=USER}, messageType=USER}, AssistantMessage [messageType=ASSISTANT, toolCalls=[], textContent=좋아요! 자비스라고 불러주시면 좋습니다. 어떻게 도와드릴까요?, metadata={role=ASSISTANT, messageType=ASSISTANT, refusal=, finishReason=STOP, index=0, annotations=[], id=chatcmpl-DJUZ6tb3ZexydLGnx8BQCTNQ8tr32}]]  
INFO  c.e.d.service.AiServiceMultiMessages.multiMessages(): [SystemMessage{textContent='    당신은 AI 비서입니다.
    제공되는 지난 대화 내용을 보고 우선적으로 답변해주세요.
', messageType=SYSTEM, metadata={messageType=SYSTEM}}, UserMessage{content='지금부터 너의 이름을 자비스라고 부를게.', metadata={messageType=USER}, messageType=USER}, AssistantMessage [messageType=ASSISTANT, toolCalls=[], textContent=좋아요! 자비스라고 불러주시면 좋습니다. 어떻게 도와드릴까요?, metadata={role=ASSISTANT, messageType=ASSISTANT, refusal=, finishReason=STOP, index=0, annotations=[], id=chatcmpl-DJUZ6tb3ZexydLGnx8BQCTNQ8tr32}], UserMessage{content='스타벅스에서 아메리카노를 마시려고해', metadata={messageType=USER}, messageType=USER}, AssistantMessage [messageType=ASSISTANT, toolCalls=[], textContent=좋은 선택이에요! 스타벅스의 아메리카노는 진한 커피 맛이 매력적이죠. 추가적으로 원하는 사이즈나 특별한 요청이 있나요? 예를 들어, 우유나 시럽을 추가하고 싶으신가요?, metadata={role=ASSISTANT, messageType=ASSISTANT, refusal=, finishReason=STOP, index=0, annotations=[], id=chatcmpl-DJUZKoWjHjtSrMR7PDtIgy7BmNniV}]]
INFO  c.e.d.service.AiServiceMultiMessages.multiMessages(): [SystemMessage{textContent='    당신은 AI 비서입니다.
    제공되는 지난 대화 내용을 보고 우선적으로 답변해주세요.
', messageType=SYSTEM, metadata={messageType=SYSTEM}}, UserMessage{content='지금부터 너의 이름을 자비스라고 부를게.', metadata={messageType=USER}, messageType=USER}, AssistantMessage [messageType=ASSISTANT, toolCalls=[], textContent=좋아요! 자비스라고 불러주시면 좋습니다. 어떻게 도와드릴까요?, metadata={role=ASSISTANT, messageType=ASSISTANT, refusal=, finishReason=STOP, index=0, annotations=[], id=chatcmpl-DJUZ6tb3ZexydLGnx8BQCTNQ8tr32}], UserMessage{content='스타벅스에서 아메리카노를 마시려고해', metadata={messageType=USER}, messageType=USER}, AssistantMessage [messageType=ASSISTANT, toolCalls=[], textContent=좋은 선택이에요! 스타벅스의 아메리카노는 진한 커피 맛이 매력적이죠. 추가적으로 원하는 사이즈나 특별한 요청이 있나요? 예를 들어, 우유나 시럽을 추가하고 싶으신가요?, metadata={role=ASSISTANT, messageType=ASSISTANT, refusal=, finishReason=STOP, index=0, annotations=[], id=chatcmpl-DJUZKoWjHjtSrMR7PDtIgy7BmNniV}], UserMessage{content='아침에 뭐를 한다고 했었지?', metadata={messageType=USER}, messageType=USER}, AssistantMessage [messageType=ASSISTANT, toolCalls=[], textContent=이전에 나눈 대 화 내용을 확인할 수는 없지만, 아침에 무엇을 하려는지 말씀해 주시면 그에 맞춰 도와드릴 수 있어요! 아침 계획이나 일정을 말씀해 주시면 좋을 것 같습니다., metadata={role=ASSISTANT, messageType=ASSISTANT, refusal=, finishReason=STOP, index=0, annotations=[], id=chatcmpl-DJUZeNflM7UmaVNF0au7210WZSOPo}]]

INFO  com.example.demo.DemoApplication.logStarted(): Started DemoApplication in 3.407 seconds (process running for 3.875)
INFO  o.a.c.c.C.[Tomcat].[localhost].[/].log(): Initializing Spring DispatcherServlet 'dispatcherServlet'
INFO  o.s.web.servlet.DispatcherServlet.initServletBean(): Initializing Servlet 'dispatcherServlet'
INFO  o.s.web.servlet.DispatcherServlet.initServletBean(): Completed initialization in 1 ms
INFO  c.e.d.s.AiServiceSelfConsistency.selfConsistency(): 0: IMPORTANT
INFO  c.e.d.s.AiServiceSelfConsistency.selfConsistency(): 1: IMPORTANT
INFO  c.e.d.s.AiServiceSelfConsistency.selfConsistency(): 2: IMPORTANT
INFO  c.e.d.s.AiServiceSelfConsistency.selfConsistency(): 3: IMPORTANT
INFO  c.e.d.s.AiServiceSelfConsistency.selfConsistency(): 4: IMPORTANT
INFO  c.e.d.s.AiServiceStepBackPrompt.stepBackPrompt(): [
    "서울에서 울릉도로 가기 위해 어떤 교통수단을 고려하고 있나요?",       
    "각 교통수단의 평균 비용을 알고 계신가요?",
    "비용 외에 시간이나 편의성 같은 다른 요소도 고려하시나요?",
    "서울에서 울릉도로 갈 때 비용이 가장 적게 드는 방법은 무엇인가요?"    
]
INFO  c.e.d.s.AiServiceStepBackPrompt.stepBackPrompt(): 단계1 질문: 서울에서 울릉도로 가기 위해 어떤 교통수단을 고려하고 있나요?, 답변: 서울에서 울 릉도로 가기 위해 고려할 수 있는 교통수단은 다음과 같습니다:

1. **항공편**: 서울에서 울릉도로 가는 가장 빠른 방법은 비행기를 이용하는  것입니다. 김포공항에서 울릉도(사동항)로 가는 항공편이 있으며, 비행 시간은 약 1시간 정도입니다.

2. **버스 + 페리**: 서울에서 포항 또는 동해로 가는 고속버스를 이용한 후,  포항 또는 동해에서 울릉도로 가는 페리를 이용할 수 있습니다. 고속버스를 타 고 가는 시간은 약 4-5시간 정도 소요되며, 페리로는 약 2-3시간 걸립니다.    

3. **기차 + 페리**: 서울에서 KTX를 타고 포항이나 동해로 이동한 후, 페리로 울릉도로 가는 방법도 있습니다. KTX는 빠르고 편리하지만, 기차 시간표에 따라 조정이 필요할 수 있습니다.

각 교통수단의 소요 시간과 비용을 고려해 본인의 상황에 맞는 방법을 선택하는 것이 좋습니다.
INFO  c.e.d.s.AiServiceStepBackPrompt.stepBackPrompt(): 단계2 질문: 각 교 통수단의 평균 비용을 알고 계신가요?, 답변: 서울에서 울릉도로 가기 위한 각 교통수단의 평균 비용은 다음과 같습니다:

1. **항공편**:
   - 평균 비용: 약 100,000원 ~ 150,000원 (왕복 기준)
   - 항공편은 빠르지만, 가격이 상대적으로 비쌀 수 있습니다. 사전 예약 시  할인 혜택을 받을 수 있는 경우도 있으니 참고하시기 바랍니다.

2. **버스 + 페리**:
   - 버스 비용: 약 30,000원 ~ 40,000원 (편도 기준)
   - 페리 비용: 약 30,000원 ~ 40,000원 (편도 기준)
   - 총 비용: 약 60,000원 ~ 80,000원 (편도 기준)
   - 이 방법은 시간이 더 소요되지만, 비용 면에서는 비교적 저렴합니다.     

3. **기차 + 페리**:
   - KTX 비용: 약 50,000원 ~ 70,000원 (편도 기준)
   - 페리 비용: 약 30,000원 ~ 40,000원 (편도 기준)
   - 총 비용: 약 80,000원 ~ 110,000원 (편도 기준)
   - KTX는 빠르지만, 가격이 버스보다 비쌀 수 있습니다. 편리한 점이 있지만, 시간표에 맞춰 계획해야 합니다.

각 교통수단의 비용은 시즌, 예약 시기, 할인 여부에 따라 달라질 수 있으므로, 여행 계획 시 최신 정보를 확인하는 것이 좋습니다.
INFO  c.e.d.s.AiServiceStepBackPrompt.stepBackPrompt(): 단계3 질문: 비용  외에 시간이나 편의성 같은 다른 요소도 고려하시나요?, 답변: 네, 서울에서 울릉도로 가는 교통수단을 선택할 때 비용 외에도 시간, 편의성, 그리고 개인적인 선호 사항 등을 고려하는 것이 중요합니다. 각 교통수단의 특징을 살펴보면 다음과 같습니다:

1. **항공편**:
   - **시간**: 가장 빠른 이동 방법으로 비행 시간이 약 1시간입니다. 그러나 공항까지의 이동 시간과 체크인 절차를 고려해야 하므로 실제 소요 시간은 더  길어질 수 있습니다.
   - **편의성**: 비행기는 신속하지만, 항공권 예약 및 공항 이동이 필요하며, 기상 상황에 따라 항공편이 취소되거나 지연될 수 있습니다.

2. **버스 + 페리**:
   - **시간**: 총 소요 시간이 약 6-8시간으로 가장 느린 방법입니다. 하지만 고속버스를 이용할 경우 편안하게 이동할 수 있습니다.
   - **편의성**: 버스 정류장에서 출발하여 페리 터미널까지의 이동이 필요하 지만, 비용이 저렴하고 경치 좋은 여정을 즐길 수 있는 장점이 있습니다.      

3. **기차 + 페리**:
   - **시간**: KTX를 이용하면 빠르게 이동할 수 있지만, 기차 시간표에 맞춰 야 하므로 일정 조정이 필요할 수 있습니다.
   - **편의성**: KTX는 편안하고 빠르며, 기차 내에서의 편의시설이 잘 갖춰져 있습니다. 페리로의 환승도 비교적 간편합니다.

이처럼 각 교통수단의 장단점을 비교하여, 자신의 일정, 예산, 편안함을 고려해 최적의 선택을 하는 것이 좋습니다. 예를 들어, 시간이 가장 중요한 경우 항공편을 선택할 수 있지만, 비용을 절약하고 싶다면 버스 + 페리 조합이 더 나을  수 있습니다. 또한, 여행의 목적이나 동행하는 사람에 따라 편의성도 중요한 요소가 될 수 있습니다.
INFO  c.e.d.s.AiServiceStepBackPrompt.stepBackPrompt(): 단계4 질문: 서울에서 울릉도로 갈 때 비용이 가장 적게 드는 방법은 무엇인가요?, 답변: 서울에서 울릉도로 가는 비용을 가장 적게 드는 방법은 **버스 + 페리** 조합입니다. 이 방법의 평균 비용은 편도로 약 60,000원 ~ 80,000원 정도이며, 왕복 시에도 비교적 저렴한 비용으로 다녀올 수 있습니다.

### 비용 요약:
- **버스**: 약 30,000원 ~ 40,000원 (편도)
- **페리**: 약 30,000원 ~ 40,000원 (편도)
- **총 비용**: 약 60,000원 ~ 80,000원 (편도)

### 소요 시간:
- 총 소요 시간은 약 6-8시간 정도로 다소 길지만, 비용 면에서 가장 경제적입 니다.

### 선택 시 고려사항:
- **시간**: 가장 빠른 방법인 항공편은 약 100,000원 ~ 150,000원 (왕복 기준)으로 비용이 더 비쌉니다.
- **편의성**: 버스 + 페리 방법은 경치 좋은 여정을 즐길 수 있는 장점이 있지만, 버스와 페리 간의 환승이 필요합니다.

결론적으로, **비용을 가장 절약하고 싶다면 버스 + 페리 조합을 선택하는 것이 좋습니다**. 다만, 시간이나 편의성을 중시한다면 다른 교통수단도 고려해보는 것이 좋습니다.
```

- 비용과 응답 지연 측면에서 비교하려면 동일 입력으로 각 기법을 벤치마킹하는 스크립트를 작성해 보세요.

필요하시면 이 README에 예시 실행 명령, 샘플 입력, 또는 간단한 테스트 파일을 추가해 드리겠습니다.
