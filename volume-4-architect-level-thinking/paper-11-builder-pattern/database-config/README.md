# Database Config — Builder Pattern

## What This Demonstrates

Builder Pattern applied to constructing an immutable `DatabaseConfig`. The config
carries three required fields (URL, username, password) and six optional fields
(pool size, SSL, connect timeout, read timeout, max retries, read replica URL).
`build()` validates that numeric values are within allowed ranges before the
object is created.

**Pressure: Complex Object Construction** — before the builder, the codebase had
multiple overloaded constructors:
`DatabaseConfig(url, user, pass)`,
`DatabaseConfig(url, user, pass, poolSize)`,
`DatabaseConfig(url, user, pass, poolSize, ssl)`, and so on. Callers combined
these freely. A `DatabaseConfig(url, user, pass, false, 5000L)` compiled fine but
silently passed `false` as `sslEnabled` when the caller intended it as pool size,
and `5000L` as pool size when it was meant as a timeout value. No compiler error;
wrong config shipped to production.

## Class Diagram

```
DatabaseConfig (immutable)
────────────────────────────────────────
- url: String               [required]
- username: String          [required]
- password: String          [required]
- poolSize: int             [default: 10]
- sslEnabled: boolean       [default: false]
- connectTimeoutMs: long    [default: 5_000]
- readTimeoutMs: long       [default: 30_000]
- maxRetries: int           [default: 3]
- readReplicaUrl: String    [default: null]
────────────────────────────────────────
+ getUrl(), getUsername(), getPassword()
+ getPoolSize(), isSslEnabled()
+ getConnectTimeoutMs(), getReadTimeoutMs()
+ getMaxRetries(), getReadReplicaUrl()
+ hasReadReplica(): boolean

      △ built by
      │
DatabaseConfig.Builder
────────────────────────────────────────
Builder(url, username, password)      ← required fields locked at construction
+ poolSize(int)          : Builder
+ sslEnabled(boolean)    : Builder
+ connectTimeoutMs(long) : Builder
+ readTimeoutMs(long)    : Builder
+ maxRetries(int)        : Builder
+ readReplicaUrl(String) : Builder
+ build()                : DatabaseConfig
   └─ validate(): poolSize in [1..100]
   └─ connectTimeoutMs > 0
   └─ readTimeoutMs > 0
   └─ maxRetries >= 0
   └─ new DatabaseConfig(this)
```

## Configuration Profiles

### Profile 1 — Minimal Dev (only required fields)

```java
new DatabaseConfig.Builder(
    "jdbc:postgresql://localhost:5432/myapp_dev",
    "dev_user", "dev_pass"
).build();
// poolSize=10, ssl=false, connectTimeout=5000ms, readTimeout=30000ms, retries=3
```

### Profile 2 — Full SSL Production

```java
new DatabaseConfig.Builder(
    "jdbc:postgresql://prod-db.us-east-1.rds.amazonaws.com:5432/myapp",
    "app_user", System.getenv("DB_PASSWORD")
)
    .poolSize(50)
    .sslEnabled(true)
    .connectTimeoutMs(3_000L)
    .readTimeoutMs(15_000L)
    .maxRetries(5)
    .build();
```

### Profile 3 — Read Replica for Analytics

```java
new DatabaseConfig.Builder(
    "jdbc:postgresql://prod-db-primary.us-east-1.rds.amazonaws.com:5432/myapp",
    "app_user", System.getenv("DB_PASSWORD")
)
    .poolSize(20)
    .sslEnabled(true)
    .readReplicaUrl("jdbc:postgresql://prod-db-replica.us-east-1.rds.amazonaws.com:5432/myapp")
    .build();
// config.hasReadReplica() → true
```

## Design Decisions

- **Defaults set in the `Builder` field declarations, not in `build()`** —
  optional fields are truly optional. A caller building a dev config gets a
  sensible pool of 10 and 5-second connect timeout without any extra calls.
  The defaults are visible in one place, not scattered across callers.
- **Required fields in the `Builder` constructor, not as setter calls** — `url`,
  `username`, and `password` are final in the builder. Passing them via
  constructor forces the compiler to reject a builder that omits them
  entirely, instead of failing at `build()` time with a null check.
- **`validate()` is a private method in `Builder`** — all range checks live in
  one place. Adding a constraint on a new field means editing one method.
- **`hasReadReplica()` convenience method on `DatabaseConfig`** — callers check
  the computed boolean instead of writing
  `config.getReadReplicaUrl() != null && !config.getReadReplicaUrl().isBlank()`
  at every routing decision site.
- **`poolSize` defaults to 10, not 5** — chosen to match HikariCP's own default,
  so this example is grounded in real-world convention rather than arbitrary choice.

## How to Run

```bash
cd volume-4-architect-level-thinking/paper-11-builder-pattern/database-config
javac *.java && java Main
```

Expected output (abbreviated):

```
=== Minimal Dev Config ===
DatabaseConfig{url='jdbc:postgresql://localhost:5432/myapp_dev', ..., poolSize=10, sslEnabled=false, ...}
Pool size  : 10
SSL        : false
Connect tmo: 5000 ms

=== Full Production Config ===
Pool size  : 50
SSL        : true
Connect tmo: 3000 ms
Read tmo   : 15000 ms
Max retries: 5

=== Read-Replica Config ===
Has replica: true
Replica URL: jdbc:postgresql://prod-db-replica.us-east-1.rds.amazonaws.com:5432/myapp

=== Validation Demo ===
Caught expected error: poolSize must be between 1 and 100, got: 200
Caught expected error: Database URL is required
```

## When to Apply

- A config or value object has many optional fields where incorrect combinations
  produce silent runtime failures rather than compile-time errors.
- Different deployment environments (dev, CI, staging, prod) share the same type
  but set different subsets of fields.

## When NOT to Apply

- Truly flat config (three required strings, nothing optional) — a plain
  constructor or a Java record is cleaner and requires no builder boilerplate.
