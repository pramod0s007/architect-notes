# Tenant Onboarding and Schema Migration — Checkpointing Only Helps at the Right Granularity

**Continues Volume 18** — picking up right after Paper 96 (Noisy Neighbor Isolation).

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

A migration with no checkpoint has no way to distinguish a tenant that's already done from one that isn't, and a restart reapplies non-idempotent work to every tenant processed before the crash (measured: 3 of 5 tenants received a signup bonus twice after a mid-run crash and restart). A checkpoint recorded at the real unit of idempotency — one tenant at a time — lets a restart resume exactly where a crash left off, with zero reapplication (measured: every tenant ended with exactly one bonus applied). This paper then shows the overcorrection: a checkpoint recorded at a coarser granularity than the real unit of idempotency, which bounds the damage but doesn't eliminate it (measured: 1 of 5 tenants was still double-processed, the one caught mid-batch when the crash occurred).

## The Measurements

- **No checkpoint (pressure)**: a mid-run crash after 3 of 5 tenants forces a full restart from the beginning — those 3 tenants get a non-idempotent operation applied twice.
- **Per-tenant checkpoint (principle-applied)**: the identical crash scenario resumes exactly where it left off — all 5 tenants end with exactly one bonus applied.
- **Per-batch checkpoint (wrong-direction)**: checkpointing exists but is recorded once per 2-tenant batch — the tenant caught mid-batch when the crash occurs still gets double-processed.

## The Finding

**Batching for efficiency and checkpointing for correctness are separate design decisions — the checkpoint granularity has to match the actual atomic unit of the work, not whatever grouping happens to be convenient for performance.** Checkpointing at any coarser granularity than the real unit of idempotency only bounds the blast radius of a crash; it doesn't eliminate it.

## Code Examples in This Repo

| Example | What It Shows |
|---------|--------------|
| [`pressure/`](./pressure/) | No checkpoint — measured 3 of 5 tenants double-processed after a crash and restart |
| [`principle-applied/`](./principle-applied/) | Per-tenant checkpoint — measured zero reapplication regardless of crash timing |
| [`wrong-direction/`](./wrong-direction/) | Per-batch checkpoint — measured 1 tenant still double-processed despite checkpointing existing |

### How to Run

```bash
cd volume-18-multi-tenant-saas-architecture/paper-97-tenant-schema-migration/pressure
javac *.java && java Main

cd volume-18-multi-tenant-saas-architecture/paper-97-tenant-schema-migration/principle-applied
javac *.java && java Main

cd volume-18-multi-tenant-saas-architecture/paper-97-tenant-schema-migration/wrong-direction
javac *.java && java Main
```
