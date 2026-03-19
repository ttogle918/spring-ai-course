# ch09-cassandra-chat-memory (한국어)

- **목적:** 메시지 영구 저장을 위해 wide-column 스토어 접근법(Cassandra)을 사용하는 예시입니다.
- **주요 파일:** `AiService`, 컨트롤러들, `docker-compose.yaml` (로컬 Cassandra 구동용).
- **저장소:** Cassandra 테이블; 높은 쓰기 처리량과 멀티 노드 배포에 적합합니다.
- **실행:**
  - `docker-compose.yaml`로 Cassandra를 시작한 뒤 `./gradlew bootRun`.
  - `application.properties`에서 contact point와 keyspace를 설정하세요.
- **주의 및 팁:**
  - Cassandra는 최종적 일관성(eventual consistency)을 제공합니다. 쿼리와 TTL을 설계하세요.
  - 워크로드에 맞게 컴팩션 및 복제 설정을 튜닝하세요.

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
