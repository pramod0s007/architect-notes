# Change Data Capture — Polling a Table Is Not the Same as Capturing Its History

**Continues Volume 16** — picking up right after Paper 85 (Exactly-Once Semantics).

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

Polling a table's current state loses every intermediate transition and every deleted row within the poll interval (measured: 5 real writes across 2 rows produced exactly 1 captured event, with a whole insert-and-delete lifecycle invisible). Tailing the database's own transaction log fixes both problems at once (measured: the same 5 writes produced exactly 5 captured events, an exact match). This paper then shows the overcorrection: compacting a correctly-tailed log by key before publishing, which still catches deletes correctly but discards intermediate transitions in exactly the same way naive polling did (measured: a real state transition was silently lost even though the delete was preserved).

## The Measurements

- **Poll current state (pressure)**: 5 real writes produce 1 captured event; a deleted row leaves no event at all.
- **Log tailing, uncompacted (principle-applied)**: 5 real writes produce 5 captured events, an exact match, including an explicit delete tombstone.
- **Log tailing, compacted by key (wrong-direction)**: the delete is still captured correctly, but an intermediate status transition is compacted away — a consumer counting transitions undercounts.

## The Finding

**The correct capture strategy depends on what the consumer actually needs: current state, or complete history.** Log tailing is a strictly better data source than polling, but publishing decisions made downstream of the log — like compaction — can silently reintroduce the exact same information loss for consumers that need full history, while remaining exactly correct for consumers that don't.

## Code Examples in This Repo

| Example | What It Shows |
|---------|--------------|
| [`pressure/`](./pressure/) | Polling current state — measured 1 captured event from 5 real writes |
| [`principle-applied/`](./principle-applied/) | Log tailing, uncompacted — measured an exact 5-of-5 capture including a delete tombstone |
| [`wrong-direction/`](./wrong-direction/) | Log tailing, compacted by key — measured a correctly-captured delete alongside a silently lost transition |

### How to Run

```bash
cd volume-16-streaming-realtime-data/paper-86-change-data-capture/pressure
javac *.java && java Main

cd volume-16-streaming-realtime-data/paper-86-change-data-capture/principle-applied
javac *.java && java Main

cd volume-16-streaming-realtime-data/paper-86-change-data-capture/wrong-direction
javac *.java && java Main
```
