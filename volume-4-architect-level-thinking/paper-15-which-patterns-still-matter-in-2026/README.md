# Which Patterns Still Matter In 2026?

## Why Revisit the Catalog

Frameworks, languages, and platforms evolved.

Some Gang of Four patterns are daily tools.

Others appear mainly in textbooks and over-designed enterprise code.

This paper is a pragmatic 2026 lens — not nostalgia, not dismissal.

## Still Strong

These patterns map cleanly to recurring pressures in modern services:

| Pattern | Why it still earns its keep |
|---------|----------------------------|
| **Strategy** | Behavior variation in services, pricing, routing |
| **State** | Workflows, devices, connection lifecycles |
| **Command** | Undo, jobs, audit trails, CQRS-style operations |
| **Specification** | Composable business rules in domain layers |
| **Builder** | HTTP clients, config, query DSLs |

Runnable samples for Strategy–Chain live under `code-samples/` (Papers 04–10).

## Situational

Use when pressure is clear — otherwise prefer simpler tools:

| Pattern | When it still fits |
|---------|-------------------|
| **Visitor** | Stable type set, many operations across types |
| **Factory** | Real creation variation behind profiles/channels |
| **Chain of Responsibility** | Middleware, filters, gateway pipelines |

See Paper 13 before defaulting to these.

## Less Common

Still taught, rarely the first tool in 2026 greenfield code:

| Pattern | Modern alternative |
|---------|-------------------|
| Abstract Factory | DI modules, plugin registries |
| Prototype | Clone via serialization frameworks / copy constructors |
| Mediator | Event buses, orchestration layers |

## Architect Rule

In 2026, **pressure + delete cost** beat pattern count.

Prefer:

- Functions and records where variation is tiny
- Tables where matrices are finite
- Framework middleware where chains are standard

Reach for named patterns when onboarding cost and change frequency justify them.

## Key Takeaways

- Patterns are not deprecated — **misuse** is common.
- Volume 2–3 papers remain the core engineering toolkit.
- Paper 14 helps choose; Paper 13 helps stop over-building.
- Re-evaluate your codebase yearly against this tier list.
