# Tenant Configuration and Feature Flags — Isolated Doesn't Mean Data-Driven

**Continues Volume 18** — picking up right after Paper 97 (Tenant Onboarding and Schema Migration).

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

A single global flag has no way to represent a setting that applies to one tenant, so enabling it for one tenant enables it for every tenant reading the same shared value (measured: flipping a flag intended for one tenant's beta immediately changed behavior seen by two unrelated tenants). Storing configuration as data, keyed by tenant and resolved at request time, isolates one tenant's setting from every other tenant's completely (measured: enabling a setting for one tenant left two other tenants' behavior unchanged). This paper then shows the overcorrection: hardcoding tenant-specific logic directly into application code, which achieves real isolation but couples every future tenant-specific change to a source code edit and a deploy (measured: onboarding one new tenant needing the same setting required writing and shipping new source code).

## The Measurements

- **Global flag (pressure)**: flipping a flag intended for one tenant's beta immediately enables it for two other, unrelated tenants — no tenant dimension exists at all.
- **Data-driven, per-tenant (principle-applied)**: enabling the setting for one tenant leaves two other tenants' behavior completely unchanged, each resolving against its own map entry.
- **Hardcoded, per-tenant (wrong-direction)**: tenant isolation is real, but onboarding a new tenant needing the same setting requires a source code change and a full deploy — a data update was never possible.

## The Finding

**Tenant isolation and data-driven changeability are two separate requirements for a configuration system — a system can satisfy the first completely while failing the second just as completely.** The bar for a real tenant configuration system is both together: no tenant's setting can reach another tenant, and no tenant's setting requires anyone to write code to change it.

## Code Examples in This Repo

| Example | What It Shows |
|---------|--------------|
| [`pressure/`](./pressure/) | Global flag — measured one tenant's rollout affecting two unrelated tenants |
| [`principle-applied/`](./principle-applied/) | Data-driven, per-tenant map — measured complete isolation between tenants |
| [`wrong-direction/`](./wrong-direction/) | Hardcoded, per-tenant logic — measured a new tenant requiring a code deploy to get the same setting |

### How to Run

```bash
cd volume-18-multi-tenant-saas-architecture/paper-98-tenant-configuration-feature-flags/pressure
javac *.java && java Main

cd volume-18-multi-tenant-saas-architecture/paper-98-tenant-configuration-feature-flags/principle-applied
javac *.java && java Main

cd volume-18-multi-tenant-saas-architecture/paper-98-tenant-configuration-feature-flags/wrong-direction
javac *.java && java Main
```
