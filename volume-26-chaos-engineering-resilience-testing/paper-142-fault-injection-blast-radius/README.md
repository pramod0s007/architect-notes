# Paper 142: Fault Injection Fundamentals & Blast Radius

Part of [Architect Notes](../../README.md) — Volume 26: Chaos Engineering & Resilience Testing.

## The Question

If injecting a fault answers a real question about resilience, why does doing it wrong turn
the test itself into the incident?

## Structure

- `pressure/` — A fault is injected into the inventory-service call path with no scoping at
  all: every request in the system routes through it, and there's no abort condition. Verified:
  100% blast radius, 3,000 of 3,000 requests affected, the full planned 30-tick duration.
- `wrong-direction/` — The team scopes the injection to a 10% canary slice of traffic — a real
  improvement — but still has no abort condition. Verified: direct exposure cut to 300 of 3,000
  requests, but the experiment still runs the full 30 ticks even though the canary's 100%
  failure was confirmed at tick 1.
- `principle-applied/` — Same 10% canary scoping, plus an automatic abort condition checked
  every tick against a predeclared safety abort threshold (canary error rate stays under 5%).
  Verified: aborts after 1 tick, only 10 requests affected — both blast radius and duration
  bounded.

## Running It

Each scenario is self-contained. From inside any of the three directories:

```bash
javac -Xlint:all *.java
java Main
```

## Full Write-Up

See [MEDIUM-DRAFT.md](../../../architect-notes-private/volume-26-chaos-engineering-resilience-testing/paper-142-fault-injection-blast-radius/MEDIUM-DRAFT.md)
(private repo) for the full analysis, comparison table, and interview Q&A.
