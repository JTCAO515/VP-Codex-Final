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

Mock chat request:

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

No API key is required. Missing `BUTLER_LLM_API_KEY` keeps the permanent deterministic mock fallback active.

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
flyctl secrets set BUTLER_LLM_API_KEY="$BUTLER_LLM_API_KEY" BUTLER_LLM_MODEL="$BUTLER_LLM_MODEL"
flyctl deploy
```

Secret variable names only:

- `BUTLER_LLM_API_KEY`
- `BUTLER_LLM_MODEL`

The service exposes `POST /butler/chat` and `GET /actuator/health`.
