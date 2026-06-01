# Banking System — Command Pattern

## What This Demonstrates

Deposit and Withdraw operations encapsulated as `Command` objects with full
undo support. `Invoker` owns the history stack. `BankAccount` is a pure domain
object — it has no knowledge of undo, history, or the invoker.

**Pressure: Behavior Encapsulation** — operations needed to be reversible,
auditable, and potentially schedulable without mixing undo logic into the
account domain or the calling code.

## Class Diagram

```
<<interface>>
Command
+ execute(): void
+ undo(): void
+ name(): String
        △
        |
   ─────────────────────────────
   |                            |
DepositCommand          WithdrawCommand
- account: BankAccount          - account: BankAccount
- amount: double                - amount: double
+ execute() → account.deposit() + execute() → account.withdraw()
+ undo()    → account.withdraw()+ undo()    → account.deposit()

Invoker                                    [history owner]
- history: Deque<Command>
+ run(command: Command): void   → execute() then push to history
+ undo(): void                  → pop and call undo()
+ canUndo(): boolean

BankAccount                                [receiver]
- id: String
- balance: double
~ deposit(amount): void         (package-private)
~ withdraw(amount): void        (package-private)
+ balance(): double
```

## Sequence / Flow

```
Client
  │
  ├─ invoker.run(new DepositCommand(account, 50.0))
  │       └─ DepositCommand.execute()
  │               └─ account.deposit(50.0)       balance: 100 → 150
  │       └─ history.push(depositCmd)
  │
  ├─ invoker.run(new WithdrawCommand(account, 30.0))
  │       └─ WithdrawCommand.execute()
  │               └─ account.withdraw(30.0)      balance: 150 → 120
  │       └─ history.push(withdrawCmd)
  │
  ├─ invoker.undo()
  │       └─ history.pop() → WithdrawCommand
  │       └─ WithdrawCommand.undo()
  │               └─ account.deposit(30.0)       balance: 120 → 150 (restored)
  │
  └─ invoker.undo()
          └─ history.pop() → DepositCommand
          └─ DepositCommand.undo()
                  └─ account.withdraw(50.0)      balance: 150 → 100 (restored)
```

## Design Decisions

- **`BankAccount` has no knowledge of history or undo** — single responsibility.
  The account only knows how to credit and debit; the Invoker owns the history.
- **`deposit()` and `withdraw()` are package-private on `BankAccount`** —
  only command classes in the same package can call them directly. External
  callers must go through the Invoker, ensuring all operations are recorded.
- **Undo is symmetric** — `DepositCommand.undo()` calls `withdraw()`;
  `WithdrawCommand.undo()` calls `deposit()`. The command captures the amount
  at construction time, so undo always reverses the exact same amount.
- **`name()` on the interface** — supports audit trail display without
  downcast or `instanceof`.

## How to Run

```bash
cd volume-2-behavioral-design/paper-06-command-pattern-through-banking-systems/banking-example
javac *.java && java Main
```

Expected output:

```
[open]                  ACC-001 balance=100.00
[deposit +50]           ACC-001 balance=150.00
[withdraw -30]          ACC-001 balance=120.00
[undo (reverses withdraw)] ACC-001 balance=150.00
[undo (reverses deposit)]  ACC-001 balance=100.00
```

## When to Apply

- Operations must be undoable, auditable, or schedulable independently of
  the domain object that carries them out.
- The domain object (e.g., `BankAccount`) should not accumulate undo logic.

## When NOT to Apply

- One-shot fire-and-forget operations with no undo requirement — the extra
  classes add complexity with no benefit.
