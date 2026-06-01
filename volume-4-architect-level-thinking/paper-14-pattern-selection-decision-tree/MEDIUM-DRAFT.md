# Pattern Selection Decision Tree

*The question "which pattern should I use?" is the wrong question. Here's the right one — and a framework to answer it in under sixty seconds.*

---

Two engineers walk into a design review. Same codebase. Same problem statement.

The first engineer says: "We have a growing conditional in the pricing service. Looks like Strategy Pattern."

The second engineer says: "What's changing — the pricing algorithm itself, or which pricing tier applies to which customer?"

Silence.

"If the algorithm changes — different computation entirely — that's Behavior Variation. Strategy. If the tier assignment changes — same calculation, different input values — that's Data Variation. Configuration. No pattern needed."

I've watched this exact exchange happen dozens of times. The first engineer had pattern knowledge. The second had classification skill. Classification skill is worth twenty times more in a real design session.

The first engineer's instinct wasn't wrong — Strategy Pattern might have been right. But applied to the wrong problem, it would have created an interface hierarchy for what was essentially a lookup table.

**The question "which pattern should I use?" is always the wrong first question. The right question is "what kind of change am I dealing with?"**

Once you answer that, the pattern choice narrows from twenty-three options to two or three. And from two or three, the correct one is usually obvious.

This paper gives you the decision tree.

---

## Step 0: Is There Pressure?

Before classifying anything, check whether a refactoring is justified at all.

Ask:
- Is the code currently causing pain? Merge conflicts, long test cycles, modification spreading across multiple files?
- Is there a measurable growth signal? New requirements arriving faster than the code absorbs them?
- Is the modification cost exceeding the abstraction cost?

**If the answer to all three is "no" or "not yet" — stop here.** The code doesn't need a pattern. It needs to be left alone until pressure appears.

See Paper 13 (When Patterns Become Anti-Patterns) for what happens when you skip this step.

---

## Step 1: What Is Changing?

Every design problem begins with change. Classify the change before naming the solution.

```
What is changing?
├── Only the data, not the algorithm?          → Bucket 1: Data Variation
├── The object participating in a workflow?    → Bucket 2: Object Variation
├── The algorithm the caller triggers?         → Bucket 3: Behavior Variation
└── The business rules, independently?        → Bucket 4: Rules Variation
```

---

### Bucket 1 — Data Variation

**Signal:** Same processing logic, different input data, configuration, or output format.

**Examples:**
- CSV vs JSON vs XML report — same fields, different layout
- Different locale strings — same keys, different values
- Different environment configs — same structure, different values

**Solution family:** Configuration, parameters, templates, generics. Patterns are usually unnecessary.

**Wrong move:** Applying Strategy Pattern to format variation. Format isn't behavior — it's data. Parameterize.

---

### Bucket 2 — Object Variation

**Signal:** The workflow is stable. The object participating in each step changes.

**Examples:**
- MySQL vs MongoDB — same repository interface, different engine
- S3 vs Azure Blob — same storage contract, different provider
- Email vs SMS — same send/track workflow, different channel

**Solution family:**
- Interface (define the contract)
- Composition (inject the implementation)
- Dependency Injection (wire the right object)
- Factory Pattern (centralize creation when *which* object varies)

**Key question:** Is the workflow changing, or just the participant? If the workflow is stable, you're in Bucket 2.

---

### Bucket 3 — Behavior Variation

**Signal:** The caller stays stable. The algorithm itself changes — not the data it processes, not the object that runs it, but the computation logic.

**Examples:**
- AES vs DES vs RSA encryption — same `encrypt(text)` call, different algorithm
- Premium vs employee vs partner pricing — same `calculatePrice(product)` call, different logic
- Strict vs lenient validation — same `validate(input)` call, different rules applied

**Solution family:**
- Strategy Pattern — algorithm swaps without changing the caller
- Command Pattern — when operations must travel (be stored, undone, queued)
- State Pattern — when the algorithm varies by current state of the system

**Key question:** Is the *computation* changing, or just who runs it (Bucket 2) or what data flows in (Bucket 1)?

---

### Bucket 4 — Rules Variation

**Signal:** Business rules grow independently. Rules combine — AND, OR, NOT. Eligibility conditions multiply.

**Examples:**
- Loan eligibility: age AND income AND credit AND employment AND country
- Product search: price AND category AND rating AND availability AND brand
- Discount qualification: first purchase OR loyalty OR seasonal OR bundle

**Solution family:**
- Specification Pattern — rules as composable predicates
- Rule Engine — for runtime-configurable rules (external configuration)
- Decision Table — for finite, stable rule matrices

**Key question:** Are the rules *combining* with AND/OR/NOT? Do they grow independently of the object model? That's Bucket 4.

---

## Step 2: Measure the Pressure

Classification tells you which bucket you're in. But not all pressure in a bucket justifies a pattern.

Before refactoring, measure:

**Frequency of change:** How often does this area change? One new algorithm per quarter is different from one per week.

**Modification cost:** How painful is adding the next case? One method, one file — or five files, two teams, three merge conflicts?

**Growth projection:** Is this a stable domain with occasional additions, or an active growth area?

**Removal cost if wrong:** Can you easily reverse the abstraction if the growth doesn't materialize?

If the pressure is light and the growth rate is unclear — wait. Refactor toward the pattern when the cost of *not* having it becomes measurable.

---

## Step 3: Choose the Minimum Abstraction

Within each bucket, there's a spectrum of solutions. Prefer the simpler end until pressure justifies moving up.

### Behavior Variation (Bucket 3)

```
Direct method call
    ↓ (first alternative appears)
Extracted method per case
    ↓ (second alternative + caller stability)
Strategy Pattern
    ↓ (operations need storage/undo/scheduling)
Command Pattern
    ↓ (behavior depends on current mode)
State Pattern
```

Don't jump to the bottom without working through the top.

### Object Variation (Bucket 2)

```
Direct instantiation
    ↓ (second implementation appears)
Interface extraction
    ↓ (creation varies by context, scattered across callers)
Factory Pattern
    ↓ (entire families must swap as a unit)
Abstract Factory
```

### Rules Variation (Bucket 4)

```
Inline conditions
    ↓ (rules combine AND/OR)
Extracted predicate methods
    ↓ (rules reused across callers, independent tests needed)
Specification Pattern
    ↓ (rules configurable at runtime, volume > 30)
Rule Engine
```

---

## The 60-Second Shortcut

For fast classification during review or design discussions:

```
Is data changing?         → Parameterize. Skip patterns.
Is the object changing?   → Interface + Composition (+ Factory if creation varies).
Is the algorithm changing?→ Strategy / Command / State.
Are rules changing?       → Specification.
```

Memorize this. It handles 80% of real design decisions correctly.

---

## Worked Examples

**Encryption service with growing algorithms.**

What changes: the encryption algorithm (same caller, different computation). → Bucket 3: Behavior Variation → Strategy Pattern.

---

**Product search with composable filters.**

What changes: search rules (AND/OR combinations, growing list). → Bucket 4: Rules Variation → Specification Pattern.

---

**Notification service with multiple channels.**

What changes: the object that sends (same workflow, different provider). → Bucket 2: Object Variation → Interface + Composition, Factory for creation.

---

**Report generation in CSV, JSON, PDF.**

What changes: the output format (same data, different representation). → Bucket 1: Data Variation → Template or parameterization. No pattern needed.

---

**Payment workflow: store, schedule, undo, audit.**

What changes: operations need to travel independently of their invoker. → Bucket 3: Behavior Variation → Command Pattern (not Strategy — operations need to be first-class objects).

---

**Order system: PENDING → CONFIRMED → SHIPPED → DELIVERED → CANCELLED.**

What changes: behavior at each operation depends on current state, and states multiply. → Bucket 3: Behavior Variation (state-driven) → State Pattern.

---

## Common Wrong Branches

**Applying Strategy when it's actually Data Variation.** Report format is not an algorithm — it's a template. Strategy adds an interface that makes each format a class when a template engine would suffice.

**Applying Specification when it's Behavior Variation.** Pricing algorithms are not rules — they're computations. Specification is for predicates (true/false), not for algorithms that return computed values.

**Applying Factory for one type.** Factory makes sense when creation varies. A factory with one case is a one-liner that adds indirection for no value.

**Jumping to Abstract Factory.** Most Bucket 2 problems need a simple interface, not a family of factories. Add complexity when the simpler version breaks under real pressure.

---

## The Interview Answer

**Question:** How do you select the right design pattern?

**Weak answer:** *"I look at the code and match it to a pattern I know."*

**Strong answer:**

*"I classify the type of variation first. Most problems fall into data variation, object variation, behavior variation, or rules variation. Data variation usually doesn't need a pattern — parameterization is enough. Object variation calls for interfaces, composition, and sometimes Factory. Behavior variation is where most GoF patterns live — Strategy for algorithm swapping, Command when operations travel, State when behavior depends on current mode. Rules variation calls for Specification. Within each category, I start with the simplest abstraction and move up the spectrum only when the pressure justifies it. The decision tree takes about sixty seconds with practice."*

---

## Key Takeaways

- Start with the pressure, not the pattern.
- Four buckets: Data Variation, Object Variation, Behavior Variation, Rules Variation.
- Within each bucket, prefer the simpler end of the abstraction spectrum until pressure justifies more.
- 60-second shortcut: data→parameterize, object→composition, behavior→Strategy/Command/State, rules→Specification.
- Wrong branch = wrong pattern = anti-pattern (Paper 13).

---

*All papers and runnable Java samples: [github.com/pramod0s007/architect-notes](https://github.com/pramod0s007/architect-notes)*

*Previous → Paper 13: When Patterns Become Anti-Patterns | Next → Paper 15: Which Patterns Still Matter in 2026*
