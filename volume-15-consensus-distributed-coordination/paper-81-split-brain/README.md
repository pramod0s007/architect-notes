# Split-Brain — "I Can't Reach It" Is Not the Same Fact as "It Stopped"

**Continues Volume 15** — picking up right after Paper 80 (The Redlock Critique).

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

A failover controller that treats "can't reach it" as "it stopped" can produce split-brain even when every individual health check behaved exactly as designed (measured: two primaries simultaneously accepted writes for the same dataset after a correctly-triggered failover). Fencing over a channel independent of the one that failed provides the missing confirmation (measured: the old primary's write was rejected before the new one was ever promoted). This paper then shows the overcorrection: a longer detection timeout, which delays a structural bug without fixing it (measured: waiting for more than three times as many failed checks reproduced the identical split-brain, just later).

## The Measurements

- **Promote on failed health checks alone**: two primaries active, recording conflicting values (100 vs. 250) for the same key.
- **Fence over an independent channel, then promote**: the old primary's write rejected, exactly one node accepts writes.
- **Longer timeout before promoting (wrong direction)**: the identical split-brain reproduces after 10 failed checks instead of 3 — delayed, not prevented.

## The Finding

**"No primary" is a safer failure state than "two primaries," and a failover controller should be willing to refuse to act when it can't positively confirm the old primary is actually gone.** A longer detection timeout controls when a structural bug happens, not whether it happens.

## Code Examples in This Repo

| Example | What It Shows |
|---------|--------------|
| [`pressure/`](./pressure/) | Promote on failed health checks, no fencing — measured two simultaneous primaries |
| [`principle-applied/`](./principle-applied/) | Fence over an independent channel before promoting — measured exactly one primary accepting writes |
| [`wrong-direction/`](./wrong-direction/) | Longer timeout, still no fencing — measured the identical split-brain, just delayed |

### How to Run

```bash
cd volume-15-consensus-distributed-coordination/paper-81-split-brain/pressure
javac *.java && java Main

cd volume-15-consensus-distributed-coordination/paper-81-split-brain/principle-applied
javac *.java && java Main

cd volume-15-consensus-distributed-coordination/paper-81-split-brain/wrong-direction
javac *.java && java Main
```
