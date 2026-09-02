# LLM Request Batching — Throughput vs. Latency Under Real Queuing

**Opens Volume 14** — AI System Design II: Serving, Cost & Agentic Systems.

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

A model inference call pays a mostly-fixed overhead cost regardless of how many requests it processes together — paying that cost once per request instead of once per batch wastes the vast majority of total processing time (measured: 91% of total time was pure overhead with no batching). Grouping requests into batches amortizes this cost (measured: 5.5x throughput improvement, identical hardware). This paper then shows the overcorrection: batching that only triggers once a batch is completely full, with no timeout fallback, which can starve requests indefinitely during real, uneven traffic (measured: 0 of 3 requests processed during a quiet period).

## The Measurements

- **No batching**: 5500ms for 100 requests (18.18 req/sec); 5000ms of that is pure per-call overhead.
- **Batching, size-only trigger**: 1000ms for 100 requests (100 req/sec) under heavy traffic — but 0 of 3 requests processed under sparse traffic, indefinitely.
- **Batching, size OR timeout trigger**: same 100 req/sec under heavy traffic; 3 of 3 requests processed once the timeout elapses under sparse traffic.

## The Finding

**A batching strategy needs a trigger on whichever comes first — a full batch, or a maximum wait time elapsed — not size alone.** A missing timeout isn't a smaller version of the batching win; it's a starvation bug that only appears under traffic patterns a synthetic load test at steady, heavy volume will never surface.

## Code Examples in This Repo

| Example | What It Shows |
|---------|--------------|
| [`pressure/`](./pressure/) | No batching — measured 91% of total processing time is pure per-call overhead |
| [`principle-applied/`](./principle-applied/) | Batch size OR timeout trigger — measured 5.5x throughput under heavy traffic, zero starvation under sparse traffic |
| [`wrong-direction/`](./wrong-direction/) | Batch size only, no timeout — measured complete starvation (0 of 3 requests) during a quiet period |

### How to Run

```bash
cd volume-14-ai-system-design-serving/paper-72-llm-request-batching/pressure
javac *.java && java Main

cd volume-14-ai-system-design-serving/paper-72-llm-request-batching/principle-applied
javac *.java && java Main

cd volume-14-ai-system-design-serving/paper-72-llm-request-batching/wrong-direction
javac *.java && java Main
```
