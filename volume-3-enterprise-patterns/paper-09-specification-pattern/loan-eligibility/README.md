# Loan Eligibility — Specification Pattern

## What It Demonstrates

A lending platform eligibility engine with five composable rules — minimum age, minimum income,
minimum credit score, blacklist exclusion, and premium income override — wired into two
decision paths: a standard path (all four base rules) and a premium override path (OR-composed
on top of the standard path).

The engine also returns **named rejection reasons** per applicant, enabling user-friendly
rejection messages without exposing rule internals to the caller.

## The Pressure: Rules Variation Over Time

A real lending platform accumulates eligibility rules gradually:

```
Month  1: age >= 18, income >= 30k
Month  3: credit score >= 650
Month  6: blacklist exclusion
Month  9: premium customer override
Month 12: regional rules, outstanding-loan limits, promotional windows
```

Without the pattern this becomes a 200-line `isEligible()` method maintained by 4 teams.
Each new rule risks breaking an existing condition. Each edge case spawns another `else if`.

With Specification, each rule is an isolated, named, testable class. Composition wires them
together in `EligibilityService`; the rules themselves never change.

## Class Diagram (ASCII)

```
<<interface>>
 Specification<T>
──────────────────────────────────────────────
 isSatisfiedBy(T): boolean
 and(Specification<T>): Specification<T>   ← default (lambda)
 or(Specification<T>):  Specification<T>   ← default (lambda)
 not():                 Specification<T>   ← default (lambda)
          ▲
          │ implements
  ┌───────┴──────────────────────────────────────┐
MinimumAgeSpecification      MinimumIncomeSpecification
MinimumCreditScoreSpecification  BlacklistSpecification
PremiumOverrideSpecification

EligibilityService
───────────────────────────────────────────────
- rules: Map<String, Specification<Customer>>   (ordered, labelled)
- standardEligibility: Specification<Customer>
- premiumOverride:      Specification<Customer>
+ isEligible(Customer): boolean
+ getFailedRules(Customer): List<String>
```

## Eligibility Tree

```
standardEligibility =
  MinimumAge(21)
    AND MinimumIncome(30,000)
    AND MinimumCreditScore(650)
    AND NOT(Blacklisted)

premiumOverride =
  PremiumOverride(income >= 120,000)
    AND MinimumAge(21)
    AND NOT(Blacklisted)          ← blacklist applies even to premium customers

fullEligibility =
  standardEligibility OR premiumOverride
```

## Named Rejection Reasons

`getFailedRules(customer)` iterates the named-rule map and returns the label of every
standard rule the customer failed:

```java
List<String> failed = service.getFailedRules(customer);
// → ["Minimum credit (650)"]          — David Park
// → ["Minimum income (30,000)",
//    "Minimum credit (650)"]           — Frank Okafor
```

This enables the UI to display specific, actionable rejection messages ("Your credit score
does not meet our minimum requirement") without the UI knowing anything about thresholds
or rule internals.

## How to Run

```bash
cd volume-3-enterprise-patterns/paper-09-specification-pattern/loan-eligibility
javac *.java
java Main
```

Expected output:
```
Applicant          Decision   Failed rules
----------------------------------------------------------------------
Alice Müller       APPROVED   —
Bob Chen           DENIED     Minimum age (21)
Carol Davis        DENIED     Minimum income (30,000)
David Park         APPROVED   Minimum credit (650)
Eve Torres         DENIED     Not blacklisted
Frank Okafor       DENIED     Minimum income (30,000), Minimum credit (650)
```

David Park fails the standard credit rule but is approved via the premium override
(isPremium = true, income = 150,000 >= 120,000 threshold).

Eve Torres is denied despite premium status because the blacklist check is ANDed into
the premium path — the override does not bypass the blacklist.

## Design Decisions

**`or()` and `not()` are default methods returning lambdas**, not wrapper classes.
The `and()` in the product-search example uses an explicit `AndSpecification` class for
inspectability; here the simpler lambda form is used because the eligibility tree does
not need serialization or explain-plan output.

**`PremiumOverride` is composed with OR** rather than embedded as a conditional branch
inside each base rule. Adding a new override path (e.g., `VIPOverride`) means adding
one `.or(vipOverride)` in `EligibilityService` — zero changes to the five existing
specifications.

**The rules map is `LinkedHashMap`** so `getFailedRules()` returns failures in definition
order — consistent for UI display and test assertions, with no extra sorting step.

**`standardEligibility` is built by stream-reducing the rules map** rather than hardcoding
an `.and()` chain. Adding a new rule to the map automatically includes it in the standard
path — no separate wiring step required.
