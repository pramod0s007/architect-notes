# KISS — Keep It Simple

**Principle:** KISS (Keep It Simple, Stupid)

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

A search service with a pluggable query parser, a distributed ranking pipeline, and an A/B experimentation framework — built to serve 40 queries per minute — is not a system. It is a monument to anticipated scale that never arrived. KISS states that simplicity should be the default design decision. The pressure is the complexity tax paid forever by a team that built for imagined requirements instead of real ones.

## The Pressure: Over-Engineering for Scale That Never Arrived

Three months of architecture work produced a system that handles 40 queries per minute but requires four engineers to understand and two hours to onboard. A two-day simple solution would have served the same load, been comprehensible to any engineer on the team, and left three months for features customers actually wanted.

## The Principle

**The simplest solution that works is the right solution — until proven otherwise.** Complexity must earn its place. Every abstraction layer, every framework, every generalization is a bet that the complexity will pay off. Most bets lose.

## Pros

- Simple systems are readable by any engineer, not just the original author
- Onboarding takes hours, not weeks
- Debugging is direct — fewer layers means fewer places for failures to hide
- Simpler systems are faster to change when requirements shift

## Cons

- Simple solutions can require rewriting when scale actually arrives
- Teams under pressure to "do it right" may read simplicity as lack of ambition
- Without discipline, "keep it simple" becomes an excuse to skip necessary structure

## When NOT to Use

- When known scale requirements genuinely demand the complexity upfront
- Core domain logic with well-understood variation points — that complexity is earned
- When regulatory or compliance requirements impose the structure

## Code Examples in This Repo

| Example | Domain | What It Shows |
|---------|--------|--------------|
| [`evolution/`](./evolution/) | Product search | 3-month over-engineered architecture vs 2-day simple solution for the same load |
| [`http-status-handler/`](./http-status-handler/) | API | `ApiResponse`: 15-line simple class vs 80-line generic framework class |
| [`config-loader/`](./config-loader/) | Config | `AppConfig`: 3-line static loader vs 4-class abstraction framework |

### How to Run

```bash
cd volume-6-clean-code-principles/paper-26-kiss-keep-it-simple/evolution
javac *.java && java Main

cd volume-6-clean-code-principles/paper-26-kiss-keep-it-simple/http-status-handler
javac *.java && java Main
```
