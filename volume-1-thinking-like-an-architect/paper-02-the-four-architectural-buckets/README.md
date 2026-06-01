# The Four Architectural Buckets

**Pattern:** Classification Framework

---

## Read the Full Article on Medium

[The Four Architectural Buckets](https://medium.com/@replytopramods.aws/the-four-architectural-buckets-73cd22f3c33c)

---
## What This Paper Is About

When you face a design problem, the question "which pattern should I use?" is almost always asked too early. Before picking a pattern, you need to classify the *type* of variation you are dealing with.

This paper introduces a four-bucket framework that narrows 23 patterns down to 2–3 candidates in under 60 seconds.

## The Four Buckets

### Bucket 1 — Data Variation
The algorithm stays the same. Only the data changes.

**Signal:** Same processing logic, different input/output format or configuration.
**Examples:** CSV vs JSON reports, locale-specific strings, environment config values.
**Solution:** Parameters, templates, configuration. Patterns often unnecessary.

### Bucket 2 — Object Variation
The workflow stays the same. The participating object changes.

**Signal:** You want to swap out a provider, engine, or channel without touching the workflow.
**Examples:** MySQL vs MongoDB, S3 vs Azure, Email vs SMS.
**Solution:** Interface + Composition + Factory.

### Bucket 3 — Behavior Variation
The algorithm itself changes.

**Signal:** Same caller, different computation. The behavior swaps.
**Examples:** Encryption algorithms, pricing strategies, validation rules.
**Solution:** Strategy, Command, State Pattern.

### Bucket 4 — Rules Variation
Business rules grow independently of object structure.

**Signal:** Rules combine with AND/OR/NOT and grow faster than the codebase.
**Examples:** Loan eligibility, search filters, discount engines.
**Solution:** Specification Pattern, Rule Engines.

## Why Misclassification Is Dangerous

The same symptom — a growing if-else chain — can come from any of the four buckets. Applying the wrong solution:
- Strategy Pattern on a Data Variation problem adds an interface where configuration would suffice
- Specification Pattern on a Behavior Variation problem adds composability where algorithm isolation is needed

Classify first. Pattern second.

## Read the Full Article


## Related Code Examples

- [pricing-engine](../../volume-2-behavioral-design/paper-04-strategy-pattern-through-real-refactoring/pricing-engine/) — Bucket 3: behavior variation in pricing
- [loan-eligibility](../../volume-3-enterprise-patterns/paper-09-specification-pattern/loan-eligibility/) — Bucket 4: rules variation in eligibility
- [storage-factory](../../volume-4-architect-level-thinking/paper-12-factory-pattern/storage-factory/) — Bucket 2: object variation in storage
