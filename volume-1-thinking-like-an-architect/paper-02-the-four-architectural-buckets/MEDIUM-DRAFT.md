# The Four Architectural Buckets

*Why experienced architects see the same five problems everywhere — even in codebases they've never touched.*

---

A few years ago I was reviewing a design proposal for a new reporting service. The team had three requirements:

1. Generate reports in CSV, JSON, and PDF
2. Deliver reports via email and S3 upload
3. Apply different formatting rules for enterprise vs standard customers

The proposed design had three inheritance hierarchies, a chain of decorators for format selection, and a Strategy Pattern for delivery. Twelve classes for a service that would be used by maybe four callers.

I asked the engineer to walk me through the design.

"The format selection varies," he said. "So that's Bucket Three — behavior variation. Strategy Pattern."

I asked if the CSV generation algorithm was different from the JSON generation algorithm.

He paused. "No. It's the same data. Just different output format."

"That's Bucket One," I said. "Data variation. The algorithm doesn't change — only the serializer. You don't need Strategy Pattern. You need a format parameter and a template."

He looked at the design. "What about the delivery — email vs S3?"

"That's Bucket Two. Object variation. The workflow — generate, serialize, deliver — stays the same. The object doing the delivering changes. Interface plus composition. No factory hierarchy required."

"And the enterprise formatting rules?"

"How often do they change?"

"They haven't changed in two years."

"Then it's a config property. Bucket One. Not a pattern problem at all."

Three hours later we had a design with two classes and one configuration object. The original proposal had twelve.

**The four-bucket framework isn't theoretical. It cuts design sessions from three hours to thirty minutes.**

There's a pattern I've noticed after working across payment platforms, content delivery systems, and enterprise APIs.

Senior engineers see hundreds of different design problems.

Architects see the same four.

---

## The Problem With Jumping Straight to Patterns

Most engineers take this path:

```
Code Smell
    ↓
Design Pattern
```

The smell is an if-else chain, so the answer is Strategy Pattern.
The smell is complex construction, so the answer is Builder Pattern.
The smell is nested conditionals, so the answer is... Strategy? Visitor? Specification?

Without an intermediate step, the pattern selection becomes guesswork dressed as architecture.

The intermediate step is classification.

```
Code Smell
    ↓
Pressure Classification
    ↓
Design Pattern
```

Classification narrows the field from twenty-three options to three or four. From those, the correct one is usually obvious.

---

## The Four Buckets

After working across enough production systems, almost every design problem falls into one of four categories:

---

### Bucket 1 — Data Variation

**What changes:** The data. The algorithm stays the same.

**How to recognize it:** The processing logic is identical. Only the input format, output format, or configuration values differ.

**Real examples:**
- Report generation: CSV vs JSON vs XML vs PDF — same data, different format
- Document templates: same fields, different layout per region or locale
- Configuration values: same system, different values per environment

**What this does NOT need:**
- Inheritance hierarchies
- Strategy interfaces
- Factory patterns

**What this usually needs:**
- Parameters
- Configuration objects
- Templates
- Generics

The key signal: if you can describe the variation as "same algorithm, different values," you're in Bucket 1. Reaching for patterns here adds indirection without reducing complexity.

---

### Bucket 2 — Object Variation

**What changes:** The object participating in a stable workflow.

**How to recognize it:** The sequence of operations — the workflow — stays the same. But the object that executes each step is swappable.

**Real examples:**
- Storage engine: MySQL vs MongoDB vs Cassandra — same persistence workflow, different engine
- Cloud storage: S3 vs Azure Blob vs GCS — same upload/download contract, different provider
- Notification channel: Email vs SMS vs Push — same send/track workflow, different delivery

**What this needs:**
- Interfaces (define the contract the workflow depends on)
- Composition (inject the implementation)
- Dependency Injection (wire the right object at the right time)
- Factory (centralize which implementation to build)

The key signal: if you can say "the workflow doesn't change, only who executes each step," you're in Bucket 2. The goal is to make the workflow independent of any specific implementation.

---

### Bucket 3 — Behavior Variation

**What changes:** The algorithm itself.

**How to recognize it:** The caller stays stable. The computation changes based on context — not because the input data varies, but because the processing logic itself is different.

**Real examples:**
- Encryption: AES vs DES vs RSA — same call `encrypt(text)`, completely different computation
- Pricing: premium vs employee vs partner — same call `calculatePrice(product)`, different pricing logic
- Validation: strict vs lenient vs region-specific — same call `validate(input)`, different rules applied

**What this needs:**
- Strategy Pattern (algorithm swaps at runtime or configuration time)
- Command Pattern (when operations need to be stored, reversed, or deferred)
- State Pattern (when the algorithm varies by current state of the system)

The key signal: if removing the if-else means replacing it with different implementations of the *same computation*, you're in Bucket 3. This is where most GoF behavioral patterns live.

---

### Bucket 4 — Rules Variation

**What changes:** Business rules, independently of the object structure.

**How to recognize it:** The rules themselves are the product — they grow, combine, and change on their own schedule. Adding a new rule shouldn't require modifying existing objects or services.

**Real examples:**
- Search filters: price range AND category AND rating AND availability
- Eligibility engines: age AND income AND credit score AND employment status
- Discount engines: first purchase OR loyalty tier OR promotional code OR bundle

**What this needs:**
- Specification Pattern (rules as composable predicates)
- Rule Engines (when rules are externally configured)
- Decision Tables (when the matrix is finite and stable)

The key signal: if the rules combine with AND, OR, NOT and grow faster than the object model, you're in Bucket 4. The goal is to make each rule a first-class object that can be composed, tested, and replaced independently.

---

## Why Classification Changes Everything

Without this framework, a growing if-else chain looks the same whether it's driven by data variation, behavior variation, or rules variation. The *symptom* is identical. The *pressure* — and therefore the correct solution — is different.

**Same symptom, different pressure:**

```java
// Looks the same on the surface...
if (format.equals("CSV"))  return generateCsv(data);   // ← Data Variation
if (type.equals("AES"))    return encryptAES(text);     // ← Behavior Variation
if (price > 1000)          applyDiscount(order);        // ← Rules Variation
```

Applying Strategy Pattern to the first example (report format) adds unnecessary abstraction — it's data variation, and parameterization is sufficient.

Applying Specification Pattern to the second example (encryption) adds unnecessary composability — it's behavior variation, and Strategy is simpler.

Applying Strategy Pattern to the third example (pricing rules) misses the composability requirement — rules combine, and Specification handles that better.

**The bucket tells you which family of solutions to consider. The pattern is the specific tool within that family.**

---

## Worked Classification Exercise

Here are four scenarios. Try to classify before reading the answer.

**Scenario A:** A service generates monthly financial reports. Product requests adding quarterly summaries and weekly snapshots. The calculations are the same — only the time window differs.

*Classification: Data Variation.* The algorithm doesn't change — parameterize the time window.

**Scenario B:** A notification service currently sends emails. Product wants to add SMS and push notifications. The workflow — compose message, send, track delivery — is the same.

*Classification: Object Variation.* The workflow is stable, the sender changes. Interface + composition.

**Scenario C:** A payment processing service supports three providers. Providers are added regularly as the business expands to new regions.

*Classification: Behavior Variation.* Each provider's payment logic is genuinely different. Strategy Pattern.

**Scenario D:** A loan eligibility service checks income, credit score, employment type, and regional regulations. New eligibility criteria are added quarterly.

*Classification: Rules Variation.* Rules compose and grow independently. Specification Pattern.

---

## The Mental Model in Practice

When reviewing unfamiliar code or designing a new feature, run through the four questions in order:

1. **Is only the data changing?** → Parameterize. Don't add abstractions.
2. **Is only the participating object changing?** → Interface + Composition.
3. **Is the algorithm itself changing?** → Strategy / Command / State.
4. **Are business rules growing independently?** → Specification / Rule Engine.

This takes about sixty seconds with practice.

It removes most of the ambiguity from "which pattern should I use?" before you've written a line of code.

---

## The Interview Answer

**Question:** How do you decide which design pattern to apply?

**Weak answer:** *"I look at the code smell and find the matching pattern."*

**Strong answer:**

*"I classify the type of variation first. Most design problems fall into one of four categories: data variation, object variation, behavior variation, or rules variation. The category tells me which family of solutions to consider — configuration and templates for data variation, composition and interfaces for object variation, Strategy and Command for behavior variation, Specification and rule engines for rules variation. The specific pattern is chosen after the category is clear."*

This answer shows a framework, not a reflex. That's the staff-level signal.

---

## Key Takeaways

- Most design problems belong to one of four buckets.
- Classifying the bucket narrows the pattern choice from twenty-three to three or four.
- Same symptom (if-else) can come from different pressures — classify before prescribing.
- The simplest solution within the correct bucket usually wins.

---

*All papers and runnable Java samples: [github.com/pramod0s007/architect-notes](https://github.com/pramod0s007/architect-notes)*

*Previous → Paper 01: Why Memorizing Design Patterns Is Holding You Back | Next → Paper 03: The Death of if-else*
