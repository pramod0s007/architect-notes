# Paper 150: Stale-While-Revalidate Caching

Part of [Architect Notes](../../README.md) — Volume 27: Modern Frontend-Backend Architecture.

## The Question

A client caches a value so reads are instant. What happens when the value changes on the server —
and does the cache have any way of finding out?

## Structure

- `pressure/` — A cache with no revalidation mechanism at all. Verified: every read is instant, but
  once the server-side price changes, every subsequent read stays permanently wrong — there is no
  path back to correctness.
- `wrong-direction/` — Overcorrects to revalidating synchronously on every single read, with no
  fallback if the revalidation fails. Verified: every read now blocks on the network, and a single
  transient server failure takes the display down entirely, discarding a perfectly good cached value.
- `principle-applied/` — Stale-while-revalidate: every read is served instantly from cache, a
  background revalidation attempt runs alongside it, the cache updates on success, and the last
  known-good value is kept (flagged possibly-stale) on failure. Verified: reads stay instant,
  the display self-corrects once the server value changes, and a later revalidation failure doesn't
  break anything.

## Running It

Each scenario is self-contained:

```bash
javac -Xlint:all *.java
java Main
```

## Full Write-Up

See [MEDIUM-DRAFT.md](../../../architect-notes-private/volume-27-modern-frontend-backend-architecture/paper-150-stale-while-revalidate-caching/MEDIUM-DRAFT.md)
(private repo) for the full analysis, comparison table, and interview Q&A.
