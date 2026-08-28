# Alerting — Symptom-Based vs. Cause-Based

**Continues Volume 12** — picking up right after Paper 62 (SLOs & Error Budgets).

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

A cause-based alert (CPU > 90%) both false-positives on a harmless background job (measured: fires at 95% CPU with 0% user impact) and false-negatives on a real outage where blocked threads keep CPU low (measured: silent at 20% CPU with 40% user error rate). Symptom-based alerting — watching user-facing error rate directly — gets both scenarios right without knowing the cause of either (measured). This paper then shows the overcorrection: a symptom-based alert measured at the wrong point in the request path (internal processing time instead of true end-to-end latency), which misses a real 3-second customer wait entirely.

## The Measurements

- **Cause-based (CPU)**: alerts on a harmless 95%-CPU compaction job; stays silent through a real 40%-error-rate outage where CPU is only 20%.
- **Symptom-based (user error rate)**: correctly silent for the compaction job; correctly alerts for the outage.
- **Symptom-based, wrong measurement point (internal processing time)**: stays silent through a 3,000ms queue backup because internal processing itself is still a fast 50ms; measuring end-to-end (queue + processing) correctly alerts.

## The Finding

**Symptom-based alerting decouples the alert from the cause — but "symptom-based" only describes what you measure, not where in the request path you measure it.** A symptom measured upstream of the actual bottleneck is barely better than not measuring it at all.

## Code Examples in This Repo

| Example | What It Shows |
|---------|--------------|
| [`pressure/`](./pressure/) | CPU-based (cause-based) alerting — measured both a false positive and a false negative |
| [`principle-applied/`](./principle-applied/) | User-facing-error-rate (symptom-based) alerting — measured correct on both scenarios |
| [`wrong-direction/`](./wrong-direction/) | Symptom measured at the wrong point (internal processing time vs. end-to-end) — measured missing a real 3-second customer wait |

### How to Run

```bash
cd volume-12-production-engineering-observability/paper-63-alerting-symptom-vs-cause/pressure
javac *.java && java Main

cd volume-12-production-engineering-observability/paper-63-alerting-symptom-vs-cause/principle-applied
javac *.java && java Main

cd volume-12-production-engineering-observability/paper-63-alerting-symptom-vs-cause/wrong-direction
javac *.java && java Main
```
