# Lookup Tables vs Polymorphism

*Sometimes a Map beats an interface hierarchy. Knowing when is architectural judgment.*

---

During a code review last year, I watched two engineers spend forty-five minutes debating whether to use Visitor Pattern or a lookup table for a routing decision matrix.

The service matched incoming event types to handler methods. Six event types. Six handlers. Each pair was a direct mapping — `USER_CREATED` → `handleUserCreated()`. No shared logic. No type-dependent dispatch. Just a table.

One engineer wanted Visitor Pattern. "It's the right tool for type-based dispatch," he argued.

The other wanted a `Map`. "It's twelve entries. A Map is twenty lines and instantly readable."

I asked one question: "How often do new event types get added?"

"About once a year."

"And when a new type arrives, does the handling logic interact with the type of the object in any complex way, or does it just call a different method?"

"Just calls a different method."

"Then it's a lookup table. Visitor Pattern is for when you have a stable type hierarchy and operations that need to dispatch on the concrete type. This is just routing. A `Map<EventType, EventHandler>` is the right answer."

The lookup table was in production by end of day. The Map is still there. No one has touched it in nine months except to add two new entries — exactly the operation a table is optimized for.

**The point isn't that Visitor is wrong. The point is that architectural judgment means choosing the tool that fits the actual pressure, not the most sophisticated tool available.**

Paper 07 introduced Visitor Pattern for cases where that sophistication is earned. This paper is about recognizing when it isn't.

Here's the question architects ask: **Is the matrix finite and stable, or is it open and growing?**

The answer determines whether you need a class hierarchy or a data structure.

---

## The Collision Matrix

Four game object types: Ship, Station, Comet, Asteroid. Six possible collision pairs.

With Visitor Pattern you get:
- Two interfaces (`GameObject`, `CollisionVisitor`)
- Four `accept()` implementations
- One `CollisionVisitor` implementation with four `visit()` methods
- Secondary visitors for double dispatch

That's twelve classes for six behaviors.

With a lookup table you get:

```java
Map<CollisionKey, CollisionAction> collisionTable = Map.of(
    key(SHIP, STATION),    (a, b) -> shipHitsStation(a, b),
    key(SHIP, COMET),      (a, b) -> shipHitsComet(a, b),
    key(SHIP, ASTEROID),   (a, b) -> shipHitsAsteroid(a, b),
    key(STATION, COMET),   (a, b) -> stationHitsComet(a, b),
    key(STATION, ASTEROID),(a, b) -> stationHitsAsteroid(a, b),
    key(COMET, ASTEROID),  (a, b) -> cometHitsAsteroid(a, b)
);

void handleCollision(GameObject a, GameObject b) {
    CollisionKey key = key(a.type(), b.type());
    CollisionAction action = collisionTable.getOrDefault(key, NO_OP);
    action.resolve(a, b);
}
```

Thirty lines. No hierarchy. Behavior is data — each pair maps to an action.

Adding a new pair: one new map entry.

---

## When a Lookup Table Wins

**Finite matrix.** You know all the pairs. The set doesn't grow unboundedly.

**Stable over time.** New pairs arrive infrequently — once a quarter, not every sprint.

**No type-dispatch requirement.** You identify type by an enum or string field, not via `instanceof`. The game engine already tracks `objectType` as data.

**Performance predictable.** `HashMap.get()` is O(1). No virtual dispatch chain. For hot paths (thousands of collision checks per frame), this matters.

**Configuration potential.** A lookup table can be loaded from a config file, a database, or an admin UI. Polymorphism requires a deployment for every new combination.

---

## When Polymorphism (or Visitor) Wins

**Open-ended type set.** New game object types are added regularly (drones, debris fields, boss enemies). Every new type needs collision behavior with every existing type. A lookup table needs manual updates per pair. A Visitor interface enforces completeness at compile time — new type = update all visitors.

**Compiler-enforced exhaustiveness.** When you add a new `GameObject` type and forget to update the lookup table, the bug is silent. When you add a new type and forget to implement `accept()`, it doesn't compile.

**Operations multiply.** You need collision, physics, rendering, and serialization across the same types. Visitor gives you one new class per new operation. A lookup table would need one new map per operation.

---

## The Architectural Trade-Off Table

| Dimension | Lookup Table | Visitor / Polymorphism |
|-----------|-------------|----------------------|
| Matrix size | Small (≤20 pairs) | Any |
| Type set growth | Stable | Open |
| Exhaustiveness check | Runtime | Compile time |
| New operation | New map | New visitor class |
| New type | Add N new entries | Update all visitors |
| Configuration | Possible | Requires deployment |
| Performance | O(1) | Virtual dispatch |
| Readability | High (behavior as data) | High (behavior as class) |

No column wins all rows. The choice depends on which dimensions matter most for your domain.

---

## The Same Pattern Elsewhere

Lookup tables as an architectural tool appear in many forms:

**Command routing in CQRS:**
```java
Map<String, CommandHandler> handlers = Map.of(
    "CreateOrder",    new CreateOrderHandler(),
    "CancelOrder",    new CancelOrderHandler(),
    "ShipOrder",      new ShipOrderHandler()
);
handlers.get(commandType).handle(command);
```

**HTTP method dispatch:**
```java
Map<String, Supplier<Response>> routes = Map.of(
    "GET /users",    this::listUsers,
    "POST /users",   this::createUser,
    "DELETE /users", this::deleteUser
);
```

**Event handler registration:**
```java
Map<EventType, List<EventHandler>> eventBus = new HashMap<>();
eventBus.computeIfAbsent(EventType.ORDER_PLACED, k -> new ArrayList<>())
        .add(new InventoryReservationHandler());
```

In each case: behavior is data, not hierarchy. The table maps a key to an action. Adding behavior is adding a row.

---

## State Machine Tables

Lookup tables and state machines overlap naturally. A state machine is a table of (current state, event) → (next state, action).

```java
// State machine as a table
record Transition(State from, Event event, State to, Runnable action) {}

List<Transition> transitions = List.of(
    new Transition(IDLE,    START,   RUNNING, this::onStart),
    new Transition(RUNNING, PAUSE,   PAUSED,  this::onPause),
    new Transition(RUNNING, STOP,    IDLE,    this::onStop),
    new Transition(PAUSED,  RESUME,  RUNNING, this::onResume),
    new Transition(PAUSED,  STOP,    IDLE,    this::onStop)
);

void handle(Event event) {
    transitions.stream()
        .filter(t -> t.from() == currentState && t.event() == event)
        .findFirst()
        .ifPresentOrElse(
            t -> { currentState = t.to(); t.action().run(); },
            () -> { throw new InvalidTransitionException(currentState, event); }
        );
}
```

For simple state machines with stable transitions, this is clearer than State Pattern. Each row is a transition. The full machine is visible at a glance. Adding a transition is adding a row.

**When the table gets complex enough that you need transition guards, history states, or concurrent regions — that's when State Pattern earns its abstraction.**

## Config-Driven Tables — When Non-Engineers Own the Rules

The most powerful application of lookup tables is when the behavior needs to change without a deployment.

Imagine a promotion engine:

```java
// Loaded from database at startup (or refresh)
Map<PromotionKey, DiscountRule> promotionTable = promotionRepository.loadActive();

// Runtime evaluation — no code change needed for new promotions
BigDecimal discount = promotionTable
    .getOrDefault(key(customer.getTier(), product.getCategory()), NO_DISCOUNT)
    .apply(order);
```

A marketing manager adds a new promotion: `GOLD_TIER + ELECTRONICS → 15% off`. Database entry. No deployment. Table refreshes. Done.

With polymorphism: a new `GoldElectronicsDiscountStrategy` class, a PR, a review, a deployment pipeline. For what is essentially a data change.

**When the business owns the rules and they change faster than engineering can deploy, lookup tables win every time.** This is the architectural decision behind most rule engines, promotion systems, and feature flag platforms.

## When to Migrate from Table to Polymorphism

Tables don't scale infinitely. Signals that a table has outgrown itself:

- **Guard conditions.** You need conditionals inside the action lambdas — the "table" now has logic, not just dispatch.
- **Action composition.** Multiple rows trigger similar but not identical behavior, and you're copy-pasting lambda bodies.
- **The table is 100+ entries.** At this scale, a table is hard to reason about and test holistically.
- **Type safety matters.** The table maps `String` keys to `Object` actions and you've had three `ClassCastException` incidents.

At this point, the table has earned an interface. Extract it. The migration is mechanical: each table entry becomes a class. The key becomes the class selector. The action becomes an `execute()` method.

**You don't have to choose table or polymorphism at day one.** Start with the table. Migrate when the complexity signals appear.

---

## The Decision Framework

When you face a matrix of behaviors, ask:

1. **How large is the matrix?** Under twenty entries — table is simpler. Over fifty with growing dimensions — hierarchy earns its complexity.

2. **How stable is the type set?** Stable types favor tables. Open types favor polymorphism's compile-time safety.

3. **How many operations?** One operation — table is fine. Ten operations over the same types — Visitor isolates each cleanly.

4. **Who needs to configure it?** Developer changes only — either works. Business user changes — table wins (no deployment).

---

## The Interview Answer

**Question:** When would you use a lookup table instead of polymorphism?

**Weak answer:** *"Lookup tables are always faster."*

**Strong answer:**

*"Lookup tables are the right tool when the behavior matrix is finite and stable, the type set doesn't grow often, and new pairs can be added without a corresponding change to a class hierarchy. They're particularly useful when behavior is data-driven — when non-engineers need to configure it, or when the combinations change at a different rate than the types. Polymorphism wins when the type set is open, exhaustiveness enforcement matters, or multiple operations need to cross the same type hierarchy cleanly. The choice is about growth rate and enforcement requirements, not performance alone."*

---

## Key Takeaways

- A lookup table is a valid architectural choice — not a shortcut.
- **Finite, stable matrix + data-driven behavior** = table wins.
- **Open type set + compile-time safety** = polymorphism wins.
- **Multiple growing operations** = Visitor earns its complexity.
- The pattern appears everywhere: command routers, event buses, HTTP dispatch.

---

*All papers and runnable Java samples: [github.com/pramod0s007/architect-notes](https://github.com/pramod0s007/architect-notes)*

*Previous → Paper 07: Visitor Pattern Without UML | Next → Paper 09: Specification Pattern*
