# Paper 187: Partial Effects: Compensation and Reconciliation

Part of [Architect Notes](../../README.md) — Volume 34: Reliable and Bounded Agent Execution.

## The Question

An overnight agent run issues a goodwill credit, emails the merchant and posts a ledger entry. Each step
commits in its own system, so when the last step fails, the first two are already out in the world: the
merchant has spent the credit, the email is in an inbox, and the run log marks the run failed and names
only the last step. The tempting fix is a try and catch that undoes everything in reverse order and
ignores errors, but both undo calls are refused and the log still claims a clean rollback. A transaction
cannot span these systems, and an email cannot be unsent. How do you handle a multi-step agent workflow
that fails halfway through?

## Structure

- `pressure/` — The Orbit Ops Agent's runner sends a $60 credit for order 5101, a notice email and a
  ledger post one step at a time, each committing alone, while a scripted plan stands in for the agent.
  The merchant spends the credit, the ledger post fails at the last step, and the run log marks the run
  failed. Verified: the clean $25 run for order 5102 passes `legit-complete-run-needs-no-compensation`
  (3 of 3 effects), but `partial-run-compensated-in-reverse` fails (0 of 2 completed steps
  compensated), `irreversible-effect-flagged-not-hidden` fails (2 effects standing, 0 flagged) and
  `reconciler-finds-unrecorded-drift` fails (0 of 1 unbooked credits reported); the morning tie-out
  shows $85 issued and $25 booked.
- `wrong-direction/` — The same runner wraps the steps in a try and catch that undoes each completed
  step in reverse order, swallows every error and logs `rolled back cleanly`. Verified: the mailer has
  no recall and the credit is already spent, so both undo calls are refused and ignored while the log
  claims a clean rollback; `partial-run-compensated-in-reverse` fails (0 of 2 compensated, 2 undo calls
  refused), `irreversible-effect-flagged-not-hidden` fails (2 effects standing, 0 flagged) and
  `reconciler-finds-unrecorded-drift` fails (0 of 1 unbooked credits reported). The clean run still
  passes.
- `principle-applied/` — Every effect has a declared compensation before the run starts, intent is
  recorded in a ledger before acting, the unwind runs newest first with one idempotency key per
  compensation, the run closes with an honest status, and a reconciler that only reports compares the
  credits at Meridian with the ledger of intent and the books. Verified: all 5 checks pass. The run
  closes FAILED with 2 compensations recorded and 2 flags open for a person, a replayed unwind after a
  restart is deduplicated, run 0418 closes DONE with 0 compensations, and the reconciler reports $105
  of drift, including legacy credit 5103 that no run recorded.

## Interactive Explorer

Download [`explorer/explorer.html`](explorer/explorer.html) and open it in a browser. It replays the real
output of the three programs as an animated timeline you can step through event by event, shows each
check turning pass or fail, and compares the three designs side by side. It is one self-contained file
that works offline. GitHub displays HTML files as source, so download the file rather than opening it on
the repository page.

## Running It

Each scenario is self-contained; run these commands inside `pressure/`, `wrong-direction/` or
`principle-applied/`:

```bash
javac -Xlint:all *.java
java Main
```

## Read the Full Article on Medium

_Article coming soon on Medium._
