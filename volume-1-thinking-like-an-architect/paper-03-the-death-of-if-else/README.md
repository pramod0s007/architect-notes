# The Death of if-else

## Why Conditionals Keep Growing

Every software engineer has seen code like this:

```java
if(type == AES)
   ...

else if(type == DES)
   ...

else if(type == BLOWFISH)
   ...
```

The code works.

The problem is not correctness.

The problem is **growth**.

Every new requirement requires modifying existing code.

Over time:

- Methods become larger
- Testing becomes harder
- Merge conflicts increase
- Complexity grows

This is one of the most common architectural pressures in software systems.

## The Real Problem

Many developers think:

> "The problem is the if-else."

That is incorrect.

The real problem is usually one of:

- **Behavior Variation**
- **State Variation**
- **Rules Variation**

The if-else is merely a symptom.

Removing the symptom without understanding the pressure often creates worse designs.

## Example 1 - Behavior Variation

```java
if(encryption == AES)
   ...

if(encryption == DES)
   ...

if(encryption == BLOWFISH)
   ...
```

**Pressure:** Behavior Variation

**Typical Solution:** Strategy Pattern

## Example 2 - State Variation

```java
if(state == IDLE)
   ...

if(state == RUNNING)
   ...

if(state == SUSPENDED)
   ...
```

**Pressure:** State Explosion

**Typical Solution:** State Pattern

## Example 3 - Rules Variation

```java
if(price > 1000)
   ...

if(category == ELECTRONICS)
   ...

if(rating > 4)
   ...
```

**Pressure:** Rules Variation

**Typical Solution:** Specification Pattern

## The Architect's View

Architects rarely ask:

> "How do I remove this if statement?"

Instead they ask:

> "Why is this conditional growing?"

The answer usually reveals the pressure.

The pressure usually reveals the abstraction.

The abstraction usually reveals the pattern.

## Key Takeaways

- if-else is not the enemy.
- Growth is the enemy.
- Conditionals are symptoms.
- Design pressure is the root cause.
- Correct classification reveals the correct abstraction.
