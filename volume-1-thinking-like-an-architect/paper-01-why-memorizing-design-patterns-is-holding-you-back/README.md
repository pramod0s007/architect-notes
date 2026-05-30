# Why Memorizing Design Patterns Is Holding You Back

## The Architect's Guide to Recognizing Design Pressures

Most software engineers learn design patterns the wrong way.

They memorize names.

They memorize UML diagrams.

They memorize definitions.

They memorize interview answers.

Then they walk into a real codebase and fail to recognize when a pattern is actually needed.

The problem is simple:

**Patterns are solutions.**

**Architects focus on problems.**

If you start with solutions, every problem begins to look like a Strategy Pattern, Factory Pattern, or Visitor Pattern.

If you start with problems, the correct abstraction usually becomes obvious.

## The 500-Line Switch Statement

Imagine you inherit a service containing a massive switch statement.

```java
switch(type) {
    case AES:
        ...
    case DES:
        ...
    case BLOWFISH:
        ...
}
```

The code works.

Nobody complains.

Nobody talks about Strategy Pattern.

Then requirements arrive.

A new algorithm is added.

Then another.

Then another.

Suddenly every change requires modifying the same method.

Testing becomes harder.

Merge conflicts increase.

The code becomes fragile.

At this point many developers say:

> We should use Strategy Pattern.

An architect asks a different question:

> What pressure is the system experiencing?

The answer is:

- **Behavior Variation**
- **Frequent Change**

Strategy Pattern is simply one possible response to that pressure.

## The Memorization Trap

Most design pattern books teach:

```
Pattern
    ↓
Definition
    ↓
Example
```

Real systems evolve differently.

```
Problem
    ↓
Pressure
    ↓
Refactoring
    ↓
Abstraction
    ↓
Pattern
```

This difference is important.

When engineers memorize patterns, they search for opportunities to use them.

When architects understand pressures, they recognize when patterns naturally emerge.

## Design Pressure

A **design pressure** is a recurring force that pushes a codebase toward a new abstraction.

Common pressures include:

### Behavior Variation

Different implementations of the same operation.

**Examples:**

- Payment processing
- Encryption
- Notification delivery

**Often leads to:**

- Strategy
- Command
- Factory

### State Explosion

Behavior changes depending on state.

**Examples:**

- StopWatch
- Order Processing
- Workflow Engines

**Often leads to:**

- State Pattern
- State Machines
- Complex Rules

### Complex Rules

Business rules grow over time.

**Examples:**

- Eligibility checks
- Search filters
- Pricing engines

**Often leads to:**

- Specification Pattern
- Rule Engines

## Example 1 – Encryption

Consider the code example:

[01-encryption-if-else.java](../paper-01/code/01-encryption-if-else.java)

Every new encryption algorithm requires modifying existing code.

The problem is not the absence of Strategy Pattern.

The problem is that **behavior varies** while the caller remains the same.

Strategy Pattern becomes a natural solution because it isolates that variation.

## Example 2 – StopWatch

Consider the StopWatch example:

[02-stopwatch-state-explosion.java](../paper-01/code/02-stopwatch-state-explosion.java)

Initially the code contains a few state checks.

As more states are introduced:

- IDLE
- RUNNING
- SUSPENDED
- PAUSED
- STOPPED

conditionals spread throughout the system.

Developers see State Pattern.

Architects see **state explosion**.

The pattern is merely the response.

## Example 3 – Banking Undo

Consider:

[03-bank-undo-command.java](../paper-01/code/03-bank-undo-command.java)

Initially:

- `deposit()`
- `withdraw()`

appear simple.

Then requirements grow:

- Undo
- Audit
- Retry
- Scheduling

Operations become first-class objects.

Command Pattern emerges naturally.

Again:

**The pattern is not the cause.**

**The pressure is the cause.**

## Architect Notes

One of the biggest differences between senior engineers and architects is where they focus their attention.

Developers often focus on abstractions.

Architects focus on forces.

A pattern is valuable only because it relieves pressure.

If there is no pressure, there is usually no reason to introduce the pattern.

This explains why many enterprise systems become over-engineered.

Teams introduce abstractions before they are needed.

The result is unnecessary complexity.

Good architecture is not about adding patterns.

It is about introducing the **minimum abstraction** required to handle the current pressure.

## Interview Questions

1. What is design pressure?
2. Why do patterns emerge?
3. When does Strategy Pattern become necessary?
4. How can State Pattern reduce complexity?
5. What is behavior variation?
6. Can a design pattern become an anti-pattern?
7. What happens when abstractions are introduced too early?

## Key Takeaways

- Patterns are consequences.
- Pressures are causes.
- Architects do not start with patterns.
- Architects start with pressures.

The next time you encounter a design problem, resist the urge to ask:

> Which pattern should I use?

Instead ask:

> What pressure is this code experiencing?

The answer will often lead you to the correct abstraction.
