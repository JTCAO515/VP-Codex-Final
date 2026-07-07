# Vp-Hermes (VisePanda Android) · Context

> AI China Travel Assistant — Android Native Client

## Domain Vocabulary

| Term | Definition |
|------|-----------|
| **VisePanda** | The product: AI China travel assistant. Android-native app with AI chat planning. |
| **Hermes** | Codename for the Android app project. Live URL: `hermesapp.go2china.space` |
| **Jetpack Compose** | Android UI framework (declarative, Kotlin). MVVM architecture. |
| **FastAPI Backend** | Python async API server (Docker, uvicorn, PostgreSQL). Routes: auth, destinations, chat (SSE), trips, admin. |
| **React Admin** | Web management panel (Vite + Tailwind + pnpm), deployed on Vercel. |
| **APK Build** | GitHub Actions CI compiles Android APK. Local: `gradlew.bat assembleDebug` on Windows. |
| **SSE Streaming** | Server-Sent Events for real-time AI chat via okhttp-sse. |
| **VPS** | Tencent Cloud (122.51.121.116:8001) — backend API + PostgreSQL. |

## Architecture

```
android/          ← Kotlin + Jetpack Compose (MVVM)
backend/          ← FastAPI + SQLAlchemy + PostgreSQL (Docker)
admin/            ← React + Vite + Tailwind (Vercel)
```

## Key Design Decisions

- **Android native over PWA** — for native device features, offline support, app store distribution
- **FastAPI backend** — async SSE support, type-safe Pydantic schemas, SQLAlchemy ORM
- **Vercel proxy** — `/api/*` rewrite to VPS for unified HTTPS entry point
- **JWT auth** — email/password based, no third-party OAuth dependency
- **Compose Design System** — shared color/typography/spacing tokens across all screens

## Known Constraints

- VPS memory: 3.6GB total (~500MB free after PostgreSQL + backend)
- APK compile: GitHub Actions works, local Windows needs `gradlew.bat assembleDebug`
- Backend port: 8001 only (security group limits), Docker Compose on VPS
