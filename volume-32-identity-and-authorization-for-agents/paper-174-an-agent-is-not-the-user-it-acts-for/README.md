# Paper 174: An Agent Is Not the User It Acts For

Part of [Architect Notes](../../README.md) — Volume 32: Identity and Authorization for Agents.

## The Question

An ops agent acts for merchant admins across a company's APIs. The quickest way to make it work is to
forward the admin's session token on every call, so every API sees only the admin: a person's click and
a model's guess look the same in the audit log, and the agent holds the person's whole authority. The
tempting repair is a service account plus an on-behalf-of header that says who the agent acts for, but a
header is a claim that any caller, or any injected instruction, can set to any admin. How should an AI
agent authenticate to downstream APIs when it acts for a user?

## Structure

- `pressure/` — The Orbit Ops Agent forwards the admin's full-scope session token to Atlas and Meridian,
  and a worst-case model stub acts on an injected order note. Verified: the 2 legitimate reads work, but
  `agent-actions-attributed-to-agent` fails (0 of 4 agent calls name the agent),
  `audit-records-subject-and-actor` fails (1 of 5 records carry the true subject and actor),
  `delegated-scope-narrower-than-session` fails (the agent token holds 3 of 3 session scopes, audience
  `*`), and `no-unauthorized-payout-change` fails (2 payout fields changed).
- `wrong-direction/` — The agent gets a broad service account, `agent:ops`, and says whom it acts for in
  an on-behalf-of header that Meridian takes as the user. Verified: attribution improves
  (`agent-actions-attributed-to-agent` passes, 6 of 6 agent calls name `agent:ops`), and a write for
  merchant 4417 under admin-3012's header is refused, but a header naming admin-4417 gets it through:
  `spoofed-on-behalf-header-ignored` fails (1 of 1 spoofed headers took effect),
  `audit-records-subject-and-actor` fails (6 of 7 records true, one in the name of admin-4417),
  `delegated-scope-narrower-than-session` fails (the account holds 3 scopes for 3 merchants, the session
  3 for 1), and `no-unauthorized-payout-change` fails (3 payout fields changed).
- `principle-applied/` — The agent has its own identity, `authz` exchanges the admin's session for one
  narrow token per API that names subject and actor, and every API authorizes the token, records the
  actor and ignores the header. Verified: all 7 checks pass. Both reads are allowed, the 6 hostile or
  misused calls (3 writes, a cross-merchant read under a forged header, a token at the wrong audience and
  a token after expiry) are denied, 0 payout fields change, and the 6 refused attempts are logged as
  `admin-3012 via agent:ops`.

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
