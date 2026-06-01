# Command Pattern Through Banking Systems

**Pattern:** Command Pattern

---

## Read the Full Article on Medium

_Article coming soon on Medium._

This repository focuses on runnable code examples. The white paper covers theory, war stories, and architectural reasoning in depth.

---
## What This Paper Is About

Command Pattern is commonly taught with "undo" as the primary use case. That framing undersells it. The real pressure is **behavior encapsulation**: when operations need to be treated as first-class objects that can travel — be stored, reversed, scheduled, logged, or replayed — independently of the code that created them.

## The Pressure: Behavior Encapsulation

A wallet service receiving a regulatory requirement: *every financial operation must be reversible, auditable, and replayable within 90 days.* A simple `credit()` method cannot satisfy that. An encapsulated `CreditCommand` object can.

**Signals that Command Pattern pressure exists:**
- Operations need to be logged or audited independently
- Undo is a first-class requirement (not an afterthought)
- Operations need to be scheduled, queued, or deferred
- The same operation needs to run in multiple contexts (immediate, batch, retry)

## The Pattern

```
Client → Invoker.run(new DepositCommand(account, 100))
Invoker stores command in history stack
Invoker.undoLast() → history.pop().undo()
```

The `BankAccount` never changes. The `Invoker` never knows the specifics. Each `Command` owns its own execute and undo logic.

## Modern Forms

| What it's called | Platform | What it is |
|-----------------|----------|------------|
| CQRS Command | Axon, MediatR, custom | Command Pattern |
| Job / Task | SQS consumer, Celery, Quartz | Command + Scheduler |
| Saga step | Temporal, Conductor | Compensating Command |
| Undo action | Any collaborative editor | Command + history stack |

## Pros

- Undo/redo out of the box
- Operations are independently serializable and schedulable
- Invoker decoupled from operation logic
- Composite commands (Transfer = Debit + Credit) for atomicity

## Cons

- More boilerplate than direct method calls
- Undo logic can be subtle for complex operations (partial failures in composite commands)
- If operations don't need to travel, direct calls are simpler

## When NOT to Use

- Simple CRUD with no undo/audit/scheduling requirements → direct service call is cleaner
- When undo is the only requirement and it's trivially reversible → event log may be sufficient

## Read the Full Article

{medium}

## Code Examples in This Repo

| Example | Domain | What It Shows |
|---------|--------|--------------|
| [`command/banking-example/`](../../code-samples/command/banking-example/) | Finance | Deposit, Withdraw, Transfer with undo stack |
| [`command/document-editor/`](../../code-samples/command/document-editor/) | Productivity | Insert, Delete, Format text with full undo/redo |
| [`command/job-scheduler/`](../../code-samples/command/job-scheduler/) | Infrastructure | Email, Report, DataSync jobs — scheduled, deferred, retriable |

### How to Run

```bash
cd code-samples/command/banking-example
javac *.java && java Main

cd code-samples/command/document-editor
javac *.java && java Main

cd code-samples/command/job-scheduler
javac *.java && java Main
```
