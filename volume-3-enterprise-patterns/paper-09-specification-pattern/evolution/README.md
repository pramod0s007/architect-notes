# Evolution: Specification Pattern — Employee Leave Approval

Domain: **Employee Leave Approval** (LeaveRequest eligibility across 8 business rules)

## The Forcing Function

Business rules for leave approval come from multiple teams (HR Policy, Payroll, Compliance, Operations).
They arrive one at a time over months. Each rule sounds simple in isolation.
Together, they create a method that no single team owns and no one wants to touch.

## Progression

| File | What it shows | The pain |
|------|--------------|----------|
| `v1_InlineRules.java` | 3 inline rules in `canApprove()` — readable, fits on one screen | No pain yet; this is fine at this size |
| `v2_GrowingRules.java` | 8 rules, 60-line method, 3 teams editing the same file | `getRejectionReasons()` is a complete duplicate; logic drift between the two copies is guaranteed |
| `v3_SpecificationApplied.java` | `Specification<LeaveRequest>` interface + 8 concrete specs + `.and()` / `.or()` composition | Adding a rule = 1 new class; `getRejectionReasons()` is free — same list, different loop |

## When Specification Pattern Wins

- Rules come from **multiple teams** who should not all edit the same method
- You need both `canApprove()` and `getRejectionReasons()` — without duplicating the conditions
- Rules need to be **composed ad hoc** (e.g., "is this eligible ignoring manager approval?")
- Each rule must be **independently unit-testable**

## When Specification Pattern is Overkill

- You have 2–3 rules owned by one team that will never grow
- You never need to explain *why* something was rejected
- Rules are never reused or composed

## Run it

```bash
cd evolution/
javac v1_InlineRules.java          && java v1_InlineRules
javac v2_GrowingRules.java         && java v2_GrowingRules
javac v3_SpecificationApplied.java && java v3_SpecificationApplied
```

Or compile all at once:

```bash
javac *.java && java v3_SpecificationApplied
```

## Key Insight

In v2, `canApprove()` and `getRejectionReasons()` are the same conditions written twice.
Every rule change requires two edits. Teams inevitably miss one.

In v3 there is a **single list of specifications**. `canApprove()` asks "are all satisfied?"
and `getRejectionReasons()` asks "which ones failed?" — both reading the same list.
Adding a rule is one new class plus one line in the constructor. Nothing else changes.
