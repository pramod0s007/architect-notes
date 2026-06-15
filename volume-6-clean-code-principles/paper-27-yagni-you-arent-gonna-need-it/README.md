# YAGNI — You Aren't Gonna Need It

**Principle:** YAGNI (You Aren't Gonna Need It)

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

A plugin system, event sourcing infrastructure, and an A/B experimentation framework — all built before a single second use case existed — represent months of investment in features the product never needed. YAGNI states that you should not add functionality until you actually need it. The pressure is the ongoing maintenance cost of code written for requirements that changed, died, or never materialized.

## The Pressure: Infrastructure Built for Features That Changed or Died

An extensible discount rule registry with an interface, a factory, and a configuration loader was written when there was exactly one discount type. Three months later, the product pivoted and the registry was deleted. The engineer who built it spent a week on code that lasted one quarter — and the team maintained it for three months in between.

## The Principle

**Do not write code for requirements you do not have today.** The cost of YAGNI violations is not the initial build time — it is the ongoing tax of maintaining, testing, and explaining code that serves no current requirement. When the second use case arrives, write the abstraction then.

## Pros

- Codebase contains only code that earns its existence today
- Less code to maintain, test, document, and explain
- When requirements change (and they will), there is less speculative code to undo
- Engineers spend time on features customers are actually using

## Cons

- Refactoring to add abstraction later takes more effort than building it upfront
- Without any foresight, systems can become genuinely hard to extend
- "YAGNI" can become an excuse to skip legitimate architectural thinking

## When NOT to Use

- Foundational infrastructure where adding extensibility later is genuinely costly (database schema, public APIs)
- Security and compliance hooks — the cost of retrofitting these is high
- When you have a concrete, committed second use case arriving within weeks

## Code Examples in This Repo

| Example | Domain | What It Shows |
|---------|--------|--------------|
| [`evolution/`](./evolution/) | Discounts | Interface + registry built before second type exists → direct class; registry added when second type arrived |
| [`user-repository/`](./user-repository/) | Users | Premature caching layer for a 50-user system — and the simple version that was actually needed |
| [`event-publisher/`](./event-publisher/) | Events | Direct handler vs `EventBus` — bus introduced only when second handler arrived |

### How to Run

```bash
cd volume-6-clean-code-principles/paper-27-yagni-you-arent-gonna-need-it/evolution
javac *.java && java Main

cd volume-6-clean-code-principles/paper-27-yagni-you-arent-gonna-need-it/event-publisher
javac *.java && java Main
```
