# Chain of Responsibility

## Why Pipelines Appear Everywhere

Enterprise requests pass through stages:

1. Authentication
2. Authorization
3. Validation
4. Rate limiting
5. Business logic

Early designs centralize this in one method.

```java
void handle(Request request) {
    authenticate(request);
    authorize(request);
    validate(request);
    rateLimit(request);
    process(request);
}
```

The sequence hardens.

Reordering, skipping, or extending stages requires editing core flow.

## The Real Problem

**Sequential decision flow.**

Each step is a decision point that may pass control forward or stop the chain.

## Chain of Responsibility Thinking

```java
interface Handler {
    void setNext(Handler next);
    void handle(Request request);
}
```

Each handler knows only the **next** link.

```java
authHandler.setNext(authzHandler);
authzHandler.setNext(validationHandler);
```

## Design Pressure

```
Sequential Decision Flow
        ↓
Chain of Responsibility
```

## Key Takeaways

- Decouples senders from receivers.
- Stages can be added or reordered without rewriting a monolithic pipeline.
- Common in middleware, filters, and gateway architectures.
- Distinct from Specification (rules) and Command (encapsulated operations).
