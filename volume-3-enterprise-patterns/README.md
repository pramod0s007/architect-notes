# Volume 3 — Enterprise Patterns

Enterprise patterns address the structural problems that appear as systems grow: complex business rules, multi-step pipelines, and domain-specific logic that outgrows simple object hierarchies.

---

## Papers in This Volume

| Paper | Title | Pressure | Code Examples |
|-------|-------|---------|--------------|
| [Paper 09](paper-09-specification-pattern/) | Specification Pattern | Rules variation | product-search, loan-eligibility |
| [Paper 10](paper-10-chain-of-responsibility/) | Chain of Responsibility | Sequential decision flow | request-pipeline, api-security-pipeline |

---

## The Pressure Map

```
Business rules grow, compose with AND/OR   → Specification Pattern         (Paper 09)
Request passes through ordered stages      → Chain of Responsibility       (Paper 10)
```

---

## Why These Are "Enterprise" Patterns

Both patterns appear when a system is large enough that:
- Rules are owned by different teams and change at different rates (Specification)
- Request handling has enough cross-cutting concerns to warrant a pipeline (Chain)

Neither is appropriate for small, stable systems.
