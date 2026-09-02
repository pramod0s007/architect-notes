# Vector Index Tradeoffs — Brute-Force, IVF, and the Recall You Give Up for Speed

**Continues Volume 13** — picking up right after Paper 66 (Chunking Strategy).

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

Brute-force vector search is exact by construction, but its cost scales linearly with the number of stored vectors (measured: comparisons equal N from 1,000 to 100,000). An IVF (inverted file) index with an adequately sized `nprobe` can match brute-force recall at a fraction of the cost (measured: 60% of the comparisons, identical answer). This paper then shows the overcorrection: a single-probe IVF index that trusts the query's single nearest bucket to contain its nearest neighbor, which can silently return a wildly wrong result for any query whose true nearest neighbor happens to sit in a different bucket (measured: a real nearest neighbor at distance 2.0 replaced by one at distance 19.24 — 10x farther — with no error or warning).

## The Measurements

- **Brute-force**: comparisons = N, always exact, no recall risk.
- **IVF, nprobe=2**: 12/20 comparisons (60%), result identical to brute force.
- **IVF, nprobe=1 (wrong direction)**: 6/20 comparisons (30%), result 10x farther from the query than the true nearest neighbor — a silent recall failure.

## The Finding

**Recall loss under approximate vector search is not uniform — it concentrates on vectors near a bucket boundary.** A single-probe IVF index can pass every "obvious" test query and still fail catastrophically on the queries that matter, because the failure has nothing to do with how many buckets exist and everything to do with which specific bucket the true answer landed in.

## Code Examples in This Repo

| Example | What It Shows |
|---------|--------------|
| [`pressure/`](./pressure/) | Brute-force search — measured comparisons scaling linearly with dataset size |
| [`principle-applied/`](./principle-applied/) | IVF index, nprobe=2 — measured 60% of brute-force cost, identical recall |
| [`wrong-direction/`](./wrong-direction/) | IVF index, nprobe=1 — measured cheaper cost, 10x recall failure on a boundary query |

### How to Run

```bash
cd volume-13-ai-system-design-retrieval/paper-67-vector-index-tradeoffs/pressure
javac *.java && java Main

cd volume-13-ai-system-design-retrieval/paper-67-vector-index-tradeoffs/principle-applied
javac *.java && java Main

cd volume-13-ai-system-design-retrieval/paper-67-vector-index-tradeoffs/wrong-direction
javac *.java && java Main
```
