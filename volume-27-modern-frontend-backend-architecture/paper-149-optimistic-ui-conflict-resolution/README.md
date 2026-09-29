# Paper 149: Optimistic UI Updates & Conflict Resolution

Part of [Architect Notes](../../README.md) — Volume 27: Modern Frontend-Backend Architecture.

## The Question

An optimistic UI update shows the result of a user's action instantly, before the server confirms it. When the server's response finally arrives, does the client know whether it's actually the newest information available?

## Structure

- `pressure/` — A "like" button applies optimistic increments locally and never reads the server's
  responses at all. Verified: local count ends at 12, but a concurrent like from a different user
  pushed the true server count to 13 — the client never finds out, because it never reconciles.
- `wrong-direction/` — The client reconciles with server responses, but applies whichever one
  arrives, in arrival order, with no way to tell an old response from a new one. Verified: the
  correct count (13) is briefly displayed when the newer response arrives first, then a
  stale, out-of-order response overwrites it back down to 11.
- `principle-applied/` — Each request is tagged with a monotonically increasing sequence number;
  a response is only applied if its sequence exceeds the highest one already applied. Verified:
  the same out-of-order arrival, but the stale response is now correctly discarded, and the
  display stays converged on the true count (13).

## Running It

Each scenario is self-contained:

```bash
javac -Xlint:all *.java
java Main
```

## Full Write-Up

See [MEDIUM-DRAFT.md](../../../architect-notes-private/volume-27-modern-frontend-backend-architecture/paper-149-optimistic-ui-conflict-resolution/MEDIUM-DRAFT.md)
(private repo) for the full analysis, comparison table, and interview Q&A.
