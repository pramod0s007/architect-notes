# Visitor Evolution

```
Object Interaction Matrix
        |
        v

Nested Type Checks

        |
        v

Growing Pairs (N x M)

        |
        v

Double Dispatch

        |
        v

Visitor Pattern
```

## Examples

```
Ship + Station
    ↓
CollisionVisitor.visit(Ship)
```

```
Ship + Comet
    ↓
CollisionVisitor.visit(Comet)
```
