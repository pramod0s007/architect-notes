# KV-Cache Reuse — Why Prefix Caching Changes the Cost Model Entirely

**Continues Volume 14** — picking up right after Paper 72 (LLM Request Batching).

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

A naive multi-turn conversation server reprocesses the entire conversation history on every turn, making cost grow with the square of conversation length rather than linearly with new content (measured: a 5-turn conversation reprocessed 550 total tokens, with turn 5 alone reprocessing 170 tokens to add 30 new ones). Caching the prefix by its actual content turns that quadratic cost linear (measured: 170 total tokens, 3.24x fewer). This paper then shows the overcorrection: caching by prefix length instead of content, which is cheaper to key but not unique — two completely unrelated conversations of the same length silently collide (measured: a password-reset conversation served a refund conversation's cached context).

## The Measurements

- **No caching**: 550 total tokens processed across 5 turns; cost grows quadratically with conversation length.
- **Content-keyed cache**: 170 total tokens processed across the identical 5 turns — 3.24x fewer.
- **Length-keyed cache (wrong direction)**: two unrelated 80-token-prefix conversations collide — one is served the other's cached context, a correctness bug, not a performance regression.

## The Finding

**A cache key's only non-negotiable job is uniquely identifying what it's a cache of.** Length is cheap to compute and compare, and it is not unique — any two unrelated prefixes of the same token count collide, silently, with no error and no signal that the wrong context was served.

## Code Examples in This Repo

| Example | What It Shows |
|---------|--------------|
| [`pressure/`](./pressure/) | No caching — measured cost growing with the square of conversation length |
| [`principle-applied/`](./principle-applied/) | Content-keyed cache — measured 3.24x fewer tokens processed, identical conversation |
| [`wrong-direction/`](./wrong-direction/) | Length-keyed cache — measured two unrelated conversations colliding on a shared prefix length |

### How to Run

```bash
cd volume-14-ai-system-design-serving/paper-73-kv-cache-reuse/pressure
javac *.java && java Main

cd volume-14-ai-system-design-serving/paper-73-kv-cache-reuse/principle-applied
javac *.java && java Main

cd volume-14-ai-system-design-serving/paper-73-kv-cache-reuse/wrong-direction
javac *.java && java Main
```
