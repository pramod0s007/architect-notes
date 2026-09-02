# Distributed Snapshotting — Consistent Cuts Across Nodes With No Shared Clock

**Closes Volume 15** — picking up right after Paper 82 (Replication Lag).

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

Independently-timed local snapshots can silently lose a message that's genuinely in flight between two nodes (measured: a $50 transfer vanished entirely from the recorded global state, present in neither node's snapshot). A marker sent on the same FIFO channel as real messages guarantees the receiver processes anything sent before the sender's snapshot, before taking its own (measured: the identical transfer was correctly reflected in the recorded total, with zero clock synchronization). This paper then shows the overcorrection: a fixed safety delay before snapshotting, which is a bet about message timing rather than a guarantee (measured: a "generous" 5-tick margin still failed against an 8-tick real delay, reproducing the identical lost-message bug).

## The Measurements

- **Independent local snapshots**: recorded total 50, actual total 100 — a $50 transfer in flight vanishes from the record.
- **Marker on the real FIFO channel**: recorded total 100, matching reality exactly — no clock synchronization involved.
- **Fixed safety delay (wrong direction)**: recorded total 50 again — an assumed 5-tick margin failed against an actual 8-tick delivery delay.

## The Finding

**A consistent distributed snapshot doesn't need to correspond to one real physical instant — it needs to reconstruct a state the system could genuinely have been in, and channel-ordering, not clock synchronization, is what makes that possible.**

## Code Examples in This Repo

| Example | What It Shows |
|---------|--------------|
| [`pressure/`](./pressure/) | Independent local snapshots — measured a $50 transfer vanishing from the recorded global state |
| [`principle-applied/`](./principle-applied/) | Marker sent on the real FIFO channel — measured the recorded total exactly matching reality |
| [`wrong-direction/`](./wrong-direction/) | Fixed safety delay instead of a marker — measured the identical lost-message bug under real delay variance |

### How to Run

```bash
cd volume-15-consensus-distributed-coordination/paper-83-distributed-snapshotting/pressure
javac *.java && java Main

cd volume-15-consensus-distributed-coordination/paper-83-distributed-snapshotting/principle-applied
javac *.java && java Main

cd volume-15-consensus-distributed-coordination/paper-83-distributed-snapshotting/wrong-direction
javac *.java && java Main
```
