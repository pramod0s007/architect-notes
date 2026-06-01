# Paper 13 — When Patterns Become Anti-Patterns: Evolution Examples

**Ask before any pattern: What pressure is growing? What happens if we wait one sprint? What is the removal cost if we're wrong?**

## The Five Smells

| File | Smell | Wrong Tool | Right Tool |
|------|-------|-----------|-----------|
| `smell1_StrategyExplosion.java` | 3 classes to return 3 strings | Strategy (for data) | `Map<TimeOfDay, String>` |
| `smell2_FactoryHell.java` | 5-layer hierarchy for one `new` | Abstract Factory Factory | Direct instantiation or DI |
| `smell3_PrematureAbstraction.java` | Interface with one eternal implementation | Interface | Concrete class; extract when second impl appears |
| `smell4_BuilderOverkill.java` | 40-line Builder for 2 required fields | Builder | Constructor or `record` |
| `smell5_InheritanceAbuse.java` | BaseService with 12 hooks, each subclass uses 5-7 | Inheritance + Template Method | Composition + Strategy |

## How to Run

```bash
cd volume-4-architect-level-thinking/paper-13-when-patterns-become-anti-patterns/evolution

javac -d out smell1_StrategyExplosion.java smell2_FactoryHell.java \
      smell3_PrematureAbstraction.java smell4_BuilderOverkill.java \
      smell5_InheritanceAbuse.java

java -cp out evolution.smell1_StrategyExplosion
java -cp out evolution.smell2_FactoryHell
java -cp out evolution.smell3_PrematureAbstraction
java -cp out evolution.smell4_BuilderOverkill
java -cp out evolution.smell5_InheritanceAbuse
```

## The Three Questions

Before applying any pattern, ask:

1. **What pressure is growing?**
   If you can't name a specific pressure (more consumers, more formats, more types)
   you may be adding the pattern speculatively.

2. **What happens if we wait one sprint?**
   If the answer is "nothing changes" — wait. Extract the pattern when the pain arrives.
   IDEs can extract an interface in 10 seconds. You don't need to pre-emptively create it.

3. **What is the removal cost if we're wrong?**
   A Strategy class added too early is deleted when you realize a Map was enough.
   A 5-level factory hierarchy added too early takes a day to untangle.
   Calibrate the risk against the speculated benefit.

## The Diagnostic Questions per Pattern

| Pattern | Ask Before Applying |
|---------|-------------------|
| Strategy | "Would I unit-test each class independently?" If no — probably not Strategy. |
| Abstract Factory | "Do I have 2+ product families today?" If no — use a simple factory or DI. |
| Interface extraction | "Do I have a second implementation today?" If no — use concrete class. |
| Builder | "Do I have 5+ params, or optional fields, or same-type positional ambiguity?" |
| Template Method | "Do the subclasses share meaningful scaffolding, or do they just differ?" |
