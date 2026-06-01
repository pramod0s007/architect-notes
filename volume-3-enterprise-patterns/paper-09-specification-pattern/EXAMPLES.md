# Specification Examples

## Example 1 - Price

```java
price > 1000
```

```java
new PriceAboveSpecification(1000)
```

## Example 2 - Category

```java
category == ELECTRONICS
```

```java
new CategorySpecification(ELECTRONICS)
```

## Example 3 - Rating

```java
rating > 4
```

```java
new RatingAboveSpecification(4)
```

## Composition

```java
spec1.and(spec2).or(spec3)
```

## Pressure

Rules Variation
