# Collision Engine — Lookup Table vs Visitor

## What It Demonstrates

The **same collision problem** as Paper 07 (`../../../paper-07-visitor-pattern-without-uml/collision-engine`),
solved with a `Map<CollisionKey, Action>` instead of a Visitor hierarchy.

`CollisionEngine` holds a lookup table. The caller registers type-pair keys at startup and
calls `engine.resolve(a, b)` at runtime — the engine looks up and invokes the correct action
with no `instanceof` chain and no polymorphic dispatch.

## The Pattern

```java
CollisionEngine engine = new CollisionEngine();

// Registration (startup)
engine.register(CollisionKey.of(Ship.class, Station.class), new ShipStationAction());
engine.register(CollisionKey.of(Ship.class, Comet.class),   new ShipCometAction());
engine.register(CollisionKey.of(Station.class, Asteroid.class), new StationAsteroidAction());

// Resolution (runtime)
engine.resolve(new Ship(), new Station());   // → ShipStationAction.apply()
engine.resolve(new Ship(), new Comet());     // → ShipCometAction.apply()
```

`CollisionKey` wraps two `Class<?>` objects and uses them for equality and hashing.
The key is **ordered** — `key(Ship, Station)` is distinct from `key(Station, Ship)` —
so registration must match the argument order used at resolution time.

## Class Diagram (ASCII)

```
CollisionEngine
──────────────────────────────
- table: Map<CollisionKey, Action>
+ register(CollisionKey, Action)
+ resolve(Object a, Object b)

  CollisionKey  (inner static class)
  ──────────────────────────────────
  - typeA: Class<?>
  - typeB: Class<?>
  + of(Object, Object): CollisionKey
  + of(Class, Class):   CollisionKey
  # equals / hashCode based on typeA + typeB

<<interface>>
   Action
──────────────────────
 apply(Object, Object)
 description(): String
       ▲
       │ implements
  ┌────┴──────────────────────┐
ShipStationAction  ShipCometAction  StationAsteroidAction
```

## When Table Wins vs When Visitor Wins

```
                    Lookup Table       Visitor Pattern
────────────────────────────────────────────────────────
Matrix size         Small (≤ ~20)      Any size
Type set growth     Stable             Open (adding types)
Operation growth    Stable             Open (adding ops)
Compiler check      Runtime (Map miss) Compile-time (interface)
New behavior        Add map entry      New class
Configurable        Yes (load from DB) Requires redeploy
Symmetry support    Easy to add        Requires extra dispatch
```

**Choose Lookup Table when:** the set of types is known and fixed, you want runtime
configurability (load rules from a database or config file), or the matrix is small enough
that a flat map is readable.

**Choose Visitor when:** operations grow independently, you need compile-time safety that
every type/operation pair is handled, or the dispatch logic needs to be tested in isolation.

## How to Run

```bash
cd volume-2-behavioral-design/paper-08-lookup-tables-vs-polymorphism/collision-engine
javac *.java
java Main
```

Expected output:
```
Lookup table: Map<CollisionKey, Action>

resolveCollision(Ship, Station)
destroy(Ship) — hit Comet
damage(Station) — struck Asteroid
```

## Design Decisions

**`CollisionKey` is ordered, not symmetric.** The implementation uses `typeA` and `typeB`
directly in `equals`/`hashCode`, so `key(Ship, Station) != key(Station, Ship)`. The
registration in `Main` always puts the active object first, making intent explicit.
To support symmetric lookup, register both orderings or normalise the key by sorting
the two class names before wrapping.

**`resolve()` throws `IllegalStateException` on a missing key** rather than silently
doing nothing — an unregistered pair is a programming error, not a valid no-op, so
fail-fast is safer than silent swallowing.

**Actions are registered as objects, not lambdas**, which allows each `Action` to carry
its own `description()` for logging and diagnostics — useful when dumping the full
collision table at startup.

**Compare with Paper 07:** the Visitor version in
`../../../paper-07-visitor-pattern-without-uml/collision-engine` gives compile-time
coverage guarantees — the compiler flags an unimplemented `visit(NewType)` overload.
The lookup table version gives runtime flexibility — rules can be loaded from config.
