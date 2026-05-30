# Visitor Pattern Examples

## Example 1 - Ship + Station

**Before**

```java
if(ship && station)
    resolveCollision(ship, station);
```

**After**

```java
ship.accept(collisionVisitor);
// visitor.visit(Ship) dispatches to paired logic
```

## Example 2 - Ship + Comet

**Before**

```java
if(ship && comet)
    destroy(ship);
```

## Example 3 - Station + Asteroid

**Before**

```java
if(station && asteroid)
    damage(station);
```

## Pressure

Object Interaction Matrix

## Architect Rule

Visitor helps when:

- Many object types
- Many operations across those types
- Interaction matrix is growing

Consider lookup tables (Paper 08) when the matrix is stable and performance matters.
