# Social Feed System — A SAAM Modifiability Walkthrough

**Evaluation method:** SAAM (Software Architecture Analysis Method) — Kazman, Bass, Abowd & Webb

**Continues Volume 10** — picking up right after Paper 56 (Messaging App).

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

Fan-out-on-write (push every post to every follower's feed immediately) makes reads instant but makes a celebrity's post cost grow without bound (measured: 0ms → 1ms → 85ms as followers grow 10 → 5,000 → 1,000,000). A hybrid — push below a follower threshold, merge at read time above it — fixes this (measured: 0-1ms flat regardless of follower count). This paper then shows the overcorrection: pure fan-out-on-read solves the celebrity scenario completely (measured: 0ms) but was never checked against an equally realistic scenario — a power user reloading a feed built from thousands of followed accounts (measured: reload cost growing from 1ms to hundreds of ms as followed-account count grows).

## The SAAM Finding

**An architecture is only as good as the scenario set it was actually walked through.** The celebrity-post scenario is dramatic and gets checked. The power-user-feed-read scenario is quietly annoying on every request and gets skipped — not because it's less real, but because it never produces a single catastrophic failure with a name.

## Code Examples in This Repo

| Example | What It Shows |
|---------|--------------|
| [`pressure/`](./pressure/) | Fan-out-on-write — measured write cost growing unbounded with follower count (0ms → 85ms) |
| [`principle-applied/`](./principle-applied/) | Hybrid (push below threshold, merge above) — measured flat 0-1ms write cost regardless of follower count, fast reads for a power user |
| [`wrong-direction/`](./wrong-direction/) | Pure fan-out-on-read — measured the celebrity scenario fully solved (0ms) while a power-user's repeated feed reload cost grows from 1ms to hundreds of ms as followed-account count grows |

### How to Run

```bash
cd volume-10-system-design-blueprints/paper-57-social-feed-system/pressure
javac *.java && java Main

cd volume-10-system-design-blueprints/paper-57-social-feed-system/principle-applied
javac *.java && java Main

cd volume-10-system-design-blueprints/paper-57-social-feed-system/wrong-direction
javac *.java && java Main
```
