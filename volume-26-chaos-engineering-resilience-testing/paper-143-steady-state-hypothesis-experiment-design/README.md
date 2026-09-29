# Paper 143: Steady-State Hypothesis & Experiment Design

Part of [Architect Notes](../../README.md) — Volume 26: Chaos Engineering & Resilience Testing.

## The Question

If a chaos experiment observes a lower success rate after injecting a fault, why isn't that
number, by itself, proof the fault caused a regression?

## Structure

- `pressure/` — A fault is injected and only the experiment group's numbers are ever measured;
  no control group exists. Verified: 90.1% mean success rate, with no reference point for
  whether that's normal or a real regression.
- `wrong-direction/` — A control group is now measured too, but the hypothesis is checked
  against a guessed, round-number threshold (90%) applied separately to both groups. Verified:
  the control group breaches that same threshold 5 of 20 ticks with zero fault present, so the
  experiment group's 9-of-20 breach count can't be cleanly attributed to the fault.
- `principle-applied/` — Same measured control group and fault series, but the hypothesis is
  now framed relative to the control group's own measured mean, with a stated tolerance.
  Verified: 93.25% measured baseline mean, a 91.25% floor (2-point tolerance), and a 90.10%
  experiment-group mean — a real, correctly-detected 3.15-point regression.

## Running It

Each scenario is self-contained. From inside any of the three directories:

```bash
javac -Xlint:all *.java
java Main
```

## Full Write-Up

See [MEDIUM-DRAFT.md](../../../architect-notes-private/volume-26-chaos-engineering-resilience-testing/paper-143-steady-state-hypothesis-experiment-design/MEDIUM-DRAFT.md)
(private repo) for the full analysis, comparison table, and interview Q&A.
