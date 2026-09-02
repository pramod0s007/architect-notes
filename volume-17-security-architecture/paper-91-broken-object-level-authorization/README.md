# Broken Object-Level Authorization — One Resource, Two Operations, One Forgotten Check

**Continues Volume 17** — picking up right after Paper 90 (Authentication vs Authorization).

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

A resource can be correctly protected on one operation and completely open on another, because each operation is a separate code path with its own, independent enforcement decision (measured: reading a resource was correctly denied while updating the identical resource, by the identical unauthorized user, succeeded). Centralizing the ownership check into a single shared function, called by every operation, closes that gap (measured: both operations failed identically once both called the same check). This paper then shows the overcorrection: centralizing the check at the network edge instead of inside the operation itself, which protects only the callers guaranteed to pass through that edge (measured: an external, gateway-routed request was correctly denied while a direct internal call to the identical operation succeeded completely).

## The Measurements

- **Per-operation, inconsistent (pressure)**: reading another user's order is correctly denied; updating the same order, by the same unauthorized user, succeeds completely.
- **Shared check inside every operation (principle-applied)**: both read and update are denied identically, because both call the same ownership check.
- **Shared check at the edge only (wrong-direction)**: an external, gateway-routed update is correctly denied; a direct internal call to the same underlying operation succeeds, unguarded.

## The Finding

**The right place for an authorization check is the smallest scope that every real caller — external and internal — is structurally forced to pass through, which is almost always inside the operation on the resource, not in front of it.** Centralizing a check only protects the set of callers guaranteed to pass through wherever it's centralized.

## Code Examples in This Repo

| Example | What It Shows |
|---------|--------------|
| [`pressure/`](./pressure/) | Per-operation checks — measured a write succeeding where an identical read was denied |
| [`principle-applied/`](./principle-applied/) | Shared check inside every operation — measured consistent denial across both operations |
| [`wrong-direction/`](./wrong-direction/) | Shared check at the gateway only — measured a direct internal caller bypassing it entirely |

### How to Run

```bash
cd volume-17-security-architecture/paper-91-broken-object-level-authorization/pressure
javac *.java && java Main

cd volume-17-security-architecture/paper-91-broken-object-level-authorization/principle-applied
javac *.java && java Main

cd volume-17-security-architecture/paper-91-broken-object-level-authorization/wrong-direction
javac *.java && java Main
```
