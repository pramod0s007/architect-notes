# Command Pattern Through Banking Systems

*Most tutorials explain Command Pattern with a remote control. Here's why that example teaches you almost nothing about when to actually use it.*

---

A remote control is a bad example for Command Pattern.

Not because it's technically wrong — it's technically fine. But it teaches you the mechanism without teaching you the pressure.

"Define an interface with execute() and undo(), create concrete commands, wire them to an invoker." This is the remote control lesson. You learn the structure. You don't learn why you'd ever build it.

Here's a story that looks like real software.

---

About two years ago, a wallet service I worked with received a regulatory requirement: every financial operation must be reversible, auditable, and replayable within 90 days for dispute resolution.

The service was simple — credit and debit operations on a user's wallet balance. The code was clean:

```java
class WalletService {
    void credit(String userId, BigDecimal amount) {
        Wallet wallet = walletRepository.findById(userId);
        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);
    }

    void debit(String userId, BigDecimal amount) {
        Wallet wallet = walletRepository.findById(userId);
        if (wallet.getBalance().compareTo(amount) < 0)
            throw new InsufficientFundsException();
        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);
    }
}
```

Forty lines. Two methods. Works perfectly.

Then the regulatory requirement arrived. And then the product team asked for bulk refunds. And then the fraud team needed operation replay for investigation. And then support needed a "reverse last N transactions" feature for customer recovery.

Each new requirement changed how `credit()` and `debit()` were called. They needed to be logged, tracked, reversible, schedulable. The methods themselves couldn't hold all of that.

**Operations had outgrown being method calls.** They needed to become objects — things that could be stored, passed around, reversed, and replayed independently of the service that created them.

That's Command Pattern pressure. Not undo. Not remote controls. Operations needing to travel.

---

## Start Simple — As You Always Should

A banking service begins with two operations:

```java
class BankAccount {

    private double balance;

    void deposit(double amount) {
        balance += amount;
    }

    void withdraw(double amount) {
        if (amount > balance) throw new InsufficientFundsException();
        balance -= amount;
    }

    double getBalance() { return balance; }
}
```

This is correct code. Clean, testable, obvious.

**Don't touch it.** No Command Pattern. No abstraction. No interface. It doesn't need any of that.

This is the starting point, not the problem. The problem comes later.

---

## The First Sign of Pressure

Product requirements arrive in waves.

**Wave 1:** "We need undo for the last transaction."

The deposit and withdrawal methods don't know how to reverse themselves. You add state tracking:

```java
void deposit(double amount) {
    lastOperation = "deposit";
    lastAmount = amount;
    balance += amount;
}

void undo() {
    if ("deposit".equals(lastOperation)) balance -= lastAmount;
    if ("withdraw".equals(lastOperation)) balance += lastAmount;
}
```

Fragile. One undo. No history. But it works for now.

**Wave 2:** "We need a full transaction history for audit compliance."

Now `BankAccount` needs to store every operation it's ever executed. The class that was clean five minutes ago is accumulating responsibilities it shouldn't own.

**Wave 3:** "Transfers need to be scheduled — execute tomorrow at 9am."

Transfers are composite: withdraw from one account, deposit to another. They need to be serializable, deferrable, and retryable.

**Wave 4:** "Regulatory requires all operations to be replayable for dispute resolution."

---

At this point the pressure is clear.

**Operations are no longer just method calls.**

They need to be:
- Stored
- Reversed
- Logged
- Deferred
- Composed
- Replayed

This is **behavior encapsulation** pressure. Operations need to become first-class objects that can travel independently of the objects that created them.

Command Pattern is the response to this pressure. Not to Wave 1. To the full picture.

---

## The Refactoring

### Step 1: Define what an operation looks like

```java
interface Command {
    void execute();
    void undo();
}
```

Two responsibilities: do the thing, and undo the thing. That's the contract.

### Step 2: Encapsulate each operation

```java
class DepositCommand implements Command {

    private final BankAccount account;
    private final double amount;

    DepositCommand(BankAccount account, double amount) {
        this.account = account;
        this.amount = amount;
    }

    @Override
    public void execute() {
        account.credit(amount);
    }

    @Override
    public void undo() {
        account.debit(amount);
    }
}

class WithdrawCommand implements Command {

    private final BankAccount account;
    private final double amount;

    @Override
    public void execute() {
        account.debit(amount);
    }

    @Override
    public void undo() {
        account.credit(amount);
    }
}

class TransferCommand implements Command {

    private final BankAccount from;
    private final BankAccount to;
    private final double amount;

    @Override
    public void execute() {
        from.debit(amount);
        to.credit(amount);
    }

    @Override
    public void undo() {
        to.debit(amount);
        from.credit(amount);
    }
}
```

Each command owns its own reversal logic. `BankAccount` doesn't need to know anything about history.

### Step 3: An invoker owns execution and history

```java
class CommandInvoker {

    private final Deque<Command> history = new ArrayDeque<>();

    void run(Command command) {
        command.execute();
        history.push(command);
    }

    void undoLast() {
        if (!history.isEmpty()) {
            history.pop().undo();
        }
    }

    List<Command> getHistory() {
        return Collections.unmodifiableList(new ArrayList<>(history));
    }
}
```

The invoker owns *when* to execute, *whether* to undo, and *what* the history looks like. `BankAccount` owns none of this.

### Step 4: Scheduling becomes natural

```java
class Scheduler {
    void enqueue(Command command, Instant executeAt) {
        // Store the command, execute at the right time
        queue.add(new ScheduledCommand(command, executeAt));
    }
}
```

A `TransferCommand` is now a value. It can be serialized to a queue, pulled at 9am, executed, logged. `BankAccount` didn't change at all.

---

## What Actually Changed

The caller used to be:

```java
account.deposit(100);
account.withdraw(50);
```

The caller is now:

```java
invoker.run(new DepositCommand(account, 100));
invoker.run(new WithdrawCommand(account, 50));
```

The `BankAccount` didn't change. The encapsulation moved from "operation as method call" to "operation as object."

That object can now travel. It can be stored, queued, reversed, audited, replayed, retried.

**This is the point.** Command Pattern isn't about undo. Undo is one use case. The deeper value is treating operations as first-class objects that have a lifecycle independent of who invoked them.

---

## Command in 2026 Production Systems

Command Pattern has never been more relevant — but it often appears under different names:

| What it's called | Where it appears | What it is |
|-----------------|-----------------|------------|
| CQRS Command | Command bus, MediatR, Axon | Command Pattern |
| Job / Task | Queue-based workers, Celery, Bull, SQS consumer | Command + Scheduler |
| Event Sourcing event | Kafka, EventStore | Commands recorded as immutable log |
| Saga step | Temporal, Conductor workflow | Compensating Command (the undo) |
| Undo action | Figma, Google Docs, any collaborative editor | Classic Command + history stack |

The name changes. The pressure is the same: operations need to be encapsulated so they can travel, be replayed, reversed, or deferred independently of who triggered them.

---

## The Interview Answer

**Question:** When should Command Pattern be used?

**Weak answer:** *"Whenever you need undo."*

**Strong answer:**

*"Command Pattern addresses behavior encapsulation — when operations must be treated as first-class objects that can be executed, stored, reversed, scheduled, or replayed independently of the calling code. Undo is one consequence. Audit trails, scheduling, retry logic, and event sourcing are others. The signal is that operations outgrow being simple method calls — they need to travel, be logged, or be undone. The pattern separates when behavior runs (invoker) from what behavior runs (command), which is where the flexibility comes from."*

---

## Composite Commands — Transactions Across Accounts

Transfer operations reveal Command Pattern's composition capability. A transfer is not one command — it's two commands that must succeed or fail atomically.

```java
class TransferCommand implements Command {

    private final Command debit;
    private final Command credit;
    private boolean debitExecuted = false;

    TransferCommand(BankAccount from, BankAccount to, double amount) {
        this.debit  = new DebitCommand(from, amount);
        this.credit = new CreditCommand(to, amount);
    }

    @Override
    public void execute() {
        debit.execute();
        debitExecuted = true;
        try {
            credit.execute();
        } catch (Exception e) {
            // Compensate: undo the debit if credit fails
            debit.undo();
            debitExecuted = false;
            throw e;
        }
    }

    @Override
    public void undo() {
        if (debitExecuted) {
            credit.undo();
            debit.undo();
        }
    }
}
```

This is the foundation of **saga pattern** in distributed systems. Each step in a long-running transaction is a command. If a later step fails, previous commands are undone via their compensation operations.

The wallet service that opened this paper — once it needed regulatory reversibility across multi-step operations — was building a saga. Command Pattern was the right foundation, not because of undo alone, but because encapsulated operations with compensation logic are what make distributed consistency tractable.

## The Scheduler — Deferred Execution

Once operations are objects, deferred execution is natural:

```java
class ScheduledCommand {
    private final Command command;
    private final Instant executeAt;

    void executeIfReady() {
        if (Instant.now().isAfter(executeAt)) {
            command.execute();
        }
    }
}

// Usage: scheduled transfer for next business day
invoker.schedule(
    new TransferCommand(savingsAccount, checkingAccount, 500.00),
    nextBusinessDay()
);
```

This is exactly how recurring payment systems, scheduled transfers, and deferred billing work — a queue of Command objects with execution timestamps, processed by a scheduler that calls `execute()` when the time arrives.

---

## Key Takeaways

- Command Pattern solves **behavior encapsulation**, not just undo.
- Operations become first-class objects when they need to travel — be stored, reversed, or deferred.
- The invoker controls *when* and *whether*. The command controls *what*.
- In 2026 this appears as CQRS commands, job queues, saga steps, and event sourcing.
- Don't introduce it for simple deposit/withdraw with no history requirements. Introduce it when operations outgrow being method calls.

---

*All papers and runnable Java samples: [github.com/pramod0s007/architect-notes](https://github.com/pramod0s007/architect-notes)*

*Previous → Paper 05: State Pattern Through a StopWatch | Next → Paper 07: Visitor Pattern Without UML*
