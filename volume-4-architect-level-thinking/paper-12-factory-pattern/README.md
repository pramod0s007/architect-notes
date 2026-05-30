# Factory Pattern

## Why `new` Scatters Creation Logic

Call sites that choose implementations with `if` and `new` couple business code to concrete types.

```java
if (channel.equals("email"))
    notifier = new EmailNotifier();
else if (channel.equals("sms"))
    notifier = new SmsNotifier();
```

Adding a channel edits every creation point.

## The Real Problem

**Object creation variation.**

The pressure is centralizing **which** implementation to instantiate without spreading `new` across the system.

## Factory Thinking

```java
Notifier notifier = NotificationFactory.create(channel);
notifier.send(message);
```

Creation moves behind a factory.

Callers depend on abstractions.

## Real Examples (This Series)

| Factory | Variation |
|---------|-----------|
| `NotificationFactory` | Email, SMS, push |
| `StorageFactory` | S3, Azure Blob, local disk |
| `PaymentFactory` | Card, UPI, wallet |

## Design Pressure

```text
Object Creation Variation
        ↓
Centralized Creation
        ↓
Factory
```

## Key Takeaways

- Factory isolates **which concrete type** to build.
- Distinct from Builder (how to assemble one complex object) and Strategy (how it behaves after creation).
- Prefer simple factory methods before Abstract Factory hierarchies.
- Watch for Factory Hell (Paper 13) when every type gets its own factory interface.

## Runnable Example

See:

code-samples/factory/notification-factory
