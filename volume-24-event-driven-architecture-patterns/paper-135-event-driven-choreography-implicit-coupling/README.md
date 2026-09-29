# Paper 135: Event-Driven Choreography & Implicit Workflow Coupling

Part of *Architect Notes* Volume 24 — Event-Driven Architecture Patterns.

## Scenario

In choreography, services react to events independently, with no central coordinator deciding what runs before what. When two listeners react to the *same* triggering event and one of them actually depends on the other having run first, that dependency doesn't disappear — it just becomes implicit, riding on whichever order the event bus happens to invoke listeners in. Nothing enforces it, and nothing warns when it breaks.

## The Three Versions

### `pressure/`
Two services — shipping and fraud check — both react to the same `OrderPlaced` event. Shipping happens to be registered first, so it schedules a shipment before the fraud check even runs, purely because of registration order.

### `wrong-direction/`
"Fix" it by reordering the listeners (fraud check first) and having shipping check a shared flag. This works for the existing two listeners — but months later, a new, unrelated listener is added ahead of the fraud check, and the exact same unprotected race silently reappears.

### `principle-applied/`
Shipping never listens to `OrderPlaced` at all — it listens only to `FraudCheckPassed`, an event fraud check explicitly publishes once an order clears. The dependency is now structural: no matter what order any other `OrderPlaced` listener is registered in, shipping cannot run before the fraud check.

## Running

Each folder is self-contained. From within a folder:

```bash
javac -Xlint:all *.java
java Main
```

## Key Takeaway

Reordering listeners, or having them check a shared flag, only fixes the specific pair of listeners that exist today — it does nothing to stop a future, unrelated listener from reintroducing the exact same race. The real fix is to make the dependency structural: the dependent service should react to a distinct event representing "the thing I depend on actually happened," not to the same triggering event everyone else reacts to.
