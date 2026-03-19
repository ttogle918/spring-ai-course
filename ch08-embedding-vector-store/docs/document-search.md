# 문서 저장소 및 검색

목적

- 문서(텍스트)를 임베딩하고 메타데이터와 함께 벡터 저장소에 보관한 뒤, 의미 기반 유사도 검색을 수행합니다.

관련 코드

- `src/main/java/com/example/demo/service/AiService.java`의 `addDocument()`, `searchDocument1()`, `searchDocument2()`
- DB 테이블 정의: `src/main/resources/sql/table.sql`

핵심 흐름

1. 예제 문서 목록을 `Document` 객체로 생성합니다(본문 + 메타데이터).
2. `vectorStore.add(documents)`로 저장하면 임베딩 생성 후 벡터와 메타데이터가 저장됩니다.
3. `vectorStore.similaritySearch(query)` 또는 `vectorStore.similaritySearch(SearchRequest)`로 검색합니다.
4. `SearchRequest`에 `topK`, `similarityThreshold`, `filterExpression`을 지정해 검색 결과를 제어할 수 있습니다.

확인 포인트

- 간단 검색(`searchDocument1`)에서는 관련 문서들이 상위에 노출되는지 확인
- 필터링 검색(`searchDocument2`)에서 `filterExpression` 조건이 제대로 적용되는지 확인

팁

- 메타데이터(예: `source`, `year`)를 활용한 필터링은 도메인 제약을 적용해 정확도를 높입니다.
- 대규모 데이터는 ANN 인덱스(HNSW 등)를 사용해 검색 성능을 확보하세요.
