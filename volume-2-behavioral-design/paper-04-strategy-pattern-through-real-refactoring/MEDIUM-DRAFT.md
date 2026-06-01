# Strategy Pattern Through Real Refactoring

*Most tutorials teach you what Strategy Pattern looks like. This one shows you what makes it necessary.*

---

In Q3 of last year, our team picked up a ticket: *"Add HMAC-SHA256 support to the document signing service."*

The service already handled two signing algorithms — RSA-2048 and Ed25519. The implementation was a 90-line class with a switch statement:

```java
class DocumentSigner {
    byte[] sign(byte[] document, String algorithm) {
        switch (algorithm) {
            case "RSA":    return signRsa(document);
            case "ED25519": return signEd25519(document);
            default: throw new UnsupportedAlgorithmException(algorithm);
        }
    }
}
```

My first instinct was to open the switch, add a `case "HMAC"` block, ship it in two hours, and move on.

Then I checked the backlog.

Four other tickets in the next two sprints: ChaCha20-Poly1305, ECDSA P-384, ML-DSA (post-quantum), and a compliance ticket requiring algorithm deprecation tracking per customer contract.

The switch statement was about to grow from two cases to six or seven. Every new algorithm would require modifying the same class, the same test file, and in three cases, the same deployment pipeline because algorithm implementations had different secret key management requirements.

**The switch statement wasn't the problem. The growth rate was the problem.**

That's when we introduced Strategy Pattern — not because a book said to, not because it was the "right" way to write a signing service, but because the modification cost of the next five changes had clearly exceeded the abstraction cost of extracting an interface.

Here's what the definition looks like in the textbook:

> "Encapsulate a family of algorithms, make them interchangeable, and let the algorithm vary independently from the clients that use it."

And here's what that definition is missing: **the pressure that makes the encapsulation worth it.**

---

## Start With the Problem, Not the Solution

Forget the definition. Forget the UML diagram. Start here:

```java
class Encryptor {

    String encrypt(String type, String text) {
        if (type.equals("AES"))
            return encryptAES(text);

        if (type.equals("DES"))
            return encryptDES(text);

        if (type.equals("BLOWFISH"))
            return encryptBlowfish(text);

        throw new IllegalArgumentException("Unknown algorithm: " + type);
    }

    private String encryptAES(String text)      { ... }
    private String encryptDES(String text)      { ... }
    private String encryptBlowfish(String text) { ... }
}
```

This code works.

The caller passes a type string, gets encrypted text back. Tests pass. No production issues. Team is happy.

**At this point, do nothing.**

No Strategy Pattern. No interface extraction. No abstract classes. The code is solving a real problem in the simplest possible way.

This is the right starting point.

---

## The First Sign of Pressure

Three months later, a new requirement arrives.

The security team has deprecated BLOWFISH. A regulatory compliance requirement adds CHACHA20. A new partnership with a payment processor requires RSA-2048.

The `encrypt` method now looks like this:

```java
String encrypt(String type, String text) {
    if (type.equals("AES"))      return encryptAES(text);
    if (type.equals("DES"))      return encryptDES(text);
    if (type.equals("CHACHA20")) return encryptChaCha20(text);
    if (type.equals("RSA"))      return encryptRSA(text);
    // BLOWFISH removed — but 4 callers still pass "BLOWFISH"...
    throw new IllegalArgumentException("Unknown algorithm: " + type);
}
```

Every new algorithm:
- Requires modifying this method
- Requires updating the same test class
- Creates merge conflicts when two engineers add algorithms simultaneously
- Grows the method without limit

The system is now experiencing **Behavior Variation** — the same caller triggering different algorithms that change independently.

**This is the pressure.** The if-else is just the symptom.

---

## The Refactoring — Three Steps

### Step 1: Identify what varies

The caller (`Encryptor`) stays stable. The algorithm changes. That's the boundary.

Every time you see "the caller stays stable and the behavior changes," you're looking at behavior variation.

### Step 2: Extract an interface for the varying part

```java
interface EncryptionStrategy {
    String encrypt(String text);
}
```

One method. One responsibility. The interface defines the contract — not the implementation.

### Step 3: Create independent implementations

```java
class AesEncryption implements EncryptionStrategy {
    public String encrypt(String text) {
        // AES implementation
        return ...;
    }
}

class ChaCha20Encryption implements EncryptionStrategy {
    public String encrypt(String text) {
        // ChaCha20 implementation
        return ...;
    }
}

class RsaEncryption implements EncryptionStrategy {
    public String encrypt(String text) {
        // RSA implementation
        return ...;
    }
}
```

Each algorithm is now a self-contained class. Independently testable. Independently deployable. Independent of everything else.

### Step 4: Use composition in the caller

```java
class Encryptor {

    private final EncryptionStrategy strategy;

    Encryptor(EncryptionStrategy strategy) {
        this.strategy = strategy;
    }

    String encrypt(String text) {
        return strategy.encrypt(text);
    }
}
```

The `Encryptor` no longer knows which algorithm is running. It delegates to whatever was injected.

---

## What Actually Changed

Many developers answer: *"We implemented Strategy Pattern."*

Architects answer: *"We isolated behavior variation."*

That distinction matters more than it sounds.

If you start with "let's implement Strategy Pattern," you might apply it to code that doesn't have behavior variation — and create unnecessary complexity.

If you start with "behavior is varying faster than the caller can absorb it," you naturally arrive at an interface extraction and composition. You call it Strategy Pattern afterward. Or you don't call it anything. The name is optional. The pressure-relief is not.

---

## The Pressure Flow

```
Behavior Variation
(same caller, multiple swappable algorithms)
        ↓
Cost of modification exceeds cost of abstraction
        ↓
Interface extraction
        ↓
Independent implementations
        ↓
Composition replaces conditional
        ↓
Strategy Pattern
```

The entry condition matters: **cost of modification exceeds cost of abstraction.**

Before that threshold, the if-else is cheaper. After it, the interface is cheaper. Strategy Pattern exists to cross that threshold cleanly.

---

## Three Real-World Examples

The encryption service is one example. The pattern appears across domains wherever behavior varies independently of its caller.

### Payment Gateway

```java
// Before: behavior variation in caller
if (provider.equals("PAYPAL"))   processPayPal(amount);
if (provider.equals("STRIPE"))   processStripe(amount);
if (provider.equals("RAZORPAY")) processRazorpay(amount);

// After: variation isolated
interface PaymentStrategy {
    void pay(double amount);
}

// Caller becomes:
paymentStrategy.pay(amount);
```

New payment provider: one new class. Existing callers: unchanged.

### Pricing Engine

```java
// Before: pricing rule embedded in caller
if (customer.isPremium())    return premiumRate * basePrice;
if (customer.isEmployee())   return employeeRate * basePrice;
if (customer.isPartner())    return partnerRate * basePrice;
return basePrice;

// After: pricing logic extracted
interface PricingStrategy {
    double calculatePrice(Product product);
}
```

New pricing tier (seasonal discount, B2B negotiated rate): one new class. The method that calls it doesn't change.

### Sorting Algorithm

```java
// Before: sort selection in caller
if (dataSize < THRESHOLD) bubbleSort(data);
else                      mergeSort(data);

// After: algorithm swappable
interface SortStrategy {
    void sort(int[] data);
}
```

Switching from merge sort to radix sort for a specific data profile: inject a different implementation. No modification to the caller.

---

## What Strategy Pattern Is NOT

**Not a replacement for all if-statements.**

If your condition controls flow rather than swapping algorithms, Strategy is the wrong tool:

```java
// Flow control — not behavior variation — leave this alone
if (user.isAuthenticated()) {
    renderDashboard();
} else {
    redirectToLogin();
}
```

**Not required for two algorithms.**

Two implementations with stable requirements often don't justify the interface. A simple `boolean useNewAlgorithm` flag or a direct method call is clearer. Add the interface when the third algorithm arrives and the pattern stops feeling speculative.

**Not the same as inheritance.**

Strategy uses composition. You inject the algorithm. Inheritance bakes it in. Composition wins when the algorithm needs to change at runtime or when you need to test the caller independently.

---

## The Interview Answer That Signals Staff Level

**Question:** When should Strategy Pattern be used?

**Weak answer:** When there are many if-else statements.

**Strong answer:**

*"Strategy Pattern addresses behavior variation — when the same caller needs to trigger different algorithms and those algorithms are expected to grow or change independently. The right time to introduce it is when the cost of modifying the conditional chain starts to exceed the cost of the abstraction: merge conflicts, test sprawl, or caller modification for every algorithm change are all signals. Before that threshold, a conditional is usually simpler. After it, an interface extraction and composition is the right move."*

Key elements of the strong answer:
- Names the pressure (behavior variation), not the symptom (if-else)
- Identifies the threshold condition for introduction
- Acknowledges when *not* to use it

---

## Runnable Code

Full before/after implementation:

```
code-samples/strategy/encryption-example/
├── EncryptionStrategy.java   ← the interface
├── AesEncryption.java        ← implementation
├── DesEncryption.java        ← implementation
├── Encryptor.java            ← the caller (uses composition)
└── Main.java                 ← wires it together
```

See: [github.com/pramod0s007/architect-notes/tree/main/code-samples/strategy](https://github.com/pramod0s007/architect-notes/tree/main/code-samples/strategy/encryption-example)

---

## Lambda Strategies — Modern Java

In modern Java (8+), Strategy Pattern often doesn't require an explicit interface or named classes. Functional interfaces serve as lightweight strategies:

```java
// Classic Strategy
interface PricingStrategy {
    double calculatePrice(Product product, Customer customer);
}

class PremiumPricingStrategy implements PricingStrategy {
    public double calculatePrice(Product product, Customer customer) {
        return product.getBasePrice() * 0.85; // 15% off
    }
}

// Modern Java — same concept, less ceremony
Function<Product, Double> premiumPricing = p -> p.getBasePrice() * 0.85;
Function<Product, Double> employeePricing = p -> p.getBasePrice() * 0.70;
Function<Product, Double> standardPricing = p -> p.getBasePrice();

Map<CustomerType, Function<Product, Double>> pricingStrategies = Map.of(
    PREMIUM,  p -> p.getBasePrice() * 0.85,
    EMPLOYEE, p -> p.getBasePrice() * 0.70,
    STANDARD, p -> p.getBasePrice()
);

double price = pricingStrategies
    .getOrDefault(customer.getType(), standardPricing)
    .apply(product);
```

When to use a full interface vs a lambda:
- **Lambda:** Single method, simple logic, no shared state, no need to unit-test the strategy in isolation
- **Full class:** Complex logic, shared dependencies (repositories, services), strategy needs its own unit tests, multiple methods in the contract

The pressure is the same. The implementation choice depends on complexity.

---

## Key Takeaways

- Strategy Pattern solves **behavior variation** — not if-statements in general.
- The pattern is introduced when **modification cost exceeds abstraction cost**.
- Composition replaces conditional growth. The caller becomes stable.
- Name the pressure first. The pattern name is optional.
- Two algorithms rarely justify the interface. Three usually do.

---

*All papers and runnable Java samples: [github.com/pramod0s007/architect-notes](https://github.com/pramod0s007/architect-notes)*

*Previous → Paper 03: The Death of if-else | Next → Paper 05: State Pattern Through a StopWatch*
