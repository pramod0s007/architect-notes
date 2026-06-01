# Document Editor — Command Pattern with Undo/Redo

## What This Demonstrates

Insert, Delete, and Format text operations encapsulated as `Command` objects
with full undo and redo stacks managed by `EditorHistory`. `Document` is a pure
receiver — it carries out edits but knows nothing about history. Any new
operation type (e.g., `MoveTextCommand`) adds one class without touching
`EditorHistory`.

**Pressure: Behavior Encapsulation** — text editor operations must be
reversible, replayable, and extensible without the editor class accumulating
a growing switch/if-else over operation types.

## Class Diagram

```
<<interface>>
Command
+ execute(): void
+ undo(): void
+ describe(): String
        △
        |
   ──────────────────────────────────────────
   |                   |                    |
InsertTextCommand  DeleteTextCommand  FormatTextCommand
- doc: Document        - doc: Document      - doc: Document
- position: int        - position: int      - start/end: int
- text: String         - length: int        - format: String
                       - deleted: String    (captured on execute)

Document                                    [receiver]
- content: StringBuilder
- formatting: Map<String, String>
+ insert(position, text): void
+ delete(position, length): void
+ applyFormat(start, end, format): void
+ removeFormat(start, end): void

EditorHistory                               [invoker]
- undoStack: Deque<Command>
- redoStack: Deque<Command>
+ execute(command): void   → run + push undo; clear redo
+ undo(): void             → pop undo; push redo
+ redo(): void             → pop redo; push undo
+ canUndo() / canRedo() / undoDepth()
```

## Sequence / Flow

```
Client
  │
  ├─ history.execute(new InsertTextCommand(doc, 0, "Hello World"))
  │       └─ InsertTextCommand.execute() → doc.insert(0, "Hello World")
  │       └─ undoStack.push(cmd)
  │       └─ redoStack.clear()
  │
  ├─ history.execute(new DeleteTextCommand(doc, 5, 1))
  │       └─ DeleteTextCommand.execute()
  │               └─ deleted = doc snapshot at [5,6]  ← captured NOW
  │               └─ doc.delete(5, 1)
  │       └─ undoStack.push(cmd)
  │
  ├─ history.undo()
  │       └─ undoStack.pop() → DeleteTextCommand
  │       └─ DeleteTextCommand.undo()
  │               └─ doc.insert(5, deleted)            ← restores snapshot
  │       └─ redoStack.push(cmd)
  │
  └─ history.redo()
          └─ redoStack.pop() → DeleteTextCommand
          └─ DeleteTextCommand.execute()               ← re-applies
          └─ undoStack.push(cmd)
          └─ redoStack.clear()                         ← branching invalidates redo
```

## Design Decisions

- **Redo stack is cleared on any new `execute()`** — same behaviour as every
  real text editor (VS Code, Word, IntelliJ). Once you type after an undo,
  the branching edit makes the old redo history meaningless.
- **`DeleteTextCommand` captures the deleted text during `execute()`** — not
  at construction time. The document state at execute time is what matters for
  accurate undo; constructing the command before execution would snapshot the
  wrong content.
- **`Document` is a pure receiver** — it knows nothing about history, stacks,
  or undo. `EditorHistory` knows nothing about what commands do. Separation of
  concerns is complete.
- **`describe()` on the interface** — `EditorHistory` prints human-readable
  history entries without casting.

## How to Run

```bash
cd volume-2-behavioral-design/paper-06-command-pattern-through-banking-systems/document-editor
javac *.java && java Main
```

Expected output:

```
=== Editing session ===
  executed: Insert "Hello World" at position 0
  executed: Insert "," at position 5
  executed: Format [0-6] as bold
  executed: Delete "," at position 5

=== Undo last 2 operations ===
  undone:   Delete "," at position 5
  undone:   Format [0-6] as bold

=== Redo ===
  redone:   Format [0-6] as bold
  redone:   Delete "," at position 5

=== Undo all the way to empty ===
  [undo] nothing to undo
```

## When to Apply

- Editor-like tools where every user action must be reversible and replayable.
- Operation types are growing (bold, italic, resize, move) — each as its own
  class rather than a branch in the editor.

## When NOT to Apply

- Stateless transformations with no undo requirement — pipeline functions
  are simpler and have no stack overhead.
