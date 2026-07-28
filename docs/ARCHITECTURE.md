# Architecture

## The shape

**Presentation → Domain ← Data.** Dependencies point inward; the domain knows nothing about
Android, Retrofit, Room or Compose.

```
┌──────────────────────────────────────────────────────────┐
│ feature/timer · race · stats · profile · auth            │  Compose + ViewModel
│   immutable UiState down, Action up                      │
└───────────────┬──────────────────────────────────────────┘
                │ use cases + repository interfaces
┌───────────────▼──────────────────────────────────────────┐
│ core/domain  (pure JVM)                                  │
│   TimerStateMachine · Averages · ScrambleGenerator       │
│   SolveRepository · RaceGateway · TokenStore …           │
└───────────────▲──────────────────────────────────────────┘
                │ implementations
┌───────────────┴──────────────────────────────────────────┐
│ core/data → core/network · core/database · core/datastore│
│ core/realtime (Socket.IO)                                │
└──────────────────────────────────────────────────────────┘
```

## Decisions, and what they cost

### Multi-module with convention plugins

Sixteen modules is more ceremony than one, and it buys two things worth the ceremony: the
module boundary is the only enforcement of the layering that survives a deadline, and Gradle
can build and cache the untouched two-thirds of the graph. `build-logic/` keeps each module's
build file at five lines — the alternative, copy-pasted `android { }` blocks, is where
multi-module projects usually rot.

`core/model` and `core/domain` are **`kotlin-jvm`, not `android-library`**. That is the load-bearing
part: there is no `android.jar` on their compile classpath, so a stray `Context` import fails to
compile rather than being noticed in review.

### The fake/real switch is one Gradle property

`cubeclash.useFakeData` decides, in exactly one Hilt module, whether the app runs on
`cubeclash-backend` or on in-memory fakes. This is copied deliberately from the Flutter client,
where it is what allowed a complete, demoable, screenshot-able app to exist before the server
did. The fakes implement the same interfaces, so nothing above the data layer can tell.

`FakeRaceGateway` goes further: it plays the *real* protocol in the real order
(`waiting → ready-check → countdown → scramble → racing → settled`) and always settles the room
itself, exactly as the server would — because the client must never be the thing that decides.

### Local-first solves

`SolveRepositoryImpl` writes to Room and returns; the network call is a follow-up that flips
`sync_state`. A timer that refuses to record a solve because a train went into a tunnel is a
broken timer. The `client_id` minted in `LogSolveUseCase` is the idempotency key the server
upserts on, so the eventual retry cannot duplicate anything.

The cost: the UI can show a row the server has never heard of. That is why `SyncState` is on the
domain model and why the solve row renders a *Syncing* chip — being honest about it is cheaper
than pretending it cannot happen.

### The timer state machine is pure

Every transition is `(state, event, now) → state`. No coroutines, no `System.currentTimeMillis()`.
The ViewModel supplies the clock and pumps ticks only while something is actually counting. The
result is that the inspection ladder — clean → `+2` at 15s → DNF at 17s — is a table of unit
tests that run in microseconds, rather than something verified by holding a phone.

### The event table is tested against the Regulations

`WcaEventTest` asserts by regulation number (9b1a, 9b3a, 9b5a, 9f2, 9f12). An edit that
contradicts the WCA fails with a pointer to the authority. It is also the contract test against
the backend's own registry: the two are separate code, and the shared facts are what keep them
honest.

### Icons are composed, not enumerated

Seventeen events are eleven puzzles and five disciplines. `PuzzleIcon` draws a base shape plus an
optional badge, parametrically, so an eighteenth event is a row in a table rather than a new
vector file — and a 4×4 and a 4BLD read as the same puzzle at a glance, which is the actual job
of an icon in a picker.

## Threading

- ViewModels launch in `viewModelScope`; everything they call is `suspend` or a `Flow`.
- Dispatchers are **injected** (`@Dispatcher(IO)` / `@Dispatcher(Default)`), never referenced
  statically, so tests can run on a virtual clock.
- Room and DataStore already move off the main thread; the repositories do not double-wrap them.

## Error handling

`DataResult<T>` = `Success | Failure(AppError)`. `ApiErrorMapper` is the border: above it,
nothing knows what an HTTP status code is. Kotlin's own `Result` is not used because it carries a
`Throwable`, and by the time a value reaches the domain the throwable has already been translated
into something the UI can render.

## What is not here yet

See *Deliberate gaps* in the README. The short version: random-*state* scramblers, encrypted token
storage, offline sync conflict resolution, and the final visual pass on the screens.
