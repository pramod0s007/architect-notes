# Specification Pattern

**Pattern:** Specification Pattern

---

## Read the Full Article on Medium

_Article coming soon on Medium._

This repository focuses on runnable code examples. The white paper covers theory, war stories, and architectural reasoning in depth.

---
## What This Paper Is About

Specification Pattern addresses **rules variation** — when business rules grow independently of your object model, combine with AND/OR/NOT, and need to be composed, reused, and tested independently.

This is distinct from Strategy Pattern (which isolates algorithms) and State Pattern (which manages modes). Rules are predicates. They compose. Strategies don't naturally compose.

## The Pressure: Rules Variation

A loan eligibility service started with one rule (`age >= 18`). Over 12 months, rules accumulated: income threshold, credit score, blacklist check, regional regulations, premium member override, promotional windows.

The `isEligible()` method became 200 lines with 16 edge cases across 4 teams. Nobody fully understood the interaction between the premium override and the NY regional regulation.

## The Pattern

Each rule is a composable predicate:

```java
interface Specification<T> {
    boolean isSatisfiedBy(T candidate);

    default Specification<T> and(Specification<T> other) { ... }
    default Specification<T> or(Specification<T> other)  { ... }
    default Specification<T> not()                        { ... }
}
```

Rules compose like sentences:

```java
Specification<Customer> eligible =
    minimumAge(18)
        .and(minimumIncome(50_000))
        .and(minimumCreditScore(650))
        .and(not(blacklisted()));
```

## Testing Benefit

Before Specification: one 200-line method, dozens of test scenarios, unclear which rule each test covers.

After Specification: each rule has 2–3 focused tests. Composition has integration tests. Every test name says exactly what it's testing.

## JPA Integration

Spring Data's `JpaSpecificationExecutor` lets the same rule objects drive both business logic and database queries:

```java
List<Customer> customers = repository.findAll(isActive().and(inRegion("EU")));
```

## Pros

- Each rule independently testable
- Composition explicit and readable
- New rule = new class, zero changes to existing rules
- Business rules readable as sentences

## Cons

- More verbose than inline conditionals for 3 static rules
- Lambda chains can be slower in hot paths (millions of evaluations/sec)
- Rules that come from a database at runtime need a rule engine, not Specification

## Read the Full Article

{medium}

## Code Examples in This Repo

| Example | Domain | What It Shows |
|---------|--------|--------------|
| [`specification/product-search/`](../../code-samples/specification/product-search/) | E-commerce | Price, category, rating, stock — composable AND/OR/NOT filters |
| [`specification/loan-eligibility/`](../../code-samples/specification/loan-eligibility/) | Lending | Age, income, credit, blacklist, premium override — `getFailedRules()` for rejection reasons |

### How to Run

```bash
cd code-samples/specification/product-search
javac *.java && java Main

cd code-samples/specification/loan-eligibility
javac *.java && java Main
```
