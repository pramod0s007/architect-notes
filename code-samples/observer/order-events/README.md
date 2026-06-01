# Observer Pattern — Order Events

Demonstrates event broadcast pressure: OrderService publishes an event when an order is placed.
Multiple independent observers react without OrderService knowing about any of them.

## Pressure
One producer (OrderService) needs to notify multiple independent consumers (email, warehouse, loyalty, analytics)
that change at different rates and are owned by different teams.

## Run
```bash
javac observer/orderevents/*.java && java observer.orderevents.Main
```

## Key Point
Adding a new observer = one new class + one `registerObserver()` call.
`OrderService` never changes regardless of how many consumers are added.
