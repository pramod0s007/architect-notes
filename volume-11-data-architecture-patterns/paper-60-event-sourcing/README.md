# Event Sourcing — The Ledger That Remembers Everything

**Continues Volume 11** — picking up right after Paper 59 (CQRS).

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

Paper 59's write model tracks only a running balance — correct, but with zero memory of the individual transactions that produced it (measured: a "list transactions over $50" report is not recoverable, ever). Event sourcing fixes this by storing every event as the durable record, computing balance and any other view by replaying the log (measured: the identical report, invented after the fact, answered correctly by replaying history). This paper then shows the overcorrection: storing the resulting balance after each transaction instead of the event itself, which reconciles the numbers but permanently loses why they changed.

## The Measurements

- **Balance only**: correct 71,500-cent final balance; "list transactions over $50" — not recoverable.
- **Event log**: same 71,500-cent balance; the same report, invented after recording, correctly returns the 2 qualifying transactions.
- **Balance snapshots (wrong direction)**: transaction amounts recoverable by diffing snapshots, but the transaction *type* (deposit/withdrawal/fee) was accepted as a parameter and never stored — "total fees collected" is permanently unanswerable.

## The Finding

**Storing "what changed" is not the same as storing "what happened."** An event log lets you answer questions nobody anticipated when the events were recorded. A snapshot of resulting state, however frequently taken, only ever tells you the numbers — never the reason they changed.

## Code Examples in This Repo

| Example | What It Shows |
|---------|--------------|
| [`pressure/`](./pressure/) | Balance-only ledger — measured a report request that's permanently unanswerable despite a correct balance |
| [`principle-applied/`](./principle-applied/) | Event-sourced ledger — measured the same report answered correctly by replaying the log |
| [`wrong-direction/`](./wrong-direction/) | Balance-snapshot ledger — measured amounts recoverable by diffing but transaction type permanently lost |

### How to Run

```bash
cd volume-11-data-architecture-patterns/paper-60-event-sourcing/pressure
javac *.java && java Main

cd volume-11-data-architecture-patterns/paper-60-event-sourcing/principle-applied
javac *.java && java Main

cd volume-11-data-architecture-patterns/paper-60-event-sourcing/wrong-direction
javac *.java && java Main
```
