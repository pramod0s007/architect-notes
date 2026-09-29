# Paper 158: Right-Sizing & Autoscaling Cost Traps

Part of [Architect Notes](../../README.md) — Volume 29: Cost-Aware Architecture (FinOps for Engineers).

## The Question

A cost-review dashboard flags every workload whose CPU request sits well above its own average
usage as an easy, safe savings opportunity — resize it down to what it typically uses and pocket
the difference. But "typical" is an average, and an average is exactly the number that erases a
workload's rare, real demand spikes into noise. How do you size a workload's request so it stays
cheap on an ordinary day without becoming too small the one day it actually needs the capacity it
already demonstrated it could need?

## Structure

- `pressure/` — Checkout's CPU request is rightsized to its own 30-day average usage (260m),
  discarding the two flash-sale days already present in that same history. Verified: when the
  same flash-sale demand (900m) repeats, the rightsized 260m request is throttled — the incident
  costs far more in lost checkout throughput than the resize ever saved.
- `wrong-direction/` — A flat 2.5x headroom multiplier is applied uniformly to every workload's
  own average, regardless of that workload's own variance. Verified: checkout's 2.5x-average
  request (660m) is still short of its own observed 900m peak, while search — a workload whose
  peak never exceeds 230m — gets a 500m request it never needed, because the multiplier was
  calibrated to nobody's specific history.
- `principle-applied/` — Each workload's request is sized from its own observed peak plus a
  disclosed 10% margin, with an explicit guard for workloads with too little history (fewer than
  14 days) to size confidently, falling back to a prior known-safe request instead. Verified:
  checkout's 990m request (900m peak + 10%) absorbs the repeated flash-sale spike; search's 230m
  request costs $48.60/month less than the blanket-multiplier policy would have charged it; and
  `recommendations-v2`, with only 3 days of history, is flagged `INSUFFICIENT_HISTORY` and held at
  its 500m fallback rather than sized off a thin sample.

## Running It

Each scenario is self-contained:

```bash
javac -Xlint:all *.java
java Main
```

## Full Write-Up

See [MEDIUM-DRAFT.md](../../../architect-notes-private/volume-29-cost-aware-architecture-finops/paper-158-right-sizing-autoscaling-cost-traps/MEDIUM-DRAFT.md)
(private repo) for the full analysis, comparison table, and interview Q&A.
