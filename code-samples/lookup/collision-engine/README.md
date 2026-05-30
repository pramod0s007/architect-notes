# Collision Engine — Lookup Table

Runnable sample for **Lookup Tables vs Polymorphism** (Paper 08).

Replaces Visitor double dispatch with an explicit finite matrix.

## Run

```bash
cd code-samples/lookup/collision-engine
javac *.java
java Main
```

## Lookup

```java
Action action = table.get(CollisionKey.of(a, b));
action.apply(a, b);
```

`CollisionEngine` holds `Map<CollisionKey, Action>`.

## Registered pairs

| Key | Action |
|-----|--------|
| Ship + Station | `ShipStationAction` |
| Ship + Comet | `ShipCometAction` |
| Station + Asteroid | `StationAsteroidAction` |

## Compare

- Visitor: `code-samples/visitor/collision-engine/`
- if-else matrix: Paper 07 README
