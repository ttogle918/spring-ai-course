# ch09-jdbc-chat-memory

- **Purpose:** JDBC-backed chat memory using a relational database (PostgreSQL schema included).
- **Key classes/files:** `AiService`, `AiService2`, `CustomChatMemoryRepositoryDialect`, `jdbc/schema-postgresql.sql`.
- **Storage:** Persisted in relational tables (see `schema-postgresql.sql`).
- **Run:**
  - Start DB (see `docker-compose.yaml`), then `./gradlew bootRun`.
  - Ensure `application.properties` has correct datasource settings.
- **Notes:**
  - Use migrations (Flyway/Liquibase) for production schema upgrades.
  - Index message columns used in queries to improve retrieval performance.

## Architecture

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
