# Collision Engine — Visitor Pattern (Double Dispatch)

## What It Demonstrates

A space-game collision engine with four object types — `Ship`, `Station`, `Comet`, `Asteroid` —
where the correct collision behavior depends on **both** participants simultaneously.

The Visitor pattern solves this with **double dispatch**: the first call resolves the type of
object A; the second call resolves the type of object B. No `instanceof` chain, no N×N switch
matrix in the caller.

## The Pressure: Object Interaction Matrix

With N game-object types the interaction matrix has N*(N-1)/2 unique pairs:

```
  4 types → 6 pairs
  8 types → 28 pairs
 16 types → 120 pairs
```

Without a pattern, every new type forces edits to every existing type's collision method.
With Visitor, adding a `BlackHole` type means adding one `visit(BlackHole)` overload to
the `Visitor` interface and implementing it in `CollisionVisitor` — existing game objects
are unchanged.

## Class Diagram (ASCII)

```
<<interface>>                      <<interface>>
  GameObject                          Visitor
──────────────                   ─────────────────────
 accept(Visitor)                  visit(Ship)
 label(): String                  visit(Station)
       ▲                          visit(Comet)
       │ implements                visit(Asteroid)
  ┌────┴────┬──────────┬──────┐         ▲
Ship   Station   Comet   Asteroid        │ implements
                                  CollisionVisitor
                                  ─────────────────
                                  - partner: GameObject
                                  - outcome: String
                                  + visit(Ship)
                                  + visit(Station)
                                  + visit(Comet)
                                  + visit(Asteroid)
                                  + outcome(): String
```

## Double Dispatch Sequence

```
CollisionVisitor visitor = new CollisionVisitor(station);
ship.accept(visitor);
│
├─ Ship.accept(visitor)              [1st dispatch: JVM calls visit(Ship)]
│    └─ visitor.visit(ship)
│         └─ partner instanceof Station?
│              └─ outcome = "resolveCollision(Ship(Odyssey), Station(Relay-7))"
│
station.accept(new CollisionVisitor(ship));
│
└─ Station.accept(visitor)           [1st dispatch: JVM calls visit(Station)]
     └─ visitor.visit(station)
          └─ partner instanceof Ship?
               └─ outcome = "resolveCollision(Ship(Odyssey), Station(Relay-7))"
```

In the full double-dispatch idiom the second dispatch would call back onto the partner
(`b.accept(new ShipCollisionVisitor(a))`), resolving both types at compile-time rather than
via `instanceof`. This sample uses a single-visitor approach that keeps the demo compact
while preserving the core dispatch mechanism.

## Collision Pairs Demonstrated

| Pair            | Outcome                                    |
|-----------------|--------------------------------------------|
| Ship + Station  | `resolveCollision(ship, station)`          |
| Ship + Comet    | `destroy(ship)` — hit comet                |
| Station + Asteroid | `damage(station)` — struck asteroid    |

## How to Run

```bash
cd volume-2-behavioral-design/paper-07-visitor-pattern-without-uml/collision-engine
javac *.java
java Main
```

Expected output:
```
Ship + Station    -> resolveCollision(Ship(Odyssey), Station(Relay-7))
Ship + Comet      -> destroy(Ship(Odyssey)) — hit Comet(C/2024-A1)
Station + Asteroid -> damage(Station(Relay-7)) — struck Asteroid(1200kg)
```

## Design Decisions

**Adding a new operation** (e.g., physics simulation, scoring) requires writing one new class
that implements `Visitor` — zero changes to `Ship`, `Station`, `Comet`, or `Asteroid`.

**Adding a new game-object type** (e.g., `BlackHole`) requires adding `visit(BlackHole)` to
the `Visitor` interface and implementing it in all existing visitors — the trade-off of the
Visitor pattern. This is acceptable when types are stable and operations grow.

**Companion example:** Paper 08 (`../../../paper-08-lookup-tables-vs-polymorphism/collision-engine`)
solves the same matrix with `Map<CollisionKey, Action>` — compare both when deciding which
approach fits your context.
