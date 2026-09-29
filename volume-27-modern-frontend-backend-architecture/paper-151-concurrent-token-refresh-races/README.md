# Paper 151: Concurrent Token Refresh Races

Part of [Architect Notes](../../README.md) — Volume 27: Modern Frontend-Backend Architecture.

## The Question

Three requests discover their access token expired at nearly the same moment and each triggers its
own refresh. If the server uses single-use, rotating refresh tokens, what happens?

## Structure

- `pressure/` — No coordination at all: each of three calls independently refreshes using the token
  it observed. Verified: only the first refresh succeeds; the second and third are rejected for
  using an already-rotated refresh token, even though, under this modeled server's reuse policy,
  the session would otherwise have remained perfectly valid.
- `wrong-direction/` — A naive in-progress guard flag: if a refresh is already running, other calls
  give up immediately instead of waiting. Verified: only one refresh call is made, but the two
  callers that saw the flag set fail anyway, even though the in-flight refresh succeeds moments later.
- `principle-applied/` — Singleflight: a call joins an existing in-flight refresh instead of starting
  its own or giving up. Verified: exactly one refresh call hits the server, and all three original
  callers succeed by sharing its result.

## Running It

Each scenario is self-contained:

```bash
javac -Xlint:all *.java
java Main
```

## Full Write-Up

See [MEDIUM-DRAFT.md](../../../architect-notes-private/volume-27-modern-frontend-backend-architecture/paper-151-concurrent-token-refresh-races/MEDIUM-DRAFT.md)
(private repo) for the full analysis, comparison table, and interview Q&A.
