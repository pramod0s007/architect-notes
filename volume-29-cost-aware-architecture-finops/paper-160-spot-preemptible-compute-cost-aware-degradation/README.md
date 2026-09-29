# Paper 160: Spot/Preemptible Compute & Cost-Aware Degradation

Part of [Architect Notes](../../README.md) — Volume 29: Cost-Aware Architecture (FinOps for Engineers).

## The Question

Spot instances are priced far below on-demand, so moving a batch job to spot looks like a clean win —
the per-hour discount alone seems to guarantee savings. But without checkpointing, a spot interruption
forces a job to restart from the very beginning, discarding all progress made so far. For a job with a
historically high interruption rate, the cumulative instance-hours wasted on repeated full restarts can
exceed what on-demand would have cost for a single, uninterrupted run. The obvious fix — add
checkpointing — has its own failure mode: a single checkpoint interval chosen conservatively enough to
protect the worst-case job, applied uniformly across the whole fleet, makes a stable, rarely-interrupted
job pay real checkpoint overhead for protection its own history never called for. How do you choose a
checkpoint policy — including "none" as a real option — that fits each job's own observed interruption
behavior, rather than one interval standardized for the whole fleet?

## Structure

- `pressure/` — Every job runs on spot with no checkpointing. Verified: `nightly-batch-etl`'s own
  no-checkpoint history shows 6 failed attempts before a 7th finally completed — 41 instance-hours
  consumed on spot ($4.10) versus $3.30 for a single on-demand run, because the discarded, redone
  hours from repeated restarts more than erase spot's per-hour discount. `quick-report-job`'s own
  history shows no such pattern, so the same uniform policy costs it only $0.10 on spot versus $0.33
  on-demand — the discount works exactly as expected for this job.
- `wrong-direction/` — A blanket 15-minute checkpoint interval, assumed sized for the worst-case job,
  is applied to every job. Verified: it's a large win for `nightly-batch-etl` ($4.10 → $1.09), but the
  same interval adds $0.0046 (4.6%) of modeled cost — checkpoint overhead plus modeled residual
  interruption loss — to `quick-report-job`, whose own history shows almost no interruptions to
  protect against.
- `principle-applied/` — Each job's own projected cost is evaluated across a small set of candidate
  checkpoint intervals (including "no checkpointing"), the lowest-modeled-cost candidate is then
  checked explicitly against that job's own on-demand cost, and a job with too few historical runs to
  trust an interruption-rate estimate is held on-demand until its raw run-count clears a disclosed
  threshold. Verified: `nightly-batch-etl` lands on the same 15-minute interval ($1.09, still cheaper
  than its $3.30 on-demand cost); `quick-report-job` lands on no checkpointing at all ($0.10, $0.0046
  cheaper than the blanket policy's forced interval); and `new-ml-training-job`, with only 2 historical
  runs, is held on ON-DEMAND ($1.98) rather than spot-scheduled off a thin sample.

## Running It

Each scenario is self-contained:

```bash
javac -Xlint:all *.java
java Main
```

## Full Write-Up

See [MEDIUM-DRAFT.md](../../../architect-notes-private/volume-29-cost-aware-architecture-finops/paper-160-spot-preemptible-compute-cost-aware-degradation/MEDIUM-DRAFT.md)
(private repo) for the full analysis, comparison table, and interview Q&A.
