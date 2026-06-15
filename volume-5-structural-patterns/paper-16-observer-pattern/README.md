# Observer Pattern — The Foundation of Event-Driven Systems

**Pattern:** Observer Pattern

---

## Read the Full Article on Medium

[Observer Pattern — The Foundation of Event-Driven Systems](https://medium.com/@replytopramods.aws/observer-pattern-the-foundation-of-event-driven-systems-fd6c38153b34)

This repository focuses on runnable code examples. The white paper covers theory, war stories, and architectural reasoning in depth.

---
## What This Paper Is About

Observer Pattern is the architectural primitive behind every event-driven system in production. Every Kafka consumer, Spring `@EventListener`, RxJava subscription, and WebSocket stream is an Observer.

The pressure: **event broadcast** — one producer generating events that multiple independent consumers need to react to, owned by different teams, changing at different rates.

## The Pressure: Event Broadcast

An e-commerce `OrderService` that called email, warehouse, loyalty, analytics, fraud, and SMS directly — one method touching eight services. Every new consumer required modifying `placeOrder()`. Every deployment risked breaking all consumers.

Observer Pattern removes `OrderService` from knowing anything about its consumers. The service publishes an event. Consumers register. Neither knows about the other.

## Push vs Pull Model

**Push:** Subject sends full event data. Simpler for observers, event can bloat.
**Pull:** Subject sends notification, observer queries what it needs. More flexible, more coupled.

Most production systems use push with a well-designed event object.

## Modern Forms

| Name | Platform | What it is |
|------|----------|-----------|
| @EventListener | Spring | In-process Observer |
| PublishSubject | RxJava | Reactive Observer |
| @KafkaListener | Kafka | Distributed Observer |
| WebSocket stream | Any | Remote Observer |

## Dead Letter Queue — The Failure Mode

Silent observer failures are the most dangerous production problem with Observer Pattern. An observer throws, the exception is swallowed, the event is lost. Points never awarded. Warehouse never notified.

**Fix:** In-process observers must throw loudly. Kafka-based observers use dead-letter topics for automatic retry.

## Read the Full Article


## Code Examples in This Repo

| Example | Domain | What It Shows |
|---------|--------|--------------|
| [`observer/order-events/`](./order-events/) | E-commerce | Email, Warehouse, Loyalty, Analytics — producer never changes when consumers added |
| [`observer/stock-price-monitor/`](./stock-price-monitor/) | Finance | Price alerts, portfolio tracking, audit log — live deregistration mid-stream |

### How to Run

```bash
cd code-samples/observer/order-events
javac *.java && java Main

cd code-samples/observer/stock-price-monitor
javac *.java && java Main
```
