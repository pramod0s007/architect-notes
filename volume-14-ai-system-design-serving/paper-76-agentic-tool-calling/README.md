# Agentic Tool-Calling Reliability — When a Retry Isn't the Same Request

**Continues Volume 14** — picking up right after Paper 75 (Context Window Budgeting).

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

A tool's correct idempotency guarantee is only as good as the caller's discipline about reusing the same key across retries — a naive retry wrapper that generates a fresh idempotency key on each attempt defeats a correctly-implemented deduplication mechanism on the receiving side (measured: one customer action produced 2 orders). Reusing the same key across every retry of the same logical action fixes this completely (measured: exactly 1 order created, identical tool, identical failure scenario). This paper then shows the overcorrection: refusing to retry at all to avoid duplicates, which doesn't resolve the ambiguity a timeout creates — it just reports a guess as fact (measured: an order that was actually created was reported to the customer as failed).

## The Measurements

- **Retry with a fresh key each time**: 2 orders created for 1 customer action — the tool's own correct dedup never got a chance to work.
- **Retry with the same key reused**: 1 order created — identical tool, identical scenario, only the caller's key discipline changed.
- **Never retry (wrong direction)**: 1 order actually created, but reported to the customer as "FAILED, please try again" — a false failure report that risks a real, manually-triggered duplicate.

## The Finding

**The bug in naive retry logic is almost never in error handling — it's in what "the same request" means when a retry happens.** Idempotency on the receiving side protects against a duplicate request, not a duplicate action expressed through two different requests. The identifier for a logical action needs to be generated once, at the point that action is decided on, and reused through every attempt to carry it out — retries included.

## Code Examples in This Repo

| Example | What It Shows |
|---------|--------------|
| [`pressure/`](./pressure/) | Fresh idempotency key on retry — measured 2 orders created for 1 customer action |
| [`principle-applied/`](./principle-applied/) | Same idempotency key reused on retry — measured exactly 1 order created |
| [`wrong-direction/`](./wrong-direction/) | No retry at all — measured a successfully created order reported to the customer as failed |

### How to Run

```bash
cd volume-14-ai-system-design-serving/paper-76-agentic-tool-calling/pressure
javac *.java && java Main

cd volume-14-ai-system-design-serving/paper-76-agentic-tool-calling/principle-applied
javac *.java && java Main

cd volume-14-ai-system-design-serving/paper-76-agentic-tool-calling/wrong-direction
javac *.java && java Main
```
