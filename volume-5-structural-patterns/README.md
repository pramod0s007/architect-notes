# Volume 5 — Structural Patterns

Structural patterns define how objects and classes are composed to form larger structures. This volume covers the most production-relevant structural patterns — Observer, Decorator, Proxy, and Adapter.

---

## Papers in This Volume

| Paper | Title | Pressure | Code Examples |
|-------|-------|---------|--------------|
| [Paper 16](paper-16-observer-pattern/) | Observer Pattern — The Foundation of Event-Driven Systems | Event broadcast | order-events, stock-price-monitor |
| [Paper 17](paper-17-decorator-pattern/) | Decorator Pattern — Adding Behavior Without Subclassing | Cross-cutting concerns | message-sender, http-client |
| [Paper 18](paper-18-proxy-pattern/) | Proxy Pattern — Controlling Access to Objects | Transparent access control | caching-repository, lazy-loading |
| [Paper 19](paper-19-adapter-pattern/) | Adapter Pattern — Making Incompatible Interfaces Work Together | Interface incompatibility | storage-adapter, payment-adapter |

---

## The Pressure Map

```
One producer, multiple independent consumers   → Observer Pattern    (Paper 16)
Multiple orthogonal capabilities to stack      → Decorator Pattern   (Paper 17)
Transparent access control / caching / lazy    → Proxy Pattern       (Paper 18)
Two incompatible interfaces, can't modify      → Adapter Pattern     (Paper 19)
```

---

## Note on Patterns Not Yet Covered

The complete GoF structural set also includes Composite, Bridge, Facade, and Flyweight. These will be added in future volumes as the series expands toward full 23-pattern coverage.
