# Paper 188: Kill Switches and Containment for Agent Fleets

Part of [Architect Notes](../../README.md) — Volume 34: Reliable and Bounded Agent Execution.

## The Question

A fleet of agents is running a bad prompt, and the runbook offers one stop: switch off the platform. That
halts every agent at once, the healthy ones included, and does nothing about the refunds already queued in
front of the payments service. The softer fix, a new prompt plus a flag each agent polls, fails the other
way: the stop waits for the agent to notice it, and the queue still drains. Every minute of delay costs
money, and every healthy agent taken down is an outage of its own. How do you stop one faulty agent
quickly and narrowly, including the work it already queued, when the agent cannot be trusted to cooperate
and the switch store itself can fail?

## Structure

- `pressure/` — Four runs of the Orbit Ops Agent start on prompt v18 and propose four $75 refunds a tick
  while Meridian's worker pays two a tick; the only stop is the platform off switch, pulled at t=5.
  Verified: the legitimate $40 refund for order 5201 works and the switch takes effect on the next call
  (the Ops Agent proposes 0 calls from t=5), but `scoped-stop-halts-only-target` fails (3 agents halted, 1
  at fault), `queued-effects-drained-after-stop` fails (8 of 8 queued refunds ran after the stop, $600) and
  `legit-other-agents-keep-working` fails (3 of 3 other-agent calls lost).
- `wrong-direction/` — Prompt v19 tells the Ops Agent to stop, and a flag for that agent only is polled
  every 4 ticks, staggered across the four runs. The scripted stub reads its prompt once, so 0 of 4 runs
  reread v19. Verified: the flag is narrow, so `scoped-stop-halts-only-target` and
  `legit-other-agents-keep-working` pass, but `stop-takes-effect-on-next-call` fails (10 calls proposed
  after the flag, the last run stopped 6 ticks later), `unknown-switch-state-fails-closed` fails (4
  refunds proposed while a run could not read its switch) and `queued-effects-drained-after-stop` fails (8
  of 8 queued refunds ran anyway, 18 refunds and $1,350 after the flag in all).
- `principle-applied/` — A tool gateway enforces scoped stops (agent, tool and tenant, with wildcards) on
  every call, reads the switch each time, fails closed when the switch store is unreadable, sweeps queued
  refunds into a held list at the stop, and is rehearsed in a drill of four scopes. Verified: all 7 checks
  pass. 0 refunds execute after the stop, 8 refunds ($600) are held for review, 20 Ops Agent calls are
  denied by the stop and 0 other-agent calls, and a one-tick store outage costs 1 legitimate email, which
  succeeds on retry.

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
