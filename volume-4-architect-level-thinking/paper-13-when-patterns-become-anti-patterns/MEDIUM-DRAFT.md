# When Patterns Become Anti-Patterns

*The most dangerous engineer in any codebase is not someone who doesn't know design patterns. It's someone who knows them too well.*

---

In my third year as a backend engineer, I inherited an alert notification service.

The service had one job: when a monitoring threshold was breached, send an email to the on-call engineer.

The original engineer — a thoughtful, experienced developer — had spent six weeks building it. The architecture review deck had nine slides. The class diagram had thirty-two boxes.

Here's what the entry point looked like:

```java
AlertNotificationContext context = AlertNotificationContextBuilder
    .newBuilder()
    .withAlertType(AlertTypeFactory.resolve(alert.getType()))
    .withRecipientStrategy(RecipientStrategyRegistry.get(alert.getSeverity()))
    .withDeliveryPolicy(DeliveryPolicyFactory.create(DeliveryChannel.EMAIL))
    .withNotificationTemplate(TemplateResolver.resolve(alert.getType(), Locale.EN))
    .build();

NotificationDispatcher.getInstance().dispatch(context);
```

Six objects to send one email.

The `RecipientStrategyRegistry` had one registered strategy: `OnCallEngineerRecipientStrategy`. The `DeliveryPolicyFactory` had one `create()` case: `EmailDeliveryPolicy`. The `TemplateResolver` had one template per alert type, all of which appended the alert message to the same boilerplate sentence.

I spent four days understanding the codebase before I could change the email subject line.

When I finally asked the original engineer why it was built this way, he said: *"We might need SMS in the future. And different recipient groups. And localization."*

Eighteen months later: still email only. Still one recipient group. Still English only.

The original engineer thought they were writing extensible software. It had patterns everywhere.

It had no pressure anywhere.

---

## Patterns Solve Pressure — Not Problems

The core idea of this series is simple:

**Patterns are responses to pressure. Without pressure, they are noise.**

But here's what nobody tells you in pattern books:

The same pattern that rescues a codebase under genuine pressure will destroy a codebase that hasn't earned it yet.

Strategy Pattern applied to a single algorithm is not good design — it's a single-implementation interface that confuses everyone who reads it.

Factory Pattern applied to one `new` call is not extensibility — it's indirection that makes the code harder to trace.

Builder Pattern applied to a three-field object is not fluency — it's ceremony that adds ten lines to accomplish what a constructor does in one.

**A pattern without pressure is technical debt disguised as architecture.**

---

## The Pattern Maturity Curve

Most engineers go through three phases with design patterns. Understanding which phase you're in changes everything.

### Phase 1 — Discovery

You read the book. You watch the conference talk. You finally understand what Strategy Pattern is.

And then you see it everywhere.

The encryption switch? Strategy. The notification if-else? Strategy. The pricing conditional? Strategy. The two-line logging decision? Absolutely Strategy.

This phase is exciting. You feel like you've unlocked a new layer of the codebase. You're not wrong that the patterns could fit — you're wrong that they should.

**The Discovery phase is dangerous because the patterns are always technically applicable.**

A Strategy interface can always replace an if-else. The question is never *can it* — the question is *does the pressure justify it*.

### Phase 2 — Overuse

You start applying patterns before they're earned.

Common symptoms:

- Interfaces that have had exactly one implementation for eighteen months
- Factories that create a single object type
- Builders for objects with three fields and no invariants
- Visitor applied to a four-pair matrix that a lookup table would solve in twenty lines
- Abstract base classes with protected hooks for behavior that never varies

The system looks sophisticated. It passes code review because the reviewers recognize the patterns. Nobody flags it because everything is technically correct.

But complexity grows faster than business value. New engineers take twice as long to get productive. Bug fixes require understanding seven layers before changing one line. Tests become hard to write because the abstraction surface is huge.

**Overuse is the most expensive phase because it's the hardest to detect. The code looks like it's following best practices.**

### Phase 3 — Architectural Judgment

The question stops being "Can I use this pattern?" and becomes "Should I use this pattern?"

At this stage you measure pressure before reaching for abstractions.

You ask:
- How many real implementations exist today?
- How often does the behavior actually change?
- What is the cost of modification without the abstraction?
- What is the removal cost if I'm wrong?
- Can a junior engineer read this in one sitting?

If the answers don't justify the pattern, you don't introduce it.

**Phase 3 is where architectural thinking begins. It's also where most interview rubrics separate senior from staff.**

---

## Five Named Anti-Patterns to Recognize in Review

### 1. Strategy Explosion

**Symptom:** One interface, one real implementation, and comments like "we'll add more strategies later."

```java
interface GreetingStrategy {
    String greet(User user);
}

class StandardGreeting implements GreetingStrategy {
    public String greet(User user) {
        return "Hello, " + user.getName();
    }
}
```

One greeting. One implementation. Three files. Zero pressure.

**Fix:** Inline until the second real algorithm appears. "We'll add more later" is not design pressure — it's speculation.

### 2. Factory Hell

**Symptom:** Factories creating factories. Callers still know every concrete type — but through three layers of indirection.

```java
AbstractNotificationFactoryFactory
    → AbstractNotificationFactory
        → EmailNotificationFactory
            → EmailNotification
```

**Fix:** A factory method or DI registration. If all your callers can enumerate every type, the factory isn't hiding creation — it's hiding it badly.

### 3. Inheritance Abuse

**Symptom:** A `BaseService` with 40 protected hooks. Subclasses that override seven methods to change one behavior. A hierarchy so deep that reading any method requires tracing five parent classes.

```java
class BasePaymentService {
    // 40 protected methods, 12 abstract hooks
}

class StripePaymentService extends BasePaymentService {
    // overrides 7 methods to change the charge call
}
```

**Fix:** Composition. Inject the one varying step as a Strategy or a functional interface. Flatten the hierarchy. The only thing inheritance earns is substitutability — and you rarely need all forty methods to be substitutable.

### 4. Visitor Overengineering

**Symptom:** Visitor Pattern applied to a 3×3 or 4×4 collision matrix.

Four object types. Four interactions. A visitor hierarchy with four `visit` methods and four `accept` methods — sixteen methods to replace a lookup table that fits in twenty lines.

**Fix:** `Map<CollisionKey, CollisionAction>`. Tables beat polymorphism when the matrix is finite and stable. Save Visitor for when you have a genuinely stable type hierarchy with many operations (Paper 07 covers when each wins).

### 5. Premature Abstraction

**Symptom:** `Repository` interface with one JPA implementation. `NotificationService` interface with one `EmailNotificationService`. `StorageClient` interface that has never had a second backing store.

```java
// The interface
interface UserRepository {
    User findById(Long id);
    void save(User user);
}

// The only implementation, for 2 years
class JpaUserRepository implements UserRepository { ... }
```

**Fix:** Concrete class until the second implementation is real. "What if we switch databases?" is a hypothetical. Real pressure is measurable. You can always extract an interface when a second real implementation appears — extracting it costs one IDE refactor. Carrying the abstraction for two years costs every engineer who reads the code.

---

## The Architect Rule — Ask Before Every Pattern

Before applying any pattern, three questions:

**1. What pressure is growing?**
Measure it. If you can't point to a concrete growth signal — merge conflicts, change frequency, failing tests, onboarding friction — the pressure might not exist yet.

**2. What happens if we wait one more sprint?**
If the answer is "nothing bad," wait. The last responsible moment has not arrived.

**3. What is the removal cost if we're wrong?**
A concrete class costs one refactor to extract. An abstract hierarchy costs weeks to collapse. Low removal cost = safer to wait.

---

## The Interview Signal

Most patterns-related interview questions test whether you know the patterns.

Staff-level questions test whether you know when *not* to use them.

**Weak answer to "Tell me about a time you removed a pattern":**
*"We switched to microservices."*

**Strong answer:**
*"We had a Repository interface that had one JPA implementation for eighteen months. Onboarding new engineers took longer because they had to understand the indirection. We removed the interface, made the repository concrete, and added tests that documented the behavior directly. When we eventually needed a Redis cache layer, we reintroduced the interface then — with a real second implementation. Removal cost was one refactor. The two years of carrying that abstraction cost hundreds of engineer-hours of cognitive overhead."*

The strong answer has:
- Measurable pain
- The simpler replacement
- The condition that would reintroduce the pattern
- A quantified trade-off

That's architectural judgment. That's what Phase 3 looks like in an interview.

---

## The Simpler Structure Usually Wins

Before reaching for a pattern, consider:

| Instead of | Try first |
|------------|-----------|
| Strategy with one implementation | Direct method or lambda |
| Factory for one type | `new` at the call site |
| Builder for three fields | Constructor or named static factory |
| Visitor for four pairs | `Map<Key, Action>` |
| Abstract base with hooks | Composition + interface |

Patterns exist for when the simpler structure breaks under pressure. Until then, simpler wins.

---

## The Maturity Signal

The difference between an engineer in Phase 1 and a staff engineer isn't pattern knowledge.

It's pattern restraint.

Phase 1: "I can apply Strategy here."

Phase 3: "Strategy would work, but the second implementation doesn't exist yet and the growth signal is unclear. I'll use a direct method and add a comment that documents the future extension point."

One year from now, if the growth signal appears, the interface extraction is a ten-minute refactor. If it doesn't appear, you avoided two years of unnecessary complexity.

> *"A pattern without pressure is not architecture. It is ceremony."*

---

*All papers and runnable Java samples: [github.com/pramod0s007/architect-notes](https://github.com/pramod0s007/architect-notes)*

*Previous → Paper 12: Factory Pattern | Next → Paper 14: Pattern Selection Decision Tree*
