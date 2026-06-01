# Evolution: Lookup Tables vs Polymorphism — HTTP Request Router

Domain: **HTTP Request Router** (GET/POST/PUT/DELETE on 4–12 routes)

## The Forcing Function

You have a fixed set of routes that gets new entries occasionally.
The question is: do you express routing as **code** or **data**?

## Progression

| File | What it shows | The pain |
|------|--------------|----------|
| `v1_IfElseRouter.java` | Nested `if-else` chains for method + path | Adding a route means finding the right else-if block; the algorithm and data are tangled together |
| `v2_LookupTable.java` | `Map<String, Function<Request, Response>>` — routes are data | Adding a route = one `.register()` call; the routing algorithm never changes |
| `v3_WhenPolymorphismWins.java` | Handlers need shared behavior (auth), multiple operations (handle + describe + requiresAuth) | Lambda Map can only express one operation per entry; a second operation needs a second Map which can go out of sync silently |

## Lookup Table Wins When

- Routing is finite and stable (a fixed list of method+path combinations)
- Each handler is a single operation (request in → response out)
- You want to add routes without touching routing logic

## Polymorphism Wins When

- Handlers need to **share behavior** via inheritance (auth check, rate limiting, logging)
- There are **multiple operations** per handler type (handle, describe, canAccess, audit)
- **Type safety** matters — the compiler should catch missing operations, not a runtime 404

## Run it

```bash
cd evolution/
javac v1_IfElseRouter.java          && java v1_IfElseRouter
javac v2_LookupTable.java           && java v2_LookupTable
javac v3_WhenPolymorphismWins.java  && java v3_WhenPolymorphismWins
```

Or compile all at once:

```bash
javac *.java && java v3_WhenPolymorphismWins
```

## Key Insight

The lookup table and polymorphism are not opposites — v3 uses **both**.
The `Map` still provides O(1) lookup. The values are typed `RouteHandler` objects
instead of raw lambdas. You get data-driven routing *and* a compiler-checked contract.
The right question is: "how many operations does each handler need?"
One operation → lambda in a Map. Multiple operations → class hierarchy.
