# ch09-vector-store-chat-memory (한국어)

- **목적:** 임베딩과 유사도 검색을 이용해 관련 과거 메시지를 조회하는 벡터 스토어 기반 채팅 메모리입니다.
- **주요 파일:** `AiService`, `AiController`, `docker-compose.yaml`, `application.properties`.
- **저장 및 검색:** 메시지의 벡터 임베딩을 저장하고 최근접 이웃 검색(유사도)을 통해 문맥을 조회합니다.
- **실행:** 벡터 DB(Milvus, Pinecone, 또는 Postgres+pgvector 등)를 시작한 뒤 `./gradlew bootRun`.
- **팁:**
  - 임베딩 모델과 벡터 차원을 일관되게 유지하세요.
  - 대규모 데이터셋은 정규화, 인덱싱, 샤딩을 고려하세요.
  - `topK`와 유사도 임계값을 튜닝해 정밀도와 재현율을 조정하세요.

## 아키텍처

```mermaid
flowchart TD
  Browser -->|HTTP| Controller[AiController / HomeController]
  Controller -->|calls| AiService[`AiService`]
  AiService -->|reads/writes| ChatMemory[Chat Memory Layer]

  subgraph Memories
    IM[In-Memory<br/>(ephemeral)]
    JDBC[JDBC / RDBMS<br/>(PostgreSQL)]
    CASS[Cassandra<br/>(wide-column)]
    VSTORE[Vector Store<br/>(embeddings)]
  end

  ChatMemory --> IM
  ChatMemory --> JDBC
  ChatMemory --> CASS
  ChatMemory --> VSTORE

  JDBC ---|docker-compose| Postgres[(Postgres DB)]
  CASS ---|docker-compose| Cassandra[(Cassandra Cluster)]
  VSTORE ---|vector DB / pgvector| VectorDB[(Milvus / PGVector / Pinecone)]

  classDef db fill:#f9f,stroke:#333,stroke-width:1px;
  class Postgres,Cassandra,VectorDB db;
```
