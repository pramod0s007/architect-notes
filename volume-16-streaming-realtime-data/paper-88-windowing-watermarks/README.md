# Windowing and Watermarks — The Right Key Doesn't Help If the Window Closes Too Soon

**Continues Volume 16** — picking up right after Paper 87 (Backpressure).

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

Bucketing events by arrival time instead of event time misattributes revenue between windows whenever network delay pushes an event's arrival across a window boundary (measured: a purchase that happened in window 0 was counted in window 1 instead, a 400c shift). Bucketing by event time with a watermark driven by event-time progress and an explicit allowed-lateness budget correctly includes late arrivals in their true window (measured: the same late purchase landed in window 0's correct 1200c total). This paper then shows the overcorrection: keeping the correct event-time key but driving the watermark from arrival time instead, which reintroduces the same class of loss at the closing step instead of the bucketing step (measured: a correctly-keyed event was dropped because its window closed in the same tick it arrived).

## The Measurements

- **Arrival-time windowing (pressure)**: a purchase that happened in window 0 gets counted in window 1 — window 0 understated by 400c, window 1 overstated by the same amount.
- **Event-time key + event-time-driven watermark (principle-applied)**: window 0 correctly totals 1200c including the late arrival; window 1 correctly totals 600c; 0c dropped.
- **Event-time key + arrival-driven watermark (wrong-direction)**: window 0 totals only 800c — the late arrival is correctly keyed but dropped because the window closed one step too early.

## The Finding

**Correct bucketing and correct watermark advancement are two independent requirements.** Bucketing by event time fixes attribution. A watermark that advances based on event-time progress (not wall-clock or arrival-time progress) is what makes it safe to actually close a window. Getting only one of the two right still produces silent data loss.

## Code Examples in This Repo

| Example | What It Shows |
|---------|--------------|
| [`pressure/`](./pressure/) | Arrival-time bucketing — measured revenue misattributed between adjacent windows |
| [`principle-applied/`](./principle-applied/) | Event-time bucketing + event-time-driven watermark — measured exact, correct totals with zero drops |
| [`wrong-direction/`](./wrong-direction/) | Event-time bucketing + arrival-driven watermark — measured a correctly-keyed event dropped anyway |

### How to Run

```bash
cd volume-16-streaming-realtime-data/paper-88-windowing-watermarks/pressure
javac *.java && java Main

cd volume-16-streaming-realtime-data/paper-88-windowing-watermarks/principle-applied
javac *.java && java Main

cd volume-16-streaming-realtime-data/paper-88-windowing-watermarks/wrong-direction
javac *.java && java Main
```
