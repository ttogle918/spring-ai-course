# 텍스트 임베딩

목적

- 자연어 텍스트를 임베딩으로 변환해 벡터 유사도 검색, 유사 문서 검색, 의미 기반 검색 등에 활용합니다.

관련 코드

- `src/main/java/com/example/demo/service/AiService.java`의 `textEmbedding()`

핵심 흐름

1. `EmbeddingModel.embedForResponse(List.of(question))`로 임베딩을 생성합니다.
2. `EmbeddingResponse`에서 메타데이터(`getMetadata()`)와 결과 벡터를 확인합니다.
3. (옵션) `VectorStore.add()`로 문서와 임베딩을 함께 저장합니다.

확인 포인트

- 로그에서 모델 이름과 임베딩 차원 확인
- 벡터 길이와 값이 예상 형태인지 확인

팁

- 긴 문장은 청크(문단 단위)로 분할해 임베딩을 생성하면 검색 정밀도가 개선됩니다.
