# ch09-jdbc-chat-memory (한국어)

- **목적:** 관계형 데이터베이스(JDBC)를 사용한 채팅 메모리 구현입니다. PostgreSQL용 스키마(`jdbc/schema-postgresql.sql`)가 포함되어 있습니다.
- **주요 파일:** `AiService`, `AiService2`, `CustomChatMemoryRepositoryDialect`, `jdbc/schema-postgresql.sql`.
- **저장소:** 관계형 테이블에 영구 저장됩니다.
- **실행:**
  - 모듈 폴더에서 `docker-compose.yaml`로 DB를 시작한 뒤 `./gradlew bootRun`.
  - `application.properties`에서 데이터소스 설정을 확인하세요.
- **주의:**
  - 프로덕션에서는 마이그레이션(Flyway/Liquibase)을 사용하세요.
  - 쿼리에 사용되는 칼럼에 인덱스를 추가하여 검색 성능을 개선하세요.

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
