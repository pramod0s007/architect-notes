# Command Pattern Examples

## Example 1 - Deposit

**Before**

```java
account.deposit(100);
```

**After**

```java
Command cmd = new DepositCommand(account, 100);
invoker.run(cmd);
```

## Example 2 - Withdraw

**Before**

```java
account.withdraw(50);
```

**After**

```java
Command cmd = new WithdrawCommand(account, 50);
invoker.run(cmd);
```

## Example 3 - Transfer

**Before**

```java
from.withdraw(amount);
to.deposit(amount);
```

**After**

```java
Command cmd = new TransferCommand(from, to, amount);
invoker.run(cmd);
```

## Example 4 - Undo

```java
invoker.undo();
```

## Example 5 - Retry / Scheduling

```java
scheduler.enqueue(new TransferCommand(from, to, amount));
```

## Architect Rule

Command Pattern becomes useful when operations must be:

- Stored
- Reversed
- Logged
- Deferred
- Composed
