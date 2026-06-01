# Volume 2 — Behavioral Design

Behavioral patterns address how objects interact and how responsibilities are distributed. This volume covers the most commonly needed patterns in production systems — all taught through the pressure that creates them.

---

## Papers in This Volume

| Paper | Title | Pressure | Code Examples |
|-------|-------|---------|--------------|
| [Paper 04](paper-04-strategy-pattern-through-real-refactoring/) | Strategy Pattern Through Real Refactoring | Behavior variation | encryption-example, payment-gateway, pricing-engine |
| [Paper 05](paper-05-state-pattern-through-a-stopwatch/) | State Pattern Through a StopWatch | State explosion | stopwatch-example, order-processing |
| [Paper 06](paper-06-command-pattern-through-banking-systems/) | Command Pattern Through Banking Systems | Behavior encapsulation | banking-example, document-editor, job-scheduler |
| [Paper 07](paper-07-visitor-pattern-without-uml/) | Visitor Pattern Without UML | Object interaction matrix | collision-engine, document-processor |
| [Paper 08](paper-08-lookup-tables-vs-polymorphism/) | Lookup Tables vs Polymorphism | Finite stable matrix | collision-engine |

---

## The Pressure Map

```
Same caller, algorithm changes     → Strategy Pattern    (Paper 04)
Behavior depends on current mode   → State Pattern       (Paper 05)
Operations need to travel/undo     → Command Pattern     (Paper 06)
Double dispatch across types       → Visitor Pattern     (Paper 07)
Finite, stable behavior matrix     → Lookup Table        (Paper 08)
```
