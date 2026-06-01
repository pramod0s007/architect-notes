# Proxy Pattern — Caching Repository

Demonstrates Virtual/Caching Proxy: the caller interacts with ProductRepository
and cannot tell whether results come from the database or a cache.

## Pressure
Database calls are expensive. Results should be transparently cached
without changing the calling code or the repository interface.

## Run
```bash
javac proxy/cachingrepository/*.java && java proxy.cachingrepository.Main
```

## Key Point
The proxy implements the same interface as the real object. The caller never changes.
Cache invalidation on write ensures consistency.
