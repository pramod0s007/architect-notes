# Decorator Pattern — Message Sender

Demonstrates combinatorial inheritance explosion: four capabilities (compression, encryption,
logging, plain send) need to be stackable in any combination without 2^4 = 16 subclasses.

## Pressure
Multiple orthogonal capabilities need to be applied in varying combinations at runtime.

## Run
```bash
javac decorator/messagesender/*.java && java decorator.messagesender.Main
```

## Key Point
Outermost decorator runs first. Order matters.
LoggedMessageSender → EncryptedMessageSender → CompressedMessageSender → PlainMessageSender
