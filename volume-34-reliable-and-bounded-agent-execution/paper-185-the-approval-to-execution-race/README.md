# Paper 185: The Approval-to-Execution Race

Part of [Architect Notes](../../README.md) — Volume 34: Reliable and Bounded Agent Execution.

## The Question

An overnight agent waits for a person's yes before it moves money. The approval arrives in the evening and
the agent acts hours later, after a rep has refunded part of the same order and a card dispute has opened.
The approval is genuine, unexpired and from an authorized person, so every check passes, and every check is
about the past. The obvious fixes are a shorter timeout and asking the model to confirm that nothing has
changed, but age is not state, and a confirmation the model cannot ground in the order is a guess. How do
you stop an approval from authorizing an action against a world the approver never saw?

## Structure

- `pressure/` — Four approvals from a support lead meet a night of changes: reps refund orders 5102 and
  5104 and a card dispute opens on 5102. When the run resumes at t=60, a scripted worst-case model stub
  proposes five refunds, one of them a repeat after a timeout, and the gateway executes all of them
  without reading any order. Verified: the legitimate refund on order 5103 works, but
  `stale-approval-rejected` fails (2 of 2 approvals on changed orders still refunded, $65),
  `state-drift-detected-at-execution` fails (0 of 2 changed orders detected),
  `approval-consumed-atomically` fails (1 refund repeated an approval that had already paid out, $30) and
  `expired-approval-rejected` fails (1 approval older than 48 ticks still produced a refund).
- `wrong-direction/` — The gateway refuses any approval older than a 12-tick window and, inside the window,
  asks the model to confirm that nothing has changed; the scripted stub answers with a constant reply
  because its context never contains the order. Verified: the window expires four calls, including both
  calls for the legitimate order 5103 refund, while ap-4, only 2 ticks old, is confirmed and refunds $25
  against a changed order. `stale-approval-rejected` fails (1 of 2 approvals on changed orders still
  refunded, $25), `state-drift-detected-at-execution` fails (0 of 2 changed orders detected) and
  `legit-unchanged-state-executes` fails (unchanged order 5103 was never refunded, ap-3 expired).
  `expired-approval-rejected` passes.
- `principle-applied/` — Each approval is stored with the order version the approver saw. The gateway
  re-reads the version at execution, opens a fresh approval request bound to the current version on any
  mismatch and keeps a 48-tick limit as a second bound. Meridian applies the refund only if the version
  still holds and spends the approval in the same step. Verified: all 6 checks pass. ap-1 expires at
  age 57, the 2 changed orders are detected and re-requested, the legitimate $30 refund runs once, its
  repeated call is rejected as already spent, and stale refunds total $0.

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
