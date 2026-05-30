# Command Evolution

```
Behavior Encapsulation
        |
        v

Simple Methods

        |
        v

Undo / Audit / Scheduling

        |
        v

Command Interface

        |
        v

Invoker + History

        |
        v

Command Pattern
```

## Examples

```
Deposit / Withdraw
    ↓
DepositCommand / WithdrawCommand
```

```
Transfer
    ↓
TransferCommand
```

```
Undo
    ↓
command.undo()
```
