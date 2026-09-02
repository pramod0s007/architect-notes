# Cross-Tenant Caching — Scoping the Read Isn't Scoping the Whole Cache

**Continues Volume 18** — picking up right after Paper 98 (Tenant Configuration and Feature Flags).

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

A cache key built from a resource ID alone collides across tenants whose IDs happen to match, serving one tenant's cached data to a completely different tenant (measured: a tenant received another tenant's product data on the very first request for its own resource). Including the tenant ID as part of the cache key eliminates the collision entirely (measured: both tenants correctly received their own data). This paper then shows the overcorrection: scoping reads by tenant without scoping invalidation the same way, which doesn't leak data but lets one tenant's write silently evict other tenants' valid, unrelated cache entries (measured: two tenants who changed nothing were forced into unnecessary database refetches after a third tenant's update).

## The Measurements

- **Untenanted key (pressure)**: TenantB's first request for its own product 100 returns TenantA's cached data instead — a direct cross-tenant leak via key collision.
- **Tenant-scoped key (principle-applied)**: the identical scenario returns each tenant's own correct data — no collision, no shared slot.
- **Scoped reads, unscoped invalidation (wrong-direction)**: no data leaks, but TenantA updating its own product forces TenantB and TenantC — who changed nothing — into 2 unnecessary database refetches.

## The Finding

**A caching layer is only as tenant-safe as its least-scoped operation.** Reads, writes, and invalidation all have to agree on the same tenant scope — checking only the read path, where the most visible failure mode lives, is checking a fraction of the actual surface.

## Code Examples in This Repo

| Example | What It Shows |
|---------|--------------|
| [`pressure/`](./pressure/) | Untenanted cache key — measured a direct cross-tenant data leak |
| [`principle-applied/`](./principle-applied/) | Tenant-scoped cache key — measured complete isolation on read |
| [`wrong-direction/`](./wrong-direction/) | Scoped reads, unscoped invalidation — measured unnecessary cross-tenant cache eviction |

### How to Run

```bash
cd volume-18-multi-tenant-saas-architecture/paper-99-cross-tenant-caching/pressure
javac *.java && java Main

cd volume-18-multi-tenant-saas-architecture/paper-99-cross-tenant-caching/principle-applied
javac *.java && java Main

cd volume-18-multi-tenant-saas-architecture/paper-99-cross-tenant-caching/wrong-direction
javac *.java && java Main
```
