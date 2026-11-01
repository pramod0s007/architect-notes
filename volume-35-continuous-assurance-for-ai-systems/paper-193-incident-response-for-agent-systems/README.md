# Paper 193: Incident Response for Agent Systems

Part of [Architect Notes](../../README.md) — Volume 35: Continuous Assurance for AI Systems.

## The Question

An Ops Agent at Halcyon Commerce reads a shared knowledge index, keeps a memory of its own and changes merchant
payout destinations through a delegated token. One poisoned finance note redirects payouts, the on-call team
follows the service playbook, and 6 ticks after the restart the redirect repeats. The restart ended the process,
but the note, the saved memory entry and the token were all still in place, and nobody could say which tokens the
agent held or which payouts it had changed. An agent keeps acting while people investigate, and what it learned
outlives its process. What should the first moves of an incident be when the failing component is a working agent
whose state survives a restart?

## Structure

- `pressure/` — Halcyon's Ops Agent retrieves a poisoned finance note, saves a memory entry that keeps the rule
  alive, and changes payout destinations through a delegated token that the gateway keeps accepting. The responders
  restart the agent and hand-fix the one merchant who complained, with no token inventory and no audit query by
  agent and time window. Verified: 3 payouts are changed and 2 are still redirected, the redirect repeats 6 ticks
  after the restart, and three checks fail: `containment-before-investigation` (never contained, 1 effect after the
  alert), `blast-radius-scoped-from-audit` (scope lists 1 of 3 changed payouts) and
  `poisoned-state-quarantined-not-just-restarted` (kb-77/c2 and mem-3 still reachable). Only
  `legit-unaffected-agents-keep-running` passes.
- `wrong-direction/` — The responders follow the service playbook: restart the agent, roll back prompt@v7 to
  prompt@v6, and rotate the shared model key from key-1 to key-2, redeploying only the Ops Agent. Verified: the
  redirect repeats under prompt@v6 and key-2, so all 3 changed payouts are still redirected, and the two healthy
  agents that still call the model with key-1 go down (`agents_down = 2`). `containment-before-investigation`
  fails (never contained), `poisoned-state-quarantined-not-just-restarted` fails (kb-77/c2 and mem-3 still
  reachable) and `legit-unaffected-agents-keep-running` fails (0 of 2 other agents ran their cycle).
- `principle-applied/` — Containment comes first: the registry lists the agent's 2 delegated tokens and revokes
  them, a scoped stop pauses that one agent, and a dry-run probe confirms the revoked token is denied. The effects
  are then scoped from an audit record keyed by agent and time window (2 payouts, 1 destination), the poisoned
  index and memory entries are quarantined, a ledger undo restores both payouts and a retried pass applies 0
  compensations, and the incident joins the test corpus (3 scenarios to 4) with a replay. Verified: all 6 checks
  pass, with `effects_after_alert = 0`, the clean chunk still retrievable, and 2 of 2 other agents still running.

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
