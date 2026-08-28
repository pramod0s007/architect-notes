# Observability — Metrics, Logs, and Traces Answer Different Questions

**Closes Volume 12** — picking up right after Paper 64 (Blameless Postmortems).

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

A metric can be completely correct and still structurally unable to answer a legitimate operational question (measured: p99 latency correctly flagged as 3,050ms — clearly elevated — with no field anywhere to say which of four services caused it). A trace breaks the same request down by span and answers exactly that question (measured: identical total of 3,050ms, correctly attributing all but 50ms of it to one service). This paper then shows the overcorrection: reaching for logs instead of traces to answer a cross-service question, without the one thing that makes cross-service logs useful for it — a shared correlation ID (measured: 3 concurrent requests, all equally plausible candidates for one log entry, with no field in either log to break the tie).

## The Measurements

- **Metrics only**: p99 latency = 3,050ms, correctly elevated; zero fields recorded that could attribute it to a service.
- **Traces (spans + correlation)**: total duration 3,050ms, matching the metric exactly; slowest span identified exactly — ServiceC, 3,000ms.
- **Uncorrelated logs (wrong direction)**: 3 gateway requests active at the moment the slow log entry started; all 3 equally plausible; ground truth unrecoverable from the logs themselves.

## The Finding

**Metrics, logs, and traces aren't tiers of the same signal — they answer structurally different classes of question.** A metric answers "is something wrong, in aggregate," cheaply, at scale, without per-request detail. A trace answers "where did the time go for this one request." A log answers "what happened, in detail, at one point" — but only resolves "which of several concurrent things does this belong to" if a correlation ID was threaded through every hop. Reaching for the wrong one produces a real, unrecoverable gap, not just a less convenient answer.

## Code Examples in This Repo

| Example | What It Shows |
|---------|--------------|
| [`pressure/`](./pressure/) | Metrics-only observer — measured p99 latency correctly elevated, structurally unable to attribute it to a service |
| [`principle-applied/`](./principle-applied/) | Trace with per-service spans — measured identical total duration, exact culprit and duration identified |
| [`wrong-direction/`](./wrong-direction/) | Uncorrelated logs — measured 3 equally plausible candidates for one log entry, no field to resolve the ambiguity |

### How to Run

```bash
cd volume-12-production-engineering-observability/paper-65-observability-signals/pressure
javac *.java && java Main

cd volume-12-production-engineering-observability/paper-65-observability-signals/principle-applied
javac *.java && java Main

cd volume-12-production-engineering-observability/paper-65-observability-signals/wrong-direction
javac *.java && java Main
```
