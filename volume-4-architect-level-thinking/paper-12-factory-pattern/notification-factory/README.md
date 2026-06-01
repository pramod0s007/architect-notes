# Notification Factory — Factory Pattern

## What This Demonstrates

Factory Pattern applied to notification channel creation. `NotificationFactory`
maps a string channel token (`"email"`, `"sms"`) to the correct `Notification`
implementation. Callers depend only on the `Notification` interface and never
import `EmailNotification` or `SmsNotification`.

**Pressure: Object Creation Variation** — before the factory, `new EmailNotifier(smtpConfig)`
was scattered across 6 call sites in different services. When WhatsApp support was
added, 3 of those 6 sites were missed. Users who triggered those code paths
received no notification at all, and the bug only surfaced in production because
the sites were in low-traffic flows not covered by integration tests.

## Class Diagram

```
<<interface>>
Notification
+ send(to: String, message: String): void
        △
        |
   ─────────────────────────────
   |                            |
EmailNotification           SmsNotification
send() → [EMAIL] to: ...    send() → [SMS]  to: ...

NotificationFactory
────────────────────────────────────────────────
- CHANNELS: Map<String, Notification>  (static, immutable)
   "EMAIL" → new EmailNotification()
   "SMS"   → new SmsNotification()
────────────────────────────────────────────────
+ create(channel: String): Notification  [static]
   └─ normalises to uppercase
   └─ looks up in CHANNELS map
   └─ throws IllegalArgumentException for unknown channel
```

## Creation Flow

```
NotificationFactory.create("email")
   │
   ├─ channel.toUpperCase() → "EMAIL"
   ├─ CHANNELS.get("EMAIL") → EmailNotification instance
   └─ returns Notification (caller never sees EmailNotification)

NotificationFactory.create("sms")
   └─ returns SmsNotification as Notification

NotificationFactory.create("push")
   └─ throws IllegalArgumentException: "Unknown channel: push"

All callers:
   Notification n = NotificationFactory.create(channel);
   n.send(userId, message);   // same call site regardless of channel
```

## Design Decisions

- **Factory holds all channel instances in a static `Map`** — callers never need
  to know which config (SMTP settings, Twilio credentials, FCM keys) each
  provider requires. That wiring is a factory concern, not a caller concern. When
  a new channel is added, only the factory changes.
- **`CHANNELS` map is `Map.of(...)` — immutable at class load** — the factory
  cannot be mutated at runtime. Provider instances are singletons within the
  factory, which is safe here because `Notification` implementations are
  stateless.
- **`channel.toUpperCase(Locale.ROOT)` before map lookup** — callers can pass
  `"email"`, `"EMAIL"`, or `"Email"` and get the same result. Locale.ROOT
  prevents Turkish-locale surprises where `"I".toLowerCase()` is `"ı"` not `"i"`.
- **`IllegalArgumentException` for unknown channels** — not a silent null return.
  An unknown channel at call time is a programmer error (wrong config value or
  typo), not a recoverable runtime condition. Fail loudly with the channel name
  in the message.
- **`Notification` interface has a single `send(to, message)` method** — the
  factory is useful precisely because all channels share this contract. If
  channels had wildly different method signatures, a factory would not help.

## How to Run

```bash
cd volume-4-architect-level-thinking/paper-12-factory-pattern/notification-factory
javac *.java && java Main
```

Expected output:

```
[EMAIL] To: user@example.com | Message: Your order shipped
[SMS]   To: +1-555-0100      | Message: Your order shipped
```

To see the error path:

```java
NotificationFactory.create("push");
// IllegalArgumentException: Unknown channel: push
```

## When to Apply

- Object creation is conditional on a string, enum, or config token, and that
  condition appears in multiple places.
- Adding a new variant should require touching exactly one place (the factory),
  not hunting for every `new ConcreteType()` call.

## When NOT to Apply

- Only one implementation exists and no variation is expected — a direct `new`
  call is simpler.
- Each instantiation needs unique runtime state passed at creation time — a
  factory that accepts no parameters per call cannot carry that context; a
  factory method with parameters or a dependency injection container is more
  appropriate.
