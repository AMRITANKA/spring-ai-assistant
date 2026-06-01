# Spring AI Assistant

Production-leaning LLM chat assistant built on **Spring Boot 3.3**, **Spring AI 1.0**, **Azure OpenAI**, and **pgvector**.

This is the working source tree for the project profiled in [`blog.md`](../blog.md). It implements the architecture described there: streaming chat over SSE, hybrid memory (sliding window + rolling summary), RAG with citation tags, function/tool calling, Azure AD JWT auth, token + cost metering, and Prometheus/OTel observability.

## Quick start (local)

```bash
# 1. Set Azure OpenAI creds
export AZURE_OPENAI_API_KEY="..."
export AZURE_OPENAI_ENDPOINT="https://<your-resource>.openai.azure.com"
export AZURE_OPENAI_CHAT_DEPLOYMENT="gpt-4o"
export AZURE_OPENAI_EMBED_DEPLOYMENT="text-embedding-3-large"

# 2. Bring up Postgres (pgvector) + Redis + the app
docker compose up --build

# 3. Stream a chat (dev profile = no auth)
curl -N -X POST http://localhost:8080/api/v1/chat/stream \
  -H "Content-Type: application/json" \
  -d '{"message":"Explain Spring AI in 3 bullets.","useRag":false}'
```

## Layout

```
src/main/java/dev/amritanka/assistant/
├── SpringAiAssistantApplication.java
├── config/        # ChatClient + Security beans
├── domain/        # Conversation, ChatTurn aggregates
├── rag/           # IngestionService (Tika → splitter → embeddings → pgvector)
├── repo/          # ConversationRepository (swap to JPA for prod)
├── service/       # ChatService, MemoryCompactor, TokenMeter
├── tools/         # @Description-annotated function-callable tools
└── web/           # ChatController, IngestionController, DTOs
```

## Endpoints

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/api/v1/chat/stream` | SSE token stream |
| `POST` | `/api/v1/documents/upload` | Ingest a doc into the RAG index |
| `GET`  | `/actuator/prometheus` | Metrics scrape endpoint |
| `GET`  | `/swagger-ui.html` | OpenAPI explorer |

## Profiles

- **dev** — auth disabled, Flyway off, `ddl-auto=update`. Use for local curl.
- **default** — Azure AD JWTs required, Flyway migrations enforced.

## Deploying to Azure Container Apps

```bash
az containerapp up \
  --name spring-ai-assistant \
  --resource-group rg-ai \
  --location eastus \
  --image <acr>.azurecr.io/spring-ai-assistant:latest \
  --env-vars AZURE_OPENAI_API_KEY=secretref:openai-key \
             AZURE_OPENAI_ENDPOINT=https://... \
             POSTGRES_URL=jdbc:postgresql://...
```

## Tests

```bash
./mvnw test
```
Includes a Testcontainers-friendly Postgres setup and a unit test for the memory compactor.
