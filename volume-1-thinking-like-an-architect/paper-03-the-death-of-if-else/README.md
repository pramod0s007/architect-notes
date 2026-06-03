# The Death of if-else

**Pattern:** Conditional Pressure Analysis

---

## Read the Full Article on Medium

[The Death of if-else](https://medium.com/@replytopramods.aws/the-death-of-if-else-8044410a1ef8)

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


## Related Code Examples

- [encryption-example](../../volume-2-behavioral-design/paper-04-strategy-pattern-through-real-refactoring/encryption-example/) — before: if-else for algorithms; after: Strategy Pattern
- [order-processing](../../volume-2-behavioral-design/paper-05-state-pattern-through-a-stopwatch/order-processing/) — before: scattered state checks; after: State Pattern
- [loan-eligibility](../../volume-3-enterprise-patterns/paper-09-specification-pattern/loan-eligibility/) — before: nested rule conditions; after: Specification Pattern
