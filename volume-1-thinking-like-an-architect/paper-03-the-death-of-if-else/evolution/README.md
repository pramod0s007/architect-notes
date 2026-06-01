# Paper 03 — The Death of if-else: Evolution Examples

Domain: **Payment Processing** — the if-else is always the symptom. Three distinct pressures cause it to grow. Identifying the pressure before prescribing the solution is the skill.

## The Core Thesis

> "The if-else is the symptom. Three pressures cause it to grow. Identify the pressure before prescribing the solution."

Not all if-else chains are problems. `v1_StableConditional.java` shows a case where refactoring would be wrong.

---

## Files

| File | Pressure | Growth Pattern | Solution |
|------|----------|----------------|----------|
| `v1_StableConditional.java` | None — leave it alone | 2 stable branches for 12 months | No refactoring |
| `v2_BehaviorPressure.java` | BEHAVIOR — gateway algorithms differ | PayPal → +Stripe → +Razorpay → +Square → +Adyen | Strategy Pattern |
| `v3_StatePressure.java` | STATE — states and legal transitions multiply | 2 states → 7 states, 42 conditional checks | State Pattern |
| `v4_RulesPressure.java` | RULES — compliance rules combine and grow | 1 rule → 6 composable rules | Specification Pattern |

---

## Progression Inside Each File

Each file shows three stages:

- **Month 1**: Original code — minimal, correct, the seed of the problem
- **Month 7-9**: The grown code — with inline `[!]` comments marking pain points and naming the pressure
- **Refactored (v3)**: Pattern applied — comments explain WHAT changed and WHY this specific pattern fits

---

## How to Identify the Pressure

| Question | Yes → |
|---|---|
| Has this if-else been unchanged for 6+ months with only 2-3 branches? | Leave it. It's stable. |
| Does each branch compute differently (different algorithm, different auth scheme)? | Behavior Pressure → Strategy Pattern |
| Does the same action behave differently depending on a current state? | State Pressure → State Pattern |
| Do branches represent rules owned by non-engineers that combine with AND/OR? | Rules Pressure → Specification Pattern |

---

## How to Run

Each file contains its own `public class` with a `main()` method.

```bash
# Compile
javac v1_StableConditional.java
javac v2_BehaviorPressure.java
javac v3_StatePressure.java
javac v4_RulesPressure.java

# Run
java v1_StableConditional
java v2_BehaviorPressure
java v3_StatePressure
java v4_RulesPressure
```

No external dependencies. All files are self-contained.

---

## Key Lessons Per File

**v1 — Stable Conditional**
The `PaymentValidator` has been untouched for a year. Two invariant checks. Introducing a Strategy or Specification pattern here would be pure ceremony with no benefit. Know when NOT to refactor.

**v2 — Behavior Pressure**
PayPal uses OAuth2 + cents. Razorpay uses HMAC + paise. Adyen uses merchant accounts. The ALGORITHM differs per gateway — this is canonical behavior variation. Strategy lets you A/B test gateways at runtime, add gateway #6 as a new class only, and give each gateway its own retry policy.

**v3 — State Pressure**
With 7 states and 6 actions, the naive approach produces 42 conditional checks. Adding an 8th state requires reopening 6 existing methods. The State Pattern eliminates this: each state class encodes what is legal from that state. Illegal transitions become explicit exceptions. The transition graph is auditable.

**v4 — Rules Pressure**
Six rules combining with AND/OR from three different teams (compliance, risk, merchant services). The Specification Pattern names each rule, makes it independently testable, and lets teams compose policies without touching existing code. The policy definition reads like the compliance document.
