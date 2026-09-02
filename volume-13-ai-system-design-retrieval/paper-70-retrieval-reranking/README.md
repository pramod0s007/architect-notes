# Retrieval Reranking — Why Top-k Retrieval Isn't Top-k Relevance

**Continues Volume 13** — picking up right after Paper 69 (Hybrid Search).

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

A bi-encoder retrieval stage embeds a query and each document separately and ranks by similarity between those two independent embeddings — the truly best answer can be successfully retrieved and still lose to a worse one purely because of how the two are scored (measured: the most relevant candidate by human label was retrieved into the top-3 and still ranked #2). A cross-encoder reranking pass fixes this by scoring the query and each candidate jointly (measured: identical candidates, correct top-1 once the scoring function could see both together). This paper then shows the overcorrection: reaching for a bigger, better bi-encoder to do the reranking, which is still architecturally unable to check whether a document addresses a specific detail in the query (measured: increasing capacity reproduced the identical wrong top-1, just with different numbers).

## The Measurements

- **Initial bi-encoder retrieval**: top-1 = generic document (score 0.88); the actually best answer scores 0.85 and ranks #2.
- **Cross-encoder rerank**: same 3 candidates, top-1 = the correct, specific answer (score 0.93), matching the human relevance label.
- **Bigger bi-encoder (wrong direction)**: top-1 = the same generic document again (score 0.90) — same failure, different numbers.

## The Finding

**Retrieval and relevance are two different questions, and using the same score to answer both is the actual root cause of this failure mode — not a flaw in any specific embedding model.** A bi-encoder is fast because it never lets the query and a document interact before scoring; a cross-encoder is accurate because it does. A bigger bi-encoder gets better at general topical similarity without ever gaining the one capability — joint comparison — that this failure actually requires.

## Code Examples in This Repo

| Example | What It Shows |
|---------|--------------|
| [`pressure/`](./pressure/) | Bi-encoder retrieval — measured the truly best answer retrieved but ranked #2 |
| [`principle-applied/`](./principle-applied/) | Cross-encoder rerank on the same candidates — measured the correct answer promoted to #1 |
| [`wrong-direction/`](./wrong-direction/) | Bigger bi-encoder — measured the identical wrong top-1, just with different scores |

### How to Run

```bash
cd volume-13-ai-system-design-retrieval/paper-70-retrieval-reranking/pressure
javac *.java && java Main

cd volume-13-ai-system-design-retrieval/paper-70-retrieval-reranking/principle-applied
javac *.java && java Main

cd volume-13-ai-system-design-retrieval/paper-70-retrieval-reranking/wrong-direction
javac *.java && java Main
```
