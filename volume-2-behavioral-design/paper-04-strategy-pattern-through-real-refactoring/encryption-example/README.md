# Encryption Example — Strategy Pattern

## What This Demonstrates

Strategy Pattern applied to encryption algorithms. The caller (`Encryptor`) stays
completely stable — it never changes regardless of how many encryption algorithms
exist. The algorithm is swapped by composing a different `EncryptionStrategy`
implementation at construction time.

**Pressure: Behavior Variation** — three encryption algorithms growing to many;
every new algorithm previously required modifying the same conditional method.

## Class Diagram

```
<<interface>>
EncryptionStrategy
+ encrypt(plainText: String): String
+ algorithmName(): String
        △
        |
   ─────────────────────────────
   |                |           |
AesEncryption   DesEncryption   BlowfishEncryption
(AES:...)       (DES:...)       (BLOWFISH:...)

Encryptor
- strategy: EncryptionStrategy
+ encrypt(plainText: String): String   → delegates to strategy.encrypt()
+ algorithm(): String                  → delegates to strategy.algorithmName()
```

## Sequence / Flow

```
Client
  │
  ├─ new Encryptor(new AesEncryption())
  │
  └─ encryptor.encrypt("architect-notes")
         │
         └─ strategy.encrypt("architect-notes")   [AesEncryption]
                │
                └─ returns "AES:architect-notes"
         │
         └─ returns "AES:architect-notes"
  │
  └─ [swap] new Encryptor(new DesEncryption())
         └─ strategy.encrypt("architect-notes")   [DesEncryption]
                └─ returns "DES:architect-notes"
```

## Design Decisions

- **Encryptor never changes** — adding AES-256, RSA, or ChaCha20 means one new
  class implementing `EncryptionStrategy`. Zero modifications to `Encryptor`.
- **`algorithmName()` on the interface** — `Encryptor.algorithm()` delegates to
  the strategy, so log/display code needs no `instanceof` checks.
- **Constructor-injection only** — `Encryptor` is `final` and immutable; if the
  algorithm must change, construct a new `Encryptor`. Keeps thread-safety simple.
- **Not real cryptography** — all implementations use a prefix stub (`AES:...`,
  `DES:...`) to keep the teaching signal clear without noise.

## How to Run

```bash
cd volume-2-behavioral-design/paper-04-strategy-pattern-through-real-refactoring/encryption-example
javac *.java && java Main
```

Expected output:

```
[AES] architect-notes -> AES:architect-notes
[DES] architect-notes -> DES:architect-notes
```

## When to Apply

- The same caller triggers different algorithms and the algorithm count is
  growing (three or more, or a clear growth signal from requirements).
- Algorithms need to be selected at runtime (e.g., per tenant, per config flag).

## When NOT to Apply

- Two stable algorithms with no growth signal — a simple `if-else` or ternary
  is easier to read and has no extra files. Apply Strategy when you see the
  third algorithm arriving or when runtime selection is required.
