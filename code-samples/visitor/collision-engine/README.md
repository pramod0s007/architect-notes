# Collision Engine — Visitor Pattern

Runnable sample for **Visitor Pattern** (Paper 07).

## Run

```bash
cd code-samples/visitor/collision-engine
javac *.java
java Main
```

## Double dispatch

```java
CollisionVisitor visitor = new CollisionVisitor(station);
ship.accept(visitor);
station.accept(visitor);
```

Each `accept` routes to `visitor.visit(ConcreteType)` using the partner type — no `instanceof` matrix in callers.

## Demonstrated pairs

| Pair | Outcome |
|------|---------|
| Ship + Station | `resolveCollision` |
| Ship + Comet | `destroy(ship)` |
| Station + Asteroid | `damage(station)` |

## Next

Paper 08 refactors the same matrix into `Map<Key, Action>` under `code-samples/lookup/collision-engine/`.
