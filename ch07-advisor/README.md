# Chapter 07 — Advisor (요약 및 체크리스트)

이 파일은 `ch07-advisor` 모듈의 학습 포인트(이미 반영된 내용)와 책에서 더 깊게 다루면 좋은 보완 항목을 한눈에 확인할 수 있도록 정리한 체크리스트입니다.

파일 위치: `ch07-advisor/CH07-Advisor.md`, Reactive 요약: `ch07-advisor-reactive/CH07-Advisor-Reactive.md`

## 포함된 내용
- Advisor 개념과 API (`CallAdvisor`, `StreamAdvisor`)
- 전/후처리(프롬프트 증강)과 컨텍스트 파라미터 전달 예제 (`MaxCharLengthAdvisor`)
- Advisor 등록 방식(빌더 기본 등록 / 호출 단위 주입)
- 내장 Advisor 사용 예 (`SimpleLoggerAdvisor`, `SafeGuardAdvisor`)
- 로깅·세이프가드 패턴과 권장 구현 방식
- Reactive 모듈의 스트리밍 훅과 non‑blocking 원칙, 차이점 정리
- 간단 검증 체크리스트(빌드·엔드포인트·SSE·파싱 등)
- Mermaid 다이어그램(구조/흐름)

## 권장 보완(책에서 더 깊게 다룰만한 항목)
다음 항목들은 책이나 실습에서 추가로 다루면 이해도가 높아지는 주제입니다. 필요하시면 각 항목에 대한 예제 코드와 테스트를 추가해 드립니다.

- JSON 스키마 기반 응답 검증 및 자동 수리(repair) 워크플로우
- 공급자별(예: OpenAI/Gemini/Ollama) 토큰/메타데이터 추출 사례와 비용 계산 예시
- Advisor 단위 유닛/통합 테스트 코드(모킹 `ChatClient`)와 샘플 테스트 케이스
- 운영 고려사항: 모니터링 지표(토큰·지연·오류), 로그 샘플링, 알림 전략
- 보안·프라이버시: 입력 필터링, 검색 결과 마스킹, 민감정보 처리 가이드
- 고부하·백프레셔 튜닝 및 리액티브 성능 실험 결과 예제
- 실제 서비스 예: 재시도·캐싱 전략, 툴(인터넷 검색) 장애 대비 패턴

## 빠른 검증 체크리스트
- [ ] `./gradlew :ch07-advisor:build` 성공
- [ ] 컨트롤러 엔드포인트(`/ai/*`) 호출 시 예상 흐름(전/후처리, 로그, 응답)이 동작
- [ ] `MaxCharLengthAdvisor`가 프롬프트에 제한 문구를 추가하는지 확인(로그/요청 페이로드)
- [ ] `SafeGuardAdvisor`가 금지 키워드에 대해 차단 메시지를 반환
- [ ] Reactive 모듈(`ch07-advisor-reactive`)에서 스트리밍 경로가 논블로킹으로 동작

---

원하시면 지금 이 체크리스트 기반으로 (A) JSON 스키마 검증 예제, (B) `ChatClient` 모킹 단위 테스트, (C) 공급자별 토큰 추출 예제 중 하나를 먼저 구현해 드리겠습니다. 어느 것을 진행할까요?
