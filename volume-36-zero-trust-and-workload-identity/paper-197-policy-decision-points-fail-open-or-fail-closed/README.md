# Paper 197: Policy Decision Points: Fail Open or Fail Closed

Part of [Architect Notes](../../README.md) — Volume 36: Zero Trust and Workload Identity.

## The Question

Every call through the Orbit tool gateway asks a policy engine whether it may run, and the gateway waits
1 tick for the answer. The engine is a service like any other, so it gets slow before it goes down. If the
timeout branch lets the call run, a flood of cheap reads that slows the engine buys a $900 refund that
policy had refused. If it refuses everything, the status reads stop, and so does the rollback that would
bring the engine back. Nobody chose either default on purpose; each was plumbing in a timeout handler.
What should an enforcement point do when its policy engine is slow or gone, and why must that answer be
decided route by route before the outage?

## Structure

- `pressure/` — The gateway asks the policy engine about every call and waits 1 tick; when no answer
  arrives, the call runs. The engine's trouble is scripted: a flood of 40 queued reads at t=20, then a
  failed deploy at t=30 that leaves it silent until a rollback restores it. Verified:
  `high-consequence-route-fails-closed` fails (2 of 3 refunds ran with no policy answer, 1 of them for
  `reports.batch`), because the $900 refund refused at t=18 runs at t=22. The read and repair checks pass
  only because default allow lets every call through.
- `wrong-direction/` — One clause changes: a call runs only when the engine answered in time and a rule
  allows it, so a timeout means deny. Verified: the refunds are refused and the one refund on the ledger
  had a policy answer, so `high-consequence-route-fails-closed` passes. But
  `legit-low-risk-read-survives-engine-outage` fails (0 of 2 ops-agent reads served) and
  `legit-repair-survives-engine-outage` fails (rollback refused, engine still down at t=38), because the
  rollback has to pass the engine it would repair.
- `principle-applied/` — A route table gives each route a decided answer. Refunds fail closed, `GET /status`
  is served from a signed last-known-good bundle for up to 14 ticks, and the rollback has its own door,
  guarded by a sealed credential, that never asks the engine and audits and alerts every call. Verified: all
  9 checks pass. No refund ran without a policy answer, 3 of 3 forged bundles were rejected, reads were
  served at ages 5 and 14 and refused at age 15, a wrong break-glass credential was refused and still
  audited, and the engine was back at t=38.

## Interactive Explorer

Download [`explorer/explorer.html`](explorer/explorer.html) and open it in a browser. It replays the real
output of the three programs as a timeline you can step through event by event, shows each check turning
pass or fail, and compares the three designs side by side. It is one self-contained file that works
offline. GitHub displays HTML files as source, so download the file rather than opening it on the
repository page.

## Running It

Each scenario is self-contained; run these commands inside `pressure/`, `wrong-direction/` or
`principle-applied/`:

```bash
javac -Xlint:all *.java
java Main
```

The output is identical on every run: there is no clock, no network and no randomness.

## Read the Full Article on Medium

_Article coming soon on Medium._
