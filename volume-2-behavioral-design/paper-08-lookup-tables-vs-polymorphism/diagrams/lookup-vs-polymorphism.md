# Lookup vs Polymorphism

```
Growing Conditional Matrix
        |
        +-------- if-else (small matrix)
        |
        +-------- Visitor (many operations)
        |
        +-------- Lookup Table (stable matrix)
```

## Collision Engine

```
if-else
    ↓
Visitor
    ↓
Map<CollisionKey, Action>
```
