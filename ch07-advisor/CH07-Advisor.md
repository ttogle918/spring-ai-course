# Chapter 07 — 모델 전·후처리 어드바이저 (요약)

이 문서는 `ch07-advisor` 모듈의 소스 코드를 기반으로 Chapter 07의 핵심 개념과 학습 포인트, 구현 노트 및 프로젝트 검증 체크리스트를 정리합니다.

## 7.1 Advisor 소개
- 개념: Advisor는 ChatClient 호출 전후의 전처리/후처리를 캡슐화하는 훅입니다. 입력 프롬프트 강화, 컨텍스트 주입, 응답 검사/필터링, 로깅 등을 담당합니다.
- 코드 위치: [src/main/java/com/example/demo/advisor](src/main/java/com/example/demo/advisor)

## 7.2 Spring AI Advisor API
- 주요 인터페이스:
  - `CallAdvisor`: 동기 호출 전후(hook) 처리
  - `StreamAdvisor`: 스트리밍 호출 전후 처리
  - `CallAdvisorChain` / `StreamAdvisorChain`: 체인으로 다음 Advisor 또는 실제 LLM 호출을 진행
- 구현 예제: `AdvisorA`, `AdvisorB`, `AdvisorC` — 전/후처리 패턴과 우선순위(order)를 보여줍니다. ([AdvisorA.java](src/main/java/com/example/demo/advisor/AdvisorA.java))

## 7.3 Advisor 구현
- 예제: `MaxCharLengthAdvisor` — 사용자 메시지에 최대 글자 수 안내문을 자동 추가하여 전처리하는 방식입니다. (프롬프트 augmentation)
  - 핵심 패턴: 기존 `Prompt`를 `augmentUserMessage`로 수정하고, `ChatClientRequest.mutate()`로 수정된 요청을 생성합니다. ([MaxCharLengthAdvisor.java](src/main/java/com/example/demo/advisor/MaxCharLengthAdvisor.java))
- 구현 팁:
  - Advisor는 부수효과 없이 입력을 변환하거나 검증 결과를 컨텍스트에 저장하는 식으로 구현하세요.
  - 우선순위(`getOrder`)로 전처리/후처리 순서를 제어할 수 있습니다.

## 7.4 Advisor 적용
- 설정 방식:
  - `ChatClient.Builder.defaultAdvisors(...)`로 빌더 레벨에서 기본 Advisor를 등록.
  - `prompt().advisors(...)`로 개별 호출마다 Advisor를 추가하거나 파라미터를 전달할 수 있음.
- 코드 예시: `AiService1`는 기본 Advisor(A,B)를 빌더에 등록하고, 호출 시 추가 Advisor(C)를 주입합니다. ([AiService1.java](src/main/java/com/example/demo/service/AiService1.java))

## 7.5 공유 데이터 이용
- Advisor는 `ChatClientRequest.context()`를 통해 호출 단위의 컨텍스트(파라미터)를 주고받을 수 있습니다. 예: `MaxCharLengthAdvisor`는 `MAX_CHAR_LENGTH` 키를 컨텍스트에서 읽어 동작을 변경할 수 있음.
- 호출 레벨에서 `advisorSpec.param(...)`로 값 설정 가능: `AiService2`에서 사용 예시 ([AiService2.java](src/main/java/com/example/demo/service/AiService2.java)).

## 7.6 내장 Advisor
- 예제: `SimpleLoggerAdvisor`, `SafeGuardAdvisor` 등 Spring AI가 제공하는 기본 Advisor를 사용해 로깅/필터링 기능을 활용할 수 있습니다. `AiService3`와 `AiService4`에서 사용됩니다.
  - `AiService3`는 `SimpleLoggerAdvisor`를 추가해 요청/응답 로깅을 수행합니다. ([AiService3.java](src/main/java/com/example/demo/service/AiService3.java))
  - `AiService4`는 `SafeGuardAdvisor`로 민감 키워드 차단을 구성합니다. ([AiService4.java](src/main/java/com/example/demo/service/AiService4.java))

## 7.7 로깅 Advisor
- 로깅 패턴: 요청/응답(또는 스트림)을 가로채서 로깅만 수행하거나, 로그 수준과 타이밍(전/후)을 조절할 수 있습니다. `SimpleLoggerAdvisor`가 대표 예시입니다.
- 실무 팁: 로그에 프롬프트/응답 전체를 남길 때는 API 키, 개인 정보 등 민감값을 마스킹하세요.

## 7.8 세이프가드 Advisor
- 목적: 불쾌감·위험·민감 정보 요청을 사전에 차단하고 사용자에게 적절한 메시지를 반환.
- 구현: `SafeGuardAdvisor` 생성자에 금지 단어 목록과 차단 시 반환할 메시지를 지정합니다. (`AiService4`) ([AiService4.java](src/main/java/com/example/demo/service/AiService4.java))

## 구현상 유의사항 / 학습 포인트
- 체인 순서 제어: Advisor의 `getOrder()`로 정확한 전처리/후처리 순서를 보장하세요.
- 스트리밍 호환성: 스트리밍 호출(`.stream()`)에 대한 Advisor는 `StreamAdvisor`를 구현해야 합니다.
- 부작용 최소화: Advisor는 가능한 순수 함수로 동작시키고, 외부 상태 변경은 최소화하세요.
- 리소스·블로킹 주의: 스트림 Advisor 내부에서 `flux.blockLast()` 같은 블로킹은 성능·스케일에 영향을 줍니다(예: `AdvisorC`).
- 테스트: `ChatClient`를 모킹하고 다양한 Advisor 조합을 유닛 테스트로 검증하세요.

## 빠른 검증 체크리스트 (Project QA)
- [ ] `./gradlew :ch07-advisor:build`가 성공하는가?
- [ ] 각 엔드포인트가 예상 동작하는가? (컨트롤러 `/ai/*` 경로 확인) — `AiService1`..`AiService4` 호출 테스트
- [ ] `MaxCharLengthAdvisor`가 프롬프트에 문자 제한 문구를 추가하는가? (로그 및 실제 LLM 요청 프롬프트 확인)
- [ ] `SafeGuardAdvisor`가 금지 키워드에 대해 차단 메시지를 반환하는가?
- [ ] 스트리밍 호출 시 `StreamAdvisor`가 정상 동작하는가? (`AiService1.advisorChain2`를 통해 테스트)
- [ ] 로깅 Advisor가 민감 정보(키 등)를 마스킹하도록 구현되어 있는가?

## 참고 파일
- Controller: [AIController.java](src/main/java/com/example/demo/controller/AIController.java)
- Services: [AiService1.java](src/main/java/com/example/demo/service/AiService1.java), [AiService2.java](src/main/java/com/example/demo/service/AiService2.java), [AiService3.java](src/main/java/com/example/demo/service/AiService3.java), [AiService4.java](src/main/java/com/example/demo/service/AiService4.java)
- Advisors: [AdvisorA.java](src/main/java/com/example/demo/advisor/AdvisorA.java), [AdvisorB.java](src/main/java/com/example/demo/advisor/AdvisorB.java), [AdvisorC.java](src/main/java/com/example/demo/advisor/AdvisorC.java), [MaxCharLengthAdvisor.java](src/main/java/com/example/demo/advisor/MaxCharLengthAdvisor.java)

---

원하시면 이 요약을 `README`로 병합하거나, 각 소주제별(예: 세이프가드, 로깅) 상세 예제 코드를 추가로 생성해 드리겠습니다.
