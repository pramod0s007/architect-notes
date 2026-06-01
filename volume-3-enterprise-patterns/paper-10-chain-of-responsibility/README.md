# Chain of Responsibility

**Pattern:** Chain of Responsibility Pattern

---

## Read the Full Article on Medium

_Article coming soon on Medium._

This repository focuses on runnable code examples. The white paper covers theory, war stories, and architectural reasoning in depth.

---
## What This Paper Is About

Chain of Responsibility addresses **sequential decision flow** — when a request passes through ordered stages where each stage can process the request, pass it forward, or terminate the chain early.

You use this pattern every day without naming it: every HTTP framework's middleware stack, every Spring Security filter chain, every Express middleware pipeline.

## The Pressure: Sequential Decision Flow

An API gateway handler grew from 80 to 400 lines over 18 months as teams added JWT validation, rate limiting, IP blocklisting, schema validation, and internal service bypass. When security needed to add HMAC signature verification, the estimate was 3 days — to add one stage to a monolithic pipeline.

**Signal:** Reordering, adding, or removing a stage should take minutes. When it takes days, the pipeline has become implicit.

## The Pattern

Each stage is an independent handler. Each handler knows only the next link:

```
IpBlocklist → Authentication → RateLimit → Validation → BusinessLogic
    ↓              ↓               ↓             ↓
  403          401/pass        429/pass       400/pass
```

Any stage can terminate the chain. Only one handler processes each request.

## Assembly is Explicit

```java
Handler pipeline = new IpBlocklistHandler();
pipeline
    .setNext(new AuthenticationHandler())
    .setNext(new RateLimitHandler())
    .setNext(new ValidationHandler())
    .setNext(new BusinessLogicHandler());
```

Different pipelines for different endpoint types — same handlers, different assembly.

## Pros

- Stages are independently testable (each handler gets a mock request, asserts response)
- Reordering = change assembly order, zero handler changes
- New stage = new handler class, add to assembly
- Short-circuit evaluation: blocked IP never reaches authentication

## Cons

- No guarantee any handler processes the request (if all pass without acting)
- Implicit data flow through request context object
- Debugging: need correlation IDs to trace which stage acted on a request

## vs Similar Patterns

| Pattern | When | Key Difference |
|---------|------|----------------|
| Chain of Resp. | Ordered pipeline, any stage stops | Sequential, pass-or-stop |
| Specification | Rule composition with AND/OR | Rules, not pipeline |
| Command | Operation as object, stored/undone | Encapsulation, not routing |

## Read the Full Article

{medium}

## Code Examples in This Repo

| Example | Domain | What It Shows |
|---------|--------|--------------|
| [`chain-of-responsibility/request-pipeline/`](../../code-samples/chain-of-responsibility/request-pipeline/) | Web API | Auth, Authorization, Validation, Rate Limiting |
| [`chain-of-responsibility/api-security-pipeline/`](../../code-samples/chain-of-responsibility/api-security-pipeline/) | API Gateway | IP blocklist, JWT, RBAC, rate limit, body validation — fluent `PipelineBuilder` |

### How to Run

```bash
cd code-samples/chain-of-responsibility/request-pipeline
javac *.java && java Main

cd code-samples/chain-of-responsibility/api-security-pipeline
javac *.java && java Main
```
