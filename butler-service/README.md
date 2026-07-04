# VisePanda Butler Service

Java 17 + Spring Boot 3 service for Butler 2.0 Phase A.

## Local

```bash
cd butler-service
./gradlew test
./gradlew bootRun
```

Health:

```bash
curl http://localhost:8080/actuator/health
```

Chat request:

```bash
curl -s http://localhost:8080/butler/chat \
  -H 'Content-Type: application/json' \
  -d '{
    "message": "Plan me a 3 day trip to Beijing",
    "trip": {
      "summary": {
        "title": "China Trip Draft",
        "durationDays": 5,
        "pace": "Balanced",
        "travelerStyle": "First-time visitor",
        "destinations": ["Beijing", "Shanghai"],
        "confidence": "Draft"
      },
      "days": [],
      "alerts": [],
      "lastUpdatedReason": "Initial VisePanda travel draft."
    }
  }'
```

At least one server-side LLM key is required for chat. If no provider key is configured, `/butler/chat`
returns an honest `503` with `ok:false` instead of fabricating a mock CanvasPatch.

Supported provider variables:

- `DASHSCOPE_API_KEY`
- `DASHSCOPE_COMPATIBLE_BASE_URL`
- `QWEN_CHAT_MODEL`
- `ZHIPU_API_KEY`
- `ZHIPU_BASE_URL`
- `ZHIPU_CHAT_MODEL`
- `MOONSHOT_API_KEY`
- `MOONSHOT_BASE_URL`
- `MOONSHOT_CHAT_MODEL`

Memory and RAG are fallback-first. Missing `SUPABASE_URL` or `SUPABASE_SERVICE_ROLE_KEY` keeps L1/L2/L3 memory in process memory and keeps RAG on static keyword search.

## Docker

```bash
cd butler-service
docker build -t visepanda-butler-service .
docker run --rm -p 8080:8080 visepanda-butler-service
```

## Fly.io

```bash
cd butler-service
flyctl launch --copy-config --region hkg
flyctl secrets set DASHSCOPE_API_KEY="$DASHSCOPE_API_KEY"
flyctl deploy
```

Secret variable names only:

- `DASHSCOPE_API_KEY`
- `DASHSCOPE_COMPATIBLE_BASE_URL`
- `QWEN_CHAT_MODEL`
- `ZHIPU_API_KEY`
- `ZHIPU_BASE_URL`
- `ZHIPU_CHAT_MODEL`
- `MOONSHOT_API_KEY`
- `MOONSHOT_BASE_URL`
- `MOONSHOT_CHAT_MODEL`
- `SUPABASE_URL`
- `SUPABASE_SERVICE_ROLE_KEY`

The service exposes:

- `POST /butler/chat`
- `POST /butler/memory/migrate`
- `GET /actuator/health`

`/butler/chat` returns `toolContext.executionPlan` and `toolContext.executionLog` for Phase C debugging.
