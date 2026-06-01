# Specification Pattern

*When your eligibility logic has more branches than your feature code, you have a rules variation problem. Here's what to do about it.*

---

A lending platform I know of started their loan eligibility check with one rule.

```java
boolean isEligible(Customer customer) {
    return customer.getAge() >= 18;
}
```

Clean. Obvious. Correct. Shipped in a single PR.

Three months later, the compliance team required a minimum income threshold. One more condition. Still manageable.

Six months in, the legal team flagged regional lending regulations — different credit score minimums for different US states, EU income thresholds that didn't match the US rules, and a blacklist check against a fraud bureau that returned asynchronously.

Nine months in, the product team introduced a "premium member override" — existing premium customers with a paid membership could bypass the income check. The marketing team added a promotional window where the credit score threshold dropped by 50 points for new users acquired through partner channels.

I joined the team at the twelve-month mark. My first task was to add one new eligibility rule: customers with outstanding loans at competitor institutions above a certain amount were ineligible.

I opened the file. Here's what `isEligible()` looked like at that point:

```java
boolean isEligible(Customer customer) {
    if (customer.getAge() < 18) return false;
    if (customer.getIncome() < MIN_INCOME) return false;
    if (customer.getCreditScore() < MIN_CREDIT_SCORE) return false;
    if (customer.isBlacklisted()) return false;
    if (customer.getCountry().equals("US") && customer.getState().equals("NY"))
        return customer.getCreditScore() >= NY_CREDIT_THRESHOLD;
    if (customer.isPremium() && customer.hasPaidMembership())
        return true; // premium override
    return !customer.hasOutstandingLoans();
}
```

This method is 200 lines in production systems. It has sixteen edge cases. Four teams touch it. Nobody fully understands the interaction between the premium override and the regional regulation.

**This is rules variation.** Business rules growing independently, combining in ways that no single method can cleanly represent.

---

## Why the Standard Tools Don't Fit

Rules variation is classified as Bucket 4 in Paper 02. It's distinct from the other buckets:

- **Data variation** → parameterize
- **Object variation** → composition
- **Behavior variation** → Strategy, Command, State
- **Rules variation** → Specification

The instinct is often to apply Strategy Pattern — one strategy per rule, composed in the caller. This gets awkward fast. Strategy isolates *algorithms*. A rule is not an algorithm — it's a predicate. Rules compose. Strategies don't naturally compose with AND, OR, NOT.

The instinct is sometimes to use a rule engine library. This works at scale. It's overkill for thirty rules and a team of five.

Specification Pattern is the middle path. It's a pattern, not a platform. It handles composition natively. It keeps rules in code where they can be tested, reviewed, and version-controlled.

---

## The Specification Abstraction

A specification is a composable predicate:

```java
interface Specification<T> {
    boolean isSatisfiedBy(T candidate);

    default Specification<T> and(Specification<T> other) {
        return candidate -> this.isSatisfiedBy(candidate) && other.isSatisfiedBy(candidate);
    }

    default Specification<T> or(Specification<T> other) {
        return candidate -> this.isSatisfiedBy(candidate) || other.isSatisfiedBy(candidate);
    }

    default Specification<T> not() {
        return candidate -> !this.isSatisfiedBy(candidate);
    }
}
```

Each rule becomes a class:

```java
class MinimumAgeSpecification implements Specification<Customer> {
    private final int minAge;

    MinimumAgeSpecification(int minAge) { this.minAge = minAge; }

    public boolean isSatisfiedBy(Customer customer) {
        return customer.getAge() >= minAge;
    }
}

class MinimumIncomeSpecification implements Specification<Customer> {
    public boolean isSatisfiedBy(Customer customer) {
        return customer.getIncome() >= MIN_INCOME;
    }
}

class BlacklistSpecification implements Specification<Customer> {
    public boolean isSatisfiedBy(Customer customer) {
        return customer.isBlacklisted();
    }
}
```

The eligibility check becomes composition:

```java
Specification<Customer> eligibility =
    new MinimumAgeSpecification(18)
        .and(new MinimumIncomeSpecification())
        .and(new MinimumCreditScification())
        .and(new BlacklistSpecification().not());

boolean isEligible = eligibility.isSatisfiedBy(customer);
```

---

## What This Buys You

**New rule:** One new class. No modification to existing specifications.

**New combination:** Compose existing specifications differently. `eligibility.or(premiumOverride())`.

**Unit testing:** Each rule is independently testable with a single `isSatisfiedBy()` assertion.

**Readable at a glance:** The composition at the call site reads as a sentence.

```java
Specification<Customer> standardEligibility =
    minimumAge(18)
        .and(minimumIncome(MIN_INCOME))
        .and(minimumCredit(MIN_CREDIT))
        .and(not(blacklisted()));

Specification<Customer> premiumEligibility =
    standardEligibility.or(premiumMemberOverride());

Specification<Customer> nyEligibility =
    standardEligibility.and(nyRegionalRequirement());
```

Three eligibility profiles. All built from the same reusable pieces. No duplication.

---

## Testing with Specifications — The Hidden Benefit

The unit testing story is worth its own section.

Before Specification Pattern, testing `isEligible()` required constructing a `Customer` object for every combination of rules:

```java
@Test
void rejectsBlacklistedCustomerEvenWithHighCredit() {
    Customer customer = Customer.builder()
        .age(30).income(80000).creditScore(750)
        .blacklisted(true)   // the thing we're testing
        .build();

    assertFalse(eligibilityService.isEligible(customer));
}
```

With Specification Pattern, each rule tests in isolation:

```java
@Test
void blacklistSpecificationRejectsBlacklistedCustomer() {
    Specification<Customer> spec = new BlacklistSpecification();
    Customer blacklisted = Customer.builder().blacklisted(true).build();
    Customer clean = Customer.builder().blacklisted(false).build();

    assertFalse(spec.isSatisfiedBy(blacklisted));
    assertTrue(spec.isSatisfiedBy(clean));
}
```

And compositions test separately:

```java
@Test
void standardEligibilityRequiresAllCriteria() {
    Specification<Customer> eligibility = standardEligibility();

    // Test one failing criterion at a time — others maximally satisfied
    assertFalse(eligibility.isSatisfiedBy(underageCustomer()));
    assertFalse(eligibility.isSatisfiedBy(lowIncomeCustomer()));
    assertFalse(eligibility.isSatisfiedBy(lowCreditCustomer()));
    assertFalse(eligibility.isSatisfiedBy(blacklistedCustomer()));
    assertTrue(eligibility.isSatisfiedBy(qualifiedCustomer()));
}
```

**Each rule has focused tests. Each composition has integration tests. The coverage matrix is explicit rather than implicit.**

Before: one 200-line method, dozens of test scenarios, unclear which rule is being tested by which case.

After: each rule has two or three tests (passes/fails/edge case), the composition test verifies the wiring, and every test name says exactly what it's testing.

## The Naming Convention — Make Rules Read Like Sentences

Specification Pattern is most valuable when the composition site reads as a business requirement.

Use factory methods or static builders named for the business concept, not the technical implementation:

```java
// Technical names — reads like code
new MinimumAgeSpecification(18)
    .and(new MinimumIncomeSpecification(MIN_INCOME))
    .and(new MinimumCreditScoreSpecification(MIN_CREDIT))
    .and(new BlacklistSpecification().not())

// Business names — reads like a requirement
minimumAge(18)
    .and(minimumIncome(MIN_INCOME))
    .and(minimumCreditScore(MIN_CREDIT))
    .and(not(onBlacklist()))
```

The second form can be read aloud in a product meeting: "minimum age 18 AND minimum income AND minimum credit AND not on blacklist."

When a compliance requirement arrives as a written rule — "customers must be 18+ with income above threshold and credit score above minimum, unless they are premium members with paid status" — the specification composition should look like a direct translation of that sentence.

**When a non-engineer can read your Specification composition and recognize the business rule, the pattern has done its job.**

---

## Where This Appears in Production

**Product search and filtering:**

```java
Specification<Product> filters =
    priceBelow(maxPrice)
        .and(categoryIs(selectedCategory))
        .and(ratingAbove(minRating))
        .and(inStock());

List<Product> results = productRepository.findAll(filters);
```

**Discount qualification:**

```java
Specification<Order> qualifiesForDiscount =
    firstPurchase()
        .or(loyaltyTierAbove(GOLD))
        .or(orderValueAbove(500));
```

**Access control:**

```java
Specification<User> canAccessDocument =
    hasRole(EDITOR)
        .or(hasRole(ADMIN))
        .and(not(accountSuspended()));
```

**Feature eligibility (A/B testing):**

```java
Specification<User> inTreatmentGroup =
    inPercentile(0, 50)
        .and(countryIs("US"))
        .and(not(inExclusionList()));
```

The pattern works wherever rules combine and grow independently.

---

## Integration with Persistence

In Spring Data / JPA systems, Specification pairs naturally with the `JpaSpecificationExecutor` interface:

```java
public interface CustomerRepository
    extends JpaRepository<Customer, Long>,
            JpaSpecificationExecutor<Customer> { }

// Usage:
Specification<Customer> spec = isActive().and(inRegion("EU"));
List<Customer> customers = repository.findAll(spec);
```

The same rule objects that drive business logic can drive database queries. One set of rules, two use cases.

---

## Migrating from a Monolithic Conditional

The migration path is mechanical once you recognize the signal. If `isEligible()` has grown past five conditions and multiple teams have touched it in the last quarter, the move is straightforward.

**Step 1: Extract each condition as a named specification.**

Every distinct `if` block becomes one class. Name it after the business concept, not the implementation. `customer.getCreditScore() < MIN_CREDIT_SCORE` becomes `MinimumCreditScoreSpecification`.

**Step 2: Replace the method body with a composed specification.**

The original conditional logic maps directly to `.and()` and `.or()` calls. Boolean short-circuit behavior is preserved — AND stops at the first failure, OR stops at the first success.

**Step 3: Write one focused test per specification class.**

This is the step that reveals the value. Tests that previously required a fully-constructed object with twelve fields now need only the fields the specification cares about.

**Step 4: Introduce factory methods for the compositions.**

Group related specifications into factory methods — `standardEligibility()`, `premiumEligibility()`, `regionalEligibility("NY")` — so the call sites read as business language. The specification classes are implementation details; the factory methods are the API.

This migration can be done incrementally. Move one rule at a time. The monolithic method and the specification-based approach can coexist during the transition — the old method stays as a compatibility layer while new rules are added as specifications.

---

## When Not to Use It

**Three static rules that never change.** A method with three `if` statements and no growth signal is cleaner than a Specification hierarchy. The overhead is not justified.

**Performance-critical hot paths.** Specification composition creates lambda chains. In extremely hot paths (millions of evaluations per second), pre-compiled predicates or lookup tables may be faster. Measure before optimizing.

**Rules that come from external configuration.** If rules are stored in a database and change at runtime without a deployment, a rule engine (Drools, Easy Rules, OpenL Tablets) is a better fit. Specification works well for rules that change with code.

---

## The Interview Answer

**Question:** When should Specification Pattern be used?

**Weak answer:** *"For all filtering logic."*

**Strong answer:**

*"Specification Pattern addresses rules variation — when business rules must be composed with AND/OR/NOT, reused across different contexts, and tested independently. The key signal is rules growing faster than the object model and combining in ways that produce unreadable nested conditionals. The pattern turns each rule into a composable predicate object. It's most valuable when the same rule appears in multiple combinations — eligibility checks, search filters, discount qualification, access control. If you have three static rules that never change, an inline method is cleaner. If rules grow, combine, and get tested independently, Specification earns its complexity."*

---

## Key Takeaways

- Specification solves **rules variation** — rules that compose and grow independently.
- Each rule is a class. Rules combine with `.and()`, `.or()`, `.not()`.
- Unit testing becomes trivial — each rule is one `isSatisfiedBy()` assertion.
- Pairs with JPA `Specification` for database-level rule application.
- Don't use it for three static rules. Use it when rules multiply and combine.

---

*All papers and runnable Java samples: [github.com/pramod0s007/architect-notes](https://github.com/pramod0s007/architect-notes)*

*Previous → Paper 08: Lookup Tables vs Polymorphism | Next → Paper 10: Chain of Responsibility*
