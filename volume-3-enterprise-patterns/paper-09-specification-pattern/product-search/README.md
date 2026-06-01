# Product Search — Specification Pattern

## What It Demonstrates

An e-commerce product catalogue filtered by composable specifications: minimum price,
category match, and minimum rating. Rules chain together with `.and()` to form a single
predicate that is evaluated against each product — no nested `if` trees, no boolean flags
scattered across a query builder.

## The Pressure: Rules Variation

Search filters accumulate independently over a product's lifetime:
- Sprint 1: price range
- Sprint 4: category filter
- Sprint 7: minimum rating
- Sprint 11: in-stock flag, seller rating, shipping speed ...

Without the pattern, each new filter is a new `if` branch added to a growing query method.
With Specification, each filter is a new class — existing specifications are unchanged,
and any combination is possible without modifying existing code (Open/Closed Principle).

## Class Diagram (ASCII)

```
<<interface>>
 Specification<T>
──────────────────────────────────
 isSatisfiedBy(T): boolean
 and(Specification<T>): Specification<T>   ← default method
       ▲
       │ implements
  ┌────┴────────────────────────────────┐
PriceSpecification  CategorySpecification  RatingSpecification
──────────────────  ─────────────────────  ──────────────────
- minimumPrice       - category              - minimumRating
isSatisfiedBy()      isSatisfiedBy()         isSatisfiedBy()

AndSpecification<T>
──────────────────────────────
- left:  Specification<T>
- right: Specification<T>
isSatisfiedBy() → left && right
```

`Product` is a plain record:
```
Product
───────────────────────────────
 name: String
 price: double
 category: Category
 rating: double
```

## Composition Flow

```
new PriceSpecification(300.0)
  .and(new CategorySpecification(ELECTRONICS))
  .and(new RatingSpecification(4.0))

Step 1:  PriceSpec                               — leaf node
Step 2:  AndSpec(PriceSpec, CategorySpec)        — wraps step 1
Step 3:  AndSpec(AndSpec(...), RatingSpec)        — wraps step 2

isSatisfiedBy(product) walks the tree:
  AndSpec.isSatisfiedBy(p)
    → left.isSatisfiedBy(p)     [AndSpec: price AND category]
        → left.isSatisfiedBy(p) [PriceSpec: price >= 300]
        → right.isSatisfiedBy(p)[CategorySpec: category == ELECTRONICS]
    → right.isSatisfiedBy(p)    [RatingSpec: rating >= 4.0]
```

## How to Run

```bash
cd volume-3-enterprise-patterns/paper-09-specification-pattern/product-search
javac *.java
java Main
```

Expected output:
```
Rule: price >= 300 AND category == ELECTRONICS AND rating >= 4

  match: Noise-Cancel Headphones — $349.99 ELECTRONICS ★4.6
```

The `4K Monitor` ($899, ELECTRONICS) is excluded because its rating is 3.9.
The `Standing Desk` ($1299, HOME) is excluded because its category is HOME, not ELECTRONICS.

## Design Decisions

**Default methods on the interface** (`and`, `or`, `not`) mean any object with a single
`isSatisfiedBy` method can act as a Specification — including lambdas:

```java
Specification<Product> inStock = p -> p.stockCount() > 0;
Specification<Product> combined = new PriceSpecification(100.0).and(inStock);
```

No abstract base class needed; composition is available everywhere.

**`AndSpecification` as an explicit class** (rather than a lambda inside the `and` default)
allows the tree to be inspected, serialized, or pretty-printed — useful for query explain
plans and audit logging.

**Stream integration** is natural: `catalog.stream().filter(spec::isSatisfiedBy)` reads
as plain English, and the specification object can be passed, stored, and reused across
requests without rebuilding predicate chains each time.

**Compare with Paper 09 loan-eligibility** (`../loan-eligibility`) which adds `or()`
and `not()` to handle premium override paths and demonstrates named-rule diagnostics.
