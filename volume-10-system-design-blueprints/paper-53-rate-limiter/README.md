# Rate Limiter — An Architecture Decision Record

**Evaluation method:** Architecture Decision Record (ADR)

**Continues Volume 10** — picking up right after Paper 52 (URL Shortener).

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

A fixed-window rate limiter counts requests and resets to zero at fixed clock boundaries — simple, and wrong exactly at the boundary: a client can spend its whole budget in the last millisecond of one window and the whole budget again in the first millisecond of the next. Measured: 200 requests accepted in a real 2ms span against a "100 per second" limit. This paper documents the fix as a real ADR — context, options considered, decision, consequences — and then shows the decision that never got written down: where the rate-limiter's state actually lives once the service runs on more than one node.

## The Decision

**Token bucket over fixed window and sliding window log.** Measured: the identical boundary-straddling scenario that let 200 requests through the fixed window let exactly 100 through the token bucket — matching the intended limit, because the budget refills continuously instead of resetting at an edge.

## The Consequence Nobody Wrote Down

The ADR fixed the algorithm; it didn't specify the deployment topology. Each of 5 application nodes ended up running its own independent, perfectly-correct token bucket. Measured: total accepted across all 5 nodes = 500/sec against an intended global limit of 100/sec — a 5x overshoot with zero defects in the algorithm itself.

## Code Examples in This Repo

| Example | What It Shows |
|---------|--------------|
| [`pressure/`](./pressure/) | Fixed window counter — measured 200 requests accepted in a real 2ms span straddling a window boundary |
| [`principle-applied/`](./principle-applied/) | Token bucket — measured exactly 100 requests accepted in the identical scenario |
| [`wrong-direction/`](./wrong-direction/) | Same token bucket, deployed as 5 independent per-node instances with no shared state — measured 500 total accepted against a 100/sec intended global limit |

### How to Run

```bash
cd volume-10-system-design-blueprints/paper-53-rate-limiter/pressure
javac *.java && java Main

cd volume-10-system-design-blueprints/paper-53-rate-limiter/principle-applied
javac *.java && java Main

cd volume-10-system-design-blueprints/paper-53-rate-limiter/wrong-direction
javac *.java && java Main
```
