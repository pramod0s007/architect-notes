# Caching Repository — Proxy Pattern (Caching Proxy)

## What This Demonstrates

Proxy Pattern applied to database access. `CachingProductRepositoryProxy`
implements the same `ProductRepository` interface as `DatabaseProductRepository`
and wraps it. Cache hits return instantly; misses delegate to the real repository.
On `save()`, the proxy invalidates both the id cache and the category cache.
The caller (`Main`) interacts only with `ProductRepository` and cannot tell
whether a result came from the cache or the database.

**Pressure: Transparent caching** — the reporting service was making 400ms
database round trips for the same product on every API request. Adding caching
inside `DatabaseProductRepository` would violate SRP (repository should only
talk to the database). Adding it in every caller would scatter cache logic across
dozens of services. The proxy keeps the original repository untouched and adds
caching transparently at one level above it.

## Class Diagram

```
<<interface>>
ProductRepository
+ findById(id: String): Product
+ findByCategory(category: String): List<Product>
+ save(product: Product): void
        △
        |
   ─────────────────────────────────────────────
   |                                            |
DatabaseProductRepository            CachingProductRepositoryProxy
findById()  → DB query (50ms)        - real: ProductRepository
findByCategory() → DB query          - idCache: Map<String, Product>
save()      → DB write               - categoryCache: Map<String, List<Product>>
                                     findById():
                                       HIT  → return idCache.get(id)
                                       MISS → real.findById(id) → store → return
                                     findByCategory():
                                       HIT  → return categoryCache.get(category)
                                       MISS → real.findByCategory() → store → return
                                     save():
                                       idCache.remove(product.getId())
                                       categoryCache.remove(product.getCategory())
                                       real.save(product)

Product
- id: String
- name: String
- category: String
- price: double
```

## Sequence Diagram

```
Client           CachingProxy           DatabaseRepository
  │                   │                        │
  │ findById("P1")    │                        │
  │──────────────────>│ MISS                   │
  │                   │──────────────────────> │ query (50ms)
  │                   │<────────────────────── │ Product
  │                   │ idCache.put("P1", p)   │
  │<──────────────────│ return Product         │
  │                   │                        │
  │ findById("P1")    │                        │
  │──────────────────>│ HIT — return instantly │
  │<──────────────────│                        │
  │                   │                        │
  │ save(product)     │                        │
  │──────────────────>│ idCache.remove("P1")   │
  │                   │ categoryCache.remove() │
  │                   │──────────────────────> │ DB write
  │                   │                        │
  │ findById("P1")    │                        │
  │──────────────────>│ MISS (invalidated)     │
  │                   │──────────────────────> │ fresh query (50ms)
  │<──────────────────│ return updated Product │
```

## Design Decisions

- **Both `idCache` and `categoryCache` are invalidated on `save()`** — if only
  the id cache were invalidated, a caller querying by category would get stale
  results (the old price) for the saved product. Both caches reference the same
  product object, so both must be invalidated together.
- **Proxy implements the full `ProductRepository` interface** — callers need no
  change. The proxy can be dropped in wherever the real repository is used; the
  construction site in `Main` is the only change required.
- **`DatabaseProductRepository` is untouched** — the caching proxy adds a layer
  without modifying the class that owns the database connection. The original
  repository can still be used directly in tests that need to verify actual
  database behavior without cache interference.
- **`idCache` keyed by product id, `categoryCache` keyed by category string** —
  the invalidation key is the same as the lookup key. `save(product)` has the
  product id and category available, so invalidation is O(1) without needing to
  scan the cache.
- **No TTL or size limit in this example** — the focus is on structural transparency
  and write-through invalidation. A production implementation would add TTL expiry
  and a maximum size (see the LRU+TTL pattern in `http-client/CachingDecorator`).

## How to Run

```bash
cd volume-5-structural-patterns/paper-18-proxy-pattern/caching-repository
javac proxy/cachingrepository/*.java && java proxy.cachingrepository.Main
```

Expected output:

```
=== First access (cache miss — hits DB) ===
[CACHE MISS] findById: P1
[DB] findById P1 (simulated 50ms)
Product{id='P1', name='Laptop Pro', category='electronics', price=999.99}

=== Second access (cache hit — no DB) ===
[CACHE HIT] findById: P1
Product{id='P1', name='Laptop Pro', category='electronics', price=999.99}

=== Category query (cache miss) ===
[CACHE MISS] findByCategory: electronics
[DB] findByCategory electronics (simulated 50ms)

=== Category query again (cache hit) ===
[CACHE HIT] findByCategory: electronics

=== Update invalidates cache ===
[CACHE] Invalidated entries for: P1
[DB] save P1

=== Post-update fetch (cache miss — price updated) ===
[CACHE MISS] findById: P1
[DB] findById P1 (simulated 50ms)
Product{id='P1', name='Laptop Pro', category='electronics', price=899.99}
```

## When to Apply

- The real subject is expensive to call (database, remote API) and the same
  inputs frequently produce the same outputs.
- Callers should not need to know caching exists or change their call patterns.
- Cache invalidation logic is deterministic and tied to writes through the same
  interface.

## When NOT to Apply

- The data changes so frequently that the cache hit rate is negligible — the
  proxy overhead (map lookups, invalidation) adds cost without benefit.
- Multiple nodes write concurrently to the same data — an in-process map cache
  will diverge across instances; a distributed cache (Redis, Memcached) is needed.
