# Paper 166: Verifying Nova's Resilience Actually Holds

Part of [Architect Notes](../../README.md) — Volume 30: Capstone: One System, End to End.

This is the fifth paper of Volume 30, continuing directly from Nova, the fictional multi-tenant
order/checkout and fulfillment platform carried through Papers 162 (data-layer architecture), 163
(API boundary evolution), 164 (order-to-fulfillment write path), and 165 (read-path caching). This
paper moves to Nova's resilience mechanisms — specifically, a circuit breaker guarding calls to a
modeled downstream inventory-lookup dependency. It ties to Volume 9 (Distributed Systems
Resilience) and Volume 26 (Chaos Engineering & Resilience Testing), applying Volume 26's own
one-time-drill-versus-continuous-suite distinction specifically to verifying that Nova's circuit
breaker still triggers correctly after each change to its call path.

## The Question

Nova's circuit breaker is checked once, before a refactor, and confirmed to open after 3 simulated
failure events routed to it. That single check is then treated as settled — nothing re-runs it
after later changes to the call path. When a later refactor adds a fallback branch, that branch
never routes the simulated failure event to the breaker's own failure counter before returning.
From that point forward, every simulated failure event can occur and the breaker's own recorded
state never reflects it, with nothing in the model re-checking to notice. A one-time manual GameDay
drill closes the gap for the call path that exists when the drill runs — but its recorded result
doesn't update itself when the call path changes afterward. What does it take to keep re-verifying
a resilience mechanism's triggering behavior as the code around it keeps changing?

## Structure

- `pressure/` — The breaker is checked once, before a refactor, and confirmed to open after 3
  simulated failure events. Verified: after a later refactor adds a fallback branch that stops
  routing simulated failures to the breaker's counter, 5 simulated failure events leave the
  breaker's state at CLOSED — nothing in the model re-runs the earlier check against this new call
  path to notice.
- `wrong-direction/` — A manual GameDay chaos drill is run once, genuinely confirming the breaker
  opens against the call path that exists at that point (recorded result: PASS). Verified: the
  same refactor happens after the drill already ran, and 5 simulated failure events again leave the
  breaker CLOSED — the drill's recorded PASS is never re-run against this new call path, so it
  keeps describing a call path that no longer exists.
- `principle-applied/` — The identical failure-injection experiment (verified byte-identical call
  paths across all 3 scenarios) is wired into an automated chaos suite that re-executes against
  whichever call path is selected for a given modeled deploy. Verified: Deploy 1 (pre-refactor call
  path) reports PASS; Deploy 2 (refactored call path) reports FAIL — the regression is reported by
  that same deploy's own suite run, not by a stale drill result recorded earlier.

## Running It

Each scenario is self-contained:

```bash
javac -Xlint:all *.java
java Main
```

## Full Write-Up

See [MEDIUM-DRAFT.md](../../../architect-notes-private/volume-30-capstone-one-system-end-to-end/paper-166-nova-resilience-verification/MEDIUM-DRAFT.md)
(private repo) for the full analysis, comparison table, and interview Q&A — including why a
one-time GameDay drill's check is genuine when it runs, but only a continuously re-executed suite
keeps re-verifying against the call path that's actually selected now.
