# Paper 147: Backend-for-Frontend (BFF) Pattern

Part of [Architect Notes](../../README.md) — Volume 27: Modern Frontend-Backend Architecture.

## The Question

A client needs data from three backend services to render one screen. Does fetching them in parallel instead of one-after-another actually make the page more reliable, or just faster?

## Structure

- `pressure/` — A client calls three downstream services sequentially (User, Orders, Inventory).
  Verified: User succeeds (80ms), Orders fails (150ms, elapsed 230ms), and the client aborts before
  ever calling Inventory — the whole page render fails, discarding the User data that already
  succeeded.
- `wrong-direction/` — A Backend-for-Frontend fetches all three in parallel instead (latency
  bounded by the slowest call, 150ms), but its aggregation is still all-or-nothing. Verified: any
  one downstream failure discards every result, including the two that succeeded — faster, but the
  same "one failure kills the whole page" problem.
- `principle-applied/` — Same parallel BFF fan-out, but each call's outcome is aggregated
  independently instead of all-or-nothing. Verified: a failed call becomes an explicit "section
  unavailable" placeholder, and the page renders with 2 of 3 sections populated instead of failing
  entirely.

## Running It

Each scenario is self-contained:

```bash
javac -Xlint:all *.java
java Main
```

## Full Write-Up

See [MEDIUM-DRAFT.md](../../../architect-notes-private/volume-27-modern-frontend-backend-architecture/paper-147-backend-for-frontend-pattern/MEDIUM-DRAFT.md)
(private repo) for the full analysis, comparison table, and interview Q&A.
