# Proxy Pattern — Controlling Access to Objects

**Pattern:** Proxy Pattern

---

## Read the Full Article on Medium

_Article coming soon on Medium._

This repository focuses on runnable code examples. The white paper covers theory, war stories, and architectural reasoning in depth.

---
## What This Paper Is About

Proxy Pattern places a surrogate in front of a real object. The surrogate implements the same interface, so the caller cannot tell the difference. The proxy intercepts access and adds behavior — lazy loading, remote communication, access control, or caching — without the caller knowing.

**Every `@Transactional` method, every lazy-loaded JPA collection, every gRPC stub, every `@PreAuthorize` annotation is a proxy.**

## The Four Proxy Types

### 1. Virtual Proxy (Lazy Loading)
Defer expensive creation until actually needed. JPA lazy-loads `@OneToMany` collections via proxy — accessing the collection triggers the DB query.

### 2. Remote Proxy (RPC Stubs)
The real object lives on another machine. gRPC generates stub classes that serialize your method call, route it over the network, and deserialize the response. The caller writes normal Java.

### 3. Protection Proxy (Access Control)
Different callers have different permissions. Spring `@PreAuthorize` generates a proxy that checks security before delegating to the real method.

### 4. Caching Proxy
Results are memoized transparently. Spring `@Cacheable` generates a proxy that checks the cache before calling the real method.

## The @Transactional Self-Call Problem

This is the most common production bug related to Spring proxies:

```java
@Transactional
public void methodA() {
    methodB();  // calls this.methodB() directly — bypasses proxy
}

@Transactional(propagation = REQUIRES_NEW)
public void methodB() { ... }  // @Transactional ignored on self-calls
```

Spring injects a proxy, not the real object. Internal calls go to `this` — the real object — not the proxy. The transaction propagation annotation on `methodB` is never seen.

**Fix:** Inject the bean into itself (`@Autowired private MyService self`) and call `self.methodB()`.

## Decorator vs Proxy

Both wrap an object. The distinction is intent:
- **Proxy** — controls access; typically transparent to the caller; often framework-managed
- **Decorator** — adds behavior; typically composed explicitly by the caller

## Read the Full Article

{medium}

## Code Examples in This Repo

| Example | Domain | What It Shows |
|---------|--------|--------------|
| [`proxy/caching-repository/`](../../code-samples/proxy/caching-repository/) | Data access | Transparent DB result caching; cache invalidation on writes |
| [`proxy/lazy-loading/`](../../code-samples/proxy/lazy-loading/) | Reports | Title access is free; PDF content loads only on demand (double-checked locking) |

### How to Run

```bash
cd code-samples/proxy/caching-repository
javac *.java && java Main

cd code-samples/proxy/lazy-loading
javac *.java && java Main
```
