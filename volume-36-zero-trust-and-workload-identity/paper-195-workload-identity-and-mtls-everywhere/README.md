# Paper 195: Workload Identity and mTLS Everywhere

Part of [Architect Notes](../../README.md) — Volume 36: Zero Trust and Workload Identity.

## The Question

A static API key is copied into five deployments, so a leak from the least important one impersonates the
gateway to the payments service, and no log can say which deployment made a call. The obvious fix is mutual
TLS: give every workload a certificate and let the payments service accept any certificate the company
authority has issued. Every call now carries a name. Yet a compromised report job with its own valid
certificate calls the refund endpoint and is accepted, because a certificate proves who is calling and says
nothing about what that caller may do. How do services authenticate each other with workload identity, and
why is mutual TLS not enough on its own?

## Structure

- `pressure/` — One shared API key sits in five deployments (the gateway, its canary, its staging copy, the
  Ops Agent and a report job), and Meridian logs every request it accepts as `orbit.gateway`. Verified: the
  legitimate gateway call works, but `distinct-identity-per-workload` fails (one logged identity stands for
  3 workloads) and `stolen-credential-unusable-without-key` fails (2 requests with a copied key were
  accepted), so the red team's $2400 refund for order 5530 goes through.
- `wrong-direction/` — Mutual TLS is turned on: the issuer gives each workload its own certificate, both
  ends of every connection are verified, and Meridian accepts any certificate the company authority has
  issued. Verified: the gateway's $35 refund for order 5521 works, and the report job's copy of the
  gateway's certificate is refused for lack of a key proof, so 3 of 4 checks pass. But
  `valid-cert-without-policy-denied` fails: `reports.batch` refunded $2400 with its own valid certificate.
- `principle-applied/` — The issuer issues an identity only when the platform's facts match the record for
  the identity requested, both sides prove the key their credential is bound to, and Meridian looks the
  verified identity up in a deny-by-default policy with one entry per legitimate call. Verified: all 7
  checks pass. The report job cannot obtain the gateway's identity, its own valid credential is denied on
  `POST /refunds`, a self-made and a copied credential are rejected, the gateway refuses a stand-in for
  Meridian, both legitimate calls still work, and `unauthorized_refund_usd` is 0.

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
