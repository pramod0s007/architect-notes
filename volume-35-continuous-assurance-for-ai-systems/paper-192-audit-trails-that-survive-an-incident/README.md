# Paper 192: Audit Trails That Survive an Incident

Part of [Architect Notes](../../README.md) — Volume 35: Continuous Assurance for AI Systems.

## The Question

A merchant disputes a payout, and a lawyer asks which of five refunds an AI agent made on its own
authority and which a person approved. The answer has to come from a record, and most systems already
have one: an application log that every service can write to, the agent's own included, and that keeps
only the newest lines. Making it bigger and tracing everything looks like the fix, but it copies
customer data into free text and lets an agent's reply imitate a line the gateway wrote. What should an
audit trail for an agent contain, who should be allowed to write it, and how can it show that nobody
changed it after the fact?

## Structure

- `pressure/` — The Ops Agent issues five refunds through the gateway while the gateway and the agent's
  own service both append to one free-text application log that keeps only the newest 8 lines. A
  scripted model stub adds four retry-loop debug lines and a reply saying finance approver fin-2
  approved order 8861. Verified: the legitimate refunds work (5 of 5 executed), but
  `no-gap-in-audit-window` fails (3 of 5 refunds have a line), `event-names-subject-and-actor` fails (0
  of 3 surviving effect lines name subject, chain and policy version),
  `agent-cannot-write-its-own-audit` fails (5 lines from the agent service were accepted) and
  `legit-incident-reconstruction-works` fails (0 of 5 refunds attributed correctly).
- `wrong-direction/` — The same log grows to 200 lines and traces every prompt, tool call and reply
  exactly as written. Verified: `no-gap-in-audit-window` now passes (5 of 5 refunds have a line, 21 of
  200 lines used), but `event-names-subject-and-actor` fails (0 of 6 refund lines name subject, chain
  and policy version), `payload-stored-as-digest-not-copy` fails (10 lines hold a customer email or
  the service token), `forged-newline-cannot-fake-entry` fails (refund lines for orders not in the
  ledger: 1), `agent-cannot-write-its-own-audit` fails (15 lines from the agent service were accepted)
  and `legit-incident-reconstruction-works` fails (0 of 5 refunds attributed correctly).
- `principle-applied/` — Only the gateway can append to the audit store. Each entry carries the
  subject, the actor chain, the tool, an argument digest, the decision and the policy version, and is
  chained by hash; every third entry is checkpointed to a separate anchor store, payloads sit behind a
  vault pointer, and every field is escaped. Verified: all 8 checks pass. The scheduled verification
  reads 6 entries and 2 anchors, 5 of 5 refunds are attributed correctly, a simulated insider's edit,
  rewrite and truncation are each detected, and erasing a payload leaves the chain intact (0 customer
  emails in the audit store).

## Interactive Explorer

Download [`explorer/explorer.html`](explorer/explorer.html) and open it in a browser. It replays the
captured output of the three programs as a timeline you can step through event by event, shows each
check turning pass or fail, and compares the three designs side by side. The file is self-contained and
works offline. GitHub shows HTML files as source, so download it rather than opening it on the
repository page.

## Running It

Each program is self-contained; run these commands inside `pressure/`, `wrong-direction/` or
`principle-applied/`:

```bash
javac -Xlint:all *.java
java Main
```

## Read the Full Article on Medium

_Article coming soon on Medium._
