# RAG Failure Modes — Context Budgets and Contradictory Chunks

**Closes Volume 13** — picking up right after Paper 70 (Retrieval Reranking).

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

Correct retrieval and correct reranking do not guarantee the right information reaches generation — a context-assembly step that concatenates chunks in document order rather than relevance order can push the single most relevant, correctly-ranked chunk past a real size budget entirely (measured: the answer-bearing chunk was cut completely). Assembling by relevance rank instead fixes this at zero cost upstream (measured: identical chunks, identical budget, correct chunk preserved). This paper then shows the overcorrection: a generous, unbounded budget removes truncation but also removes the one thing limiting how much stale or duplicate material could ride along (measured: a never-purged stale chunk contradicted the correct answer in the same assembled context, with no field marking which was current).

## The Measurements

- **Document-order assembly, tight budget**: the most relevant chunk is cut entirely; the assembled context is missing the answer.
- **Relevance-order assembly, same budget**: the most relevant chunk fits; only the least relevant chunk is cut.
- **Generous budget, all chunks included (wrong direction)**: nothing truncated, but the context contains two contradicting answers to the same question with no way to disambiguate.

## The Finding

**Context assembly is an architecturally distinct step from retrieval, reranking, and index freshness — each with independent failure modes.** A generous context budget is not a safe default: it trades a detectable failure (a missing answer) for a silent one (an undetectable contradiction). Fixing this requires deduplication and recency-awareness in the index itself, not just a bigger context window.

## Code Examples in This Repo

| Example | What It Shows |
|---------|--------------|
| [`pressure/`](./pressure/) | Document-order concatenation under a tight budget — measured the correctly-ranked best chunk cut entirely |
| [`principle-applied/`](./principle-applied/) | Relevance-order concatenation, same budget — measured the correct chunk preserved |
| [`wrong-direction/`](./wrong-direction/) | Generous budget, all chunks included — measured a stale chunk contradicting the correct answer, undetected |

### How to Run

```bash
cd volume-13-ai-system-design-retrieval/paper-71-rag-failure-modes/pressure
javac *.java && java Main

cd volume-13-ai-system-design-retrieval/paper-71-rag-failure-modes/principle-applied
javac *.java && java Main

cd volume-13-ai-system-design-retrieval/paper-71-rag-failure-modes/wrong-direction
javac *.java && java Main
```
