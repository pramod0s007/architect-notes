# Paper 14 — Pattern Selection Decision Tree: Classification Exercises

**The tree takes 60 seconds with practice. Wrong branch = wrong pattern = Paper 13 territory.**

## The Decision Tree

```
What is varying?
├── Data / values only              -> Config map or Template, NOT Strategy
├── Which object to create          -> Interface + Factory
├── How an algorithm runs           -> Strategy Pattern
├── Rules combined in many ways     -> Specification Pattern
└── Behavior changes with state     -> State Pattern
```

## The Five Scenarios

| Scenario | Service | Symptom | Wrong Diagnosis | Correct Diagnosis | Right Pattern |
|----------|---------|---------|----------------|-------------------|---------------|
| 1 | `ReportService` | Growing format if-else | Strategy (3 classes for strings) | Data Variation | `Map<Format, Meta>` |
| 2 | `StorageService` | Provider-specific if-else | Decorator (wraps same behavior) | Object Variation | Interface + Factory |
| 3 | `PricingService` | Customer-tier if-else | Template Method (shared scaffold) | Behavior Variation | Strategy |
| 4 | `EligibilityService` | Rule combination explosion | Strategy (one strategy per product) | Rules Variation | Specification |
| 5 | `SubscriptionService` | State-dependent behavior | Strategy (swap strategy on state change) | Behavior (state-dependent) | State Pattern |

## How to Run

```bash
cd volume-4-architect-level-thinking/paper-14-pattern-selection-decision-tree/evolution

javac -d out ClassificationExercises.java

java -cp out evolution.ClassificationExercises
```

## Why Wrong Diagnoses Happen

Most wrong diagnoses skip the classification step and jump straight to a pattern
that superficially resembles the problem:

- "There's if-else" → "Use Strategy" (works for behavior variation, wrong for data variation)
- "I need to create objects" → "Use Abstract Factory" (usually a simple Factory or DI suffices)
- "Rules are complex" → "Use Strategy" (rules that combine belong in Specification)

The classification step forces you to ask: "What is actually varying?"
That single question routes you to the right branch.

## The Classification in Practice

1. Read the growing if-else.
2. Ask: "Is the varying thing a value, an object type, an algorithm, a rule, or a state?"
3. Match to the bucket.
4. Apply the pattern for that bucket.

Total time with practice: 60 seconds.
Total time without practice: 3 sprints of wrong abstractions.
