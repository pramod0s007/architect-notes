# Replication Lag & Read-Your-Writes Consistency — A Fixed Wait Is a Guess, Not a Guarantee

**Pattern:** Reading a replica immediately after a write (write succeeds on primary, replica read is stale — read-your-writes violated) → a fixed wait before reading the replica (works when actual lag is short, fails silently — returning the previous value, not an error — when actual lag exceeds the wait) → a version-tracked read (client remembers its write's version, reads the replica only once it's caught up, otherwise falls back to the primary — correct regardless of actual lag)

**Volume 23** (Database Internals for Architects) — Paper 128.

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

Read replicas exist to scale reads without adding load to the primary — but they only reflect the primary's state after some real, variable delay, because replication takes time and that time is not constant. A read-your-writes guarantee is not something a replica gives you automatically; a write succeeding on the primary and a read immediately afterward being routed to the replica can see the old value, because replication simply hasn't delivered it yet (verified: write succeeds, immediate replica read returns null, read-your-writes violated). The obvious patch — wait a fixed amount of time before reading the replica — works exactly as long as the actual lag stays under whatever constant was chosen, and fails silently, returning the previous value rather than an error, the moment it doesn't (verified: a 2-tick fixed wait is correct against a 1-tick actual lag, and wrong against a 5-tick actual lag). The actual fix tracks the version number the client's own write returned and only serves that client's read from the replica once the replica has caught up to it, falling back to the primary otherwise (verified: correct under both a 1-tick and a 5-tick actual lag, and back to serving from the replica the moment it catches up).

## The Pressure: Reading Your Own Write From a Lagging Replica

Replication is asynchronous: a write can complete and be fully durable on the primary well before it has propagated to any replica. A read immediately routed to a replica in that window isn't hitting a bug in the replica — the replica is correctly reporting the only state it actually has. The bug is in assuming a read right after a write, on a different node, will already reflect it.

## The Principle

**Read-your-writes has to be built on an actual signal that a specific write has arrived, not a time-based assumption about how long replication usually takes.** A fixed wait is a guess about lag, and lag is not constant — it depends on replica load, network conditions, and write volume, none of which stay fixed. The fix isn't a longer wait; it's tracking the version (or log position) a write actually produced, and only trusting a replica's read once its applied version has caught up to that specific version — falling back to the primary, conditionally and temporarily, whenever it hasn't.

## Pros

- Keeps read load off the primary for the common case, verified serving reads from the replica once it has genuinely caught up, not just falling back on every request
- Correctness holds regardless of how long replication actually takes, verified with both a 1-tick and a 5-tick actual lag, with no constant to tune or guess
- The fallback to the primary is self-limiting: it only happens while the replica is provably behind the client's own last write, not indefinitely

## Cons

- Requires the write path to return a version (or equivalent position marker) and the read path to carry it forward — this is a real API and plumbing change, not a configuration flag
- Falling back to the primary under lag still adds load to the primary in exactly the situations (replica behind, possibly because it's under load) where that load is least welcome
- Doesn't help with reads that need a *different* client's recent write (cross-client read-your-writes) — this specific mechanism only tracks the current client's own last write

## When Reading Straight From the Replica, No Version Check, Is Still Acceptable

- The read doesn't need to reflect the current client's own very recent write at all — e.g. a public dashboard, an analytics report, or any read that's inherently fine being eventually consistent
- The application already routes a user's own post-write reads back to the primary for a short, deliberately conservative fixed window as a coarser, cheaper approximation, and has explicitly accepted the residual risk that window doesn't cover
- Replication lag in the actual deployment is reliably and verifiably small enough (with monitoring, not assumption) relative to how soon a read-your-writes read can occur

## Code Examples in This Repo

| Example | Domain | What It Shows |
|---------|--------|--------------|
| [`pressure`](./pressure) | No lag handling | A read routed to the replica immediately after a write sees the old value — read-your-writes violated |
| [`wrong-direction`](./wrong-direction) | Fixed wait before reading replica | Correct against a 1-tick actual lag; silently wrong (stale, not null) against a 5-tick actual lag |
| [`principle-applied`](./principle-applied) | Version-tracked read | Correct against both a 1-tick and a 5-tick actual lag; falls back to primary only while genuinely behind |

### How to Run

```bash
cd volume-23-database-internals-for-architects/paper-128-replication-lag-read-your-writes/pressure
javac *.java && java Main

cd volume-23-database-internals-for-architects/paper-128-replication-lag-read-your-writes/wrong-direction
javac *.java && java Main

cd volume-23-database-internals-for-architects/paper-128-replication-lag-read-your-writes/principle-applied
javac *.java && java Main
```
