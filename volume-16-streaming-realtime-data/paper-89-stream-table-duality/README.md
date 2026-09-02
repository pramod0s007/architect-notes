# Stream-Table Duality — A Table Is a Fold, Not a Second Copy

**Closes Volume 16** — Streaming & Real-Time Data Architecture, following on from Paper 88 (Windowing and Watermarks).

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

A table maintained via a separate, independent write path from its source stream can silently drift the moment either write fails without the other (measured: one account's balance in a "live" table permanently disagreed with what the stream itself recorded, with no signal it had happened). A table defined purely as a fold of its stream has no independent existence to drift with (measured: the table before and after a simulated crash were identical, by construction). This paper then shows the overcorrection: a table with no dual write at all — a genuine single source of truth — can still drift if the fold processes events out of their true per-key offset order (measured: a correctly single-sourced table still produced the wrong final balance).

## The Measurements

- **Dual write (pressure)**: the stream correctly recorded an account's final balance; a separate, independent table write for that same event failed silently, leaving the table at a stale value.
- **Fold the stream (principle-applied)**: the table before and after a simulated crash are identical, because both are the same fold of the same stream — there is no second path to drift with.
- **Fold, but merge-delivery order (wrong-direction)**: no dual write, single source of truth — and the table still drifted, because two updates for the same key were folded out of their true offset order.

## The Finding

**"Derived from the stream" and "folded in true offset order" are two independent requirements for a table to be trustworthy.** Satisfying only one still produces a table that silently disagrees with its own source of truth — by a different mechanism than a dual write, but with the identical symptom.

## Code Examples in This Repo

| Example | What It Shows |
|---------|--------------|
| [`pressure/`](./pressure/) | Dual write — measured a table silently drifting from its own stream |
| [`principle-applied/`](./principle-applied/) | Table = fold(stream) — measured identical rebuild after a simulated crash |
| [`wrong-direction/`](./wrong-direction/) | Single source, wrong fold order — measured drift despite no dual write |

### How to Run

```bash
cd volume-16-streaming-realtime-data/paper-89-stream-table-duality/pressure
javac *.java && java Main

cd volume-16-streaming-realtime-data/paper-89-stream-table-duality/principle-applied
javac *.java && java Main

cd volume-16-streaming-realtime-data/paper-89-stream-table-duality/wrong-direction
javac *.java && java Main
```
