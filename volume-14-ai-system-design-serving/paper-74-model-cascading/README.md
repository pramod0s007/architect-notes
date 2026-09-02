# Model Cascading — Cheap-Model-First Routing and What Confidence Doesn't Guarantee

**Continues Volume 14** — picking up right after Paper 73 (KV-Cache Reuse).

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

Routing every query to the most expensive, most capable model is always correct and always maximally costly (measured: 100% correct, $0.12 for 4 queries, including 2 genuinely simple ones). Routing by a known, pre-validated query category cuts cost substantially with zero correctness lost (measured: 48% cost reduction, 4/4 correct). This paper then shows the overcorrection: routing by the cheap model's own self-reported confidence instead of a verified category, which is cheaper still — and silently wrong exactly on the query the cascade exists to catch (measured: 73% cost reduction, but a confidently wrong answer on the one complex query that needed escalation, never caught).

## The Measurements

- **Always expensive**: $0.12 total, 4/4 correct — no routing decision made at all.
- **Category-based routing**: $0.062 total (48% less), 4/4 correct — routed by a signal validated before request time.
- **Confidence-based routing (wrong direction)**: $0.033 total (73% less), 3/4 correct — a complex query answered wrong with 92% self-reported confidence, never escalated.

## The Finding

**A model's confidence in its own answer and that answer's actual correctness are two different things, and treating them as the same signal is the entire failure mode.** Confidence-based routing that skips calibration isn't a smaller version of the correct fix — it reproduces the exact failure the cascade was built to prevent, while looking cheaper on a cost dashboard.

## Code Examples in This Repo

| Example | What It Shows |
|---------|--------------|
| [`pressure/`](./pressure/) | Always route to the expensive model — measured 100% correct, uniformly high cost |
| [`principle-applied/`](./principle-applied/) | Route by known query category — measured 48% cost reduction, zero correctness lost |
| [`wrong-direction/`](./wrong-direction/) | Route by the cheap model's own confidence — measured cheaper cost, a confidently wrong answer left uncaught |

### How to Run

```bash
cd volume-14-ai-system-design-serving/paper-74-model-cascading/pressure
javac *.java && java Main

cd volume-14-ai-system-design-serving/paper-74-model-cascading/principle-applied
javac *.java && java Main

cd volume-14-ai-system-design-serving/paper-74-model-cascading/wrong-direction
javac *.java && java Main
```
