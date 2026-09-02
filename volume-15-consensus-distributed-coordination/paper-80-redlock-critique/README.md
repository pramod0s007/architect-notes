# The Redlock Critique — Distributed Locks Under Clock Drift

**Continues Volume 15** — picking up right after Paper 79 (Quorum Reads and Writes).

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

A majority-quorum distributed lock can be double-granted by a single lock server's clock running fast, without any server malfunctioning by its own standard (measured: two clients simultaneously held valid majority grants for the same lock). A fencing token at the protected resource makes the lock layer's mistake irrelevant (measured: the resource rejected the stale client's write even though both clients still believed they held the lock). This paper then shows the overcorrection: shrinking the lock's TTL to reduce the window for clock drift, which shrinks the window by the same proportion as the drift, not the underlying risk (measured: a 5x smaller TTL reproduced the identical double-grant at a smaller scale).

## The Measurements

- **Standard Redlock-style majority lock**: two clients (A and B) both hold a valid majority grant simultaneously, 20ms apart by wall-clock time, due to one server's clock running 40% fast.
- **Fencing token at the resource**: the resource accepts Client B's higher-numbered token and rejects Client A's lower, stale one — the double-grant at the lock layer never reaches the resource.
- **Shrink the TTL to 20ms (wrong direction)**: the identical double-grant reproduces at a 4ms window instead of a 20ms one — rarer, not eliminated.

## The Finding

**A distributed lock and a fencing token solve different halves of the same problem — minimizing how often two clients believe they hold a lock, and guaranteeing what happens at the resource when they both do anyway — and a real system needs both, not one instead of the other.**

## Code Examples in This Repo

| Example | What It Shows |
|---------|--------------|
| [`pressure/`](./pressure/) | Standard majority lock across 5 servers, one with a fast clock — measured two simultaneous valid grants |
| [`principle-applied/`](./principle-applied/) | Fencing token at the protected resource — measured the stale client's write rejected regardless of the lock layer's mistake |
| [`wrong-direction/`](./wrong-direction/) | Shrunk TTL as the fix — measured the identical double-grant at a smaller, still-real scale |

### How to Run

```bash
cd volume-15-consensus-distributed-coordination/paper-80-redlock-critique/pressure
javac *.java && java Main

cd volume-15-consensus-distributed-coordination/paper-80-redlock-critique/principle-applied
javac *.java && java Main

cd volume-15-consensus-distributed-coordination/paper-80-redlock-critique/wrong-direction
javac *.java && java Main
```
