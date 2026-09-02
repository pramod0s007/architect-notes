# Chunking Strategy — Why Chunk Boundaries Silently Corrupt Retrieval

**Opens Volume 13** — AI System Design I: RAG & Vector Search.

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

A fixed-size chunk boundary can split the exact sentence that answers a query, and no amount of retrieval sophistication downstream can recover an answer that was never left intact in any single chunk (measured: the highest-scoring chunk for a real question was proven to lack the answer entirely). Sentence-aware chunking fixes this at the source, with zero retrieval-side change (measured: identical query, identical retriever, now returns the correct, intact sentence). This paper then shows the overcorrection: adding overlap to fixed-size windows, which increases storage/embedding cost without removing the one defective chunk that caused the original failure (measured: 67% more chunks, same broken fragment, same failure).

## The Measurements

- **Fixed-size chunking**: retrieved chunk contains the actual answer — false. The answer-bearing sentence was split exactly at the chunk boundary.
- **Sentence-aware chunking**: retrieved chunk contains the actual answer — true. Same query, same retriever, same target size.
- **Fixed-size + 100-char overlap (wrong direction)**: 5 chunks instead of 3 (+67%), original defective chunk unchanged, retrieval still fails.

## The Finding

**Chunk-boundary corruption is a chunking-layer problem, not a retrieval-layer problem.** A better reranker, a better embedding model, or more retrieval tuning cannot return text that doesn't exist intact in any chunk. Overlap is a real, widely-used mitigation — but it dilutes the problem by adding redundant coverage; it does not guarantee the defective chunk is removed or that retrieval will prefer the intact copy over the fragment.

## Code Examples in This Repo

| Example | What It Shows |
|---------|--------------|
| [`pressure/`](./pressure/) | Fixed-size chunking — measured the answer-bearing sentence split exactly at the chunk boundary, retrieval fails |
| [`principle-applied/`](./principle-applied/) | Sentence-aware chunking — measured the same sentence kept intact, retrieval succeeds |
| [`wrong-direction/`](./wrong-direction/) | Fixed-size chunking + overlap — measured 67% more chunks, same defective chunk, same failure |

### How to Run

```bash
cd volume-13-ai-system-design-retrieval/paper-66-chunking-strategy/pressure
javac *.java && java Main

cd volume-13-ai-system-design-retrieval/paper-66-chunking-strategy/principle-applied
javac *.java && java Main

cd volume-13-ai-system-design-retrieval/paper-66-chunking-strategy/wrong-direction
javac *.java && java Main
```
