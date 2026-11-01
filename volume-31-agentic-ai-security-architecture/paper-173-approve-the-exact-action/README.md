# Paper 173: Approve the Exact Action, Not the Summary

Part of [Architect Notes](../../README.md) — Volume 31: Agentic AI Security Architecture.

## The Question

An operations agent proposes refunds, and the team decides that anything risky needs a human click. The
approval screen shows a summary the model wrote, while the call that finally runs carries its own
arguments, so the click can approve one thing while the system executes another. The same approval can
also be replayed, or used long after it was given. Asking for approval more often only teaches reviewers
to click through, and the summary is still not bound to the action. How do you make a human approval mean
something: tied to the exact call that will run, used once and short-lived, while keeping approvals rare
enough that a person actually reads them?

## Structure

- `pressure/` — The approval screen shows the agent's own summary, and the approval is a flag keyed by an
  id that the executor checks and nothing else. A scripted agent sends five calls: one that matches its
  approval, one with swapped arguments, a replay, a use 800 ticks after the click, and one with no
  approval. Verified: the legitimate $400 refund for order 6120 runs, but `approved-digest-matches-executed`
  fails (1 of 4 executed refunds differ from what the reviewer saw), `swapped-arguments-rejected` fails
  (order 6188 refunded $6500 under an approval for order 6131), `approval-single-use` fails (1 approval id
  ran more than once) and `approval-expires` fails (1 execution used an approval older than 30 ticks). The
  reviewer approved $1200 in total and the ledger paid $7700.
- `wrong-direction/` — The team asks for approval on every tool call: 24 prompts in a shift, still showing a
  model summary in front of the same executor. A scripted reviewer reads the first 6 prompts, then clicks
  through, and a perfect reviewer reads all 24. Verified: all 22 routine refunds run, but
  `swapped-arguments-rejected` fails (the swapped call ran in 2 of 2 shifts, even when every prompt was
  read), `accurate-bad-action-declined` fails (the tired reviewer approved the accurate $4800 refund and it
  ran) and `approvals-stay-readable` fails (24 prompts, over the 6 a reviewer reads).
- `principle-applied/` — The approval service builds the canonical call (tool, merchant, order, amount,
  expiry, nonce), renders the screen from that same object and records its SHA-256 digest. The executor
  recomputes the digest from the call about to run, rejects a mismatch, checks expiry and consumes a
  single-use nonce, and refunds of $50 or less skip the queue. Verified: all 8 checks pass. The swapped
  call is rejected (digest 1d18af19 differs from approved 5b35ed3c), the approved refund runs once, the
  replay and the late use are rejected, 21 routine refunds run without a prompt, only 3 prompts reach the
  reviewer, the accurate $4800 refund is declined, and 0 dollars run that were not approved.

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
