# Noisy Neighbor Isolation — A Small Tenant's Fair Share Isn't Automatic

**Opens Volume 18** — Multi-Tenant & SaaS Architecture, following on from Volume 17 (Security Architecture for Staff Engineers).

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

Shared capacity with no tenant boundary lets one tenant's high volume consume the entire pool, starving a smaller tenant regardless of how modest that tenant's actual request was (measured: a tenant asking for 10 of 100 available units got zero, because a different tenant's unrelated burst arrived first). A guaranteed per-tenant quota with unused capacity reclaimed protects every tenant's floor while still fully utilizing the system (measured: the small tenant got 100% of its request, the large tenant got everything the system could still spare). This paper then shows the overcorrection: a rigid, non-reclaimable quota that protects tenants just as completely, but leaves real capacity idle while throttling a tenant with genuine demand for exactly that idle capacity (measured: 40% of system capacity sat unused while a legitimate tenant was capped below its real need).

## The Measurements

- **Shared pool, no boundary (pressure)**: Tenant B (10 requested) gets 0 processed because Tenant A's 150-request burst consumes the entire 100-unit shared capacity first.
- **Guaranteed quota + reclaim (principle-applied)**: Tenant B gets all 10 it needed; Tenant A gets 90 (its 50-unit quota plus Tenant B's 40 unused units); total utilization 100/100.
- **Rigid fixed quota, no reclaim (wrong-direction)**: Tenant B still gets all 10; Tenant A is hard-capped at 50 despite wanting 150; total utilization only 60/100, 40 units idle.

## The Finding

**The correct measurement for a multi-tenant fairness system is never just "does every tenant have a limit" — it's whether unused capacity can move to wherever real demand actually is, without ever compromising any tenant's guaranteed floor.**

## Code Examples in This Repo

| Example | What It Shows |
|---------|--------------|
| [`pressure/`](./pressure/) | Shared pool, no tenant boundary — measured a small tenant starved to zero |
| [`principle-applied/`](./principle-applied/) | Guaranteed quota + reclaim — measured full protection and full utilization together |
| [`wrong-direction/`](./wrong-direction/) | Rigid fixed quota, no reclaim — measured protection alongside 40% idle capacity |

### How to Run

```bash
cd volume-18-multi-tenant-saas-architecture/paper-96-noisy-neighbor-isolation/pressure
javac *.java && java Main

cd volume-18-multi-tenant-saas-architecture/paper-96-noisy-neighbor-isolation/principle-applied
javac *.java && java Main

cd volume-18-multi-tenant-saas-architecture/paper-96-noisy-neighbor-isolation/wrong-direction
javac *.java && java Main
```
