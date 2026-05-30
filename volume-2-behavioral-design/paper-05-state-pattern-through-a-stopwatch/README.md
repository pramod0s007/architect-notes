# State Pattern Through a StopWatch

## Why State Logic Becomes Difficult

Many systems behave differently depending on their current state.

**Examples:**

- StopWatch
- Order Processing
- Payment Workflow
- Document Approval

Initially the logic seems simple.

```java
if(state == IDLE)
    ...

if(state == RUNNING)
    ...
```

Then new states arrive.

- IDLE
- RUNNING
- PAUSED
- SUSPENDED
- STOPPED

Complexity begins to grow.

## The Real Problem

Many developers think:

> "The problem is too many if-statements."

The real problem is:

**State Explosion.**

Behavior changes based on state.

As states increase, transitions become harder to manage.

## Original Design

```java
class StopWatch {

    State state;

    void start() { }

    void stop() { }

    void pause() { }

    void resume() { }
}
```

Soon every operation contains conditional logic.

## Refactoring Step 1

Extract state-specific behavior.

```java
interface WatchState {

    void start();

    void stop();

    void pause();

    void resume();
}
```

## Refactoring Step 2

Create state implementations.

```java
class IdleState implements WatchState {}
class RunningState implements WatchState {}
class PausedState implements WatchState {}
```

## Refactoring Step 3

Delegate behavior to the active state.

```java
class StopWatch {

    private WatchState state;

    void start() {
        state.start();
    }
}
```

## What Actually Changed?

Many developers answer:

> "We implemented State Pattern."

Architects answer:

> "We isolated state-specific behavior."

The pattern is the consequence.

State Explosion is the cause.

## Design Pressure

```
State Explosion
        ↓
Refactoring
        ↓
State Isolation
        ↓
Delegation
        ↓
State Pattern
```

## Key Takeaways

- State Pattern solves state explosion.
- The pattern is not the goal.
- State-specific behavior should be isolated.
- Growing state transitions are a design pressure.
