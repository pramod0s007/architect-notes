# The Death of if-else

**Pattern:** Conditional Pressure Analysis

---

## Read the Full Article on Medium

_Article coming soon on Medium._

This repository focuses on runnable code examples. The white paper covers theory, war stories, and architectural reasoning in depth.

---
## What This Paper Is About

There is a recurring opinion in software engineering that the if-else statement is a code smell. Some code review checklists flag it automatically. Some engineers pride themselves on zero conditionals.

This paper argues that opinion is wrong — and shows why treating it as a rule produces worse code.

**if-else is not the problem. Uncontrolled growth is.**

## The Three Growth Pressures

### Pressure 1: Behavior Variation
```java
if (algorithm == AES) encryptAES(text);
if (algorithm == DES) encryptDES(text);
```
The algorithm itself changes. Every new algorithm adds a branch.
**Correct response:** Strategy Pattern.

### Pressure 2: State Variation
```java
if (state == IDLE) handleIdle();
if (state == RUNNING) handleRunning();
if (state == PAUSED) handlePaused();
```
Each new state multiplies across every method.
**Correct response:** State Pattern.

### Pressure 3: Rules Variation
```java
if (price > 1000) applyDiscount(order);
if (category == ELECTRONICS) applyTax(order);
if (customer.isPremium()) waiveShipping(order);
```
Rules compose with AND/OR and grow independently.
**Correct response:** Specification Pattern.

## When to Leave the if-else Alone

```java
// Stable two-branch flow control — correct as-is
if (user.isAuthenticated()) {
    renderDashboard();
} else {
    redirectToLogin();
}
```

```java
// Environment config — stable, never grows
return env.equals("production") ? PROD_URL : STAGING_URL;
```

These are not code smells. They are clear, correct, maintainable code. Adding Strategy Pattern here would make them worse.

**The diagnostic question is not "does an if-else exist?" It is "why is this conditional growing?"**

## Read the Full Article

{medium}

## Related Code Examples

- [`code-samples/strategy/encryption-example/`](../../code-samples/strategy/encryption-example/) — before: if-else for algorithms; after: Strategy Pattern
- [`code-samples/state/order-processing/`](../../code-samples/state/order-processing/) — before: scattered state checks; after: State Pattern
- [`code-samples/specification/loan-eligibility/`](../../code-samples/specification/loan-eligibility/) — before: nested rule conditions; after: Specification Pattern
