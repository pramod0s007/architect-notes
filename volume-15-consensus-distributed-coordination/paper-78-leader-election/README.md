# Leader Election — Why a Naive Race Produces Two Leaders and a Majority Vote Can't

**Opens Volume 15** — Consensus & Distributed Coordination.

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

A naive "declare yourself leader" race can produce two simultaneous leaders under a network partition (measured: two nodes both believed they were leader and recorded conflicting values for the same key). Requiring a strict majority of the whole cluster, not just reachable nodes, makes two simultaneous leaders mathematically impossible (measured: the minority partition correctly elected no leader at all). This paper then shows the overcorrection: a time-bound lease, which prevents two nodes from believing they're leader simultaneously but not a stale write already in flight from a leader whose lease has since expired (measured: a former leader's late write silently overwrote the current leader's already-applied value).

## The Measurements

- **First-to-declare election**: 2 leaders active simultaneously during a partition, conflicting values recorded (100 vs. 250) for the same key.
- **Majority-vote election**: exactly 1 leader elected across the entire cluster; the minority partition correctly elects none.
- **Lease without fencing (wrong direction)**: a stale write from a former leader (value=999) silently overwrites the current leader's already-applied write (value=200).

## The Finding

**Election answers "who's allowed to lead"; a fencing token answers "is this specific write actually from whoever's leading right now" — they are different guarantees, and a real system needs both.** Majority voting eliminates simultaneous leaders by construction. A lease alone does not protect against a stale write that was already in flight before the lease expired.

## Code Examples in This Repo

| Example | What It Shows |
|---------|--------------|
| [`pressure/`](./pressure/) | First-to-declare election — measured two simultaneous leaders under a network partition |
| [`principle-applied/`](./principle-applied/) | Majority-vote election — measured exactly one leader, minority partition correctly elects none |
| [`wrong-direction/`](./wrong-direction/) | Lease without a fencing token — measured a stale write silently overwriting the current leader's write |

### How to Run

```bash
cd volume-15-consensus-distributed-coordination/paper-78-leader-election/pressure
javac *.java && java Main

cd volume-15-consensus-distributed-coordination/paper-78-leader-election/principle-applied
javac *.java && java Main

cd volume-15-consensus-distributed-coordination/paper-78-leader-election/wrong-direction
javac *.java && java Main
```
