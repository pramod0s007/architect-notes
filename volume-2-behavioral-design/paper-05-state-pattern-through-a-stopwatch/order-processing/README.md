# E-Commerce Order Processing — State Pattern

## What This Demonstrates

Full e-commerce order lifecycle: PENDING → CONFIRMED → SHIPPED → DELIVERED,
with CANCELLED and REFUNDED as terminal states. Each state class owns its
valid transitions and throws `IllegalStateException` on invalid ones. `Order`
holds zero conditional logic — it delegates every lifecycle call to the
current `OrderState`.

**Pressure: State Explosion** — real order/subscription services grow from
2 to 9+ states, making each method a 60+ line switch block. Every new state
and every new operation multiplies the existing branching complexity.

## State Transition Diagram

```
                  confirm()
  [PENDING] ─────────────────→ [CONFIRMED]
      │                             │
   cancel()                    cancel() / ship()
      │                             │
      ↓              ship()         ↓
 [CANCELLED] ←─────────────── [SHIPPED]
      ↑                             │
      │                         deliver()
      │                             │
      │                             ↓
      │                        [DELIVERED]
      │                             │
      │                          refund()
      │                             │
      └──────────────────────→ [REFUNDED]
```

Terminal states (CANCELLED, REFUNDED) throw on all further transitions.

Valid transitions summary:

```
PENDING   : confirm() → CONFIRMED  | cancel() → CANCELLED
CONFIRMED : ship()    → SHIPPED    | cancel() → CANCELLED
SHIPPED   : deliver() → DELIVERED
DELIVERED : refund()  → REFUNDED
CANCELLED : all operations throw IllegalStateException
REFUNDED  : all operations throw IllegalStateException
```

## Class Diagram

```
<<interface>>
OrderState
+ confirm(order: Order): void
+ ship(order: Order): void
+ deliver(order: Order): void
+ cancel(order: Order): void
+ refund(order: Order): void
+ label(): String
        △
        |
   ──────────────────────────────────────────
   |            |           |         |      |        |
PendingState ConfirmedState ShippedState DeliveredState CancelledState RefundedState

Order                                              [context]
- id: String
- customerEmail: String
- state: OrderState          (starts as PendingState)
+ confirm() / ship() / deliver() / cancel() / refund()  → delegates to state
+ getStatus(): String
~ setState(newState: OrderState): void  ← called only by state classes
```

## Sequence / Flow

```
Client
  │
  ├─ new Order("ORD-1001", "alice@example.com")   → state = PendingState
  │
  ├─ order.confirm()
  │       └─ PendingState.confirm(order)
  │               └─ order.setState(new ConfirmedState())
  │
  ├─ order.ship()
  │       └─ ConfirmedState.ship(order)
  │               └─ order.setState(new ShippedState())
  │
  ├─ order.deliver()
  │       └─ ShippedState.deliver(order)
  │               └─ order.setState(new DeliveredState())
  │
  ├─ order.refund()
  │       └─ DeliveredState.refund(order)
  │               └─ order.setState(new RefundedState())
  │
  └─ [illegal] order.confirm()   ← now in RefundedState
          └─ RefundedState.confirm(order)
                  └─ throws IllegalStateException("Order already refunded")
```

## Design Decisions

- **Terminal states throw on all transitions** — CANCELLED and REFUNDED are
  end-of-life. Throwing loudly prevents silent data corruption where a
  cancelled order is accidentally confirmed again.
- **`setState()` is package-private** — only state classes in the same package
  can drive transitions. External callers cannot bypass the lifecycle.
- **`label()` on the interface** — `order.getStatus()` delegates to the current
  state without any string constants or enums in `Order` itself.
- **Each state is instantiated fresh on each transition** — small objects with
  no mutable fields; no singleton sharing, no thread-safety concern.
- **`Order` carries domain identity (`id`, `customerEmail`)** — states receive
  the `Order` reference so they can log meaningful messages with order ID
  without coupling to any external system.

## How to Run

```bash
cd volume-2-behavioral-design/paper-05-state-pattern-through-a-stopwatch/order-processing
javac *.java && java Main
```

Expected output:

```
=== Happy path: full lifecycle ===
[Order ORD-1001] Created for alice@example.com — state: PENDING
[Order ORD-1001] PENDING -> CONFIRMED: payment accepted
[Order ORD-1001] CONFIRMED -> SHIPPED: dispatched from warehouse
[Order ORD-1001] SHIPPED -> DELIVERED: confirmed by customer
[Order ORD-1001] DELIVERED -> REFUNDED: return accepted, credit issued
Final status: REFUNDED

=== Early cancel path ===
[Order ORD-1002] Created for bob@example.com — state: PENDING
[Order ORD-1002] PENDING -> CANCELLED: cancelled before confirmation
Final status: CANCELLED

=== Illegal transition guard ===
[Order ORD-1003] PENDING -> CONFIRMED: payment accepted
Caught expected error: Cannot refund order ORD-1003 — not yet delivered
Status unchanged: CONFIRMED
```

## When to Apply

- An entity has 4+ states and each state allows only a subset of operations.
- Invalid transitions must fail loudly with context (order ID, state name).
- New states (e.g., ON_HOLD, PARTIALLY_SHIPPED) must be addable without
  modifying existing state classes.

## When NOT to Apply

- A simple active/inactive flag — use a boolean, not six state classes.
