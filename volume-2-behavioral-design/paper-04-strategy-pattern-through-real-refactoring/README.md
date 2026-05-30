# Strategy Pattern Through Real Refactoring

## The Problem With Memorizing Strategy Pattern

Most engineers learn Strategy Pattern like this:

**Definition**

> "Encapsulate interchangeable algorithms behind a common interface."

Then they see:

```java
interface Strategy {
    void execute();
}
```

Unfortunately, this teaches the solution before the problem.

A better question is:

> Why does Strategy Pattern emerge?

## The Original Design

Imagine an encryption service.

```java
class Encryptor {

    String encrypt(String type, String text) {

        if(type.equals("AES"))
            return encryptAES(text);

        if(type.equals("DES"))
            return encryptDES(text);

        if(type.equals("BLOWFISH"))
            return encryptBlowfish(text);

        throw new IllegalArgumentException();
    }
}
```

The code works.

No pattern is required.

## The First Sign Of Pressure

A new encryption algorithm arrives.

Then another.

Then another.

Every new algorithm requires modifying the same method.

The system is experiencing:

**Behavior Variation.**

This is the real problem.

Not the if-statement.

## Refactoring Step 1

Extract an interface.

```java
interface EncryptionStrategy {
    String encrypt(String text);
}
```

## Refactoring Step 2

Create independent implementations.

```java
class AesEncryption implements EncryptionStrategy {}
class DesEncryption implements EncryptionStrategy {}
class BlowfishEncryption implements EncryptionStrategy {}
```

## Refactoring Step 3

Use composition.

```java
class Encryptor {

    private final EncryptionStrategy strategy;

    Encryptor(EncryptionStrategy strategy) {
        this.strategy = strategy;
    }

    String encrypt(String text) {
        return strategy.encrypt(text);
    }
}
```

## What Actually Changed?

Many developers answer:

> "We implemented Strategy Pattern."

Architects answer:

> "We isolated behavior variation."

That distinction matters.

## Design Pressure

```
Behavior Variation
        ↓
Refactoring
        ↓
Interface Extraction
        ↓
Composition
        ↓
Strategy Pattern
```

## Key Takeaways

- Strategy Pattern solves behavior variation.
- The pattern is not the goal.
- The abstraction is introduced because pressure exists.
- Composition replaces conditional growth.

## Runnable Example

See:

code-samples/strategy/encryption-example
