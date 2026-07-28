# 🧊 CubeClash — Android

[![CI](https://github.com/donik1998/cubeclash-android/actions/workflows/ci.yml/badge.svg)](https://github.com/donik1998/cubeclash-android/actions/workflows/ci.yml)

The native Android client for **CubeClash**, a competitive speedcubing app: a solo WCA timer plus live 1v1 head-to-head races. It talks to the same [`cubeclash-backend`](https://github.com/donik1998/cubeclash-backend) as the [Flutter client](https://github.com/donik1998/cubeclash-flutter), and re-implements the same clean layering in idiomatic Kotlin — *one architecture, idiomatic per platform*.

Why a second client at all: big-tech mobile loops interview in native. One well-architected native client proves the language and the platform idioms, while the shared backend and shared architecture keep it *a client* rather than a second product.

## Stack

| | |
|---|---|
| **Language** | Kotlin 2.3 |
| **UI** | Jetpack Compose · Material 3 · a token-bound design system |
| **Pattern** | MVVM / MVI — ViewModel + immutable UI state + unidirectional actions |
| **Concurrency** | Coroutines + Flow |
| **DI** | Hilt |
| **Networking** | Retrofit + OkHttp + kotlinx.serialization, JWT interceptor with transparent refresh |
| **Real-time** | Socket.IO wrapped behind a `RaceGateway` that exposes a `Flow` |
| **Local store** | Room (solves, local-first) + DataStore (settings, tokens) |
| **Build** | Gradle 9.6 · AGP 9.3 · version catalog · convention plugins · 16 modules |
| **Tests** | JUnit4 · Turbine · MockK · Compose UI tests |

## Getting started

Requires **JDK 21** and the Android SDK (compileSdk 36).

```bash
./gradlew assembleDebug  # builds against in-memory fakes — no backend needed
./gradlew test           # 53 unit tests (`testDebugUnitTest` alone skips the pure-JVM modules)
./gradlew lintDebug
```

Then open the project in Android Studio — AGP 9.3 needs a recent Studio (Android Studio 2026.1 or newer); older versions will offer to upgrade rather than open it.

### Running against a real backend

The app ships with `cubeclash.useFakeData=true`, which mirrors the Flutter client's
`--dart-define=USE_FAKE_DATA`: every repository and the race gateway resolve to in-memory
fakes, so the whole app — including a full scripted 1v1 race — is demoable with no server.

```bash
./gradlew assembleDebug \
  -Pcubeclash.useFakeData=false \
  -Pcubeclash.apiBaseUrl=http://10.0.2.2:3000/v1/ \
  -Pcubeclash.socketUrl=http://10.0.2.2:3000
```

`10.0.2.2` is the host machine as seen from the emulator. Start the backend first
(`docker compose up -d && npm run start:dev` in `cubeclash-backend`).

## Module graph

```
app                     the tab shell, navigation, theme wiring

core/model              pure Kotlin — the 17 WCA events, results, scrambles, race protocol types
core/domain             pure Kotlin — use cases, repository contracts, averaging, scramblers,
                        the timer state machine
core/designsystem       Compose theme from the Figma tokens + base components
core/ui                 shared composables that know about the model (scramble card, solve row…)
core/data               repository implementations — real and fake — plus the mappers
core/network            Retrofit surface, DTOs, auth interceptor, error mapping
core/database           Room entities, DAO, database
core/datastore          settings and the JWT pair
core/realtime           the Socket.IO race gateway and its scripted stand-in
core/testing            test rules and fixtures

feature/{auth,timer,race,stats,profile}
```

Dependencies point inward: a feature may see `core/model`, `core/domain`, `core/designsystem`
and `core/ui` — never another feature, and never the data layer directly. `core/model` and
`core/domain` are **pure JVM modules with no Android SDK on the classpath at all**, so
"is this business logic or is it Android?" is a compiler-enforced question rather than a
code-review one.

## The parts worth reading

- **`core/model/WcaEvent.kt`** — all seventeen events in one table, mirroring
  `cubeclash-backend/src/domain/wca-events.ts`. `WcaEventTest` asserts against the WCA
  Regulations by number, and doubles as the contract test between the two registries.
- **`core/domain/timer/TimerStateMachine.kt`** — the solve state machine: pure, clock-injected,
  so the whole 15-second inspection ladder is tested in microseconds instead of by hand.
- **`core/domain/stats/Averages.kt`** — one DNF is absorbed as the worst attempt, two make the
  average a DNF, and a mean trims nothing. The rules that are easy to get wrong, in one place.
- **`core/realtime/`** — the race gateway. `FakeRaceGateway` plays the real protocol in the real
  order, so swapping the live Socket.IO implementation in is one Hilt binding.
- **`core/designsystem/icon/PuzzleIcon.kt`** — event icons are *composed* (base shape + badge)
  rather than enumerated, so an eighteenth event is a table row, not a new vector file.

## Deliberate gaps

Stated rather than hidden, because a scaffold that pretends to be finished is worse than one
that says where it stops:

- **Scramblers are random-move, not random-state.** All seventeen events generate legal
  scrambles with the redundancy rules enforced, but the Flutter client's genuine random-*state*
  Pyraminx and Skewb solvers have not been ported yet, and Square-1 wants a two-phase solver.
  What this will never do is *substitute* — a fake Megaminx scramble built from 3×3 moves would
  look right, which is worse than admitting the gap.
- **Tokens are in plain DataStore**, not a Keystore-backed cipher. App-private on a healthy
  device; not the same as encrypted at rest.
- **`GET /scramble`, `/sync` conflict resolution and tournaments** are wired in shape but not
  exercised — the backend is being built alongside this.
- **Screens are structural, not final.** Every screen exists, is themed and is navigable, but
  the Figma frames have more detail in them than this pass implements.

## Related repos

- [`cubeclash-backend`](https://github.com/donik1998/cubeclash-backend) — NestJS + PostgreSQL + Redis, the server-authoritative race server
- [`cubeclash-flutter`](https://github.com/donik1998/cubeclash-flutter) — the primary client
- [`cubeclash-ios`](https://github.com/donik1998/cubeclash-ios) — the other native client

## License

MIT © 2026 Doniyor Murodkulov

> Working title — the repo may be renamed once the product name is finalized.
