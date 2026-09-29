# Paper 162: Nova's Requirements → Architecture Trade-off Table

Part of [Architect Notes](../../README.md) — Volume 30: Capstone: One System, End to End.

**This is the opening paper of the final volume of the series.** Instead of independent toy
problems, all 7 papers in Volume 30 follow one running system — **Nova**, a fictional multi-tenant
B2B order/checkout and fulfillment platform. This paper picks Nova's data-layer architecture; every
later paper in this volume takes that architecture as a given input, and each names which earlier
volume in the series its own mechanism draws on.

## The Question

Three candidate data-layer architectures are on the table. One is cheapest and looks like the
obvious pick on every number checked. Nova also has four of its own disclosed hard constraints —
a compliance-driven isolation requirement, a minimum tenant count, a minimum peak write capacity,
and a monthly budget ceiling. How do you build a trade-off table that actually checks a candidate
against a system's own disclosed constraints, rather than against a generic checklist that scores
a hard requirement as just one weighted input among several?

## Structure

- `pressure/` — The cheapest of three candidates is picked with no trade-off table at all. Verified:
  the picked architecture fails Nova's own disclosed compliance isolation requirement (isolation
  level 1, required level 2), a gate that was never checked before the pick was made.
- `wrong-direction/` — A real trade-off table is built, scoring all three candidates on a generic
  weighted formula (cost 50%, write capacity 30%, isolation 20%). Verified: the same
  isolation-failing architecture wins the weighted composite (score 0.8333), because a low
  isolation score is outweighed by a high cost score in the formula.
- `principle-applied/` — The same three candidates are checked against Nova's own disclosed
  constraints as pass/fail gates first (isolation ≥ 2, tenants ≥ 50, writes ≥ 500/sec, cost ≤
  $6,000), and only the survivor(s) are ranked, by lowest cost. Verified: exactly one candidate
  (Per-Tenant Schema, Shared Cluster) survives all four gates and is recommended — this is the
  architecture Papers 163-168 build on for the rest of Nova.

## Running It

Each scenario is self-contained:

```bash
javac -Xlint:all *.java
java Main
```

## Full Write-Up

See [MEDIUM-DRAFT.md](../../../architect-notes-private/volume-30-capstone-one-system-end-to-end/paper-162-nova-requirements-architecture-tradeoffs/MEDIUM-DRAFT.md)
(private repo) for the full analysis, comparison table, and interview Q&A.
