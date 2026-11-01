# Paper 194: Network Location Is Not Identity

Part of [Architect Notes](../../README.md) — Volume 36: Zero Trust and Workload Identity.

## The Question

Halcyon Commerce's refund service, Meridian, answers any caller whose address starts with 10. The network is
flat, so that rule covers the tool gateway, the reporting batch job and every other workload the
orchestrator has ever started. It holds until a restarted batch job is leased the address the gateway
used before. The obvious repair is a shorter list that trusts only the gateway's address, but an address
is a lease the orchestrator hands out, so the list goes stale when an address is recycled, and the log
cannot say which workload called. If where a request came from proves so little, what should a service
verify about the caller instead?

## Structure

- `pressure/` — Meridian admits any caller whose address starts with 10. and asks nobody for a credential;
  the orchestrator leases the gateway's old address to the reporting batch job, which then calls all three
  endpoints. Verified: the legitimate refunds for orders 7702 and 7703 work, but only because the door is
  open; `unknown-workload-on-trusted-subnet-denied` fails (order 7731 refunded 2500 from 10.0.4.17) and
  `identity-required-on-every-endpoint` fails (3 of 3 endpoints answered a caller with no credential).
- `wrong-direction/` — Meridian builds an allow-list once, from the gateway's address at t=01, and never
  refreshes it. Verified: a probe from the unlisted address 10.0.9.31 is denied, so `unlisted-address-denied`
  passes, but `unknown-workload-on-trusted-subnet-denied` fails (order 7731 refunded 2500 from the
  recycled 10.0.4.17), `reused-address-gives-no-authority` fails (10.0.4.17 held by 2 workloads and the
  later one got the refund), `legit-gateway-works-from-any-address` fails (order 7703 denied after the
  gateway moved to 10.0.4.52) and `denials-log-identity-not-just-address` fails (2 of 2 denials name only
  an address).
- `principle-applied/` — An issuer signs a credential that names each workload, Meridian verifies it on every
  request, checks the name against a per-endpoint policy that denies by default, and keeps the address
  only in the log. Verified: all 7 checks pass. Of the red team's 6 calls from the recycled address, 5 are
  refused (including a forged credential) and 1 is a status read its own credential allows; the gateway's
  3 refunds execute from 3 addresses, and the unauthorized amount is 0. The credential is a keyed-digest
  stand-in, and no model is involved. The article also measures what the checks miss, including a
  credential handed to the wrong workload.

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
