# API Security Pipeline — Chain of Responsibility with PipelineBuilder

## What It Demonstrates

A full API gateway security pipeline with five handler stages — IP blocklist, JWT
authentication, RBAC authorization, rate limiting, and request body validation —
assembled via a fluent `PipelineBuilder`. Different endpoint types (public API, internal
service) compose different subsets of the same handler objects.

## The Pressure: Growing Stage Count with Variable Composition

The simpler `request-pipeline` example (`../request-pipeline`) has four fixed stages.
Real API gateways need:
- Multiple rate-limit profiles (public: 100 req/min, internal: 10,000 req/min)
- Internal service bypass (skip auth + rate limit)
- Per-endpoint permission maps
- IP deny-list fed from a threat-intelligence feed

Hard-coding these into a single handler method makes reordering and per-route variation
impractical. `PipelineBuilder` makes each pipeline configuration explicit and readable.

## Class Diagram (ASCII)

```
abstract SecurityHandler
─────────────────────────────────────────────
- next: SecurityHandler
+ setNext(SecurityHandler): SecurityHandler  ← returns next for fluent chaining
+ handle(ApiRequest): ApiResponse            ← abstract; each handler's check
# passToNext(ApiRequest): ApiResponse        ← delegates to next, or returns 200 OK
          ▲
          │ extends
  ┌───────┴──────────────────────────────────────────┐
IpBlocklistHandler  AuthenticationHandler  AuthorizationHandler
RateLimitHandler    RequestValidationHandler

PipelineBuilder
─────────────────────────────────────
- head: SecurityHandler
- tail: SecurityHandler
+ add(SecurityHandler): PipelineBuilder   ← fluent; returns this
+ build(): SecurityHandler                ← returns head of chain
```

Data objects:
```
ApiRequest                        ApiResponse
──────────────────────────────    ──────────────────────────────
 ip, authorizationHeader           statusCode: int
 userId, endpoint, method, body    message: String
```

## Pipeline Flow

```
ApiRequest
    │
    ▼
[IpBlocklistHandler]    → 403 Forbidden     (IP on deny-list)
    │
    ▼
[AuthenticationHandler] → 401 Unauthorized  (missing/invalid Bearer token)
    │
    ▼
[AuthorizationHandler]  → 403 Forbidden     (user lacks endpoint permission)
    │
    ▼
[RateLimitHandler]      → 429 Too Many      (per-IP sliding window exceeded)
    │
    ▼
[RequestValidationHandler] → 400 Bad Req    (POST/PUT with empty body)
    │
    ▼
  200 OK
```

## Builder Pattern — Two Pipelines from the Same Handlers

```java
// Public API: full security stack
SecurityHandler publicPipeline = new PipelineBuilder()
    .add(new IpBlocklistHandler(blockedIps))
    .add(new AuthenticationHandler())
    .add(new AuthorizationHandler(permissions))
    .add(new RateLimitHandler(100))          // 100 req/window
    .add(new RequestValidationHandler())
    .build();

// Internal service: skip IP check, auth, and rate limiting
SecurityHandler internalPipeline = new PipelineBuilder()
    .add(new AuthorizationHandler(internalPermissions))
    .add(new RequestValidationHandler())
    .build();
```

Reordering stages means reordering `.add()` calls — no handler class changes.

## How to Run

```bash
cd volume-3-enterprise-patterns/paper-10-chain-of-responsibility/api-security-pipeline
javac *.java
java Main
```

Six scenarios are demonstrated (rate limiter is set to 2 requests/window for demo visibility):

| Scenario | Expected result |
|----------|----------------|
| 1. Blocked IP (10.0.0.99) | 403 — IP address blocked |
| 2. Invalid token (bad-token) | 401 — Invalid or missing bearer token |
| 3. Unauthorized endpoint (bob → /api/orders) | 403 — Insufficient permissions |
| 4a. Valid request, alice first call | 200 OK |
| 4b. Valid request, alice second call | 200 OK |
| 5. Rate limit exceeded, alice third call | 429 — Too many requests |
| 6. POST with empty body (admin-1) | 400 — Request body required |

## Design Decisions

**`PipelineBuilder` combines Chain of Responsibility with the Builder pattern** — the
pipeline topology (which handlers, in what order) is declared once, in one place, and
is readable without tracing handler constructors. This is the key structural difference
from `request-pipeline`, where the chain is wired inside `PipelineBuilder.build()` as
hardcoded constructor calls.

**`setNext()` returns `SecurityHandler`** (the argument, not `this`) enabling fluent
manual chaining when needed: `first.setNext(second).setNext(third)`. The `PipelineBuilder`
uses this internally.

**`passToNext()` returns `200 OK` when there is no next handler** — the absence of a
next handler signals that all checks have passed. This removes the need for a terminal
"success" handler object at the end of every chain.

**`RateLimitHandler` is stateful** (per-IP request count map) and is constructed once
then reused across requests. The `reset()` method clears counts between test scenarios
in `Main`. A production implementation would back this with a Redis TTL key.

**Each handler logs its own PASS/BLOCKED line** — the structured output makes it
immediately clear which stage stopped a request, which is critical for API gateway
debugging and audit logging.
