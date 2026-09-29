# Paper 165: Nova's Read Path Under Multi-Tenant Load

Part of [Architect Notes](../../README.md) — Volume 30: Capstone: One System, End to End.

This is the fourth paper of Volume 30, continuing directly from Nova, the fictional multi-tenant
order/checkout and fulfillment platform carried through Papers 162 (data-layer architecture), 163
(API boundary evolution), and 164 (order-to-fulfillment write path). This paper moves to Nova's
read path — the catalog-lookup cache tenants share when browsing and checking out. It ties to
Volume 22 (Caching Architecture Deep Dive) and Volume 20 (JVM Performance for Architects), and its
fix reuses the historical-usage-projection discipline this series established in Volume 29
(Cost-Aware Architecture / FinOps for Engineers), applied here to cache capacity instead of cost.

## The Question

Nova's catalog-lookup cache is one shared LRU cache with no concept of which tenant an item
belongs to. A small tenant's steady 20-item hot working set normally stays resident. A much larger
tenant's one-time cold catalog scan, sharing the same cache with no tenant boundary, can evict that
entire hot working set — even though the smaller tenant's own access pattern never changed at all.
Partitioning the cache per tenant is the obvious fix, and it closes that specific gap. But a
partition still needs a size, and a naive fixed even split assumes every tenant has the same
working-set size. What does it take to size each tenant's own partition correctly?

## Structure

- `pressure/` — One shared 100-slot LRU cache, no tenant isolation. Verified: Tenant A's 20-item
  hot working set (20/20 hits confirmed) is entirely evicted (0/20 hits, 0%) by Tenant B's one-time
  100-item cold scan, even though Tenant A's own access pattern never changed.
- `wrong-direction/` — Per-tenant partitioning fixes the cross-tenant eviction (isolation works: no
  tenant's traffic can evict another's, in this modeled implementation), but each partition is a
  naive fixed 50/50 split of the same 100-slot total. Verified: Tenant A's 20-item working set fits
  fine (100%), but Tenant B's real 80-item working set doesn't fit its 50-slot partition — Tenant B
  thrashes internally (0/80 hits, 0%) on every repeat cycle.
- `principle-applied/` — Same total 100-slot capacity, but each tenant's partition is sized
  proportional to that tenant's own historically measured working-set size (Volume 29's
  historical-usage-projection discipline, applied to cache capacity). Verified: Tenant A gets 20
  slots, Tenant B gets 80 slots — both hit 100%. The paper is explicit that this exact match is a
  coincidence of this scenario's two supplied sizes summing to the modeled total capacity, not a
  general guarantee of proportional allocation, and that the allocation is a one-time snapshot that
  needs periodic recomputation as real usage changes.

## Running It

Each scenario is self-contained:

```bash
javac -Xlint:all *.java
java Main
```

## Full Write-Up

See [MEDIUM-DRAFT.md](../../../architect-notes-private/volume-30-capstone-one-system-end-to-end/paper-165-nova-read-path-multitenant-load/MEDIUM-DRAFT.md)
(private repo) for the full analysis, comparison table, and interview Q&A — including why
isolation and usage-weighted sizing are two independent, both-necessary fixes for two distinct
gaps in the same shared-cache design.
