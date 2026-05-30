# The Four Architectural Buckets

## Why Most Design Problems Look Different But Are Actually The Same

Many engineers see hundreds of different software design problems.

Architects often see only a handful.

The reason is simple.

Different systems may look different on the surface, but the underlying pressures are usually the same.

After studying many codebases, I found that most design problems fall into four major categories:

- **Data Variation**
- **Object Variation**
- **Behavior Variation**
- **Rules Variation**

Understanding these buckets is more valuable than memorizing dozens of design patterns.

Patterns are merely tools.

Buckets help us identify the problem.

## Bucket 1 – Data Variation

The algorithm stays the same.

Only the data changes.

**Examples:**

- CSV vs JSON vs XML reports
- Different configuration values
- Different document templates

**Typical solutions:**

- Parameters
- Configuration
- Templates
- Generics

When only data changes, introducing inheritance is usually unnecessary.

## Bucket 2 – Object Variation

The workflow remains the same.

The participating object changes.

**Examples:**

- MySQL vs MongoDB
- AWS S3 vs Azure Blob Storage
- Email vs SMS providers

**Typical solutions:**

- Composition
- Dependency Injection
- Interfaces

The goal is to replace the object without changing the workflow.

## Bucket 3 – Behavior Variation

The algorithm itself changes.

**Examples:**

- Encryption algorithms
- Pricing strategies
- Validation mechanisms

**Typical solutions:**

- Strategy Pattern
- Command Pattern
- State Pattern

This is where most GoF patterns live.

## Bucket 4 – Rules Variation

Business rules become increasingly complex.

**Examples:**

- Search filters
- Eligibility engines
- Discount calculations

**Typical solutions:**

- Specification Pattern
- Rule Engines
- Decision Tables

Rules variation is one of the most common sources of accidental complexity in enterprise systems.

## Why This Framework Matters

Many engineers jump directly from a code smell to a design pattern.

Architects take an intermediate step.

```
Problem
    ↓
Bucket
    ↓
Abstraction
    ↓
Pattern
```

This dramatically reduces unnecessary complexity.

## Key Takeaways

- Most design problems belong to one of four buckets.
- Buckets are more important than patterns.
- Patterns are responses to pressure.
- Correct classification often reveals the correct abstraction.
