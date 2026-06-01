# Lookup Table Examples

## Example - Collision Engine

**if-else**

```java
if(a instanceof Ship && b instanceof Station) { ... }
else if(a instanceof Ship && b instanceof Comet) { ... }
```

**Visitor**

```java
a.accept(collisionVisitor);
```

**Lookup table**

```java
CollisionAction action = table.get(CollisionKey.of(a, b));
action.apply(a, b);
```

## Comparison

| Style | Best when |
|-------|-----------|
| if-else | Few pairs, rarely changes |
| Visitor | Many operations, stable types |
| Lookup | Finite matrix, performance-sensitive |

## Architect Rule

Prefer lookup tables when the key space is bounded and teams want explicit, testable mappings.
