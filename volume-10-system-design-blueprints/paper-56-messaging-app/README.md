# Messaging App Delivery — The 4+1 Architectural Views

**Evaluation method:** 4+1 Architectural View Model (Kruchten)

**Continues Volume 10** — picking up right after Paper 55 (Ride Booking Dispatch).

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

A messaging inbox that appends messages in arrival order and a receipt tracker that applies status updates in arrival order both look correct in isolation (the Logical View) — until the Process View's reality (out-of-order, duplicated, delayed delivery) breaks both promises. Measured: out-of-order delivery scrambles message order; a delayed receipt regresses "Read" back to "Delivered." This paper fixes both by anchoring to a server-assigned sequence number instead of arrival order, then shows the overcorrection: a fix that looks structurally identical but orders by each device's own (unsynchronized) clock instead, silently reintroducing the same bug.

## The Measurements

- **Naive (arrival order)**: messages sent 1,2,3, delivered 2,1,3 → displayed 2,1,3 (wrong). Receipt DELIVERED→READ→late-DELIVERED → shown status DELIVERED (wrong, was already Read).
- **Fixed (server sequence number)**: same out-of-order + duplicate delivery → displayed 1,2,3, duplicate dropped (correct). Same late receipt → status stays READ (correct).
- **Wrong direction (client clock timestamp)**: a 5-second clock skew makes a genuine reply display before the question it replies to — the exact bug already fixed, reintroduced by an unauthoritative ordering key.

## The 4+1 Views Finding

**A Logical View's abstraction can look completely correct while quietly assuming something about the Process View (delivery order) or the Physical View (synchronized clocks) that isn't true.** The fix has to anchor ordering to something neither view can violate — a value produced once by an authority every device already trusts, not arrival order and not any device's own clock.

## Code Examples in This Repo

| Example | What It Shows |
|---------|--------------|
| [`pressure/`](./pressure/) | Naive append-order inbox + naive receipt tracker — measured scrambled message order and regressed receipt status |
| [`principle-applied/`](./principle-applied/) | Server-sequence-ordered + deduped inbox, monotonic receipt tracker — measured correct order and non-regressing status |
| [`wrong-direction/`](./wrong-direction/) | Ordering by client device timestamp instead of server sequence — measured a 5-second clock skew reordering a real conversation; same substitution applied to receipts — measured a relay clock running 10s fast wrongly regressing a `READ` status back to `DELIVERED` |

### How to Run

```bash
cd volume-10-system-design-blueprints/paper-56-messaging-app/pressure
javac *.java && java Main

cd volume-10-system-design-blueprints/paper-56-messaging-app/principle-applied
javac *.java && java Main

cd volume-10-system-design-blueprints/paper-56-messaging-app/wrong-direction
javac *.java && java Main
```
