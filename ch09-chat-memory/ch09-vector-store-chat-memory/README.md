# ch09-vector-store-chat-memory

- **Purpose:** Chat memory backed by a vector store (embeddings + similarity search) to retrieve relevant past messages.
- **Key classes/files:** `AiService`, `AiController`, `docker-compose.yaml` (vector store or DB may be referenced), `application.properties`.
- **Storage & Search:** Stores vector embeddings for messages and uses nearest-neighbor search (similarity) to retrieve context.
- **Run:** Start required vector DB (see `docker-compose.yaml`), set API keys/env, then `./gradlew bootRun`.
- **Notes & Tips:**
  - Ensure consistent embedding model and vector dimensionality.
  - Consider normalization, indexing, and sharding for large datasets.
  - Tune retrieval `topK` and similarity thresholds to balance recall and precision.

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
