# Paper 163: Nova's API Boundary Across Tenants

Part of [Architect Notes](../../README.md) — Volume 30: Capstone: One System, End to End.

This is the second paper of Volume 30, following directly from Paper 162, which picked Nova's
data-layer architecture (Per-Tenant Schema, Shared Cluster) against Nova's own disclosed hard
constraints. This paper takes that architecture as a given input and moves to Nova's API boundary
— the shared order endpoint every tenant calls. It ties to Volume 21 (API Design & Evolution) and
Volume 18 (Multi-Tenant & SaaS).

## The Question

Nova's order API returns an `itemCount` field on every order response. A packaging feature ships,
and `itemCount` is repurposed to carry a new meaning (package count instead of total units) — same
field name, same endpoint, no version bump. A tenant's billing client, unchanged, reads `itemCount`
and multiplies it by a fixed per-unit price, exactly as before. What breaks, and what evolves an
API's shared contract safely across every tenant reading it, without a maintenance cost that grows
with tenant count?

## Structure

- `pressure/` — `itemCount` silently switches meaning from total units to package count on the
  same shared endpoint. Verified: a tenant's unchanged billing client computes $50.00 instead of
  $600.00 for the same 120-unit order — a $550.00 shortfall, with no request failure, no missing
  field, and no exception thrown.
- `wrong-direction/` — Each tenant gets its own dedicated endpoint definition, so each tenant's
  field meanings are correct for that tenant. Verified: this works per-tenant, but the number of
  endpoint definitions Nova must keep in sync grows by one with every tenant onboarded — at Nova's
  own disclosed minimum of 50 concurrent tenants (Paper 162), that's 50 separate endpoint
  definitions to update for a single new field.
- `principle-applied/` — One shared endpoint for every tenant. `itemCount`'s original meaning
  (total units) is frozen permanently; the packaging feature is exposed as a brand-new, additive
  `packageCount` field alongside it. Verified: a tolerant-reader consumer's billing math is
  unaffected by the addition, and one endpoint definition — not one per tenant — absorbs the
  change, regardless of tenant count.

## Running It

Each scenario is self-contained:

```bash
javac -Xlint:all *.java
java Main
```

## Full Write-Up

See [MEDIUM-DRAFT.md](../../../architect-notes-private/volume-30-capstone-one-system-end-to-end/paper-163-nova-api-boundary-multitenant/MEDIUM-DRAFT.md)
(private repo) for the full analysis, comparison table, and interview Q&A — including the
tolerant-reader precondition that additive-only field evolution depends on.
