# Stock Price Monitor — Observer Pattern with Live Deregistration

## What This Demonstrates

Observer Pattern applied to a live market data feed. `StockPricePublisher` tracks
current prices and notifies all registered observers whenever a price changes.
Three observers react independently: `PriceAlertObserver` fires on moves above a
percentage threshold, `PortfolioObserver` recalculates portfolio P&L, and
`AuditLogObserver` records every tick. Bob's `PortfolioObserver` deregisters
mid-stream — subsequent price updates stop reaching him without affecting Alice's
portfolio or the audit log.

**Pressure: Event Broadcast** — a market data service publishes one price feed.
Multiple independent consumers (alert engine, portfolio valuations, audit trail,
charting, hedging algorithms) all need to react to the same ticks. Without the
Observer pattern, the publisher accumulates explicit references to each consumer
and every new consumer requires modifying `updatePrice()`.

## Class Diagram

```
<<interface>>
StockPriceObserver                    StockPriceEvent
+ onPriceChanged(event): void         - symbol: String
        △                             - previousPrice: double
        |                             - newPrice: double
   ────────────────────────────────   - percentChange: double
   |              |              |    - timestamp: Instant
PriceAlert    Portfolio       AuditLog
Observer      Observer        Observer
threshold:%   name+holdings   events:List
onPriceChanged onPriceChanged onPriceChanged
→ log ALERT   → recalc P&L   → append entry

StockPricePublisher
────────────────────────────────────────────────────
- observers: List<StockPriceObserver>
- lastPrices: Map<String, Double>
+ registerObserver(observer): void
+ removeObserver(observer): void
+ updatePrice(symbol, newPrice): void
   └─ skips notification if price unchanged
   └─ builds StockPriceEvent(symbol, previous, newPrice)
   └─ for each observer: observer.onPriceChanged(event)
```

## Sequence Diagram

```
Main                  StockPricePublisher         Observers
  │                          │                       │
  │  updatePrice("AAPL",     │                       │
  │              197.95)     │                       │
  │─────────────────────────>│                       │
  │                          │ notifyObservers(event)│
  │                          │──────────────────────>│ PriceAlertObserver
  │                          │                       │   → AAPL +6.99% THRESHOLD BREACHED
  │                          │──────────────────────>│ alicePortfolio
  │                          │                       │   → AAPL +649.50 (50 shares)
  │                          │──────────────────────>│ bobPortfolio
  │                          │                       │   → AAPL not in Bob's holdings
  │                          │──────────────────────>│ AuditLogObserver
  │                          │                       │   → append AAPL $185→$197.95
  │                          │                       │
  │  removeObserver(bob)     │                       │
  │─────────────────────────>│                       │
  │                          │                       │
  │  updatePrice("MSFT",     │                       │
  │              430.00)     │                       │
  │─────────────────────────>│                       │
  │                          │──────────────────────>│ PriceAlertObserver  (still active)
  │                          │──────────────────────>│ alicePortfolio      (still active)
  │                          │                   ✗   │ bobPortfolio        (deregistered)
  │                          │──────────────────────>│ AuditLogObserver    (still active)
```

## Design Decisions

- **`removeObserver()` uses `List.remove()`** — safe to call at any time,
  including from within a callback. The `notifyObservers()` method iterates
  the list directly; in this example deregistration happens between `updatePrice`
  calls (not inside a callback), so this is safe. For observer removal during
  event handling, a copy-on-iterate strategy or `CopyOnWriteArrayList` would be
  appropriate.
- **Price equality check before notification** — `updatePrice()` compares the
  new price to the last known price using `Double.compare`. If the price is
  unchanged, no event is fired and no observer is called. This prevents observer
  spam on repeated ticks of the same value.
- **`StockPriceEvent` carries both old and new price** — observers can compute
  percent change themselves (as `PriceAlertObserver` does) without querying the
  publisher. The event is a self-contained snapshot; no observer needs to call
  back into the publisher to understand the move.
- **`AuditLogObserver` accumulates events in a `List`** — the list is readable
  via `getLog()` after the simulation. This pattern mirrors production audit
  trails where the observer writes to a database or message queue; in this
  example, the in-memory list makes the audit verifiable in the demo without
  external infrastructure.
- **`PriceAlertObserver` threshold is constructor-injected** — the alert
  sensitivity (e.g. 5%) is a configuration concern, not hardcoded. Different
  alert systems can register with different thresholds using the same class.

## How to Run

```bash
cd volume-5-structural-patterns/paper-16-observer-pattern/stock-price-monitor
javac *.java && java Main
```

Expected output (abbreviated):

```
>>> AAPL earnings beat — price jumps 7%
  [ALERT]     AAPL moved +6.99% — THRESHOLD BREACHED (limit: 5.0%)
  [Portfolio] Alice — AAPL: +649.50 (50 shares @ $185.00 → $197.95)
  [AuditLog]  AAPL: $185.00 → $197.95 (+6.99%)

>>> TSLA recall news — drops 12%
  [ALERT]     TSLA moved -12.00% — THRESHOLD BREACHED (limit: 5.0%)
  [Portfolio] Bob — TSLA: -585.80 (20 shares @ $245.00 → $215.60)

>>> Bob unregisters from market feed
>>> MSFT post-market surge
  [Portfolio] Alice — MSFT: +450.00 (30 shares @ $415.00 → $430.00)
  (Bob's portfolio: no update — deregistered)
```

## When to Apply

- One data source (feed, sensor, repository) has multiple independent consumers.
- Consumers subscribe and unsubscribe dynamically at runtime.
- The publisher should not import or reference any concrete consumer.

## When NOT to Apply

- Guaranteed delivery is required — the basic observer list has no retry or
  persistence. If an observer throws, subsequent observers in the list may not
  be called.
- Consumers must run concurrently on separate threads — a synchronous observer
  list blocks the publisher thread for each observer; use a message queue or
  reactive stream for that case.
