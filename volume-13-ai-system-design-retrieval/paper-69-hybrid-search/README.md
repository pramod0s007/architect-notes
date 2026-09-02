# Hybrid Search — When Pure Semantic Similarity Misses What the User Actually Typed

**Continues Volume 13** — picking up right after Paper 68 (Embedding Staleness).

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

Pure vector similarity structurally undervalues exact, specific terms a user explicitly typed — a rare token like a product code contributes weakly to an embedding built to capture general meaning, so a document with broader, generic relevant language can outrank the document that actually matches what was typed (measured: querying "X200 warranty" retrieved a generic document that never mentions "X200" at all). A bounded, same-scale keyword boost fixes this without distorting the ranking elsewhere (measured: the correct document won by exactly the margin the boost was sized to provide). This paper then shows the overcorrection: fusing scores by adding a raw, unbounded keyword count directly to a capped vector score, which lets a near-content-free, keyword-stuffed document dominate completely (measured: a spammy listing that just repeats the code four times outranked both other documents).

## The Measurements

- **Vector similarity only**: correct document scores 0.62; a generic, irrelevant document wins at 0.81.
- **Hybrid, bounded boost**: correct document scores 0.82 (0.62 + 0.20 boost) and wins by 0.01 over the generic document's 0.81.
- **Hybrid, raw unbounded count (wrong direction)**: a near-content-free listing scores 4.15 (0.15 + 4 raw occurrences) and wins outright — a worse failure than pure vector search alone.

## The Finding

**Hybrid search isn't "keyword search plus vector search as separate fallbacks" — it's one fused signal where both parts operate on scales that were actually designed to be added together.** Fusing an unbounded signal with a bounded one doesn't balance them; it lets whichever signal has the larger numeric range dominate completely, regardless of which one is actually more informative for a given query.

## Code Examples in This Repo

| Example | What It Shows |
|---------|--------------|
| [`pressure/`](./pressure/) | Vector similarity only — measured the exact-match document losing to a generic, irrelevant one |
| [`principle-applied/`](./principle-applied/) | Hybrid with a bounded, same-scale keyword boost — measured the correct document winning by the intended margin |
| [`wrong-direction/`](./wrong-direction/) | Hybrid with unnormalized raw keyword counts — measured a near-content-free document dominating completely |

### How to Run

```bash
cd volume-13-ai-system-design-retrieval/paper-69-hybrid-search/pressure
javac *.java && java Main

cd volume-13-ai-system-design-retrieval/paper-69-hybrid-search/principle-applied
javac *.java && java Main

cd volume-13-ai-system-design-retrieval/paper-69-hybrid-search/wrong-direction
javac *.java && java Main
```
