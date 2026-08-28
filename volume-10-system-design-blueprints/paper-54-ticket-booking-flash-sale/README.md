# Ticket Booking at Flash-Sale Load — An ATAM Trade-off Analysis

**Evaluation method:** ATAM (Architecture Trade-off Analysis Method) — Kazman, Klein & Clements

**Continues Volume 10** — picking up right after Paper 53 (Rate Limiter).

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

A shared seat-inventory counter under flash-sale concurrency. No protection at all reliably oversells (measured: 100 seats, 200 requests, 100% oversold every run). Both pessimistic (synchronized) and optimistic (CAS-based) locking are correct by construction — the paper's real finding is that the trade-off between them isn't stable: optimistic locking is measurably faster at moderate contention, but that advantage disappears at the flash sale's actual target load, because a completely different bottleneck (raw thread-creation overhead) takes over.

## The Measurements

- **No protection**: 100 seats, 200 concurrent requests → all 200 succeed, 100 oversold, every run.
- **Moderate contention** (100 seats, 200 requests): pessimistic 12ms, optimistic 8ms — optimistic ~30-40% faster, both zero oversold.
- **Extreme contention** (20 seats, 5,000 requests — the real target load): pessimistic 647-671ms, optimistic 641-757ms across 3 runs — statistically indistinguishable, no consistent winner, both still zero oversold.

## The ATAM Finding

**A trade-off measured at one contention level doesn't necessarily hold at another.** The honest conclusion isn't "pick optimistic" or "pick pessimistic" — it's that past a sensitivity point (driven by the buyer-to-seat ratio), a different cost (thread-per-request overhead) dominates both approaches equally, and the real architectural lever becomes admission control, not the locking algorithm.

## Code Examples in This Repo

| Example | What It Shows |
|---------|--------------|
| [`pressure/`](./pressure/) | No concurrency protection — measured 100% oversell rate (200 successes for 100 seats) across every run |
| [`principle-applied/`](./principle-applied/) | Pessimistic vs. optimistic locking at moderate contention — measured optimistic ~30-40% faster, both zero oversold |
| [`wrong-direction/`](./wrong-direction/) | Same two algorithms at extreme contention (20 seats, 5,000 requests) — measured the earlier advantage disappear into noise, both ~650ms |

### How to Run

```bash
cd volume-10-system-design-blueprints/paper-54-ticket-booking-flash-sale/pressure
javac *.java && java Main

cd volume-10-system-design-blueprints/paper-54-ticket-booking-flash-sale/principle-applied
javac *.java && java Main

cd volume-10-system-design-blueprints/paper-54-ticket-booking-flash-sale/wrong-direction
javac *.java && java Main
```
