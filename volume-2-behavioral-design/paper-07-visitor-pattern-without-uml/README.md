# Visitor Pattern Without UML

## Why Object Interactions Explode

Consider a collision engine.

```java
if(ship && station) { ... }
if(ship && comet) { ... }
if(station && asteroid) { ... }
```

Each pair is a unique interaction.

As object types grow, the matrix grows **quadratically**.

This is **object interaction matrix** pressure.

## The Real Problem

Many developers think:

> "We have too many if-statements."

The real problem is:

Interactions depend on **two types** at once.

Adding a new object type forces changes across many conditional blocks.

## Visitor Thinking (Without UML)

Visitor separates:

- **Objects** (Ship, Station, Comet)
- **Operations** (Collision, Damage, Score)

Each object accepts a visitor.

Each visitor implements behavior for every object type.

This is **double dispatch**:

```
object.accept(visitor)
    → visitor.visit(object)
```

## Refactoring Direction

```java
interface GameObject {
    void accept(CollisionVisitor visitor);
}

interface CollisionVisitor {
    void visit(Ship ship);
    void visit(Station station);
    void visit(Comet comet);
}
```

## What Actually Changed?

Many developers answer:

> "We used Visitor Pattern."

Architects answer:

> "We moved interaction logic out of nested conditionals into a structured matrix."

## Design Pressure

```
Object Interaction Matrix
        ↓
Double Dispatch
        ↓
Visitor
```

## Key Takeaways

- Visitor addresses interaction explosion across types.
- Double dispatch replaces nested type checks.
- New interactions often mean new visitor implementations, not editing every conditional.
- Visitor is powerful but has trade-offs (covered in Paper 08).

## Runnable Example

See:

code-samples/visitor/collision-engine
