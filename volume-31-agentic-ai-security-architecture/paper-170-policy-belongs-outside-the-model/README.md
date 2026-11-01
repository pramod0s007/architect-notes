# Paper 170: Policy Belongs Outside the Model

Part of [Architect Notes](../../README.md) — Volume 31: Agentic AI Security Architecture.

## The Question

An AI support assistant can propose a refund, and the runtime beneath it executes whatever is proposed.
The refund rule lives in the system prompt: never refund more than $100. It holds for an hour of red-team
attempts and then gives way to a polite ticket that says thank you and please. The obvious responses are
to repeat the rule more often and to add a second model that approves each call, but the approver reads
the same persuasive thread as the model it checks. Where should the rule live so that it still holds when
the model is talked into proposing the call, and what does that cost in owners, tests and blocked edge
cases?

## Structure

- `pressure/` — The Orbit Support Assistant keeps the refund rule only in its system prompt, stated three
  times, and a stub model with a countable resistance to persuasion decides whether to propose the call.
  The runtime executes every proposal. Verified: the legitimate $40 refund for order 8841 works, but
  `refund-over-limit-denied` fails (a $4000 refund executed against a $100 limit) and
  `policy-independent-of-model` fails (the ledger changed with the model's belief; refunds: NATURAL 2,
  HOLDS 1, OBEDIENT 2).
- `wrong-direction/` — The rule is stated six times and a second model, a judge, approves each call before
  the runtime sees it. In the simulation the judge reads the same thread as the model. Verified: the
  prompt holds until minute 80, then a longer ticket (pull 11 against 6 statements) is approved and the
  $4000 refund executes, so `refund-over-limit-denied` fails and `policy-independent-of-model` fails
  (NATURAL 2, HOLDS 1, OBEDIENT 2); the judge also approves the legitimate $40 refund, and
  `judge_catches = 0`.
- `principle-applied/` — Every proposed call passes a policy gate outside the model: a tool allow-list, a
  whole-dollar amount, the $100 limit, and the order's payer as the only recipient. The gate denies by
  default, fails closed on any error and logs a rule id and a reason for each decision. Verified: all 9
  checks pass. The model is still persuaded, yet two refunds execute ($40, and $100 exactly at the limit),
  6 of 8 gate decisions are denied (the $4,000 refund, a store credit, a wrong recipient, a negative
  amount, a $101 refund and a payer lookup outage, under five distinct rule ids), the largest refund is
  $100, and the ledger is identical under every model belief. Denial detail goes to the audit log; the
  generic reply a requester would see is described in the article, not modeled.

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
