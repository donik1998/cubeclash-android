# 🧊 CubeClash — Android (Kotlin)

Native Android client for **CubeClash**, a competitive speedcubing app. Re-implements the same clean architecture as the Flutter client against the same backend — *one architecture, idiomatic per platform*.

## Stack
- **Language:** Kotlin
- **UI:** Jetpack Compose · **Pattern:** MVVM / MVI
- **Concurrency:** Coroutines + Flow
- **Networking:** Retrofit / Ktor (JWT interceptor + refresh)
- **Real-time:** OkHttp / Ktor WebSocket (Flow)
- **Local store:** Room (offline solves)
- **DI:** Hilt
- **Tests:** JUnit + Compose UI tests

## Backend
Talks to `cubeclash-backend` (NestJS REST + WebSocket).

## Status
🚧 **Scaffolding.** v2 client — begins after Flutter ships.

## License
MIT © 2026 Doniyor Murodkulov

> Working title — repo may be renamed once the product name is finalized.
