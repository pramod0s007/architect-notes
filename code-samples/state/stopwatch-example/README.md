# StopWatch State Pattern Example

Runnable sample for **State Pattern** (Paper 05).

Contrasts with if-else state checks in:

`volume-1-thinking-like-an-architect/paper-01/code/02-stopwatch-state-explosion.java`

## Run

```bash
cd code-samples/state/stopwatch-example
javac *.java
java Main
```

## API

| Method | Typical transition |
|--------|-------------------|
| `start()` | IDLE → RUNNING |
| `pause()` | RUNNING → PAUSED |
| `resume()` | PAUSED → RUNNING |
| `stop()` | RUNNING/PAUSED → IDLE |

Invalid transitions throw `IllegalStateException`.
