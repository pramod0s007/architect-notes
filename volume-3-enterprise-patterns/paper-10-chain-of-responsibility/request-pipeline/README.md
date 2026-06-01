# Request Pipeline — Chain of Responsibility

## What It Demonstrates

An HTTP request pipeline with four sequential stages — Authentication, Authorization,
Validation, Rate Limiting — each implemented as an independent handler object. Any handler
can stop the chain by calling `reject()`; all subsequent handlers are skipped automatically.

## The Pressure: Sequential Decision Flow

A monolithic request handler grows one stage at a time:

```
Month 1:  if (!isAuthenticated) return 401;
Month 3:  if (!isAuthorized) return 403;
Month 5:  if (!isValid) return 400;
Month 8:  if (isRateLimited) return 429;
          // ... 400 lines, 4 teams, reordering took days
```

Chain of Responsibility decomposes this into handler objects that can be assembled,
reordered, and tested in isolation. Reordering means changing the builder call, not the
handler implementations.

## Class Diagram (ASCII)

```
abstract Handler
─────────────────────────────────────────────────────
- next: Handler
+ link(Handler): Handler      ← returns next for fluent chaining
+ handle(Request, PipelineContext)    ← final; drives chain
# process(Request, PipelineContext)   ← abstract; each handler's check
# stageName(): String                 ← abstract; used in trace
# pass(Request, PipelineContext)      ← records stage in trace
# reject(PipelineContext, String)     ← sets failure; chain stops
       ▲
       │ extends
  ┌────┴───────────────────────┬─────────────────┬──────────────────┐
AuthenticationHandler  AuthorizationHandler  ValidationHandler  RateLimitHandler

static class PipelineContext (inner)
─────────────────────────────────────
- trace:         StringBuilder
- failureStage:  String
- failureReason: String
+ failed(): boolean
+ trace(): String
```

## Pipeline Flow

```
Request → [Authentication] → [Authorization] → [Validation] → [RateLimit] → Success
               │                   │                │               │
           401 Unauth          403 Forbidden    400 Bad Req    429 Too Many
         (missing token)      (wrong role)    (POST no body)  (per-IP limit)
```

Each handler inspects the `PipelineContext` at entry — if the context is already failed,
the handler returns immediately without running. This means `reject()` in any handler
terminates the rest of the chain without throwing an exception.

## How to Run

```bash
cd volume-3-enterprise-patterns/paper-10-chain-of-responsibility/request-pipeline
javac *.java
java Main
```

Expected output:
```
=== Valid request ===
Request
   ↓
Authentication
   ↓
Authorization
   ↓
Validation
   ↓
Rate Limiting
   ↓
Success

=== Missing token ===
Request
   ↓
Authentication (rejected)
   ↓
Stopped — missing token
```

## Design Decisions

**`link()` returns `Handler` (not `void`)** to enable fluent chaining:

```java
authentication.link(authorization).link(validation).link(rateLimit);
```

Each call returns the argument, so the chain reads left-to-right in the order handlers
execute.

**`handle()` is `final`** in the base class — it owns the guard logic (`if (context.failed()) return`)
and the delegation to `next`. Subclasses only implement `process()`, which cannot accidentally
bypass the failed-context guard.

**`PipelineContext` is a mutable trace accumulator** passed through the chain rather than
returning results from each handler. This avoids a cascade of `if (result.failed()) return result`
boilerplate in every handler and keeps the chain-walking code in one place (`Handler.handle()`).

**`PipelineBuilder` assembles and runs in one step** (`run(request)` creates a fresh context
and a fresh chain) — each call is stateless from the caller's perspective, which is safe
for concurrent use.

**Compare with api-security-pipeline** (`../api-security-pipeline`) which adds a fluent
`PipelineBuilder.add()` API, IP blocklist, RBAC authorization, and configurable rate-limit
profiles — demonstrating how the same base pattern scales to production complexity.
