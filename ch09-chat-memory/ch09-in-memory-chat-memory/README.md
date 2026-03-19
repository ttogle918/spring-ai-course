# ch09-in-memory-chat-memory

- **Purpose:** Simple in-memory chat memory implementation used for local development and demos.
- **Key classes:** `AiService`, `AiController`, `HomeController`, `DemoApplication`.
- **Storage:** Volatile in-process data structures (lists/maps). Messages are lost on restart.
- **Run:** `./gradlew bootRun` (or `gradlew.bat bootRun` on Windows).
- **Config:** see [ch09-in-memory-chat-memory/src/main/resources/application.properties](ch09-in-memory-chat-memory/src/main/resources/application.properties).
- **When to use:** fast iteration, tests, and prototyping.
- **Tips:**
  - Not suitable for multi-instance or production; use persistent store for durability.
  - For larger workloads, replace with a JDBC, Cassandra, or vector-store-backed memory.

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
