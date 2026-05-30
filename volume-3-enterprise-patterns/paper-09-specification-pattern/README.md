# Specification Pattern

## Why Business Rules Become Unmanageable

Search and eligibility systems often start simple.

```java
if(price > 1000)
    ...

if(category == ELECTRONICS)
    ...

if(rating > 4)
    ...
```

Each rule is independent.

Then rules combine:

- AND
- OR
- NOT

Conditionals become **trees**.

## The Real Problem

**Rules Variation.**

Business rules grow faster than object structure.

The pressure is composable, reusable rule logic — not boolean sprawl.

## Specification Thinking

A specification is a predicate:

```java
interface Specification<T> {
    boolean isSatisfiedBy(T candidate);
}
```

Rules become composable objects:

```java
Specification<Product> spec =
    priceAbove(1000)
        .and(categoryIs(ELECTRONICS))
        .and(ratingAbove(4));
```

## Design Pressure

```
Rules Variation
        ↓
Specification Pattern
```

## Key Takeaways

- Specification isolates business rules from queries and entities.
- Composition (and/or/not) replaces nested conditionals.
- Often pairs with Repository or Query layers in enterprise systems.
- Classified as **Bucket 4 – Rules Variation** in Paper 02.

## Runnable Example

See:

code-samples/specification/product-search
