# Replication Lag — When Your Own Write Disappears on the Next Read

**Continues Volume 15** — picking up right after Paper 81 (Split-Brain).

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

Round-robin read routing has no way to know a reader just wrote the data they're about to read, and can return stale data even though every replica is behaving correctly (measured: a user's own just-written bio was replaced by an older one on the very next read). Routing reads by a required replication position, with fallback to the primary, guarantees freshness only when it's actually needed (measured: both a caught-up-replica case and a fallback-to-primary case returned the fresh write). This paper then shows the overcorrection: pinning a writer's session to the primary, which only solves the writer's own problem and does nothing for other readers of the same data (measured: a second, uninvolved user saw the identical stale data the original bug produced).

## The Measurements

- **Round-robin, no tracking**: a user's own write vanishes on the immediate next read, served by a replica that hadn't caught up.
- **Route by required LSN, fallback to primary**: both a caught-up-replica read and a fallback-to-primary read return the fresh write.
- **Pin writer's session to primary (wrong direction)**: the writer sees their own fresh write; a different, uninvolved reader still sees stale data moments later.

## The Finding

**"Read your own writes" and "every reader sees a write promptly" are different consistency guarantees, and a fix for one is not automatically a fix for the other.** Blanket session pinning only ever solves the narrower guarantee, at unnecessary ongoing cost to the primary.

## Code Examples in This Repo

| Example | What It Shows |
|---------|--------------|
| [`pressure/`](./pressure/) | Round-robin read routing — measured a user's own write disappearing on the immediate next read |
| [`principle-applied/`](./principle-applied/) | Route by required replication position, fallback to primary — measured freshness guaranteed in both the caught-up and fallback cases |
| [`wrong-direction/`](./wrong-direction/) | Pin the writer's session to the primary — measured the fix helping only the writer, not other readers |

### How to Run

```bash
cd volume-15-consensus-distributed-coordination/paper-82-replication-lag/pressure
javac *.java && java Main

cd volume-15-consensus-distributed-coordination/paper-82-replication-lag/principle-applied
javac *.java && java Main

cd volume-15-consensus-distributed-coordination/paper-82-replication-lag/wrong-direction
javac *.java && java Main
```
