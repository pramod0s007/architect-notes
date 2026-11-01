# Paper 198: Continuous Verification and Step-Up

Part of [Architect Notes](../../README.md) — Volume 36: Zero Trust and Workload Identity.

## The Question

An operator signs in to the Ops console, and later the laptop falls out of compliance. Nothing asks
again, so the session keeps its full authority, including the power to change where a merchant's
payouts go. A copy of the token, lifted from a shared debug log, works from another machine until it
expires. Shorter tokens look like the cure, but they push a team toward a silent refresh that checks
nothing, and the weakness only moves. How should trust be recomputed on every request, with fresh
proof demanded only for the actions that need it? What does continuous verification mean in a zero
trust design, and where does step-up authentication fit?

## Structure

- `pressure/` — Meridian accepts any token that has not expired, and nothing else is asked after the
  sign-in on lap-3. A copy of the token lifted from a debug log is replayed from `rt-box`, and a
  noncompliant posture report is stored where no rule reads it. Verified:
  `stolen-token-from-new-device-denied` fails (1 payout change executed from `rt-box`),
  `posture-change-downgrades-session` fails (1 executed while lap-3 was noncompliant) and
  `sensitive-action-requires-step-up` fails (1 executed on a sign-in older than the 2-tick window,
  0 prompts shown). `legit-routine-action-no-prompt` passes: 2 routine refunds, 0 prompts.
- `wrong-direction/` — Tokens live 4 ticks instead of 12 and an operator with a lapsed token signs in
  again, then a refresh token with no lifetime and no checks makes the prompts stop. Verified:
  `legit-routine-action-no-prompt` fails (2 of 4 routine refunds stopped at a forced sign-in),
  `stolen-token-from-new-device-denied` fails (3 payout changes executed from `rt-box`, the last 79
  ticks after the operator's last request) and `refresh-reevaluates-signals` fails (3 of 3 refreshes
  granted FULL without reading a signal). `expired-token-denied` passes: 4 requests refused as
  EXPIRED, 0 run after expiry.
- `principle-applied/` — Meridian asks the pdp on every request: token expiry, a proof of the key
  bound at sign-in, a posture level that a failing report lowers at once, and a fresh strong sign-in
  within a 2-tick window for `PUT /payout-destination`. A refresh needs the same proof and re-reads
  the signals. Verified: all 6 checks pass. The copied token is REJECTED twice and its refresh
  REFUSED, 3 payout changes run (2 step-ups, 0 from another device), 3 routine refunds run with 0
  prompts, and 1 payout request is denied after the repair until the next refresh at t=11.

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
