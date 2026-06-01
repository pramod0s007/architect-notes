# Builder Pattern

**Pattern:** Builder Pattern

---

## Read the Full Article on Medium

_Article coming soon on Medium._

This repository focuses on runnable code examples. The white paper covers theory, war stories, and architectural reasoning in depth.

---
## What This Paper Is About

Builder Pattern addresses **complex object construction** — when a class has many optional parameters, cross-field invariants, and callers need readable, named assembly rather than positional arguments.

The pattern separates the construction of a complex object from its representation.

## The Pressure: Complex Object Construction

A payment integration service had a 10-parameter constructor for `HttpRequest`. Two engineers used it in different files with swapped parameter positions. Java silently auto-promoted `true` (a boolean) to `1` for the timeout field.

**1 millisecond timeout. In production. For 3 months.** Causing silent double-charges.

Builder Pattern makes this physically impossible: each step is named, and `build()` validates invariants before the object exists.

## The Structure

```
Builder (mutable scratchpad)  →  build()  →  Result (immutable)
  .post(url)                      validates    HttpRequest
  .header("Auth", token)          all fields
  .timeoutSeconds(30)
  .retryPolicy(EXPONENTIAL)
```

## Pros

- Named parameters: no more "what does position 7 mean?"
- Invariant validation at `build()` — errors at the call site, not deep in execution
- Optional fields with defaults — no telescoping constructors, no null arguments
- Immutable result — thread-safe by construction

## Cons

- More boilerplate than a constructor for simple objects
- Overused for objects with 3 fields — a constructor is clearer

## When NOT to Use

- ≤4 fields, all required, no invariants → constructor is cleaner
- Named configurations that are used repeatedly → static factory methods (`RetryPolicy.exponential(3)`)
- When a fluent API adds ceremony without clarity

## You Use This Every Day

OkHttp, AWS SDK v2, Elasticsearch Java client, Retrofit — all use Builder for configuration objects. The pressure is always the same: many optional fields, invariants at build time, readable assembly.

## Read the Full Article


## Code Examples in This Repo

| Example | Domain | What It Shows |
|---------|--------|--------------|
| [`builder/http-request-builder/`](./http-request-builder/) | Networking | Method, URL, headers, body, timeout, retry — the foundational Builder |
| [`builder/database-config/`](./database-config/) | Infrastructure | SSL, connection pool, timeouts, read replica — `build()` validates invariants |
| [`builder/search-request/`](./search-request/) | Search | Query, filters, pagination, sort — cross-field validation (`maxPrice >= minPrice`) |

### How to Run

```bash
cd code-samples/builder/http-request-builder
javac *.java && java Main

cd code-samples/builder/database-config
javac *.java && java Main

cd code-samples/builder/search-request
javac *.java && java Main
```
