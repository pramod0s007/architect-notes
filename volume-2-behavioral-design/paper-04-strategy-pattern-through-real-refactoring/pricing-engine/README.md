# Pricing Engine — Strategy Pattern

## What This Demonstrates

Four customer pricing tiers — Standard (0%), Premium (20%), Employee (40%),
Partner (15%) — each implemented as an independent strategy. `PriceCalculator`
never changes when the finance team adds or modifies a tier. The tier name and
discount percentage are exposed on the interface itself, so display logic needs
no `instanceof` checks.

**Pressure: Behavior Variation** — pricing algorithm differs per customer type;
new tiers are added without touching `PriceCalculator`.

## Class Diagram

```
<<interface>>
PricingStrategy
+ calculatePrice(basePrice: double, quantity: int): double
+ tierName(): String
+ discountPercent(): double
        △
        |
   ───────────────────────────────────────────────
   |                  |               |           |
StandardPricing   PremiumPricing  EmployeePricing  PartnerPricing
Strategy          Strategy        Strategy         Strategy
(0% discount)     (20% discount)  (40% discount)   (15% discount)

PriceCalculator                                   [context]
- strategy: PricingStrategy
+ PriceCalculator(strategy)
+ setStrategy(strategy): void
+ calculate(basePrice, quantity): double
+ printQuote(productName, basePrice, quantity): void
```

## Sequence / Flow

```
Client
  │
  ├─ new PriceCalculator(new StandardPricingStrategy())
  │
  ├─ calc.printQuote("Wireless Headphones", 120.00, 1)
  │       └─ strategy.calculatePrice(120.00, 1)   [StandardPricingStrategy → 120.00]
  │       └─ strategy.tierName()                  → "Standard"
  │       └─ strategy.discountPercent()           → 0%
  │
  ├─ calc.setStrategy(new PremiumPricingStrategy())
  │
  ├─ calc.printQuote("Wireless Headphones", 120.00, 1)
  │       └─ strategy.calculatePrice(120.00, 1)   [PremiumPricingStrategy → 96.00]
  │
  └─ ... repeated for Employee and Partner tiers
```

## Design Decisions

- **`tierName()` and `discountPercent()` on the interface** — `printQuote()`
  uses both without knowing which concrete strategy it holds. Eliminates
  conditional display logic and `instanceof` checks entirely.
- **`setStrategy()` on `PriceCalculator`** — supports mid-session tier changes
  (e.g., a customer upgrades to Premium during checkout without a new
  calculator instance).
- **Each strategy is stateless** — strategies hold no mutable state; they are
  safe to share across threads or reuse as singletons in production.
- **`calculatePrice` takes both `basePrice` and `quantity`** — strategies can
  apply volume-based discounts if needed (Partner tier is a natural candidate)
  without changing the interface signature.

## How to Run

```bash
cd volume-2-behavioral-design/paper-04-strategy-pattern-through-real-refactoring/pricing-engine
javac *.java && java Main
```

Expected output:

```
[Standard   ] Wireless Headphones    qty=1    base=120.00  discount=   0%  total=120.00
[Premium    ] Wireless Headphones    qty=1    base=120.00  discount=  20%  total=96.00
[Employee   ] Wireless Headphones    qty=3    base=120.00  discount=  40%  total=216.00
[Partner    ] Wireless Headphones    qty=50   base=120.00  discount=  15%  total=5100.00

Note: PriceCalculator code never changed — only the strategy.
```

## When to Apply

- Pricing rules differ by customer segment and new segments arrive regularly.
- Display code needs tier metadata (name, discount %) without switching on type.

## When NOT to Apply

- Two tiers with no roadmap for a third — a ternary operator is clearer and
  has no extra files to navigate.
