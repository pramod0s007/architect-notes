# Paper 18 — Proxy Pattern: Evolution Examples

**Domain:** Document Access Service

## Progression

| Version | File | State | Core Problem |
|---------|------|-------|--------------|
| v1 | `v1_NoProxy.java` | Auth mixed into business logic | 4 copy-pasted role checks; changing auth policy = 4 edits |
| v2 | `v2_ProtectionProxy.java` | Protection Proxy | All auth in `requireRole()` in one class; `DocumentServiceImpl` has zero auth code |
| v3 | `v3_CachingLayer.java` | Two stacked proxies | `CachingProxy` wraps `ProtectionProxy` wraps `Impl`; each layer has one responsibility |

## Why This Domain

Document access control is a universal enterprise concern. The v1 problem
(auth copy-pasted into business methods) appears in almost every large codebase
and is the most common reason services become hard to modify.

## How to Run

```bash
cd volume-5-structural-patterns/paper-18-proxy-pattern/evolution

javac -d out v1_NoProxy.java v2_ProtectionProxy.java v3_CachingLayer.java

java -cp out evolution.v1_NoProxy
java -cp out evolution.v2_ProtectionProxy
java -cp out evolution.v3_CachingLayer
```

## Stacking Proxies (v3)

```
Caller
  -> CachingDocumentProxy   (one job: cache management)
       -> DocumentServiceProxy  (one job: access control)
            -> DocumentServiceImpl  (one job: business logic)
```

Each layer is independently testable and independently replaceable:
- Remove caching: replace `CachingDocumentProxy` with the delegate directly
- Change auth rules: edit `DocumentServiceProxy.requireRole()` only
- Add retry: insert a `RetryDocumentProxy` between Caching and Protection

## The Proxy Family

| Proxy Type | Responsibility |
|-----------|---------------|
| Protection Proxy | Role/permission enforcement |
| Caching Proxy | Result memoization |
| Remote Proxy | Network call abstraction |
| Lazy Proxy | Deferred initialization (heavy resources) |
| Logging Proxy | Audit trail / observability |

All share the same structure: implement the interface, hold a delegate, add one concern.
