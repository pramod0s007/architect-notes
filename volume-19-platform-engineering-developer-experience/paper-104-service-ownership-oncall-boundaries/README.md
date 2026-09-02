# Service Ownership and On-Call Boundaries — Notifying Everyone Isn't Ownership

**Continues Volume 19** — picking up right after Paper 103 (Local-Prod Parity).

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

Routing an alert to whichever service shows the symptom, rather than whichever service owns the likely root cause, wastes real time even when every team involved investigates correctly and in good faith (measured: 15 minutes lost to a thorough, correct investigation that was never going to find the actual problem). Deciding ownership for each alert type in advance, based on the likely root cause rather than the visible symptom, lets the correct team start immediately with zero wasted investigation (measured: zero minutes of delay once ownership was pre-established). This paper then shows the overcorrection: paging every team that could conceivably be involved, which guarantees fast notification but not fast action, because diffusion of responsibility among multiple paged responders can delay real work longer than routing to a single, wrong team would have (measured: broad fan-out notified the correct team instantly and still delayed its actual start past the narrow-but-wrong routing's own delay).

## The Measurements

- **Routed to symptom (pressure)**: Service A is paged, correctly rules itself out after 15 minutes, escalates — Service B (the real owner) doesn't start until minute 15.
- **Pre-established ownership (principle-applied)**: Service B is paged directly and starts at minute 0 — no investigation time wasted.
- **Broad fan-out (wrong-direction)**: both services are paged instantly at t=0, but diffusion of responsibility delays Service B's actual start until minute 20 — worse than the pressure case's 15-minute delay.

## The Finding

**Notification and ownership are different guarantees.** Broad paging solves "does someone who can help know" and does nothing on its own to solve "who has explicitly taken responsibility for starting right now." Illustrative minute counts stand in for a real, documented pattern of incident-response delay, not universal constants.

## Code Examples in This Repo

| Example | What It Shows |
|---------|--------------|
| [`pressure/`](./pressure/) | Routed to symptom — measured 15 minutes wasted before the real owner starts |
| [`principle-applied/`](./principle-applied/) | Pre-established ownership — measured zero minutes of delay |
| [`wrong-direction/`](./wrong-direction/) | Broad fan-out to every possible team — measured diffusion of responsibility delaying real work past the pressure case's own delay |

### How to Run

```bash
cd volume-19-platform-engineering-developer-experience/paper-104-service-ownership-oncall-boundaries/pressure
javac *.java && java Main

cd volume-19-platform-engineering-developer-experience/paper-104-service-ownership-oncall-boundaries/principle-applied
javac *.java && java Main

cd volume-19-platform-engineering-developer-experience/paper-104-service-ownership-oncall-boundaries/wrong-direction
javac *.java && java Main
```
