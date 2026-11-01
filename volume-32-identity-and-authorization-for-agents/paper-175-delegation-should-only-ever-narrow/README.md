# Paper 175: Delegation Should Only Ever Narrow

Part of [Architect Notes](../../README.md) — Volume 32: Identity and Authorization for Agents.

## The Question

A Halcyon Commerce operations agent holds a token that can read orders, read payouts and change payout
destinations for two merchants. To reconcile one merchant's week it spawns a sub-agent, and the simplest
design hands that sub-agent the same token. The next simplest limits it with a sentence in the prompt. In
both designs a payout memo carrying planted text can redirect money to an account nobody approved, because
nothing makes the child's authority smaller than the parent's. The sub-agent is fooled, the token is
genuine, and the payments service honors it. How do you let an agent delegate work so that the child can
never hold, request or use more authority than the parent, even when the child is fooled and a service has
not been upgraded?

## Structure

- `pressure/` — The Ops Agent takes a reconciliation request for merchant 3012, spawns the sub-agent
  agent:recon, and hands it its own token (3 scopes, merchants 3012 and 5120, ceiling 50000, expiry 600). A
  worst-case model stub obeys a planted payout memo that asks for three redirects, and the Meridian payments
  service checks only that the token fits the request. Verified: the legitimate reconcile works (3 lines,
  $4200), but `sub-agent-confined-to-one-merchant` fails (touched merchants 3012, 5120) and
  `no-unauthorized-payout-change` fails (2 payouts redirected, $39060).
- `wrong-direction/` — The sub-agent is limited by a sentence in its prompt ("merchant 3012 only, read only,
  never write") and receives a narrow token, but the authorization service mints any token whose scope and
  merchant names exist and never consults the parent token. The obedient stub asks for more and gets it.
  Verified: the narrow token denies the first redirect, but `widening-request-denied` fails (1 of 1 widening
  requests issued), `child-authority-subset-of-parent` fails (merchants [4417], ceiling 1000000 over 50000,
  expiry 900 over 600), `sub-agent-confined-to-one-merchant` fails (touched merchants 3012, 4417, 5120) and
  `no-unauthorized-payout-change` fails (3 payouts redirected, $66560).
- `principle-applied/` — The authorization service issues a child token only when every dimension (scopes,
  merchants, ceiling, expiry and chain depth) sits inside the parent's, and Meridian re-walks the whole
  delegation chain on every call, so a widened token is stopped at use as well as at issue. Verified: all 7
  checks pass. The simulated model is still fooled, yet the legitimate reconcile works (3 lines, $4200), 6
  widening requests are denied with 0 issued, 0 of 5 payouts are redirected, the helper hop stops at chain
  depth 3 over the limit of 2, and a hand-built token from an older endpoint is rejected.

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
