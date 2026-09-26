# Commonplace

A personal knowledge base with semantic search and RAG-powered Q&A. Paste in text or articles; ask questions in natural language and get answers grounded in your own sources.

## What it does

- **Ingest:** accepts raw text (URL ingestion planned), chunks it, embeds each chunk, stores it in Postgres with pgvector.
- **Search:** finds semantically similar chunks using cosine similarity over an HNSW index.
- **Ask (planned):** retrieves the top-K relevant chunks and asks a local LLM to answer using only those chunks, with citations.

## Architecture

    text ──▶ chunk (TokenTextSplitter)
         ──▶ embed (Ollama / mxbai-embed-large, 1024 dims)
         ──▶ store (Postgres + pgvector, HNSW index)

    question ──▶ embed
             ──▶ similarity search (cosine distance, top-K)
             ──▶ build prompt with retrieved chunks
             ──▶ generate answer (Ollama / qwen2.5:3b)

## Stack

| Component | Choice | Why |
|---|---|---|
| Runtime | Java 25, Spring Boot 4 | Current LTS, modern virtual threads |
| App layer | Spring Boot | Auto-config for datasource, AI, and vector store |
| Embeddings | Ollama + mxbai-embed-large (1024 dims) | Local, private, strong retrieval quality |
| LLM | Ollama + qwen2.5:3b | Local, small enough to run on laptop |
| Vector store | Postgres + pgvector | One database for vectors and metadata; SQL for everything |
| Index | HNSW (cosine) | Fast approximate search, builds on empty table |
| Chunking | Spring AI TokenTextSplitter | Token-based, punctuation-aware, overlap-safe |

## Why these choices

**Why pgvector over a dedicated vector DB (Pinecone, Qdrant, Weaviate):**
One database instead of two. Metadata and vectors live together, so filtering and joining are just SQL. For a personal knowledge base, operational simplicity beats specialized performance.

**Why HNSW over IVFFlat:**
Ingestion is incremental — chunks get added over time. HNSW can be built on an empty table and stays fast as data grows. IVFFlat needs existing data to train clusters.

**Why cosine distance:**
Text embeddings are directionally meaningful, not positionally. Cosine measures angle between vectors, ignoring magnitude. It's the standard metric for text embeddings.

**Why mxbai-embed-large over nomic-embed-text:**
1024 dimensions vs 768. More room to encode fine semantic distinctions. Retrieval quality is the ceiling on generation quality — worth the extra ~400MB.

**Why local models (Ollama) over hosted APIs:**
No API keys, no per-token cost, no data leaving the machine. Slower, but this is a personal tool, not a product.

## Setup

### Prerequisites

- Java 25
- Docker
- Ollama

### 1. Pull models

    ollama pull mxbai-embed-large
    ollama pull qwen2.5:3b

### 2. Start Postgres with pgvector

    docker run -d \
      --name commonplace-db \
      -e POSTGRES_PASSWORD=devpass \
      -e POSTGRES_USER=dev \
      -e POSTGRES_DB=commonplace \
      -p 5432:5432 \
      -v commonplace-pgdata:/var/lib/postgresql/data \
      pgvector/pgvector:pg16

### 3. Run

    ./mvnw spring-boot:run

Schema is created automatically on first boot (`initialize-schema: true`).

## API

| Method | Path | Body / Query | Returns |
|---|---|---|---|
| POST | `/api/ingest` | `{ "text": "..." }` | `201` on success |
| GET | `/api/search` | `?q=...&topK=5` | List of similar chunks with scores |
| GET | `/api/ask` | `?q=...` | *(planned)* Answer + source chunks |

## Not yet done

- URL ingestion (fetch + extract article text)
- Streaming LLM responses
- Deduplication on ingest
- Metadata filtering (by source, date, tag)
- Kafka-based async ingestion pipeline
- Redis-backed dedup and job queue

## License

MIT