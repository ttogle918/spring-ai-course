# Spring AI Course("이것이 Spring AI다" 책 기반)

이 저장소는 Spring을 이용한 AI 연동 학습용 예제 모음입니다. "이것이 Spring AI다"의 각 챕터별로 실습 프로젝트와 설명을 제공합니다. 설명과 일부 코드는 기존 샘플 코드(저자 제공 소스 코드)에서 수정, 추가하였고, 문서 파일은 학습 후 Gemini의 도움을 받아 정리한 것입니다.

요약
- 요구사항: JDK 17 이상 권장(LTS), Git, IDE(권장: VS Code)
- 빌드 도구: Gradle Wrapper(`gradlew`, `gradlew.bat`)

JDK 설치 (Windows)
1. AdoptOpenJDK/Adoptium 또는 OpenJDK 배포판 설치(예: Temurin, Zulu)
2. 시스템 환경변수에 `JAVA_HOME` 추가(예: C:\Program Files\AdoptOpenJDK\jdk-17)
3. 명령 프롬프트에서 설치 확인:

```bash
java -version
javac -version
```

Spring 프로젝트 생성 (Spring Initializr)
- 웹: https://start.spring.io 에 접속하여 Project, Language, Java 버전, Dependencies 선택 후 Generate
- VS Code: `Ctrl+Shift+P` → `Spring Initializr: Generate a Maven/Gradle Project` 선택하여 생성

프로젝트 실행
- 개발 모드 (Windows):

```powershell
.\gradlew.bat bootRun
```

- Unix/macOS:

```bash
./gradlew bootRun
```

- JAR로 패키징 후 실행:

```bash
./gradlew bootJar
java -jar build/libs/<project>-0.0.1-SNAPSHOT.jar
```

디렉토리(챕터) 목차
- [ch01-spring-ai-project/README.md](ch01-spring-ai-project/README.md) : Spring Boot 기초 — 애플리케이션 구조, DI/IoC, 템플릿, 설정, 빌드/실행
- [ch02-chat-model-api/README.md](ch02-chat-model-api/README.md) : Chat Model API — REST/스트리밍, 템플릿 연동, 외부 모델 통합, 스트리밍 처리

추가 팁
- Gradle Wrapper를 사용하면 로컬에 Gradle을 설치할 필요가 없습니다.
- Windows에서 `Ctrl+Shift+P`는 VS Code 명령 팔레트입니다(사용자가 생각한 Shift+F+P는 잘못된 조합). Spring Initializr 명령을 찾아 프로젝트를 생성하세요.

추천 리소스

- **JDK 다운로드 (권장: Temurin/Adoptium)**: https://adoptium.net/ — Windows용 설치 프로그램과 설치 가이드 제공
- **VS Code 추천 확장**:
	- Spring Boot Extension Pack (vscjava.vscode-spring-boot)
	- Spring Initializr (Pivotal) (vscjava.vscode-spring-initializr)
	- Java Extension Pack (vscjava.vscode-java-pack)
	- Lombok Annotations Support (for IDE)

설정 팁
- `JAVA_HOME`이 제대로 설정되어 있으면 VS Code의 Java 확장과 Gradle 실행이 원활합니다.
- VS Code에서 `Extensions` 패널을 열고 위 확장을 설치하세요.
