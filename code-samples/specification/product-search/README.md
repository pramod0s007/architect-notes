# Product Search — Specification Pattern

Runnable sample for **Specification Pattern** (Paper 09).

## Run

```bash
cd code-samples/specification/product-search
javac *.java
java Main
```

## Composition

```java
Specification<Product> spec =
    new PriceSpecification(300.0)
        .and(new CategorySpecification(Product.Category.ELECTRONICS))
        .and(new RatingSpecification(4.0));
```

Replaces nested `if (price && category && rating)` trees with composable predicates.

## Rules demonstrated

| Rule | Specification |
|------|-----------------|
| `price >= 300` | `PriceSpecification` |
| `category == ELECTRONICS` | `CategorySpecification` |
| `rating >= 4` | `RatingSpecification` |
| AND chain | `AndSpecification` via `.and()` |
