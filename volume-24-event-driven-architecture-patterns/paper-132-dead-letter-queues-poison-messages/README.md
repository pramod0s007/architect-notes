# Paper 132: Dead Letter Queues & Poison Message Handling

Part of *Architect Notes* Volume 24 — Event-Driven Architecture Patterns.

## Scenario

A message queue processes events one at a time. One event ("poison") always fails processing, no matter how many times it's retried, because the problem is in the message itself, not in the environment trying to process it.

## The Three Versions

### `pressure/`
A naive consumer retries the same failed message forever instead of moving on. The poison message blocks every healthy message behind it — 0 of 3 messages get processed even after 5 retries (capped only so the demo terminates; a real consumer configured this way retries indefinitely).

### `wrong-direction/`
Retry a bounded number of times (3), then silently drop the message and move on. This unblocks the queue — 2 of 3 messages process normally — but the poison message's data is now gone forever, with no record it ever existed.

### `principle-applied/`
Retry a bounded number of times, then move the message to a separate Dead Letter Queue instead of dropping it. The queue unblocks exactly like the wrong-direction version, but the poison message's data is preserved — available for inspection or replay.

## Running

Each folder is self-contained. From within a folder:

```bash
javac -Xlint:all *.java
java Main
```

## Key Takeaway

A consumer that retries the same failed message before advancing has no way to distinguish "this will eventually succeed" from "this will never succeed" — a poison message exploits exactly that blind spot. Unblocking the queue by dropping the message trades a visible failure (a stuck queue) for an invisible one (lost data). A dead letter queue gets both: the healthy messages keep moving, and the failed message's data is never lost.
