# Which Patterns Still Matter in 2026?

**Pattern:** Pattern Relevance (Meta)

---

## Read the Full Article on Medium

_Article coming soon on Medium._

This repository focuses on runnable code examples. The white paper covers theory, war stories, and architectural reasoning in depth.

---
## What This Paper Is About

An opinionated view of which GoF patterns appear in production systems in 2026, tiered by how frequently the underlying pressure appears. Includes the AI era impact and what pattern literacy looks like at the staff engineer level.

## Tier 1: Appear in Almost Every Codebase

| Pattern | Pressure | Still Common? | Why |
|---------|---------|--------------|-----|
| Strategy | Behavior variation | Yes | Pricing, routing, encryption, validation |
| State | State explosion | Yes | Order workflows, subscriptions, devices |
| Command | Behavior encapsulation | Yes | CQRS, job queues, undo, audit |
| Specification | Rules variation | Yes | Eligibility, search, policy engines |
| Builder | Complex construction | Yes | SDKs, configs, DSLs |

## Tier 2: Situational — Valuable in Specific Domains

| Pattern | When | Alternative |
|---------|------|-------------|
| Visitor | Stable type hierarchy, growing operations | Lookup table for small matrices |
| Factory | Real creation variation | `new` at call site when types don't vary |
| Chain | Ordered pipelines with skip/stop | Sequential method calls when order never changes |

## Tier 3: Rarely Your First Choice in 2026

| Pattern | Why Less Common | Modern Alternative |
|---------|----------------|-------------------|
| Prototype | Cloning handled by frameworks | Serialization, copy constructors |
| Abstract Factory | Rare need for family swap | DI modules, plugin SPIs |
| Mediator | God object risk | Event buses (Kafka, Guava), orchestrators |

## AI Era Note

AI coding assistants lower the boilerplate cost of patterns. This makes **Phase 1 and Phase 2 overuse (Paper 13) more likely, not less.** AI generates the structure; it does not evaluate whether the pressure exists.

Pattern literacy + restraint is the modern staff engineer signal. Not pattern count.

## Read the Full Article


## All Code Examples in This Repo

| Pattern | Tier | Examples |
|---------|------|---------|
| Strategy | 1 | `strategy/encryption-example/`, `payment-gateway/`, `pricing-engine/` |
| State | 1 | `state/stopwatch-example/`, `order-processing/` |
| Command | 1 | `command/banking-example/`, `document-editor/`, `job-scheduler/` |
| Specification | 1 | `specification/product-search/`, `loan-eligibility/` |
| Builder | 1 | `builder/http-request-builder/`, `database-config/`, `search-request/` |
| Visitor | 2 | `visitor/collision-engine/`, `document-processor/` |
| Factory | 2 | `factory/notification-factory/`, `storage-factory/` |
| Chain | 2 | `chain-of-responsibility/request-pipeline/`, `api-security-pipeline/` |
| Observer | 2 | `observer/order-events/`, `stock-price-monitor/` |
| Decorator | 2 | `decorator/message-sender/`, `http-client/` |
| Proxy | 2 | `proxy/caching-repository/`, `lazy-loading/` |
| Adapter | 2 | `adapter/storage-adapter/`, `payment-adapter/` |
