# Builder Pattern

## Why Construction Code Explodes

Complex objects accumulate optional fields, defaults, validation, and environment-specific wiring.

```java
HttpRequest request = new HttpRequest(
    "POST",
    url,
    headers,
    body,
    timeout,
    followRedirects,
    retryPolicy,
    ...);
```

Constructors and telescoping setters become unreadable.

## The Real Problem

**Complex object construction.**

The pressure is not "we need a pattern."

The pressure is **safe, readable assembly** of objects with many valid combinations.

## Builder Thinking

```java
HttpRequest request = HttpRequest.builder()
    .post(url)
    .header("Authorization", token)
    .body(json)
    .timeout(Duration.ofSeconds(30))
    .build();
```

Construction steps are explicit.

Validation runs once at `build()`.

## Real Examples (This Series)

| Domain | Why Builder fits |
|--------|------------------|
| HTTP Request | Headers, method, body, timeouts |
| Database Configuration | URLs, pools, SSL, credentials |
| Search Request | Filters, pagination, sort, facets |

## Design Pressure

```text
Complex Object Construction
        ↓
Stepwise Assembly
        ↓
Builder
```

## Key Takeaways

- Builder addresses construction complexity, not behavior variation (Strategy) or creation families (Factory).
- Prefer immutability after `build()` when the object is a value or request DTO.
- Validate invariants at build time, not scattered across call sites.
- Do not use Builder for every POJO — apply when parameter combinations explode.
