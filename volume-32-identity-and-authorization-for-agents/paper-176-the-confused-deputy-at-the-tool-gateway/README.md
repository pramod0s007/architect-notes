# Paper 176: The Confused Deputy at the Tool Gateway

Part of [Architect Notes](../../README.md) — Volume 32: Identity and Authorization for Agents.

## The Question

An agent platform puts one gateway in front of its backends so every tool call has a single door. The
gateway authenticates the agent and then does the work with a service credential of its own, because that
credential is the only one that reaches every backend. A request for another merchant's payouts arrives
from an agent working for merchant 3012, and the gateway answers, because the gateway is allowed to. The
obvious fix is to validate the id and tell the model to ask only for its own merchant. But 4417 is a real
id, and a prompt is a request, not a control. How do you make a gateway act with the caller's authority and
never its own, when the model can still ask for anything and the tokens now have to travel?

## Structure

- `pressure/` — Orbit's tool gateway authenticates the calling agent, then reads Atlas and Meridian with
  its own service credential and trusts the merchant id in the request. A worst-case agent stub asks once
  for its own merchant, 3012, and once for merchant 4417. Verified: the legitimate report for 3012 works,
  but `cross-merchant-read-denied` fails (2 backend reads served for a merchant the caller does not hold,
  $92600 of payouts exposed) and `downstream-call-uses-caller-authority` fails (4 of 4 backend calls ran
  as svc:gateway).
- `wrong-direction/` — The same gateway and credential, plus an id check (four digits, present in the
  merchant directory) and a system prompt that says to ask only for your own merchant. Verified: ids 44170
  and 9001 are rejected and `invalid-merchant-id-rejected` passes, but merchant 4417 is well formed and
  known, so `cross-merchant-read-denied` and `downstream-call-uses-caller-authority` fail with the same
  numbers as before.
- `principle-applied/` — The gateway accepts only tokens issued for it, authorizes each call against the
  merchants in the caller's token, and takes a separate, narrower token per backend through an exchange at
  authz, with one shared rule at every service and no passthrough of the caller's token. Verified: all 5
  checks pass. The request for merchant 4417 is denied at the gateway, a gateway that forgot its own check
  is stopped by authz, a replayed token and a forwarded token are rejected on audience, 0 foreign reads
  are served, and the legitimate report still returns 212 orders and 3 payouts, $18240.

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
