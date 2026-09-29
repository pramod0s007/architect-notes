# Paper 168: Nova's Cost-Aware Operation — Capstone Synthesis

Part of [Architect Notes](../../README.md) — Volume 30: Capstone: One System, End to End.

This is the seventh and final paper of Volume 30, and of the entire 168-paper "Architect Notes"
plan. It closes out Nova, the fictional multi-tenant order/checkout and fulfillment platform this
volume has followed since Paper 162: the data-layer architecture (162), the API boundary (163), the
order-to-fulfillment write path (164), read-path caching (165), circuit-breaker re-verification
(166), and the customer-support PII lookup path (167). This paper ties Volume 29 (Cost-Aware
Architecture/FinOps) and Volume 12 (SRE Observability), and asks one last question about Nova as a
whole: when its infrastructure cost rises, can anything in this model say which tenant and which
mechanism — the cache from Paper 165, the circuit breaker from Paper 166, the security gates from
Paper 167 — is responsible?

## The Question

Nova tracks its own operational cost as a single running total, incremented by every operation
across every tenant and mechanism, with nothing else recorded alongside it. When that total rises,
can anything attribute the rise to a specific tenant and mechanism afterward? Does recording two
separate logs — one for cost, one for mechanism/health — solve that, or does it just move the same
gap somewhere less obvious?

## Structure

- `pressure/` — A single running total (`totalCost`), incremented by `recordOperation(modeledCost)`.
  Tenant, mechanism, and status appear only in surrounding `println` labels, never passed into or
  retained by any method. Verified: the only retained structured cost state is the final sum.
- `wrong-direction/` — Adds two independent lists, `costLog` and `mechanismLog`, populated by two
  separately-called recording methods. Verified: because this program calls both methods back-to-
  back for every operation, the two logs stay in matching position for this run — but that alignment
  is enforced only by this program's fixed call order, not recorded in either log's own entries.
- `principle-applied/` — A unified `OperationEvent` class carries `tenantId`, `mechanism`,
  `healthStatus`, and `costDelta` together as fields on the same object. Verified:
  `costByTenantMechanism` groups modeled cost directly from those fields (`tenant-a/CACHE=40`,
  `tenant-a/RESILIENCE=5`, `tenant-b/CACHE=2`), and `diagnoseTopContributor` correctly identifies
  `tenant-a/CACHE` as the largest contributor (40 out of 47 total) — with no assumption about
  matching order across two separate collections.

## Running It

Each scenario is self-contained:

```bash
javac -Xlint:all *.java
java Main
```

## Full Write-Up

See [MEDIUM-DRAFT.md](../../../architect-notes-private/volume-30-capstone-one-system-end-to-end/paper-168-nova-cost-aware-operation-capstone-synthesis/MEDIUM-DRAFT.md)
(private repo) for the full analysis, comparison table, and interview Q&A — including why two
separately-populated logs still leave a cost-to-mechanism pairing fragile, and why explicit
correlation (whether by co-locating fields on one event, as this paper demonstrates, or by carrying
a shared key across two records) is what actually closes the gap.

This paper closes Volume 30, and the entire 168-paper "Architect Notes" plan.
