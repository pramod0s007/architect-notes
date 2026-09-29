# Consumer-Driven Contract Testing — A Green Build Doesn't Know What Consumers Actually Read

**Pattern:** Provider's own tests only (green on a silent field rename, because the provider never checks a field it never reads back) → hand-authored integration test (learns from the last incident, but still only covers the scenario it was told about, missing an unrelated change to a different scenario) → consumer-authored executable contracts, run in the provider's build (each real consumer states exactly what it depends on and against which scenario, so the provider never has to guess)

**Volume 21** (API Design & Evolution at Scale) — Paper 115.

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

Consumer-driven contract testing inverts who gets to decide what "backward compatible" means. A provider's own test suite only ever checks what the provider itself reads back out of its own response — so a silent field rename ("status" → "orderStatus") passes every test the provider owns, while a real, untracked consumer silently stops generating invoices (verified: `no invoice: status=null`). Adding a hand-authored integration test that explicitly checks the renamed field is a real improvement, but it only covers the scenario the provider team was told about — a later, unrelated change to a *different* scenario (a spelling cleanup on cancelled orders) stays green through that same test, while a second untracked consumer silently stops issuing refunds (verified: `no refund: status=CANCELED`). The real fix has each consumer author its own executable contract — code that runs the consumer's real decision logic against a candidate response, declaring exactly which scenario it needs to be checked against — and has the provider's build run every registered contract before allowing a deploy. The identical spelling-cleanup change is blocked immediately, at the exact contract the earlier hand-authored test missed (verified: `DEPLOY BLOCKED`).

## The Pressure: A Green Build Doesn't Know What Consumers Actually Read

The response is modeled as a plain `Map<String, Object>` — deliberately, not a typed POJO — because a silent key rename is exactly what a real, dynamic JSON API response allows, and a compiler would prevent this scenario from ever compiling. The provider's own tests check only "id" and "total," because those are the only fields the provider itself reads back; it has no reason to check "status," since it only ever writes that field. A real consumer that reads "status" breaks the moment it's renamed, and nothing in the provider's build notices.

## The Principle

**A contract must be authored and owned by the consumer, not guessed at by the provider — and it must declare exactly which scenario it needs to be checked against.** This is a different concern from classifying a change as breaking or non-breaking (an earlier paper in this series, Paper 112, covers that classification question directly). Even a provider that correctly identifies a change as breaking still has no way to know *which* consumers it breaks, or how, unless those consumers state it themselves, in a form the provider's own build can actually execute — and even a well-intentioned hand-authored test still only covers what its authors thought to check, never what they weren't told about.

## Pros

- Removes the provider's guesswork entirely — each consumer states, in executable code, exactly what "correct" means for its own use case
- Catches breaks in exactly the scenario a provider-authored test would have no reason to exercise, since the provider was never told that scenario mattered to anyone
- Scales naturally as consumers are added — each new consumer registers its own contract instead of the provider needing to anticipate every future dependency (in practice this benefits from a shared contract broker/registry as the consumer count grows; the two-consumer example here doesn't stress-test that)

## Cons

- Requires real coordination overhead — every consumer team needs a process for authoring, registering, and maintaining its own contract against the provider's build
- A contract that's stale (written once, never updated as the consumer's real logic changes) provides false confidence — it's only as good as the consumer team's discipline in keeping it current
- Doesn't help with anonymous or unregistered consumers — a public API with unknown clients has no way to receive or run a contract from them

## When NOT to Use Consumer-Driven Contract Testing

- There is exactly one consumer, owned by the same team as the provider — a shared PR review and shared test suite likely already covers this at lower coordination cost
- The API is genuinely internal and versioned in lockstep with all its consumers — a compiler-enforced interface already provides an equivalent guarantee
- The consumer population is unknown or unbounded — a public API with anonymous clients has no consumer available to author a contract in the first place

## Code Examples in This Repo

| Example | Domain | What It Shows |
|---------|--------|--------------|
| [`pressure`](./pressure) | Provider's own tests only | A silent field rename passes every test the provider owns, while a real consumer silently breaks |
| [`wrong-direction`](./wrong-direction) | Hand-authored integration test | The test correctly catches the previous incident but stays green through an unrelated change to a scenario it never exercises |
| [`principle-applied`](./principle-applied) | Consumer-authored executable contracts | Each consumer's own contract, routed to its own declared scenario, blocks the deploy that broke it |

### How to Run

```bash
cd volume-21-api-design-evolution-at-scale/paper-115-consumer-driven-contract-testing/pressure
javac *.java && java Main

cd volume-21-api-design-evolution-at-scale/paper-115-consumer-driven-contract-testing/wrong-direction
javac *.java && java Main

cd volume-21-api-design-evolution-at-scale/paper-115-consumer-driven-contract-testing/principle-applied
javac *.java && java Main
```
