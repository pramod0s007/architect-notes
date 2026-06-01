# Paper 17 — Decorator Pattern: Evolution Examples

**Domain:** File Reader

## Progression

| Version | File | State | Core Problem |
|---------|------|-------|--------------|
| v1 | `v1_InheritanceExplosion.java` | 8 classes for 3 capabilities | 2^N classes needed for N binary capabilities; adding Logging = 16 classes |
| v2 | `v2_DecoratorApplied.java` | 4 classes total | Any combination composed at runtime; adding Logging = 1 class |

## The Math

Three binary capabilities (Buffering, Compression, Encryption):

| Approach | Classes | Adding 4th capability |
|----------|---------|----------------------|
| Inheritance | 2^3 = 8 | 2^4 = 16 (doubles!) |
| Decorator | N + 1 = 4 | N + 2 = 5 (linear) |

## Why This Domain

File reading is the classic Decorator use case: stream processing pipelines
layer capabilities (buffering, compression, encryption) in arbitrary order.
Each capability is genuinely independent and genuinely composable.

## How to Run

```bash
cd volume-5-structural-patterns/paper-17-decorator-pattern/evolution

javac -d out v1_InheritanceExplosion.java v2_DecoratorApplied.java

java -cp out evolution.v1_InheritanceExplosion
java -cp out evolution.v2_DecoratorApplied
```

## Composition Order Matters

When decorators are stacked, order determines processing sequence:

```java
// Read order: decrypt first, then decompress, then buffer I/O
new BufferingDecorator(
    new CompressionDecorator(
        new EncryptionDecorator(
            new FileDataReader(path))))
```

The innermost decorator is applied first on read. When writing (the inverse),
the order would be reversed: buffer → compress → encrypt → write.

## When Decorator Is Not the Answer

Decorator is right when:
- Capabilities are genuinely independent
- Any combination should be valid
- The number of combinations is large (3+ capabilities)

Decorator is over-engineering when:
- Only 1-2 fixed combinations ever exist in practice
- The capabilities have dependencies (compress before encrypt is mandatory)
- A fixed pipeline (chain of responsibility) is clearer
