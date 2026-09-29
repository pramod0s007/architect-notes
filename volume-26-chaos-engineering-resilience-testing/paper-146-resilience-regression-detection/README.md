# Paper 146: Resilience Regression Detection

Part of [Architect Notes](../../README.md) — Volume 26: Chaos Engineering & Resilience Testing.

## The Question

A circuit breaker's code is still there, unchanged since a code review approved it. Does that mean it still works?

## Structure

- `pressure/` — Two services shipped with a correctly configured circuit breaker each; neither has
  ever been re-verified since. Verified: Service A's circuit breaker was later removed entirely by
  an unrelated refactor, and Service B's failure threshold was silently changed from 5 to 500 by a
  different unrelated refactor — both undiscovered, because nothing has ever checked.
- `wrong-direction/` — A manual code-review checklist item asks "does a CircuitBreaker class still
  exist?" Verified: Service A's removal is correctly flagged, but Service B's class is still
  present, so the checklist passes it even though its trigger point has moved far outside the
  workload it's actually meant to protect against.
- `principle-applied/` — An automated resilience-assertion test feeds 10 consecutive failures
  through each mechanism and asserts it opens by the 5th. Verified: Service A fails immediately
  (no mechanism to exercise), and Service B's breaker never opens within the 10 failures — both
  regressions caught by the same behavioral check, which throws and exits non-zero on failure
  (run with `2>&1` to see the assertion output).

## Running It

Each scenario is self-contained:

```bash
javac -Xlint:all *.java
java Main
```

## Full Write-Up

See [MEDIUM-DRAFT.md](../../../architect-notes-private/volume-26-chaos-engineering-resilience-testing/paper-146-resilience-regression-detection/MEDIUM-DRAFT.md)
(private repo) for the full analysis, comparison table, and interview Q&A.
