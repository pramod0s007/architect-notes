# SLOs & Error Budgets — Alerting on the Rate You'll Actually Run Out

**Opens Volume 12** — production engineering and observability practice, standalone papers (not one running code system), each still shipping a runnable Java demonstration of the underlying mechanism.

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

A fixed-threshold alert on instantaneous error rate misses the incident that actually threatens an SLO: a sustained, moderately-elevated error rate that exhausts an entire month's error budget while never crossing a threshold tuned to catch dramatic outages (measured: budget exhausted by the halfway point of the window, alert never fires). Burn-rate alerting fixes this by asking whether the budget will run out before the window ends, not whether things look bad right now (measured: same incident caught with 985 minutes of runway to spare). This paper then shows the overcorrection: shrinking the burn-rate window to react faster also makes it fire on a harmless, self-resolving blip.

## The Measurements

- **Naive fixed threshold**: never fires for a sustained 2% error rate that exhausts the entire error budget by the window's halfway point.
- **Burn rate, long window**: fires at minute 15 for the same incident (985 minutes of runway); never fires for a harmless 2-minute blip.
- **Burn rate, short window (wrong direction)**: fires at minute 5 for the real incident (faster) — but also fires at minute 100 for the same harmless blip the long-window version correctly ignored.

## The Finding

**Reacting faster and reacting correctly are different properties of an alerting window.** A single window length trades detection speed against false-positive rate along the same axis — real multi-window burn-rate alerting (the practice Google's SRE work formalizes) uses more than one window at once specifically to get both.

## Code Examples in This Repo

| Example | What It Shows |
|---------|--------------|
| [`pressure/`](./pressure/) | Fixed-threshold alerter — measured never firing for a budget-exhausting slow burn |
| [`principle-applied/`](./principle-applied/) | Long-window burn-rate alerter — measured catching the slow burn early and ignoring a harmless blip |
| [`wrong-direction/`](./wrong-direction/) | Short-window burn-rate alerter — measured catching the slow burn even faster, but also false-alarming on the blip |

### How to Run

```bash
cd volume-12-production-engineering-observability/paper-62-slos-error-budgets/pressure
javac *.java && java Main

cd volume-12-production-engineering-observability/paper-62-slos-error-budgets/principle-applied
javac *.java && java Main

cd volume-12-production-engineering-observability/paper-62-slos-error-budgets/wrong-direction
javac *.java && java Main
```
