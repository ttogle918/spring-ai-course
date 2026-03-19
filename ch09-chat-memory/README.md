# ch09 — Chat Memory Implementations

This chapter contains multiple example implementations of chat memory backends. Use the links below to open the README for each implementation.

- [In-memory implementation](ch09-in-memory-chat-memory/README.md): fast, ephemeral memory for development.
- [JDBC implementation](ch09-jdbc-chat-memory/README.md): relational DB persistence (PostgreSQL schema included).
- [Cassandra implementation](ch09-cassandra-chat-memory/README.md): wide-column store for high-throughput scenarios.
- [Vector-store implementation](ch09-vector-store-chat-memory/README.md): embeddings + similarity search for semantic retrieval.

Suggested next steps:
- Start the module's required DB (if present) with `docker-compose up` inside the module folder.
- Inspect `application.properties` in each module to configure credentials and endpoints.

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
