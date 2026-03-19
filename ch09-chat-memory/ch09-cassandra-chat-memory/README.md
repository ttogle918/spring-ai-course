# ch09-cassandra-chat-memory

- **Purpose:** Cassandra-backed chat memory demonstrating a wide-column store approach for message persistence.
- **Key classes/files:** `AiService`, controllers, `docker-compose.yaml` for local Cassandra.
- **Storage:** Cassandra tables; good for high write throughput and multi-node deployments.
- **Run:**
  - Start Cassandra via `docker-compose.yaml`, then run `./gradlew bootRun`.
  - Configure contact points and keyspace in `application.properties`.
- **Notes & Tips:**
  - Cassandra provides eventual consistency; design queries and TTLs accordingly.
  - Tune compaction and replication settings for your workload.

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
