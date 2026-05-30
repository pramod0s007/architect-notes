# Lookup Tables vs Polymorphism

## When Conditionals Become Tables

The collision engine does not always need Visitor.

Sometimes the interaction matrix is **finite and stable**.

```java
if(ship && station) { ... }
if(ship && comet) { ... }
if(station && asteroid) { ... }
```

As pairs grow, conditionals become a **matrix**.

## The Real Problem

**Growing conditional matrix.**

Each new pair adds another branch.

Refactoring choices:

| Approach | Trade-off |
|----------|-----------|
| if-else | Simple, grows messy |
| Visitor | Structured, heavier |
| Lookup table | Fast, data-driven |

## Lookup Table Approach

```java
Map<CollisionKey, CollisionAction> table = Map.of(
    key(SHIP, STATION), this::shipStation,
    key(SHIP, COMET), this::shipComet,
    key(STATION, ASTEROID), this::stationAsteroid
);

table.get(key(a, b)).resolve(a, b);
```

Behavior is **data**, not scattered conditionals.

## Design Pressure

```
Growing Conditional Matrix
        ↓
Lookup Table
```

## Key Takeaways

- Lookup tables are a valid architectural choice.
- Not every matrix problem needs Visitor or polymorphism.
- Choose based on growth rate, performance, and team readability.
- Paper 03 classified collision as behavior variation — table vs Visitor is an implementation decision.
