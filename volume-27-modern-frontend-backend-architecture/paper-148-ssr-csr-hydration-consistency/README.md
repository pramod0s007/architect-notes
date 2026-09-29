# Paper 148: SSR/Hydration Staleness

Part of [Architect Notes](../../README.md) — Volume 27: Modern Frontend-Backend Architecture.

## The Question

A server-rendered page paints instantly because its data was baked in at render time. Does the client's hydration logic know how old that baked-in data actually is?

## Structure

- `pressure/` — The client trusts the embedded snapshot forever, regardless of age. Verified: a
  page served immediately happens to be correct (0ms old), but a page served from a 10-minute-old
  CDN cache shows a stale, incorrect price with no indication anything changed — identical client
  logic in both runs, only the outcome differs.
- `wrong-direction/` — The client unconditionally re-fetches the current value on every hydration.
  Verified: correctness is restored in both runs, but the fresh page pays the same 180ms re-fetch
  cost as the stale one even though it needed no re-validation at all — the SSR fast-paint benefit
  is discarded every time, whether or not it was ever wrong.
- `principle-applied/` — The server embeds a render timestamp alongside the data; the client
  computes the snapshot's actual age and only re-fetches when it exceeds a declared staleness
  tolerance. Verified: the fresh page skips the re-fetch entirely (0ms cost, full benefit retained),
  and the stale page triggers exactly the 180ms re-fetch it actually needs.

## Running It

Each scenario is self-contained:

```bash
javac -Xlint:all *.java
java Main
```

## Full Write-Up

See [MEDIUM-DRAFT.md](../../../architect-notes-private/volume-27-modern-frontend-backend-architecture/paper-148-ssr-csr-hydration-consistency/MEDIUM-DRAFT.md)
(private repo) for the full analysis, comparison table, and interview Q&A.
