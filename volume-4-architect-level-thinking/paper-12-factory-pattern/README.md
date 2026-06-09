# Factory Pattern

**Pattern:** Factory Pattern

---

## Read the Full Article on Medium

[Factory Pattern](https://medium.com/@replytopramods.aws/factory-pattern-0a2ea0e58d1e)

---
## What This Paper Is About

Factory Pattern addresses **object creation variation** — when which concrete type gets instantiated varies by context (channel, environment, profile, tenant), and that creation logic would otherwise scatter across multiple callers.

The signal is not "we should centralize." The signal is: **scattered `new` means missed updates.**

## The Pressure: Creation Variation

A notification service added WhatsApp as a delivery channel. Of 6 places with `new EmailNotifier()` or `new SmsNotifier()`, only 3 were updated. Push notifications worked in order confirmation and billing. Support, audit, and alerts never received them.

The support team spent a week investigating. The fix was changing 3 forgotten `new` calls.

Factory Pattern centralizes creation. New channel = one change, all callers updated automatically.

## Factory Method vs Simple Factory vs Abstract Factory

| Form | Use When |
|------|---------|
| Factory method (static) | Named construction variants for one class |
| Simple Factory class | Creation reused across multiple callers |
| Abstract Factory | Entire families of objects must swap as a unit |
| Registry | Types registered at runtime, extensible without deployment |

## Pros

- One update point when new types are added
- Callers depend on the interface, not the implementation
- Configuration and wiring centralized in the factory

## Cons

- Adds indirection for creation that doesn't actually vary
- Factories for single types are premature abstraction

## When NOT to Use

- One concrete type, no variation expected → `new` at call site is correct
- Two types that don't share an interface → factory returning `Object` is worse
- Creation that never varies → factory adds indirection without value

## Factory vs Builder

Factory: *which* concrete type to create (varies by key/profile/env).
Builder: *how* to assemble one complex object (many optional fields).
These are complementary — Factory creates, Builder configures.

## Read the Full Article


## Code Examples in This Repo

| Example | Domain | What It Shows |
|---------|--------|--------------|
| [`factory/notification-factory/`](./notification-factory/) | Notifications | Email, SMS — centralized creation |
| [`factory/storage-factory/`](./storage-factory/) | Infrastructure | S3, Azure Blob, Local disk — `createFromEnvironment()` reads system property |

### How to Run

```bash
cd code-samples/factory/notification-factory
javac *.java && java Main

cd code-samples/factory/storage-factory
javac *.java && java Main
```
