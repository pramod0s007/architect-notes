# Paper 178: Revocation Is the Hard Half of Delegation

Part of [Architect Notes](../../README.md) — Volume 32: Identity and Authorization for Agents.

## The Question

A finance admin is offboarded at 14:01 and the identity system disables the account. Minutes earlier the
admin's agent had been issued a four-hour token and had passed a narrowed copy to a reconciliation
sub-agent. At 14:20 that sub-agent changes a payout destination, and the gateway lets it through because
the signature and expiry are valid. The obvious fix is a revocation list that gateways pull on a schedule,
but the list names only the tokens somebody thought to revoke, and the polling interval is exactly how long
a revoked chain keeps working. How do you make a departed user's delegation stop at the next call,
including sub-agents nobody listed and gateways that cannot hear the revocation signal?

## Structure

- `pressure/` — Offboarding disables the account at 14:01, but the four-hour tokens issued at 13:50 stay
  valid and the gateway checks only signature and expiry. A worst-case agent stub reads a payout report at
  13:55 and changes the payout destination at 14:20. Verified: the legitimate report read works, but
  `revoked-chain-denied-immediately` fails (a write landed 19 ticks after the account was disabled) and
  `token-lifetime-bounded` fails (longest token 240 ticks, bound 15); exposure after the disable is 229
  ticks.
- `wrong-direction/` — Offboarding also revokes the user's token by id, and the gateway pulls the
  revocation list every 15 ticks. Verified: a 14-tick window opens between the revoke and the next pull,
  so 2 writes land after it; `revoked-chain-denied-immediately` fails (the first write landed 4 ticks after
  the revoke), `revocation-cascades-to-sub-agents` fails (the sub-agent's token was never listed and still
  worked at 14:20), and `token-lifetime-bounded` fails (240 ticks against a bound of 15).
- `principle-applied/` — Tokens live 15 ticks, the authorization server keeps a revocation epoch per user
  and pushes it to gateways, every effectful call is checked against that epoch, token exchange is refused
  once the parent is revoked, and a gateway whose feed is more than 3 ticks old fails closed. Verified: all
  5 checks pass. The 14:01 write is denied (0 writes after the revoke), the exchange for a new child is
  refused, a replica that lost its feed blocks the call at 14:04 (at most 3 ticks of exposure), and the
  14:20 call is expired.

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
