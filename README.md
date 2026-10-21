# Spring AI — Semantic Search with SimpleVectorStore and OpenAI

**Project page:** https://josesaid.github.io/api-spring-ai-simple-vector-store-openai-v1/

A hands-on Spring Boot example that shows how to build **semantic search** over a PDF document using **Spring AI**, **OpenAI embeddings** and the in-memory **`SimpleVectorStore`**. It also includes a set of small embedding experiments (cosine similarity, similarity matrices) and a basic chat REST endpoint.

This project is part of the *Java & Spring AI Examples* series (module **03 – Semantic Search**).

---

## Table of Contents

- [What it does](#what-it-does)
- [Tech stack](#tech-stack)
- [Project structure](#project-structure)
- [How it works](#how-it-works)
  - [1. Indexing (runs once)](#1-indexing-runs-once)
  - [2. Semantic search (runs n times)](#2-semantic-search-runs-n-times)
  - [3. Embedding experiments](#3-embedding-experiments)
  - [4. Chat endpoint](#4-chat-endpoint)
- [Prerequisites](#prerequisites)
- [Configuration](#configuration)
- [Running the application](#running-the-application)
- [Sample output](#sample-output)
- [REST API](#rest-api)
- [Customization](#customization)
- [Limitations](#limitations)
- [Key concepts](#key-concepts)

---

## What it does

On startup the application:

1. Reads `src/main/resources/documents/spring_ai.pdf` page by page.
2. Splits the pages into token-based chunks.
3. Generates an embedding for every chunk using OpenAI.
4. Stores the chunks and their vectors in an in-memory `SimpleVectorStore`.
5. Runs a sample semantic query (`"¿Por qué usar RAG?"`) and prints the top 3 most similar chunks with their similarity score, metadata and a text preview.

It also exposes a simple REST endpoint to chat with the OpenAI model through Spring AI's `ChatClient`.

## Tech stack

| Component            | Version / Detail                                   |
|----------------------|----------------------------------------------------|
| Java                 | 24                                                 |
| Spring Boot          | 4.1.0                                              |
| Spring AI            | 2.0.0 (via `spring-ai-bom`)                        |
| AI provider          | OpenAI (chat + embeddings)                         |
| Vector store         | `SimpleVectorStore` (in-memory)                    |
| Document reader      | `PagePdfDocumentReader` (`spring-ai-pdf-document-reader`) |
| Text splitter        | `TokenTextSplitter`                                |
| Build tool           | Maven (wrapper included)                           |

### Dependencies

- `spring-boot-starter-webmvc` — REST layer
- `spring-ai-starter-model-openai` — OpenAI chat and embedding models
- `spring-ai-pdf-document-reader` — PDF parsing
- `spring-ai-vector-store` — `VectorStore` abstraction and `SimpleVectorStore`
- `spring-boot-starter-webmvc-test` — testing

## Project structure

```
src/main/
├── java/com/mx/development/jose/said/
│   ├── ApiSpringAiDemoV1Application.java   # Spring Boot entry point
│   ├── config/
│   │   └── VectorStoreConfig.java          # SimpleVectorStore bean
│   ├── controller/
│   │   └── ChatController.java             # GET /api/v1/chat-service/chat
│   ├── dto/
│   │   ├── IndexingResult.java             # Pages read / chunks indexed
│   │   └── Resultado.java                  # Text + similarity score
│   ├── runner/
│   │   ├── PdfIndexingRunner.java          # Indexes the PDF and runs a query at startup
│   │   └── SemanticSearchRunner.java       # Embedding / cosine similarity experiments (disabled)
│   └── service/
│       ├── chat/
│       │   ├── ChatService.java
│       │   └── ChatServiceImpl.java        # ChatClient wrapper
│       └── pdf/
│           └── PdfIndexingService.java     # Indexing + similarity search
└── resources/
    ├── application.yml                     # Port, profile, logging
    ├── application-openai.yml              # OpenAI API key
    └── documents/
        └── spring_ai.pdf                   # Source document to index
```

## How it works

```
                 ┌──────────────────────── INDEXING (once) ────────────────────────┐
 spring_ai.pdf → PagePdfDocumentReader → TokenTextSplitter → OpenAI Embeddings → SimpleVectorStore
                 └─────────────────────────────────────────────────────────────────┘

                 ┌──────────────────────── SEARCH (n times) ───────────────────────┐
 user query    → OpenAI Embedding → similaritySearch(topK=3, threshold=0.50) → ranked chunks
                 └─────────────────────────────────────────────────────────────────┘
```

### 1. Indexing (runs once)

`PdfIndexingService.indexar(Resource)`:

- `PagePdfDocumentReader` turns each PDF page into a Spring AI `Document`.
- `TokenTextSplitter` (default settings) splits those documents into smaller chunks that fit comfortably within the embedding model's context.
- `vectorStore.add(chunks)` embeds every chunk with the configured `EmbeddingModel` (OpenAI) and keeps the vectors in memory.
- Returns an `IndexingResult(documentosLeidos, fragmentosIndexados)` — number of pages read and chunks indexed.

### 2. Semantic search (runs n times)

`PdfIndexingService.buscar(String)` builds a `SearchRequest` with:

| Parameter             | Value  | Meaning                                         |
|-----------------------|--------|-------------------------------------------------|
| `topK`                | `3`    | Return at most the 3 most similar chunks        |
| `similarityThreshold` | `0.50` | Discard chunks with similarity score below 0.50 |

The query is embedded with the same model and compared against the stored vectors (cosine similarity). Matches are returned as `Document`s with a `score`.

`PdfIndexingRunner` (a `CommandLineRunner`) orchestrates both steps at startup and prints the results to the console.

### 3. Embedding experiments

`SemanticSearchRunner` contains several self-contained `CommandLineRunner` beans to explore how embeddings behave. They are **disabled by default** (the class's `@Component` and most `@Bean` annotations are commented out). Enable the one you want to try:

| Runner             | What it shows                                                                                         |
|--------------------|-------------------------------------------------------------------------------------------------------|
| `embedding`        | Generates a single embedding, prints its dimension and the first 10 values.                           |
| `embeddingCompare` | Cosine similarity between technical sentences vs. unrelated ones (pizza, dog).                        |
| `embedding2`       | Similarity matrix across terms like *Inteligencia Artificial*, *Artificial Intelligence*, *Machine Learning*, *Deep Learning*, *Perro*. |
| `embedding3`       | Similarity matrix of synonyms (*Automóvil*, *Coche*, *Carro*, …) and unrelated words.                |
| `embedding4`       | Ranks a list of words by similarity to a query (e.g. a misspelled *"Pinsa"* vs *"Pizza"*).           |

The class also includes a manual `cosineSimilarity(float[], float[])` implementation for educational purposes.

To enable them, uncomment `@Component` on `SemanticSearchRunner` and the `@Bean` annotation of the runner(s) you want to execute.

### 4. Chat endpoint

`ChatServiceImpl` wraps Spring AI's `ChatClient` and sends the user question directly to the OpenAI chat model (no retrieval augmentation — the vector store is not used here).

## Prerequisites

- **JDK 24**
- **Maven 3.9+** (or use the included `./mvnw` wrapper)
- An **OpenAI API key** — get one at <https://platform.openai.com/api-keys>

## Configuration

`application.yml`

```yaml
server:
  port: 8088
spring:
  application:
    name: api-spring-ai-demo-v1
  profiles:
    active: openai
logging:
  level:
    org.springframework.ai: DEBUG
    org.springframework.web.client: DEBUG
app:
  indexing:
    enabled: true
```

`application-openai.yml`

```yaml
spring:
  ai:
    openai:
      api-key: ${API_KEY}
```

The OpenAI key is read from the **`API_KEY`** environment variable. Never commit your real key to the repository.

> **Note:** with `DEBUG` logging enabled for `org.springframework.ai` and `org.springframework.web.client`, request/response details are written to the console. Lower the level to `INFO` if you don't need them.

## Running the application

### From the command line

```bash
export API_KEY=sk-...your-openai-key...
./mvnw spring-boot:run
```

Or build and run the JAR:

```bash
./mvnw clean package
API_KEY=sk-... java -jar target/api-spring-ai-simple-vector-store-openai-v1-0.0.1-SNAPSHOT.jar
```

### From IntelliJ IDEA

1. Open **Run → Edit Configurations…** and select `ApiSpringAiDemoV1Application`.
2. In **Environment variables**, add `API_KEY=<your OpenAI key>`.
3. Run the application.

The app starts on **http://localhost:8088**.

## Sample output

```
INDEXACIÓN DEL DOCUMENTO
------------------------------
Documentos leídos  : <pages>
Chunks indexados   : <chunks>
Estado             : Finalizado

BÚSQUEDA SEMÁNTICA
------------------------------
Consulta: ¿Por qué usar RAG?
Resultados encontrados: 3

RESULTADO 1
------------------------------
Similitud : 0.8xxx
Metadatos : {page_number=..., file_name=spring_ai.pdf, ...}
Contenido : <first 200 characters of the matching chunk>...
...
```

Actual numbers depend on the PDF content and the embedding model.

## REST API

### `GET /api/v1/chat-service/chat`

Sends a question to the OpenAI chat model and returns the plain-text answer.

| Query param | Type   | Required | Description          |
|-------------|--------|----------|----------------------|
| `question`  | string | yes      | The prompt to send   |

**Example**

```bash
curl "http://localhost:8088/api/v1/chat-service/chat?question=What%20is%20Spring%20AI%3F"
```

## Customization

- **Change the query:** edit the `consulta` variable in `PdfIndexingRunner` (other examples are left commented out, e.g. `"¿Qué es Spring AI?"`, `"¿Por qué se divide un documento en chunks?"`).
- **Index a different PDF:** place it under `src/main/resources/documents/` and update the `ClassPathResource` path in `PdfIndexingRunner`.
- **Tune the search:** adjust `topK` and `similarityThreshold` in `PdfIndexingService.buscar`.
- **Tune chunking:** configure `TokenTextSplitter.builder()` (chunk size, overlap, etc.) in `PdfIndexingService.indexar`.
- **Persist the index:** `SimpleVectorStore` supports `save(File)` / `load(File)` to avoid re-embedding the PDF on every startup.

## Limitations

- `SimpleVectorStore` is **in-memory only**: the index is rebuilt (and the PDF re-embedded, incurring OpenAI costs) on every startup. For production, use a persistent vector store such as PGVector, Redis, Chroma, Qdrant, etc.
- The `app.indexing.enabled` property is declared but not yet wired to `PdfIndexingRunner`; indexing always runs at startup.
- The chat endpoint does not use the vector store — it is a plain LLM call, not RAG. Combining both (e.g. with a `QuestionAnswerAdvisor`) is the natural next step.

## Key concepts

- **Embedding:** a numeric vector that represents the meaning of a text. Texts with similar meaning have vectors that are close to each other.
- **Cosine similarity:** a measure of the angle between two vectors (1 = same direction / very similar, 0 = unrelated).
- **Chunking:** splitting large documents into smaller fragments so each one can be embedded and retrieved precisely.
- **Vector store:** a database optimized to store embeddings and find the nearest ones to a query vector.
- **Semantic search:** retrieving content by meaning rather than exact keyword match — the foundation of **RAG** (Retrieval-Augmented Generation).
