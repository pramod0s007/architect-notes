# Paper 134: Exactly-Once Processing via Deduplication

Part of *Architect Notes* Volume 24 — Event-Driven Architecture Patterns.

## Scenario

Deduplicating by message id (checking whether an id was already processed) is the standard way to build exactly-once processing on top of at-least-once delivery. But a dedup store can't remember every id forever — ids get reused over the lifetime of a long-running system, and the store's own retention window has to be actively managed. Choose it wrong in either direction and you reopen a failure mode.

## The Three Versions

### `pressure/`
A dedup store with a retention window (1 offset) narrower than the broker's actual guaranteed maximum redelivery lag (3 offsets). A legitimate, in-guarantee redelivery arrives after its dedup record was already evicted, and gets falsely reprocessed as brand new (4 processed, expected 3).

### `wrong-direction/`
"Fix" the too-small window by never evicting anything. This correctly catches the real redelivery — but much later, after a message id has genuinely been recycled by the upstream system for a completely unrelated new message, the dedup store still holds the old record and wrongly skips the new message entirely (3 processed, expected 4 — one message silently lost).

### `principle-applied/`
Set the retention window to actually match the broker's real guaranteed maximum redelivery lag. The real redelivery is still caught; the genuinely new message reusing an old id, arriving well outside the guarantee, is correctly processed (4 processed, exactly as expected).

## Running

Each folder is self-contained. From within a folder:

```bash
javac -Xlint:all *.java
java Main
```

## Key Takeaway

A dedup store that never evicts anything isn't the safe default it looks like — it just delays the failure until an id gets reused, at which point it silently drops a genuinely new message instead of duplicating an old one. The correct retention window has to be derived from a real, verifiable property of the delivery system's guarantee, not picked as either an arbitrarily small number or "never" by default.
