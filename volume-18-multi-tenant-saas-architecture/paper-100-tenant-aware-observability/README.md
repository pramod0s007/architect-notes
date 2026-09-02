# Tenant-Aware Observability — Tagging Isn't the Same as Fitting

**Closes Volume 18** — Multi-Tenant & SaaS Architecture, following on from Paper 99 (Cross-Tenant Caching).

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

An aggregate metric with no tenant dimension can hide a complete outage for one tenant behind a healthy-looking average from unrelated traffic (measured: a tenant with a 100% error rate produced an aggregate error rate comfortably under the alert threshold, firing no alert). Tagging every metric with its tenant makes a single tenant's outage visible and alertable independently of how much healthy traffic surrounds it (measured: the identical outage correctly triggered an alert once measured against the tenant's own traffic). This paper then shows the overcorrection: tenant-tagged metrics that still fail to surface an outage because the observability backend's finite series capacity is exhausted before the affected tenant's series can register (measured: a real, correctly-tagged outage produced zero recorded metrics).

## The Measurements

- **Aggregate only (pressure)**: TenantX's 100% error rate (50 of 50 requests failing) is diluted into a 5.0% aggregate error rate, under the 10% alert threshold — no alert fires.
- **Tagged per tenant (principle-applied)**: the identical scenario shows TenantX at 100% error rate, alert fired, while 5 other tenants correctly show 0%.
- **Tagged, capacity-limited backend (wrong-direction)**: 5 healthy tenants fill a 5-series capacity limit before TenantX registers; TenantX's series is rejected and its 50 failing requests are never recorded anywhere.

## The Finding

**Tenant tagging and observability capacity at scale are two separate requirements — a system can satisfy the first completely while still producing the exact same blind spot the tagging was meant to eliminate.**

## Code Examples in This Repo

| Example | What It Shows |
|---------|--------------|
| [`pressure/`](./pressure/) | Aggregate metrics only — measured a complete tenant outage hidden by a healthy-looking average |
| [`principle-applied/`](./principle-applied/) | Per-tenant tagged metrics — measured the outage correctly surfaced and alerted |
| [`wrong-direction/`](./wrong-direction/) | Tagged metrics, capacity-limited backend — measured the outage invisible again due to series capacity exhaustion |

### How to Run

```bash
cd volume-18-multi-tenant-saas-architecture/paper-100-tenant-aware-observability/pressure
javac *.java && java Main

cd volume-18-multi-tenant-saas-architecture/paper-100-tenant-aware-observability/principle-applied
javac *.java && java Main

cd volume-18-multi-tenant-saas-architecture/paper-100-tenant-aware-observability/wrong-direction
javac *.java && java Main
```
