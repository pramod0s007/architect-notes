# Factory Pattern

*`new` is honest. It tells you exactly what gets created. Factory Pattern is for when that honesty creates too much coupling.*

---

A platform notification service I worked on started with two delivery channels: email and SMS.

The creation code was scattered — every component that needed to send a notification had its own version:

```java
// In OrderService
if (user.getPreference().equals("email"))
    new EmailNotifier(smtpConfig).send(user, message);
else
    new SmsNotifier(twilioConfig).send(user, message);

// In AlertService — same pattern, copied
if (channel.equals("email"))
    new EmailNotifier(smtpConfig).notify(recipient, alert);
else
    new SmsNotifier(twilioConfig).notify(recipient, alert);

// In BillingService — same again
// ...4 more places
```

When product added push notifications, the engineering ticket said: "Add push notifications to the notification service."

What it should have said: "Find all six places where notification creation is scattered and add a third case to each of them."

Three of the six places were updated. Three weren't. Push notifications worked in order confirmation and billing, but not in alerts or support messages. The support team spent a week investigating why their users weren't receiving push alerts before someone noticed the creation code in `AlertService` hadn't been updated.

**That's the pressure Factory Pattern solves. Not "we should centralize object creation because it's cleaner." Because scattered creation means missed updates.**

Here is honest code:

```java
Notifier notifier;
if (channel.equals("email")) {
    notifier = new EmailNotifier(smtpConfig);
} else if (channel.equals("sms")) {
    notifier = new SmsNotifier(twilioConfig);
} else if (channel.equals("push")) {
    notifier = new PushNotifier(fcmConfig);
} else {
    throw new IllegalArgumentException("Unknown channel: " + channel);
}
notifier.send(message);
```

This works. Every caller knows exactly what gets created. `new` is right there.

The problem: this block exists in twelve places.

Product adds WhatsApp as a channel. You find eleven of the twelve. Eleven callers get the update. One doesn't. Production throws `NullPointerException` three days after the release.

**This is object creation variation pressure.** Creation logic scattered across callers, tightly coupled to every concrete type.

Factory Pattern centralizes creation. Callers depend on the abstraction, not the construction.

---

## The Refactoring

Move creation behind a factory:

```java
class NotificationFactory {

    private final SmtpConfig smtpConfig;
    private final TwilioConfig twilioConfig;
    private final FcmConfig fcmConfig;

    NotificationFactory(SmtpConfig smtp, TwilioConfig twilio, FcmConfig fcm) {
        this.smtpConfig = smtp;
        this.twilioConfig = twilio;
        this.fcmConfig = fcm;
    }

    Notifier create(String channel) {
        return switch (channel) {
            case "email" -> new EmailNotifier(smtpConfig);
            case "sms"   -> new SmsNotifier(twilioConfig);
            case "push"  -> new PushNotifier(fcmConfig);
            default      -> throw new IllegalArgumentException("Unknown channel: " + channel);
        };
    }
}
```

Every caller becomes:

```java
Notifier notifier = factory.create(channel);
notifier.send(message);
```

Adding WhatsApp: one change, one file, propagates everywhere.

---

## What Factory Buys You

**Single update point.** New channel = one new case in one factory. All callers update automatically.

**Caller decoupling.** Callers don't import `EmailNotifier`, `SmsNotifier`, `PushNotifier`. They import `Notifier` (the interface) and `NotificationFactory`. Adding a new implementation doesn't touch callers.

**Configuration centralization.** The factory holds `smtpConfig`, `twilioConfig`, `fcmConfig`. Callers don't need to know how each notifier is wired — they just call `create()`.

**Testability.** Inject a `MockNotificationFactory` in tests. Return `FakeEmailNotifier`. No SMTP server required.

---

## Factory Method vs Factory Class vs Abstract Factory

These three are often confused. They address the same pressure at different scales.

### Factory Method

A method on a class that creates and returns an object. The simplest form:

```java
class OrderService {
    private Notifier createNotifier(String channel) {
        return switch (channel) {
            case "email" -> new EmailNotifier();
            case "sms"   -> new SmsNotifier();
            default      -> throw new IllegalArgumentException(channel);
        };
    }
}
```

Use this when creation is needed in one place and the logic is simple.

### Factory Class (Simple Factory)

A dedicated class whose responsibility is creation. The version shown above. Use this when creation logic is reused across multiple callers or when the factory needs its own configuration.

### Abstract Factory

A family of factories — a factory that creates factories. Used when entire sets of related objects must be swapped as a unit:

```java
interface NotificationFactory {
    Notifier createNotifier();
    NotificationLogger createLogger();
    NotificationTracker createTracker();
}

class EmailNotificationFactory implements NotificationFactory { ... }
class SmsNotificationFactory implements NotificationFactory  { ... }
```

Use Abstract Factory when switching channels means switching the *entire stack* — notifier, logger, and tracker together. If you only need to swap the notifier, Abstract Factory is overkill.

---

## Factory vs Builder — What's the Difference?

These two are often confused because they both create objects. The pressure is different.

| | Factory | Builder |
|-|---------|---------|
| **Pressure** | *Which* concrete type to create | *How* to assemble a complex object |
| **Input** | A selection key (channel, type, env) | Named steps, optional configuration |
| **Output** | Different concrete types | Same type, different configuration |
| **Structure** | Simple method or class | Fluent builder with `build()` |

Factory selects between `EmailNotifier` and `SmsNotifier`. Builder assembles an `HttpRequest` with many optional fields.

They often appear together:

```java
HttpRequest request = HttpRequest.builder()  // Builder: how to construct
    .post(url)
    .body(payload)
    .build();

HttpClient client = HttpClientFactory.create(profile);  // Factory: which client
client.send(request);
```

---

## Real Examples in Production

| Domain | Factory purpose |
|--------|----------------|
| Storage | `StorageFactory.create("s3")` → S3Client, `"azure"` → AzureBlobClient |
| Payment | `PaymentFactory.create("stripe")` → StripeGateway, `"razorpay"` → RazorpayGateway |
| Auth | `AuthStrategyFactory.create("jwt")` → JwtValidator, `"oauth"` → OAuthValidator |
| Export | `ExportFactory.create("pdf")` → PdfExporter, `"csv"` → CsvExporter |
| Cache | `CacheFactory.create("redis")` → RedisCache, `"local"` → InMemoryCache |

The pattern across all: a selection key determines which concrete implementation to instantiate. The caller depends on the interface, not the implementation.

---

## From Scattered `new` to Factory — The Migration Path

When scattered creation has caused enough pain (like the push notification incident), the migration is mechanical:

**Step 1: Identify all creation sites**
```bash
grep -r "new EmailNotifier\|new SmsNotifier\|new PushNotifier" src/
```

Result: six files.

**Step 2: Create the interface (if not already present)**
```java
interface Notifier {
    void send(String userId, String message);
}
```

**Step 3: Create the factory**
```java
class NotificationFactory {

    private final SmtpConfig smtpConfig;
    private final TwilioConfig twilioConfig;
    private final FcmConfig fcmConfig;

    Notifier create(String channel) {
        return switch (channel) {
            case "email" -> new EmailNotifier(smtpConfig);
            case "sms"   -> new SmsNotifier(twilioConfig);
            case "push"  -> new PushNotifier(fcmConfig);
            default      -> throw new IllegalArgumentException("Unknown: " + channel);
        };
    }
}
```

**Step 4: Replace each creation site**
```java
// Before (in each of six files):
new EmailNotifier(smtpConfig).send(userId, message);

// After (in each of six files):
factory.create(channel).send(userId, message);
```

**Step 5: Add WhatsApp**
One new case in `NotificationFactory.create()`. All six callers updated automatically.

That's the entire migration. It's mechanical, safe (each caller works the same), and the configuration — `smtpConfig`, `twilioConfig`, etc. — moves to one place.

## Registry Pattern — Dynamic Factory

When the set of types grows at runtime (plugins, feature flags, tenant-specific implementations), a registry-based factory is more flexible:

```java
class NotificationRegistry {

    private final Map<String, Supplier<Notifier>> registry = new HashMap<>();

    void register(String channel, Supplier<Notifier> factory) {
        registry.put(channel, factory);
    }

    Notifier create(String channel) {
        Supplier<Notifier> factory = registry.get(channel);
        if (factory == null) throw new IllegalArgumentException("Unknown: " + channel);
        return factory.get();
    }
}

// At startup: register known channels
registry.register("email", () -> new EmailNotifier(smtpConfig));
registry.register("sms",   () -> new SmsNotifier(twilioConfig));

// At runtime: register tenant-specific channel from DB config
registry.register("whatsapp_tenant_42", () -> new WhatsAppNotifier(tenant42Config));
```

Callers are identical. The registry is open for extension without modification.

## When Factory Is Wrong

**One concrete type that never changes.** If you always create `EmailNotifier` and the business will never use SMS or push, the factory adds indirection without value. Just use `new EmailNotifier()` and extract the factory when variation appears.

**Two types that are genuinely different.** If `EmailNotifier` and `SmsNotifier` don't share an interface because they have different call signatures, a factory that returns `Object` is not a factory — it's confusion.

**Creation doesn't vary.** If every caller always creates the same type with the same configuration, there's nothing for a factory to decide. A factory with one `case` is premature abstraction.

The pressure is *creation variation*. Without variation, there's no pressure. Without pressure, the pattern is noise.

---

## The Interview Answer

**Question:** When should Factory Pattern be used?

**Weak answer:** *"Whenever you use `new`."*

**Strong answer:**

*"Factory Pattern addresses object creation variation — when which concrete type gets instantiated varies by context (channel, environment, profile, tenant), and that decision logic would otherwise scatter across multiple callers. The signal is creation code duplicated in multiple places, or callers importing and constructing concrete types they shouldn't know about. Factory centralizes the selection logic and configuration, letting callers depend only on the abstraction. It's distinct from Builder — Factory decides which type to create; Builder decides how to assemble one complex object. If there's only one concrete type and no variation expected, the factory adds indirection without value."*

---

## Key Takeaways

- Factory solves **object creation variation** — which concrete type gets built.
- Centralizes creation: one update point when new types are added.
- Callers depend on the interface, not the implementation.
- Factory Method (one method) → Simple Factory (dedicated class) → Abstract Factory (family of factories). Scale to the pressure.
- Factory ≠ Builder: Factory selects *what*; Builder assembles *how*.

---

*All papers and runnable Java samples: [github.com/pramod0s007/architect-notes](https://github.com/pramod0s007/architect-notes)*

*Previous → Paper 11: Builder Pattern | Next → Paper 13: When Patterns Become Anti-Patterns*
