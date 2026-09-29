# API Versioning — An Additive-Only Promise Doesn't Survive a Semantic Change

**Pattern:** In-place semantic change to an existing field, no version bump (silently breaks old clients) → "additive-only" field addition with shared, still-evolving computation (still breaks, just less obviously) → real version boundary with a permanently frozen v1 code path and an independently-evolving v2

**Volume 21** (API Design & Evolution at Scale) — opens the volume, following Volume 20 (Performance Engineering / JVM).

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

A "don't make breaking changes" checklist (don't remove fields, don't rename fields, don't change types) protects against structural breaking changes, which are loud — a missing field throws an exception. It says nothing about semantic breaking changes, which are silent: a field keeps its exact name, type, and shape, but starts meaning something different, and nothing in the response can tell a client that. This paper demonstrates a `total` field silently changing from "subtotal only" to "subtotal plus tax" on the same endpoint with no version bump, silently overcharging a client that still applies its own tax on top (verified: $8.64 silent overcharge on a $100 order). A first fix attempt — "just add a new field alongside, never touch the old one" — still fails, because the old field's value was computed from shared, still-evolving business logic rather than being genuinely frozen (verified: the field ships already wrong, then drifts further on an unrelated business-rule change). The real fix separates the two contracts at the code level: a permanently frozen v1 handler hardcoded to the original contract, and an independent v2 handler free to evolve (verified: a real tax-rate change propagates cleanly to v2 while v1's field and the old client's displayed price never move).

## The Pressure: Same Field, New Meaning, No Version Bump

A structural change to an API contract is loud and fails fast. A semantic change to an existing field — same name, same type, different meaning — fails silently, because the field still parses correctly and looks plausible; there is no signal anywhere that its meaning changed underneath an existing client.

## The Principle

**"Additive-only" evolution is a promise about values, not names.** Adding new fields while an existing field's underlying computation still changes is not backward compatible, even though nothing was removed or renamed. A real version boundary is a frozen code path — an old contract's field has to be pinned to its original definition, not merely still present under its original name while quietly sharing computation with a contract that's still evolving.

## Pros

- Freezing a v1 code path and letting v2 evolve independently allows both old and new clients to be served correctly, indefinitely, with zero interference
- A version boundary based on separate handlers (not shared computation) is easy to reason about and easy to audit — v1's behavior is guaranteed never to change by construction
- New fields can genuinely be added without a version bump, as long as no existing field's computation is touched at all

## Cons

- Maintaining a permanently frozen v1 handler alongside an evolving v2 handler means carrying that old code path indefinitely, or committing to an explicit deprecation/sunset process
- "Additive-only" as a bare naming rule (don't remove/rename) gives false confidence if the underlying computation for an existing field isn't also frozen
- Detecting a semantic (not structural) breaking change generally requires contract/consumer-awareness testing, not just schema validation — schema validation alone will not catch this class of bug

## When NOT to Version an Endpoint

- No external consumers exist yet — an internal-only API with no consumers you don't control doesn't need this discipline
- The change is purely additive in the strict sense: a genuinely new, optional field that no existing client reads, with zero change to any existing field's computation
- All consumers are known, finite, and can be coordinated directly — a hard cutover may be simpler than maintaining two permanent code paths indefinitely

## Code Examples in This Repo

| Example | Domain | What It Shows |
|---------|--------|--------------|
| [`pressure`](./pressure) | In-place semantic change | `total`'s meaning changes on the same endpoint, no version bump — old client silently overcharges by $8.64 |
| [`wrong-direction`](./wrong-direction) | "Additive-only" with shared computation | A new field is added alongside, but the old field still drifts because its computation was never frozen |
| [`principle-applied`](./principle-applied) | Real version boundary | A permanently frozen v1 handler and an independent v2 handler — v1's contract never drifts regardless of business-rule changes |

### How to Run

```bash
cd volume-21-api-design-evolution-at-scale/paper-112-api-versioning/pressure
javac *.java && java Main

cd volume-21-api-design-evolution-at-scale/paper-112-api-versioning/wrong-direction
javac *.java && java Main

cd volume-21-api-design-evolution-at-scale/paper-112-api-versioning/principle-applied
javac *.java && java Main
```
