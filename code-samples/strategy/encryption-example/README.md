# Encryption Strategy Example

Production-quality teaching sample for **Strategy Pattern** (Paper 04).

Contrasts with the if-else version in:

`volume-1-thinking-like-an-architect/paper-01/code/01-encryption-if-else.java`

## Run

```bash
cd code-samples/strategy/encryption-example
javac *.java
java Main
```

## Expected output

```text
[AES] architect-notes -> AES:architect-notes
[DES] architect-notes -> DES:architect-notes
```

## Design

- `EncryptionStrategy` — behavior contract
- `AesEncryption` / `DesEncryption` — interchangeable algorithms
- `Encryptor` — stable caller using composition
- `Main` — wiring and demo

Not real cryptography — intentional for teaching.
