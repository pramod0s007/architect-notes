# Embedding Staleness — Why a Stale Embedding Model Breaks Retrieval Invisibly

**Continues Volume 13** — picking up right after Paper 67 (Vector Index Tradeoffs).

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

Two independently trained embedding models produce vectors that are individually valid but not comparable to each other — comparing a query embedded with a new model against documents still embedded with the old one produces a similarity score with no semantic meaning (measured: a refund question retrieved a shipping document at 0.99 similarity — higher confidence than most real systems ever see, with no error). Fully re-embedding every document on a model upgrade fixes this at the source (measured: identical query, correct document wins at 0.996). This paper then shows the overcorrection: re-embedding a document only when it's next edited, which leaves the most stable, least-edited content — often the most important content — permanently stale (measured: an un-migrated but correct document scored 0.16 while an unrelated stale document won at 0.99).

## The Measurements

- **No re-embedding (pressure)**: query about refund retrieves the shipping document at 0.9891 similarity — wrong, confident, and silent.
- **Full re-embed on upgrade**: same query, correct document wins at 0.9963 similarity.
- **Re-embed on next edit only (wrong direction)**: correct, un-migrated document scores 0.1622; an unrelated, also-stale document wins at 0.9891.

## The Finding

**A model upgrade is not complete until every stored vector has been recomputed under the new model.** An index that mixes vectors from two independently trained models isn't "slightly degraded" — it's comparing numbers that were never meant to be compared, and the resulting similarity scores can be higher and more confident-looking than a correct match, with no signal that anything is wrong.

## Code Examples in This Repo

| Example | What It Shows |
|---------|--------------|
| [`pressure/`](./pressure/) | Query from a new model compared against documents still on the old model — measured a confident, wrong top result |
| [`principle-applied/`](./principle-applied/) | All documents re-embedded with the current model — measured the correct document winning by a wide margin |
| [`wrong-direction/`](./wrong-direction/) | Documents migrated only when edited — measured an un-migrated correct document losing to an unrelated stale one |

### How to Run

```bash
cd volume-13-ai-system-design-retrieval/paper-68-embedding-staleness/pressure
javac *.java && java Main

cd volume-13-ai-system-design-retrieval/paper-68-embedding-staleness/principle-applied
javac *.java && java Main

cd volume-13-ai-system-design-retrieval/paper-68-embedding-staleness/wrong-direction
javac *.java && java Main
```
