(The file `c:\Users\ttogl\workspace\spring-ai-course\ch01-spring-ai-project\README.md` exists, but is empty)
# ch01 — Spring Boot 기초

이 챕터에서는 Spring Boot 프로젝트를 처음부터 이해하고 간단한 웹 애플리케이션을 만들며 Spring의 핵심 개념을 익히는 것을 목표로 합니다.

핵심 포인트
- **애플리케이션 구조**: `src/main/java`, `src/main/resources/templates`, `static` 디렉토리의 역할과 관례
- **의존성 주입(DI)과 IoC**: `@Component`, `@Service`, `@Repository`, `@Controller` 등 스프링 빈 구성과 생명주기
- **진입점**: `@SpringBootApplication`과 `main()` 메서드로 애플리케이션이 어떻게 시작되는지
- **설정**: `application.properties`를 통한 환경별 설정 관리
- **템플릿과 정적 리소스**: Thymeleaf 기반 템플릿 엔진과 `static` 리소스 제공 방식
- **빌드/실행**: Gradle Wrapper를 사용한 로컬 실행과 패키징

프로젝트에서 살펴볼 위치
- 소스: `src/main/java` — 컨트롤러, 서비스, 도메인 클래스
- 템플릿: `src/main/resources/templates` — `.html` 템플릿 파일
- 정적 자원: `src/main/resources/static` — CSS, JS, 이미지
- 설정: `src/main/resources/application.properties`

빠른 실행
- 개발 모드로 실행: `gradlew.bat bootRun` (Windows) 또는 `./gradlew bootRun` (Unix)
- JAR 생성: `./gradlew bootJar`
- 생성된 JAR 실행: `java -jar build/libs/<project>-0.0.1-SNAPSHOT.jar`

학습 포인트(이 챕터에서 강조하는 내용)
- 왜 Spring을 사용하는가: DI와 모듈화로 테스트 가능하고 유지보수 쉬운 구조 만들기
- 빈 스코프와 라이프사이클 이해하기
- 컨트롤러-서비스-리포지토리 계층 분리의 이유
- 설정 파일로 환경을 분리하는 방법

참고
- Gradle Wrapper(`gradlew`, `gradlew.bat`)를 사용하면 로컬에 Gradle을 따로 설치할 필요가 없습니다.
