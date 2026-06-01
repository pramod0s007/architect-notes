# Paper 16 — Observer Pattern: Evolution Examples

**Domain:** Temperature Sensor / IoT

## Progression

| Version | File | State | Core Problem |
|---------|------|-------|--------------|
| v1 | `v1_TightlyCoupled.java` | 3 hard-coded downstream calls | Sensor directly calls AlertSystem, Dashboard, Logger — 3 imports, 3 fields |
| v2 | `v2_MoreConsumers.java` | 6 hard-coded downstream calls | Adding each new consumer required editing TemperatureSensor constructor + method |
| v3 | `v3_ObserverApplied.java` | Observer Pattern | Sensor knows zero downstream systems; adding DataAnalyticsObserver = 1 class + 1 `subscribe()` call |

## Why This Domain

IoT sensors are a natural fit for Observer: the hardware produces readings independently
of who wants to consume them. Tightly coupling the sensor to its consumers makes it
impossible to add new consumers without touching sensor code — and sensor code is often
close to hardware drivers where changes are expensive.

## How to Run

```bash
cd volume-5-structural-patterns/paper-16-observer-pattern/evolution

javac -d out v1_TightlyCoupled.java v2_MoreConsumers.java v3_ObserverApplied.java

java -cp out evolution.v1_TightlyCoupled
java -cp out evolution.v2_MoreConsumers
java -cp out evolution.v3_ObserverApplied
```

## Key Insight

In v2, TemperatureSensor has 7 constructor parameters because every new consumer
added a new field. That 7-parameter constructor is the measurable cost of tight coupling.

In v3, TemperatureSensor has one constructor parameter: its own `sensorId`.
It is permanently decoupled from all downstream systems.

## Observer Pattern Tradeoffs

| | Benefit | When It Matters |
|--|---------|----------------|
| Subscribe/unsubscribe at runtime | Dynamic consumer list | Consumers join/leave the system at runtime |
| Sensor knows no consumers | Zero downstream coupling | Each sprint adds a new consumer |
| Observers tested independently | No sensor required in observer test | Large test surfaces |
| Event carries data | No callback signature changes | Event data grows over time |

Observer adds indirection — when you have 2 consumers that never change,
direct calls are simpler. Observer earns its complexity when the consumer list grows.
