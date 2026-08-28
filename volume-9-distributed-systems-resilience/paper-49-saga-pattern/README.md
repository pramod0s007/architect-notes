# Saga Pattern — Undoing What You Can't Roll Back

**Pattern:** Saga — a sequence of local transactions across services, each with a compensating transaction to undo it

**Continues Volume 9's running example**, picking up right after Paper 48 (Circuit Breaker & Bulkhead).

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

An order touches three healthy, resilient services in sequence — payment, inventory, shipping. All three do exactly what they're asked. The order still fails, because the warehouse genuinely can't fulfill it, and nothing undoes the payment charge or inventory reservation that already succeeded. This paper measures the leaked state directly, fixes it with compensating transactions accumulated as each step succeeds, and shows the overcorrection: compensation logic written per-branch instead of generically, which handles the tested failure path and silently drops another.

## The Pressure: Three Correct Steps, One Wrong Aggregate Outcome

Measured: an order the warehouse can't ship leaves `payment charged=true` and `inventory reserved=true`, despite the order itself failing. No dependency was down, no call timed out — every individual step behaved correctly, and the aggregate result was still wrong.

## The Principle

**A saga accumulates a compensating action for every step as it succeeds, and undoes all of them in reverse order the moment a later step fails.** The compensation logic doesn't need to know which step failed — only that everything already recorded needs to be undone.

## Pros

- Verified: full cleanup on failure — payment refunded, inventory released, regardless of which step failed
- Doesn't require a distributed transaction or cross-service locking
- Compensation logic is generic (accumulate-then-reverse), not rewritten per failure branch

## Cons

- Compensating actions must be idempotent — they can be retried by Paper 47's resilience logic
- Intermediate states are briefly visible to the rest of the system (eventual consistency, not full isolation)
- Easy to accidentally hand-write compensation per branch instead of accumulating it generically — the failure mode this paper's wrong-direction demonstrates

## When NOT to Use

- When a single database transaction can span all the steps — use a real ACID transaction instead
- When compensations can't be made safely idempotent
- When eventual consistency (briefly-visible intermediate states) is unacceptable for the domain

## Code Examples in This Repo

| Example | Domain | What It Shows |
|---------|--------|--------------|
| [`evolution/`](./evolution/) | Order fulfillment (continuing Papers 44-48) | v1: one local transaction, one service, clean rollback (fine) -> v2: three services, no compensation, leaked state (measured: charged=true, reserved=true after a failed order) -> v3: compensations accumulated as steps succeed, undone in reverse on failure (measured: fully cleaned up) |
| [`wrong-direction/`](./wrong-direction/) | Per-branch compensation | Compensation written separately for each failure branch — correctly refunds on the "out of stock" path, silently forgets to refund on the "shipping unavailable" path (measured: charged=true even after "compensation" ran) |

### How to Run

```bash
cd volume-9-distributed-systems-resilience/paper-49-saga-pattern/evolution/v1
javac *.java && java Main

cd volume-9-distributed-systems-resilience/paper-49-saga-pattern/evolution/v2
javac *.java && java Main

cd volume-9-distributed-systems-resilience/paper-49-saga-pattern/evolution/v3
javac *.java && java Main

cd volume-9-distributed-systems-resilience/paper-49-saga-pattern/wrong-direction
javac *.java && java Main
```
