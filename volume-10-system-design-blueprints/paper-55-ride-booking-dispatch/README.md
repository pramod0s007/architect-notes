# Ride Booking Dispatch — A Cost-Benefit Analysis

**Evaluation method:** CBAM (Cost-Benefit Analysis Method) — Kazman, Asundi & Klein

**Continues Volume 10** — picking up right after Paper 54 (Ticket Booking / ATAM).

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

Matching a rider to the nearest driver by scanning every driver on the platform is correct and gets more expensive exactly as the business succeeds (measured: 4ms → 51ms → 309ms as driver count grows 1,000 → 10,000 → 50,000). This paper uses CBAM to put a real number on the fix — grid bucketing — and then shows the overcorrection: building the exact same data structure with a cell size that doesn't match driver density, which silently reproduces the original cost.

## The Measurements

- **Naive linear scan**: 4ms → 51ms → 309ms as drivers grow 1,000 → 10,000 → 50,000 (5,000 requests each).
- **Grid bucketing, properly sized**: 6ms → 6ms → 13ms at the same driver counts — ~24x faster at 50,000 drivers, dramatically flatter growth curve.
- **Grid bucketing, cell size == city size**: 4ms → 49-51ms → 282-283ms — functionally identical to the naive scan, same code, one parameter changed.

## The CBAM Finding

**The benefit of a spatial index is a number you can weigh against its engineering cost — but only if the one parameter that makes it work (cell size relative to driver density) is actually tuned.** Building the data structure without tuning it spends the engineering investment and delivers none of the modeled benefit.

## Code Examples in This Repo

| Example | What It Shows |
|---------|--------------|
| [`pressure/`](./pressure/) | Naive O(n) linear scan — measured cost growing linearly with total driver count |
| [`principle-applied/`](./principle-applied/) | Grid-bucketed dispatch, properly sized cells — measured ~24x faster at 50,000 drivers |
| [`wrong-direction/`](./wrong-direction/) | Same grid code, cell size equal to the whole city — measured performance indistinguishable from no index at all |

### How to Run

```bash
cd volume-10-system-design-blueprints/paper-55-ride-booking-dispatch/pressure
javac *.java && java Main

cd volume-10-system-design-blueprints/paper-55-ride-booking-dispatch/principle-applied
javac *.java && java Main

cd volume-10-system-design-blueprints/paper-55-ride-booking-dispatch/wrong-direction
javac *.java && java Main
```
