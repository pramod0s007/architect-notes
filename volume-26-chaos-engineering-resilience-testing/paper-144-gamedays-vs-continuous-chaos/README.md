# Paper 144: GameDays vs Continuous/Automated Chaos

Part of [Architect Notes](../../README.md) — Volume 26: Chaos Engineering & Resilience Testing.

## The Question

A GameDay verified a resilience mechanism eighteen months ago. Is it still verified today?

## Structure

- `pressure/` — A single one-time GameDay runs once, at tick 1, and never again. Two regressions
  to the same mechanism are introduced later in the deploy timeline. Verified: 0 of 2 regressions
  ever detected by testing — both live entirely after the only check that will ever run.
- `wrong-direction/` — The same GameDay is now re-run on a fixed periodic schedule, every 5 ticks.
  Verified: 1 of 2 regressions detected (caught at tick 16, a 3-tick delay); the other regression
  (ticks 7-9) lives and dies entirely between two scheduled checks and is never seen.
- `principle-applied/` — The check now runs continuously, against every deploy (every tick), not
  on a calendar schedule. Verified: 2 of 2 regressions detected, each caught the same tick it was
  introduced (0-tick delay) — there's no longer a gap for a regression to hide in.

## Running It

Each scenario is self-contained:

```bash
javac -Xlint:all *.java
java Main
```

## Full Write-Up

See [MEDIUM-DRAFT.md](../../../architect-notes-private/volume-26-chaos-engineering-resilience-testing/paper-144-gamedays-vs-continuous-chaos/MEDIUM-DRAFT.md)
(private repo) for the full analysis, comparison table, and interview Q&A.
