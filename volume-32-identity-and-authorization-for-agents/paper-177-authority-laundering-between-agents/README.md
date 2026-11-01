# Paper 177: Authority Laundering Between Agents

Part of [Architect Notes](../../README.md) — Volume 32: Identity and Authorization for Agents.

## The Question

A support agent holds no payout authority, but it can ask an operations agent that does. The operations
agent trusts the request because the caller is a colleague inside the platform, and it carries out the
adjustment with its own, far wider authority. One poisoned order note is enough for the asker to move
money it was never allowed to touch, and the ledger records only the last actor. The obvious fix is an
allow-list of who may ask, with a field naming the sender. But the sender writes that field, and a name
carries no limits. How do you stop a low-privilege agent from using a high-privilege agent to do what it
cannot do itself?

## Structure

- `pressure/` — The Support Assistant forwards every adjustment request it reads, including one hidden in
  an order note, and the Ops Agent executes each with its own authority because the caller is inside the
  platform. Verified: the legitimate $120 correction for merchant 3012 works, but
  `low-privilege-request-not-laundered` fails (merchant 4417 adjusted by $4000),
  `callee-authority-is-intersection` fails (2 of 2 executions ran with Ops' own authority, up to $50000,
  not the task's) and `chain-recorded-in-audit` fails (2 of 2 ledger entries name only agent:ops).
- `wrong-direction/` — Ops accepts requests only from agents on an allow-list (Support removed, agent:recon
  kept) and reads a `from` field naming the sender. Verified: the honest $120 correction is rejected, so
  `legit-delegated-adjustment-works` fails; the injected request is rejected under its true name, then
  accepted once the stub writes `from=agent:recon`, so `low-privilege-request-not-laundered` fails (1
  injected adjustment executed with Ops' own authority) and `forged-origin-claim-rejected` fails (1 message
  accepted from a sender that was agent:support).
- `principle-applied/` — The authz service signs one narrow token per Support task, Ops verifies it and
  acts with the intersection of its own authority and the token's, and the whole chain is recorded in the
  ledger and the audit log. Verified: all 5 checks pass. The $120 correction runs as `payouts:write` on
  merchant 3012 up to $500 with the chain `admin-3012 > agent:support > agent:ops`, the $4000 request is
  denied (0 ledger entries for merchant 4417), and the edited token is rejected because its signature does
  not verify.

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
