# Order Events — Observer Pattern

## What This Demonstrates

Observer Pattern applied to order lifecycle events. `OrderService` publishes an
`OrderPlacedEvent` when an order is saved. Three typed observers — `EmailNotificationObserver`,
`WarehouseObserver`, and `LoyaltyPointsObserver` — react independently to the
same event. A fourth, anonymous lambda observer is added mid-stream with no
change to `OrderService`.

**Pressure: Event Broadcast** — `OrderService.placeOrder()` originally called
8 downstream services directly. Every new consumer (loyalty, analytics, fraud
scoring) required modifying `placeOrder()`. The method grew to 60 lines and
was changed 11 times in 4 months. Each change risked breaking the existing
8 calls, and a regression in any downstream call blocked the entire order.

## Class Diagram

```
<<interface>>
OrderEventObserver                        OrderPlacedEvent
+ onOrderPlaced(event): void              - order: Order
        △                                 - occurredAt: Instant
        |
   ──────────────────────────────────
   |              |                  |
EmailNotification WarehouseObserver  LoyaltyPointsObserver
Observer          onOrderPlaced()    onOrderPlaced()
onOrderPlaced()   → packing request  → award points
→ send email

OrderService
────────────────────────────────────────────────
- observers: List<OrderEventObserver>
+ registerObserver(observer): void
+ placeOrder(order): void
   └─ orderRepository.save(order)        ← business logic unchanged
   └─ publish(new OrderPlacedEvent(order))
         └─ observers.forEach(o -> o.onOrderPlaced(event))

Order
- id: String
- customerId: String
- email: String
- quantity: int
- totalPrice: double
```

## Sequence Diagram

```
Client                OrderService              Observers
  │                       │                        │
  │  placeOrder(order)    │                        │
  │──────────────────────>│                        │
  │                       │ save(order)            │
  │                       │──────────────────────  │
  │                       │                        │
  │                       │ publish(OrderPlacedEvent)
  │                       │────────────────────────>│ EmailObserver.onOrderPlaced()
  │                       │                        │   → send confirmation to alice@...
  │                       │────────────────────────>│ WarehouseObserver.onOrderPlaced()
  │                       │                        │   → raise packing request ORD-001
  │                       │────────────────────────>│ LoyaltyObserver.onOrderPlaced()
  │                       │                        │   → award 149 points to CUST-42
  │                       │────────────────────────>│ [lambda] Analytics.onOrderPlaced()
  │                       │                        │   → track ORD-001 (added later,
  │                       │                        │     no change to OrderService)
  │<──────────────────────│
```

## Design Decisions

- **Lambda observer added in `Main` without any new class** — `OrderEventObserver`
  is a functional interface (one abstract method). Any lambda that matches
  `(OrderPlacedEvent) -> void` is a valid observer. Simple consumers like analytics
  tracking do not need a full class; the pattern accommodates both.
- **`observers.forEach(o -> o.onOrderPlaced(event))` over indexed iteration** —
  safe for the common case, and the `forEach` lambda avoids accidental index
  manipulation. For production use, consider iterating a copy if observers might
  deregister during notification.
- **`OrderService` has zero imports of concrete observer classes** — it depends
  only on the `OrderEventObserver` interface. Adding a FraudScoringObserver,
  RecommendationEngineObserver, or any future consumer requires zero changes to
  `OrderService`.
- **`OrderPlacedEvent` is a value object** — immutable snapshot of the order at
  the moment it was placed. Observers cannot mutate the event, and they all see
  the same consistent state regardless of execution order.

## How to Run

```bash
cd volume-5-structural-patterns/paper-16-observer-pattern/order-events
javac observer/orderevents/*.java && java observer.orderevents.Main
```

Expected output:

```
Order saved: ORD-001
[Email]     Confirmation sent to alice@example.com for order ORD-001 ($149.99)
[Warehouse] Packing request raised for ORD-001 — 3 item(s)
[Loyalty]   Awarded 149 points to CUST-42 for order ORD-001

--- Adding analytics observer without changing OrderService ---
Order saved: ORD-002
[Email]     Confirmation sent to bob@example.com for order ORD-002 ($29.99)
[Warehouse] Packing request raised for ORD-002 — 1 item(s)
[Loyalty]   Awarded 29 points to CUST-17 for order ORD-002
[Analytics] Order tracked: ORD-002
```

## When to Apply

- One producer needs to notify multiple independent consumers and the set of
  consumers is open-ended or grows over time.
- Adding a new consumer should not require modifying the producer.
- Consumers are owned by different teams and should be independently deployable.

## When NOT to Apply

- Only one consumer exists and no growth is expected — a direct method call is
  simpler and easier to trace in a debugger.
- Consumers must execute in a guaranteed sequence and share state — the observer
  list provides no ordering guarantee across teams, and shared mutable state
  between observers reintroduces coupling.
