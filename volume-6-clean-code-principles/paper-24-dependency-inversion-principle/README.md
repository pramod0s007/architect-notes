# Dependency Inversion Principle — Depend on Abstractions, Not Concretions

**Principle:** Dependency Inversion Principle (DIP)

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

When business logic directly instantiates `new MySQLRepository()`, `new SmtpEmailSender()`, and `new StripeClient()`, you cannot test the business logic without a real database, a real mail server, and a real payment terminal. DIP states that high-level modules should not depend on low-level modules — both should depend on abstractions. The pressure is untestable, unswappable business logic locked to infrastructure choices made on day one.

## The Pressure: Business Logic Directly Instantiates Infrastructure

An `OrderService` that calls `new MySQLRepository()` inside its constructor has made a permanent infrastructure commitment. Swapping to PostgreSQL requires modifying business logic. Running a unit test requires a running MySQL instance. DIP inverts that — infrastructure depends on the business interface, not the other way around.

## The Principle

**High-level policy should not import low-level detail.** Inject abstractions. The business logic defines the interface it needs. Infrastructure implements it. A test double implements it too.

## Pros

- Business logic is testable with no infrastructure running
- Swapping infrastructure (MySQL → PostgreSQL, SMTP → SendGrid) requires no changes to business logic
- Each dependency is explicit — no hidden `new` calls buried in methods
- Multiple implementations can coexist (production vs test vs staging)

## Cons

- More interfaces and wiring — DI frameworks help but add their own complexity
- Injection chains can become deep — constructor with 6 injected abstractions is a smell
- Overuse produces architecture where nothing is ever directly instantiated — can obscure program flow

## When NOT to Use

- Pure value objects and utilities — `new ArrayList<>()` does not need injection
- When the implementation truly will never change and testing does not require isolation
- Scripts and one-off tools where the wiring overhead exceeds the benefit

## Code Examples in This Repo

| Example | Domain | What It Shows |
|---------|--------|--------------|
| [`evolution/`](./evolution/) | Orders | `OrderService` hardwired to MySQL + SMTP + Stripe → abstractions injected, test doubles work |
| [`notification-service/`](./notification-service/) | Alerts | `AlertService` with injected `MessageSender` — SMS and email implementations swappable |
| [`report-data-service/`](./report-data-service/) | Reporting | `SalesReportService` with injected `DataStore` — in-memory test double vs real DB |

### How to Run

```bash
cd volume-6-clean-code-principles/paper-24-dependency-inversion-principle/evolution
javac *.java && java Main

cd volume-6-clean-code-principles/paper-24-dependency-inversion-principle/notification-service
javac *.java && java Main
```
