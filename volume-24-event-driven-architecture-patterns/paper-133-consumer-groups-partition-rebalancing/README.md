# Paper 133: Consumer Groups & Partition Rebalancing

Part of *Architect Notes* Volume 24 — Event-Driven Architecture Patterns.

## Scenario

A partition in a consumer group is owned by exactly one consumer at a time. When a rebalance reassigns ownership, there's a dangerous window between a consumer being revoked and that consumer finding out — during which it's a "zombie" that can still take real actions, including committing offsets.

## The Three Versions

### `pressure/`
A naive partition blindly accepts any commit, from any consumer, at any time. Consumer A keeps processing after being revoked (unaware), while Consumer B independently starts from the last committed offset. Messages 2, 3, and 4 are each fully processed twice.

### `wrong-direction/`
"Fix" the duplicate-processing problem by having a revoked consumer commit optimistically — the offset for its entire assigned batch, assuming it would have finished. This stops duplicates, but since Consumer A was revoked before actually reaching message 4, the committed offset now points past a message nobody ever processed. Message 4 is silently skipped forever.

### `principle-applied/`
Epoch (generation) fencing: every rebalance bumps the partition's epoch, and a commit is only accepted if it carries the current epoch. Consumer A's stale-epoch commit is rejected; Consumer B's current-epoch commit succeeds. The committed offset always reflects real, confirmed progress — no message is ever skipped, though bounded at-least-once duplication remains (a safer, well-understood trade-off).

## Running

Each folder is self-contained. From within a folder:

```bash
javac -Xlint:all *.java
java Main
```

## Key Takeaway

The dangerous window in any consumer group isn't the rebalance itself — it's the gap between a consumer being revoked and that consumer finding out. "Fixing" duplicate processing by committing optimistically just trades a visible, tolerable failure (duplicates) for an invisible, permanent one (silently skipped messages). Epoch fencing doesn't prevent a zombie from doing work — it only prevents that work's commit from being trusted, which is enough to guarantee the offset never advances past unconfirmed data.
