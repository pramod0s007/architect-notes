# Paper 140: Retry Storms in Service Mesh Sidecars

Part of [Architect Notes](../../README.md) — Volume 25: Cloud-Native & Kubernetes Architecture.

## The Question

If exponential backoff already reduces how often each client retries, why can a retry
storm still hit a downstream service exactly as hard as if there were no backoff at all?

## Structure

- `pressure/` — 50 client sidecars, all with a fixed 1-tick retry interval and no backoff or
  jitter, fail simultaneously after a shared downstream blip. Verified: 150 total retry
  attempts, 4 of 5 ticks over capacity, fully drained by tick 5 — but only by hammering a
  10-unit-per-tick capacity ceiling in perfect lockstep every single tick.
- `wrong-direction/` — Exponential backoff is added (interval doubles: 1, 2, 4, 8, 16...),
  but every client still started at the same instant and follows the identical deterministic
  schedule. Verified: total attempts and storm-tick count are *unchanged* (150 and 4) — the
  same full-herd collisions still happen, just spaced further apart — while the last client's
  response time balloons from tick 5 to tick 121.
- `principle-applied/` — Exponential backoff *with* full jitter (each client's delay is
  randomized within the current backoff window, AWS-style, using a fixed seed for
  reproducibility). Verified: total attempts fall to 64 (from 150), max simultaneous demand
  falls from a full 50-client stampede to 15, and the full-capacity collision never recurs —
  at the honest cost of a slower drain (tick 15) than the naive best case (tick 5).

## Running It

Each scenario is self-contained. From inside any of the three directories:

```bash
javac -Xlint:all *.java
java Main
```

## Full Write-Up

See [MEDIUM-DRAFT.md](../../../architect-notes-private/volume-25-cloud-native-kubernetes-architecture/paper-140-retry-storms-service-mesh-sidecars/MEDIUM-DRAFT.md)
(private repo) for the full analysis, comparison table, and interview Q&A.
