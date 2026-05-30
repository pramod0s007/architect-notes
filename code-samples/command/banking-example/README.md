# Banking Command Pattern Example

Runnable sample for **Command Pattern** (Paper 06).

Contrasts with if-else undo logic in:

`volume-1-thinking-like-an-architect/paper-01/code/03-bank-undo-command.java`

## Run

```bash
cd code-samples/command/banking-example
javac *.java
java Main
```

## Features

- `deposit` / `withdraw` as command objects
- `Invoker` with history
- `undo()` reverses the last command
