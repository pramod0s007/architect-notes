# Decorator Pattern — Adding Behavior Without Subclassing

**Pattern:** Decorator Pattern

---

## Read the Full Article on Medium

_Article coming soon on Medium._

This repository focuses on runnable code examples. The white paper covers theory, war stories, and architectural reasoning in depth.

---
## What This Paper Is About

Decorator Pattern solves **combinatorial inheritance explosion** — when multiple independent capabilities need to be applied to a component in any combination. Instead of creating 2^n subclasses for n capabilities, each capability is a wrapper that delegates to whatever it wraps.

## The Pressure: Cross-Cutting Concerns

Logging. Caching. Retry. Metrics. Rate limiting. Circuit breaking. These are cross-cutting concerns — they apply across many components but don't belong inside any of them.

Without Decorator: each cross-cutting concern is either duplicated across every component, or stuffed into a base class that becomes unmaintainable.

With Decorator: each concern is one class. Any component can be wrapped with any combination.

```java
new LoggingDecorator(
    new RetryDecorator(3,
        new CachingDecorator(TTL,
            new RealRepository())))
```

## The Ordering Rule

Outermost decorator runs first. Order defines policy:
- **Logging outermost** — log the full request before transformation
- **Auth before business logic** — fail fast if unauthorized
- **Caching before expensive operations** — return without hitting the DB
- **Retry wraps the core call** — not the cache or the log

Wrong order = wrong security model or wrong caching behavior.

## Java I/O — The Canonical Example

```java
InputStream input =
    new GZIPInputStream(        // decompress
        new BufferedInputStream(// buffer
            new FileInputStream("data.gz"))); // read
```

This IS Decorator Pattern. Three independent capabilities, composed freely.

## Pros

- Capabilities are independently testable and removable
- Runtime composability — choose which capabilities to apply per context
- Eliminates 2^n subclass explosion

## Cons

- Deep chains are hard to debug (stack traces become unreadable)
- Order mistakes create subtle bugs
- Use a builder to assemble deep chains legibly

## Read the Full Article

{medium}

## Code Examples in This Repo

| Example | Domain | What It Shows |
|---------|--------|--------------|
| [`decorator/message-sender/`](../../code-samples/decorator/message-sender/) | Messaging | Compression, Encryption, Logging — stackable in any order |
| [`decorator/http-client/`](../../code-samples/decorator/http-client/) | Networking | Retry, Logging, Caching (LRU+TTL), CircuitBreaker — full cross-cutting stack |

### How to Run

```bash
cd code-samples/decorator/message-sender
javac *.java && java Main

cd code-samples/decorator/http-client
javac *.java && java Main
```
