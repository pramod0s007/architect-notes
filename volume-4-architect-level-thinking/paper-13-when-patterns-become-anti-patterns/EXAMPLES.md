# Anti-Pattern Examples

## Strategy Explosion

**Smell:** One implementation, many strategy classes.

**Fix:** Inline or use a simple map until a second real algorithm appears.

## Factory Hell

**Smell:** `AbstractFactoryFactory` for two `new` calls.

**Fix:** Factory method or DI container registration.

## Inheritance Abuse

**Smell:** `BaseService` with 40 protected hooks.

**Fix:** Composition + Strategy for the one varying step.

## Visitor Overengineering

**Smell:** Visitor for four collision pairs.

**Fix:** `Map<CollisionKey, Action>` (Paper 08).

## Premature Abstraction

**Smell:** `Repository` interface with one JPA implementation.

**Fix:** Concrete repository until a second store is real.

## Pressure

Misidentified or absent design pressure
