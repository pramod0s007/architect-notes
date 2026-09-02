# Output Guardrails — Validating Before a Model's Answer Moves Real Money

**Closes Volume 14** — picking up right after Paper 76 (Agentic Tool-Calling Reliability).

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

A model's structured output can be well-formed and still wrong in a way that changes its actual meaning (measured: a negative refund amount, applied directly, became a $50 charge instead of a refund). Schema and range validation, with rejection on failure, catches this before it takes effect (measured: the malformed request was rejected and flagged, the valid requests were unaffected). This paper then shows the overcorrection: auto-repairing invalid output instead of rejecting it, which removes the only signal that a problem occurred (measured: an auto-repaired, still-processed refund generated zero flags despite originating from corrupted model output).

## The Measurements

- **No validation**: a negative refund amount is applied directly — net customer impact -$10.00 (a $50 charge disguised as a refund).
- **Validate, reject on failure**: the malformed request is rejected and flagged for review — net impact $40.00, exactly the two genuinely valid refunds.
- **Validate, auto-repair on failure (wrong direction)**: the negative amount is sign-flipped and processed — net impact $90.00, with zero flags raised on the one request that most needed review.

## The Finding

**The goal of a guardrail isn't guessing the model's intent more cleverly — it's guaranteeing that whatever doesn't satisfy the required schema and value constraints never takes effect without a human finding out.** Auto-repairing invalid output doesn't fix the underlying problem; it removes the only signal that a problem occurred in the first place.

## Code Examples in This Repo

| Example | What It Shows |
|---------|--------------|
| [`pressure/`](./pressure/) | No validation — measured a negative refund amount applied directly as a real charge |
| [`principle-applied/`](./principle-applied/) | Schema and range validation, reject on failure — measured the malformed request caught and flagged before it took effect |
| [`wrong-direction/`](./wrong-direction/) | Auto-repair invalid values instead of rejecting — measured a corrupted request processed with zero visibility |

### How to Run

```bash
cd volume-14-ai-system-design-serving/paper-77-output-guardrails/pressure
javac *.java && java Main

cd volume-14-ai-system-design-serving/paper-77-output-guardrails/principle-applied
javac *.java && java Main

cd volume-14-ai-system-design-serving/paper-77-output-guardrails/wrong-direction
javac *.java && java Main
```
