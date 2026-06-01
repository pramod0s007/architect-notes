# HTTP Request Builder — Builder Pattern

## What This Demonstrates

Builder Pattern applied to constructing an immutable `HttpRequest` via a fluent
`HttpRequestBuilder`. The request has multiple fields — URL, HTTP method, headers
(map), request body, timeout, and retry policy — and the builder enforces
invariants before the object is allowed to exist.

**Pressure: Complex Object Construction** — the pre-builder version accepted 10+
positional parameters in a constructor. A production incident traced a 1ms
timeout bug to a boolean argument being silently auto-promoted to `int` in the
wrong position. Passing `true` for `followRedirects` before the `timeoutMillis`
int parameter compiled without error, but ran with a 1ms timeout on every call.

## Class Diagram

```
HttpRequestBuilder                       HttpRequest (immutable)
─────────────────────────                ───────────────────────────
- url: String                            - url: String
- method: String  = "GET"                - method: String
- timeoutMillis: int = 30_000            - timeoutMillis: int
- headers: Map<String,String>            - headers: Map<String,String> (unmodifiable)
- body: String                           - body: String
────────────────────────────             ───────────────────────────
+ url(url)       : HttpRequestBuilder    + url()          : String
+ method(method) : HttpRequestBuilder    + method()       : String
+ timeout(ms)    : HttpRequestBuilder    + timeoutMillis() : int
+ header(k,v)    : HttpRequestBuilder    + headers()      : Map<String,String>
+ body(body)     : HttpRequestBuilder    + body()         : String
+ build()        : HttpRequest
   └─ validates url != null
   └─ validates timeout > 0
   └─ POST requires body
   └─ constructs immutable HttpRequest
```

## Builder Flow

```
new HttpRequestBuilder()
  .url("https://api.example.com/orders")   → sets url
  .method("POST")                          → sets method
  .header("Authorization", "Bearer token") → adds to headers map
  .header("Content-Type", "application/json")
  .body("{\"sku\":\"BOOK-1\"}")            → sets body
  .timeout(5000)                           → sets timeoutMillis
  .build()
     │
     ├─ url blank?        → throw IllegalStateException("url is required")
     ├─ timeout <= 0?     → throw IllegalStateException("timeout must be positive")
     ├─ POST + no body?   → throw IllegalStateException("POST requires a body")
     └─ new HttpRequest(url, method.toUpperCase(), timeoutMillis, headers, body)
            └─ headers wrapped in Collections.unmodifiableMap(new LinkedHashMap<>(...))
```

## Design Decisions

- **POST without body throws at `build()`** — the invariant is enforced before
  the object exists, not deep inside a network call. The error message names the
  broken contract, not an NPE in some HTTP stack frame.
- **`method` defaults to `"GET"` in the builder** — callers who only need a GET
  with a URL and timeout never have to specify the method. Defaults live in the
  builder, not scattered across callers.
- **Headers accumulated via `header(k, v)` calls** — each call adds one header
  to a `LinkedHashMap` so insertion order is preserved, and the same key can be
  overwritten by calling `header()` again. No `Map.of()` or varargs syntax needed
  at the call site.
- **`HttpRequest` constructor is package-private** — callers are forced through
  `HttpRequestBuilder`. The only way to create a valid `HttpRequest` is one that
  passed validation.
- **Immutability via `Collections.unmodifiableMap`** — the headers map passed to
  `HttpRequest` is defensively copied and made unmodifiable; the builder can be
  reused or modified after `build()` without affecting the built object.

## How to Run

```bash
cd volume-4-architect-level-thinking/paper-11-builder-pattern/http-request-builder
javac *.java && java Main
```

Expected output:

```
POST https://api.example.com/orders timeout=5000ms headers=2 body={"sku":"BOOK-1"}
```

To see validation fire, add to `Main.java` temporarily:

```java
// Missing body on POST — throws at build(), not at send time
new HttpRequestBuilder()
    .url("https://api.example.com/orders")
    .method("POST")
    .build();   // IllegalStateException: POST requires a body
```

## When to Apply

- An object has 5 or more constructor parameters, especially if several share the
  same type (multiple `int` or `boolean` fields adjacent in the signature).
- Some parameters are optional with sensible defaults.
- Invalid combinations need to be caught before the object is used.

## When NOT to Apply

- Two or three required fields with no optional variants — a regular constructor
  with named parameters (or a static factory method) is simpler and equally clear.
