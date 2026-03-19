# ch09-in-memory-chat-memory (한국어)

- **목적:** 로컬 개발 및 데모용으로 사용되는 간단한 인메모리 채팅 메모리 구현입니다.
- **주요 클래스:** `AiService`, `AiController`, `HomeController`, `DemoApplication`.
- **저장소:** 프로세스 내 메모리(리스트/맵). 재시작 시 데이터가 사라집니다.
- **실행:** `./gradlew bootRun` (Windows에서는 `gradlew.bat bootRun`).
- **설정:** `src/main/resources/application.properties`를 확인하세요.
- **사용처:** 빠른 반복 개발, 테스트, 프로토타이핑.
- **팁:**
  - 멀티 인스턴스나 프로덕션에는 적합하지 않습니다. 내구성이 필요하면 영구 저장소를 사용하세요.
  - 워크로드가 커지면 JDBC, Cassandra 또는 벡터 스토어 기반 메모리로 교체하세요.

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
