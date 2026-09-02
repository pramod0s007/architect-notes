# Quorum Reads and Writes — Why W+R>N Is a Real Boundary, Not a Suggestion

**Continues Volume 15** — picking up right after Paper 78 (Leader Election).

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

A write and read quorum that don't have to overlap can produce a stale read even after the write already succeeded (measured: a read immediately following an acknowledged write returned the pre-write value). W+R>N guarantees any read quorum and write quorum out of the same N replicas share at least one member (measured: the guaranteed-overlapping replica correctly appeared in the read set). This paper then shows the overcorrection: satisfying W+R>N numerically while the read path returns whichever response arrives first instead of comparing versions, which throws away the guarantee it just paid for (measured: an identically-configured, numerically valid quorum still returned stale data).

## The Measurements

- **W=1, R=1 on N=3**: a read immediately after an acknowledged write returns the pre-write value — the quorums never had to overlap.
- **W=2, R=2, version comparison**: the guaranteed-overlapping replica is found and its higher version correctly selected — the read returns the latest write.
- **W=2, R=2, first-response-wins (wrong direction)**: the overlap guarantee holds, but the reader returns the fastest-arriving response instead of comparing versions — stale data returned despite a numerically valid quorum configuration.

## The Finding

**A quorum-consistent system requires three things together: ordered versions, an overlap-guaranteeing quorum size, and a read path that actually inspects every response for the highest version — not just two of the three.** W+R>N guarantees the latest version is somewhere in the response set. It does not deliver it automatically.

## Code Examples in This Repo

| Example | What It Shows |
|---------|--------------|
| [`pressure/`](./pressure/) | W=1, R=1 — measured a stale read immediately after an acknowledged write |
| [`principle-applied/`](./principle-applied/) | W=2, R=2 with version comparison — measured the read correctly returning the latest write |
| [`wrong-direction/`](./wrong-direction/) | W=2, R=2 with first-response-wins — measured a stale read despite a numerically valid quorum |

### How to Run

```bash
cd volume-15-consensus-distributed-coordination/paper-79-quorum-reads-writes/pressure
javac *.java && java Main

cd volume-15-consensus-distributed-coordination/paper-79-quorum-reads-writes/principle-applied
javac *.java && java Main

cd volume-15-consensus-distributed-coordination/paper-79-quorum-reads-writes/wrong-direction
javac *.java && java Main
```
