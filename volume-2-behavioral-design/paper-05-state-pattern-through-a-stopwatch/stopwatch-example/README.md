# StopWatch — State Pattern

## What This Demonstrates

A `StopWatch` with three states — IDLE, RUNNING, PAUSED. Each state owns its
valid transitions and throws `IllegalStateException` on invalid ones. The
`StopWatch` context holds no conditional logic; it delegates every operation to
the current `WatchState` object, which transitions the context by calling
`transitionTo(new NextState())`.

**Pressure: State Explosion** — 3 states x 4 operations = 12 conditional
branches to maintain without the State Pattern. Each new state multiplies the
existing branch count.

## State Transition Diagram

```
         start()
  [IDLE] ────────────────────→ [RUNNING]
    ↑                              │
    │  stop()                   pause()
    │                              │
    │                              ↓
    └───────── stop() ────────  [PAUSED]
                                   │
                               resume()
                                   │
                                   └──────────────→ [RUNNING]
```

Valid transitions summary:

```
IDLE    : start() → RUNNING   | stop() → IDLE (no-op)
RUNNING : pause() → PAUSED    | stop() → IDLE
PAUSED  : resume() → RUNNING  | stop() → IDLE
```

Any other call (e.g., `start()` while RUNNING) throws `IllegalStateException`.

## Class Diagram

```
<<interface>>
WatchState
+ start(watch: StopWatch): void
+ pause(watch: StopWatch): void
+ resume(watch: StopWatch): void
+ stop(watch: StopWatch): void
+ name(): String
        △
        |
   ─────────────────────────────
   |            |               |
IdleState   RunningState   PausedState

StopWatch                              [context]
- state: WatchState
- elapsedMillis: long
- segmentStartMillis: long
+ start() / pause() / resume() / stop()  → delegates to state
+ stateName(): String
+ elapsedMillis(): long
~ transitionTo(next: WatchState): void   ← called by states
~ beginSegment() / endSegment(): void    ← called by states
```

## Sequence / Flow

```
Client
  │
  ├─ watch.start()
  │       └─ IdleState.start(watch)
  │               └─ watch.beginSegment()
  │               └─ watch.transitionTo(new RunningState())
  │
  ├─ watch.pause()
  │       └─ RunningState.pause(watch)
  │               └─ watch.endSegment()        ← accumulates elapsed
  │               └─ watch.transitionTo(new PausedState())
  │
  ├─ watch.resume()
  │       └─ PausedState.resume(watch)
  │               └─ watch.beginSegment()
  │               └─ watch.transitionTo(new RunningState())
  │
  └─ watch.stop()
          └─ RunningState.stop(watch)
                  └─ watch.endSegment()
                  └─ watch.transitionTo(new IdleState())
```

## Design Decisions

- **States return void but call `transitionTo()`** — the state itself decides
  the next state and installs it. The context never makes that decision.
- **`beginSegment()` / `endSegment()` are package-private on `StopWatch`** —
  state classes are in the same package and use them to accumulate real wall-
  clock time. External callers cannot accidentally corrupt timing.
- **New state objects on every transition** — each `IdleState`, `RunningState`,
  `PausedState` is constructed fresh. These are tiny value objects; no shared
  mutable state, no reuse complexity.
- **`stop()` in `IdleState` is a no-op** — already stopped; throwing here would
  be a caller trap. The rule: throw only when the operation is genuinely
  nonsensical (e.g., `pause()` while already idle).

## How to Run

```bash
cd volume-2-behavioral-design/paper-05-state-pattern-through-a-stopwatch/stopwatch-example
javac *.java && java Main
```

Expected output (timings will vary):

```
Initial: IDLE
start  -> RUNNING
pause  -> PAUSED, elapsed=50ms
resume -> RUNNING
stop   -> IDLE, elapsed=80ms
```

## When to Apply

- An object has 3+ distinct states and behavior differs meaningfully across
  them for the same method call.
- Invalid transitions must be caught and fail loudly rather than silently
  ignored.

## When NOT to Apply

- Two states with a simple boolean flag — the State Pattern adds four files
  for a problem solved by `if (running)`.
