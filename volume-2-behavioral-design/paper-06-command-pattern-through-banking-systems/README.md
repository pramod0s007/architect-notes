# Command Pattern Through Banking Systems

## Why Operations Become Objects

Many systems start with simple operations.

```java
void deposit(double amount) { }
void withdraw(double amount) { }
```

The caller and the behavior seem fixed.

Then requirements grow:

- Undo
- Retry
- Audit trails
- Scheduling
- Transaction history

Operations are no longer just methods. They become **first-class behaviors** that must be stored, replayed, and composed.

## The Real Problem

Many developers think:

> "We need a history stack."

The real problem is:

**Behavior Encapsulation.**

Each operation is a unit of work that may need to be invoked, reversed, logged, or deferred independently of the object that triggered it.

## Original Design

```java
class BankAccount {

    void deposit(double amount) {
        balance += amount;
    }

    void withdraw(double amount) {
        balance -= amount;
    }
}
```

Simple and correct.

No pattern required.

## The First Sign Of Pressure

Undo arrives.

Then audit.

Then scheduled transfers.

Every new capability modifies how operations are invoked and tracked.

The system is experiencing **behavior encapsulation** pressure.

## Refactoring Step 1

Encapsulate each operation.

```java
interface Command {
    void execute();
    void undo();
}
```

## Refactoring Step 2

Concrete commands.

```java
class DepositCommand implements Command { }
class WithdrawCommand implements Command { }
class TransferCommand implements Command { }
```

## Refactoring Step 3

Invoker executes and stores history.

```java
class CommandInvoker {
    void run(Command command) {
        command.execute();
        history.push(command);
    }

    void undo() {
        history.pop().undo();
    }
}
```

## What Actually Changed?

Many developers answer:

> "We implemented Command Pattern."

Architects answer:

> "We encapsulated behavior as objects."

## Design Pressure

```
Behavior Encapsulation
        ↓
Refactoring
        ↓
Command Objects
        ↓
Invoker + History
        ↓
Command Pattern
```

## Key Takeaways

- Command Pattern responds to behavior encapsulation.
- Undo is a symptom; encapsulated operations are the abstraction.
- The invoker separates *when* behavior runs from *what* behavior runs.
- Growing operation requirements are design pressure, not a pattern checklist.
