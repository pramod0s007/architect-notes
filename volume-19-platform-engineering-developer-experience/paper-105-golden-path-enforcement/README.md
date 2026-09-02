# Golden Path Enforcement — A Passing Gate Isn't the Same as a True Field

**Closes Volume 19** — Platform Engineering & Developer Experience, following on from Paper 104 (Service Ownership and On-Call Boundaries).

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

Independently chosen tooling per team means no field exists to correlate a request across services, forcing manual, obviously incomplete reconstruction during any incident that spans a team boundary (measured: two log lines from the identical real request shared no common field to match on). A single required identifier propagated through a shared library turns cross-service correlation into an exact field match (measured: the identical incident correlated correctly once both services routed through the same logging library). This paper then shows the overcorrection: a hard, exception-free gate that only checks field presence, which gets satisfied by a placeholder under real deadline pressure and merges unrelated incidents into a false correlation (measured: a team blocked with no legitimate path through the gate added a constant placeholder that made two unrelated requests appear identical to any automated correlation).

## The Measurements

- **No shared standard (pressure)**: Service A's `requestId=abc-123` and Service B's `counterId=47291` for the same real request share no common field — correlation is false.
- **Shared library, adopted (principle-applied)**: both services emit `traceId=req-789` for the same request — correlation is true, an exact match.
- **Hard gate, no exception (wrong-direction)**: a team blocked by the gate hardcodes `traceId="unknown"` to pass it — two completely unrelated real requests both carry that value and are falsely correlated as the same request.

## The Finding

**A passing gate is never proof that its underlying requirement was genuinely met — it's only proof that whatever the gate actually checks was satisfied, and the gap between those two things is exactly where fake compliance under pressure lives.** This is the same failure Paper 101 identified for deployment access, reappearing at the tooling layer with a worse consequence: not a blocked deploy, but data that looks trustworthy and isn't.

## Code Examples in This Repo

| Example | What It Shows |
|---------|--------------|
| [`pressure/`](./pressure/) | No shared standard — measured zero correlation across two independently-tooled services |
| [`principle-applied/`](./principle-applied/) | Shared required field — measured exact correlation with no application logic changes |
| [`wrong-direction/`](./wrong-direction/) | Hard gate, no exception process — measured a placeholder value falsely merging two unrelated requests |

### How to Run

```bash
cd volume-19-platform-engineering-developer-experience/paper-105-golden-path-enforcement/pressure
javac *.java && java Main

cd volume-19-platform-engineering-developer-experience/paper-105-golden-path-enforcement/principle-applied
javac *.java && java Main

cd volume-19-platform-engineering-developer-experience/paper-105-golden-path-enforcement/wrong-direction
javac *.java && java Main
```
