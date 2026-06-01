# HTTP Client — Decorator Stack (Retry, Logging, Caching, CircuitBreaker)

## What This Demonstrates

Decorator Pattern applied to an HTTP client with four cross-cutting concerns:
retry with exponential backoff, request/response logging with timing,
GET response caching with TTL and LRU eviction, and a circuit breaker that
transitions through CLOSED → OPEN → HALF-OPEN states. Each concern is a
separate class that wraps any `HttpClient`. They compose into a clean pipeline
where each layer is independently removable.

**Pressure: Cross-cutting concerns** — logging, retry, caching, and circuit
breaking need to apply to every HTTP call across the system. Baking all four
into a single class creates a monolithic blob that cannot be tested in isolation.
Inheritance produces a name explosion: `LoggingRetryingCachingCircuitBreakingHttpClient`.
Decorator produces 4 wrapper classes that combine freely.

## Class Diagram

```
<<interface>>
HttpClient
+ get(url: String): Response
+ post(url: String, body: String): Response
        △
        |
   ──────────────────────────────────────────────────────
   |               |              |              |       |
SimpleHttp    Retry          Caching        Logging   CircuitBreaker
Client        Decorator      Decorator      Decorator  Decorator
(base)        - delegate     - delegate     - delegate - delegate
              - maxRetries   - cache:Map    + get/post - failures
              - backoffMs    - ttlMs           → log    - state
              get/post:      - maxSize         → time   get/post:
              retry 5xx      get():            → call     OPEN:
              with backoff   check cache         delegate   short-circuit
                            HIT: return         → log    HALF-OPEN:
                            MISS: delegate      result     probe
                            + store                     CLOSED: pass-through
```

## Decorator Stack Order

The order matters. This is the stack assembled in `Main.java`:

```
LoggingDecorator          ← outermost: logs the full request and total elapsed time
  CachingDecorator        ← returns cached response, skips all inner decorators
    RetryDecorator        ← retries the real call on 5xx with exponential backoff
      CircuitBreakerDecorator ← opens after 3 consecutive failures, probes on HALF-OPEN
        SimpleHttpClient  ← innermost: makes the actual simulated network call
```

**Why this order?**

- `CachingDecorator` inside `LoggingDecorator` — cache hits are still logged
  (with timing showing ~0ms). If logging were inside caching, cache hits would
  be invisible in logs.
- `CachingDecorator` outside `RetryDecorator` — a cache hit skips retries and
  the circuit breaker entirely, which is correct. If caching were inside retry,
  only successful retry outcomes would be cached, missing the most important case.
- `RetryDecorator` outside `CircuitBreakerDecorator` — each retry attempt is
  counted by the circuit breaker as a separate call. Persistent failures across
  all retry attempts accumulate toward the trip threshold.
- `CircuitBreakerDecorator` innermost (above the base) — the circuit protects
  the real network, not the retry logic. Once OPEN, no retries are attempted.

## Sequence Diagram — Cache Hit

```
Client          Logging      Caching      Retry    CircuitBreaker  SimpleHttpClient
  │  get(url)     │            │            │             │               │
  │──────────────>│            │            │             │               │
  │               │ start timer│            │             │               │
  │               │───────────>│ HIT        │             │               │
  │               │<───────────│ return     │             │               │
  │               │ log 0ms    │            │             │               │
  │<──────────────│            │            │             │               │
```

## Sequence Diagram — Retry + Circuit Trip

```
Client          Logging      Caching      Retry    CircuitBreaker  SimpleHttpClient
  │  get(/fail)   │            │            │             │               │
  │──────────────>│───────────>│ MISS ─────>│             │               │
  │               │            │            │───────────>│ CLOSED ──────>│ 503
  │               │            │            │<───────────│ failure+1 ←───│
  │               │            │       retry│───────────>│ CLOSED ──────>│ 503
  │               │            │            │<───────────│ failure+2 ←───│
  │               │            │       retry│───────────>│ CLOSED ──────>│ 503
  │               │            │            │<───────────│ OPEN (3 fails)│
  │               │            │<───────────│            │               │
  │               │<───────────│ 503        │            │               │
  │<──────────────│ log failure│            │            │               │
```

## Design Decisions

- **`CachingDecorator` outside `LoggingDecorator` is wrong** — if logging were
  the outermost layer and caching the second, cache hits would appear in logs. But
  if caching were outermost, cache hits would be completely invisible — no log
  entry at all. The chosen order ensures every request, cached or not, produces
  a log line.
- **POST requests are never cached** — POST is not idempotent. Returning a
  cached POST response would silently suppress the state-changing side effects
  (order creation, payment processing) that the caller expects.
- **Circuit breaker threshold of 3 consecutive failures** — configured at
  construction time (`CircuitBreakerDecorator(base, 3, 100)`). The factory
  owns the threshold; callers cannot misconfigure it per-call.
- **HALF-OPEN state sends exactly one probe request** — after the reset timeout
  elapses, the circuit allows one request through. If it succeeds, the circuit
  closes. If it fails, the circuit reopens. This prevents a flapping circuit
  from flood-testing a recovering service.
- **All decorators implement `HttpClient`** — every decorator can wrap any other
  decorator or the base client. Removing one layer requires only removing it from
  the construction chain; no other class changes.

## How to Run

```bash
cd volume-5-structural-patterns/paper-17-decorator-pattern/http-client
javac *.java && java Main
```

Expected output (abbreviated):

```
=== 1. Caching Demo: Two GETs for the same URL ===
  [Cache] MISS https://api.example.com/products/42
  [Logging] GET https://api.example.com/products/42 -> 200 (Xms)
  [Cache] HIT  https://api.example.com/products/42
  [Logging] GET https://api.example.com/products/42 -> 200 (0ms)

=== 3. Retry Exhaustion + Circuit Breaker Trip ===
  [Retry] 503 — retrying in 50ms (attempt 1/3)
  [Retry] 503 — retrying in 100ms (attempt 2/3)
  [CircuitBreaker] OPEN — tripped after 3 consecutive failures
  Final response: 503

=== 4. Circuit Breaker OPEN — all requests blocked ===
  [CircuitBreaker] OPEN — short-circuiting GET ...
  Response: 503 — Circuit breaker is OPEN — request blocked

=== 5. Circuit Breaker Recovery (wait 150ms) ===
  [CircuitBreaker] HALF-OPEN — probing
  Response: 200
  Circuit state: CLOSED
```

## When to Apply

- Multiple cross-cutting concerns need to be applied to the same interface in
  varying combinations (different environments may need different subsets of
  logging, retry, caching).
- Each concern should be independently testable: test `RetryDecorator` with a
  stub `HttpClient` that always returns 503, without involving real network or
  caching logic.

## When NOT to Apply

- One concern with no variation — a simple wrapper method is clearer.
- The concerns have fixed ordering and are always applied together — a single
  composed class avoids construction-site boilerplate at the cost of flexibility.
