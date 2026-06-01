# State Pattern Through a StopWatch

**Pattern:** State Pattern

---

## Read the Full Article on Medium

_Article coming soon on Medium._

This repository focuses on runnable code examples. The white paper covers theory, war stories, and architectural reasoning in depth.

---
## What This Paper Is About

State Pattern addresses **state explosion** — when a system's behavior depends on its current mode, and adding new modes requires editing every method in the class rather than adding one new class.

This is not about the number of if-statements. It is about the **cost of adding a new state**.

## The Pressure: State Explosion

Consider a subscription service that started with 2 states (ACTIVE, CANCELLED) and grew to 9 states over 18 months. The `processFeatureRequest()` method was 60 lines. The `processPaymentEvent()` method was 80 lines. Adding one new state took an estimated 2 weeks.

**That is the signal.** When a new state forces you to audit every method, state logic has no home.

## The Pattern

Each state becomes a class that owns its valid transitions. Invalid transitions throw `IllegalStateException` cleanly from within the state.

```
StopWatch delegates to active state:
  IDLE.start()    → transitions to RUNNING
  RUNNING.pause() → transitions to PAUSED
  PAUSED.resume() → transitions back to RUNNING
  IDLE.pause()    → throws IllegalStateException
```

The StopWatch itself has zero conditional logic. It delegates everything.

## Pros

- Adding a new state = one new class; zero changes to existing states or the context
- Invalid transition logic lives in one place (the state), not scattered across methods
- States are independently testable
- Transition rules are explicit and visible

## Cons

- Increases class count proportionally to state count
- For 2–3 stable states, an enum + switch or simple boolean flags are cleaner
- State transitions can become hard to trace when states hold references back to context

## When NOT to Use

- Binary state (active/inactive) → boolean field is cleaner
- 2–3 states that have been stable for a year → conditional is correct
- State machine with complex guard conditions → dedicated state machine library (Spring Statemachine, Stateless4j) may be more appropriate

## Read the Full Article


## Code Examples in This Repo

| Example | Domain | What It Shows |
|---------|--------|--------------|
| [`state/stopwatch-example/`](./stopwatch-example/) | Time tracking | IDLE→RUNNING→PAUSED transitions, invalid transition rejection |
| [`state/order-processing/`](./order-processing/) | E-commerce | PENDING→CONFIRMED→SHIPPED→DELIVERED, CANCELLED, REFUNDED — full lifecycle |

### How to Run

```bash
cd code-samples/state/stopwatch-example
javac *.java && java Main

cd code-samples/state/order-processing
javac *.java && java Main
```
