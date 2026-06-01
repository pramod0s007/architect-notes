# Builder Pattern

*Constructors are honest. They tell you exactly what an object needs. Builder Pattern is for when that honesty becomes unreadable.*

---

A bug report arrived in our team's inbox on a Tuesday morning.

The integration service that forwarded requests to a third-party payments API was occasionally sending requests with a 0-millisecond timeout. The third-party API would accept these, attempt to process, and then the connection would close before the response arrived. Retries would double-charge some users.

The root cause:

```java
// Caller A (written by engineer 1)
HttpRequest request = new HttpRequest(
    "POST", paymentsUrl, headers, body, 30000, true, 3, null, null, false);

// Caller B (written by engineer 2, three months later)
HttpRequest request = new HttpRequest(
    "POST", paymentsUrl, headers, body, true, 30000, null, null, 3, false);
```

Two engineers. Same constructor. Different parameter order assumption.

The constructor signature was:

```java
HttpRequest(String method, String url, Map<String,String> headers,
    String body, int timeoutMs, boolean followRedirects,
    int retryCount, RetryPolicy retryPolicy,
    List<Integer> retryOnStatus, boolean verifySsl)
```

Engineer 2 swapped `timeoutMs` and `followRedirects`. The constructor accepted it without complaint — `int` and `boolean` are different types, but Java silently auto-promoted `true` to `1` for the timeout and `30000` to `true` (non-zero = true) for followRedirects.

A timeout of 1 millisecond. In production. For three months.

No test caught it because the unit tests mocked the HTTP layer. No review caught it because ten positional parameters look identical regardless of order.

**This is what constructors become when objects have more than four or five parameters.** They're honest — they tell you everything — but they don't protect you from anything.

Here is an honest constructor:
    headers,
    body,
    30,
    true,
    3,
    RetryPolicy.EXPONENTIAL,
    null,
    true
);
```

Everything the object needs is right there. The constructor is honest.

But what is the ninth parameter? What does `true` mean in the tenth position? Is the `null` intentional?

The constructor is honest. It is not readable. And when objects have more than four or five parameters — especially optional ones — readability starts to matter more than honesty.

**This is complex object construction pressure.** Builder Pattern is the response.

---

## When Construction Becomes Complex

Three signals that a constructor is outgrowing itself:

**Signal 1: More than four parameters, some optional.**

```java
// Which of these is optional?
new DatabaseConfig(url, username, password, poolSize, sslEnabled,
    connectTimeout, readTimeout, maxRetries, readReplicaUrl, schema);
```

Callers can't remember the order. Optional parameters require overloaded constructors or nulls in positions.

**Signal 2: Invalid combinations need to be caught.**

```java
// Can you catch this at compile time?
new HttpRequest("POST", url, null, null, -1, false, 0, null, null, false);
```

Some combinations are logically invalid: a POST request with no body, a negative timeout, zero retries. A constructor can throw — but the error appears at runtime, not at the call site.

**Signal 3: Construction happens over multiple lines in callers.**

```java
Map<String, String> headers = new HashMap<>();
headers.put("Content-Type", "application/json");
headers.put("Authorization", "Bearer " + token);
headers.put("X-Request-ID", requestId);

List<String> retryOn = Arrays.asList("500", "502", "503");

HttpRequest request = new HttpRequest("POST", url, headers, body, 30,
    true, 3, RetryPolicy.EXPONENTIAL, retryOn, true);
```

Construction is scattered. The call site is cluttered. The object isn't built — it's assembled in a ritual.

---

## The Builder Refactoring

Move construction into a dedicated builder:

```java
HttpRequest request = HttpRequest.builder()
    .post(url)
    .header("Content-Type", "application/json")
    .header("Authorization", "Bearer " + token)
    .header("X-Request-ID", requestId)
    .body(payload)
    .timeoutSeconds(30)
    .retryPolicy(RetryPolicy.EXPONENTIAL)
    .retryOn(500, 502, 503)
    .followRedirects(true)
    .build();
```

Construction is linear. Each step is named. Optional steps can be omitted. Invalid combinations are caught in `build()`.

---

## The Structure

```java
class HttpRequest {

    private final String method;
    private final String url;
    private final Map<String, String> headers;
    private final String body;
    private final int timeoutSeconds;
    private final boolean followRedirects;
    private final RetryPolicy retryPolicy;
    private final List<Integer> retryOnStatus;

    private HttpRequest(Builder builder) {
        this.method = builder.method;
        this.url = builder.url;
        this.headers = Collections.unmodifiableMap(builder.headers);
        this.body = builder.body;
        this.timeoutSeconds = builder.timeoutSeconds;
        this.followRedirects = builder.followRedirects;
        this.retryPolicy = builder.retryPolicy;
        this.retryOnStatus = Collections.unmodifiableList(builder.retryOnStatus);
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {

        private String method;
        private String url;
        private final Map<String, String> headers = new LinkedHashMap<>();
        private String body;
        private int timeoutSeconds = 30;
        private boolean followRedirects = true;
        private RetryPolicy retryPolicy = RetryPolicy.NONE;
        private final List<Integer> retryOnStatus = new ArrayList<>();

        public Builder get(String url)  { this.method = "GET";  this.url = url; return this; }
        public Builder post(String url) { this.method = "POST"; this.url = url; return this; }

        public Builder header(String name, String value) {
            headers.put(name, value);
            return this;
        }

        public Builder body(String body) { this.body = body; return this; }

        public Builder timeoutSeconds(int seconds) {
            if (seconds <= 0) throw new IllegalArgumentException("Timeout must be positive");
            this.timeoutSeconds = seconds;
            return this;
        }

        public Builder retryPolicy(RetryPolicy policy) {
            this.retryPolicy = Objects.requireNonNull(policy);
            return this;
        }

        public Builder retryOn(int... statusCodes) {
            for (int code : statusCodes) retryOnStatus.add(code);
            return this;
        }

        public Builder followRedirects(boolean follow) {
            this.followRedirects = follow;
            return this;
        }

        public HttpRequest build() {
            if (method == null) throw new IllegalStateException("HTTP method is required");
            if (url == null)    throw new IllegalStateException("URL is required");
            if ("POST".equals(method) && body == null)
                throw new IllegalStateException("POST request requires a body");
            return new HttpRequest(this);
        }
    }
}
```

---

## What Builder Buys You

**Readability:** Each line of the builder call is self-documenting. `.timeoutSeconds(30)` is clearer than the ninth positional argument.

**Optional parameters without overloads:** Omit any optional step. Defaults are set in the builder. No telescoping constructors. No null positions.

**Invariant enforcement at build time:** `build()` runs validation once. A POST with no body is caught before the object exists. The error is at the call site, not deep in a network call.

**Immutability:** The constructed object is immutable — all fields are set in the constructor, none have setters. The builder is the mutable scratchpad; the result is frozen.

---

## Builder vs Constructor vs Factory — When to Use Each

| Tool | Use when |
|------|---------|
| Constructor | ≤4 parameters, all required, no invariants to check across fields |
| Static factory method | Named construction variants, simple, no optional steps |
| Builder | Many optional parameters, invariants span multiple fields, immutability required |
| Factory Pattern | *Which* concrete type to create varies — not *how* to assemble it |

Builder solves construction readability. Factory solves creation variation. They address different pressures and often appear together.

---

## Real Examples in Libraries You Already Use

You've used Builder Pattern without thinking about it:

```java
// OkHttp
Request request = new Request.Builder()
    .url(url)
    .addHeader("Authorization", "Bearer " + token)
    .post(RequestBody.create(json, JSON))
    .build();

// Elasticsearch Java client
SearchRequest searchRequest = new SearchRequest.Builder()
    .index("products")
    .query(q -> q.match(m -> m.field("name").query("laptop")))
    .from(0).size(20)
    .build();

// AWS SDK v2
S3Client client = S3Client.builder()
    .region(Region.US_EAST_1)
    .credentialsProvider(DefaultCredentialsProvider.create())
    .build();
```

Every major Java SDK uses Builder for configuration objects. The pressure is always the same: many optional fields, invariants to enforce, readability at the call site.

---

## The Interview Answer

**Question:** When should you use Builder instead of a constructor or factory?

**Weak answer:** *"Whenever there are many fields."*

**Strong answer:**

*"Builder Pattern addresses complex object construction — when a class has many optional parameters, when invalid field combinations need to be caught together at build time, and when callers need readable, named assembly rather than positional arguments. The key signals are: constructors with more than four or five parameters where some are optional, multiple overloaded constructors to handle optional combinations, or invariants that require multiple fields to be validated together. The built object is typically immutable — the builder is the mutable scratchpad, and `build()` produces the final frozen instance. If the object has three required fields and no invariants, a constructor is cleaner."*

---

## Static Factory Methods vs Builder — Choosing Between Them

When an object has a small number of commonly used configurations, static factory methods are often clearer than a full builder:

```java
// Static factory methods — named configurations, no builder ceremony
class RetryPolicy {
    public static RetryPolicy none() {
        return new RetryPolicy(0, Duration.ZERO, false);
    }

    public static RetryPolicy exponential(int maxAttempts) {
        return new RetryPolicy(maxAttempts, Duration.ofSeconds(1), true);
    }

    public static RetryPolicy fixed(int maxAttempts, Duration interval) {
        return new RetryPolicy(maxAttempts, interval, false);
    }
}

// Usage: clear, named, no builder required
RetryPolicy policy = RetryPolicy.exponential(3);
```

Use static factory methods when:
- A small number of meaningful configurations cover most cases
- The configurations have stable, recognizable business names
- The object has ≤4 fields with no complex optional combinations

Use Builder when:
- Many optional fields with defaults
- Invariants span multiple fields (validate at `build()`)
- Call sites need named parameters for readability
- The object is immutable and assembly is non-trivial

These are complements, not alternatives. Many well-designed libraries offer both: named factory methods for common cases, a builder for custom assembly.

```java
// Both on the same class
Duration timeout = Duration.ofSeconds(30);           // factory method for common case
HttpRequest request = HttpRequest.builder()           // builder for custom assembly
    .post(url).body(payload).timeoutSeconds(30).build();
```

## Key Takeaways

- Builder solves **construction readability** — not runtime behavior.
- Use it when: many optional fields, cross-field invariants, immutability required.
- `build()` is the validation point — errors are at the call site.
- Builder ≠ Factory: Builder controls *how to assemble*, Factory controls *which type to create*.
- Most major Java SDKs use Builder for configuration objects — you've seen this pattern everywhere.

---

*All papers and runnable Java samples: [github.com/pramod0s007/architect-notes](https://github.com/pramod0s007/architect-notes)*

*Previous → Paper 10: Chain of Responsibility | Next → Paper 12: Factory Pattern*
