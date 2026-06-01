# Paper 02 — The Four Architectural Buckets: Evolution Examples

Domain: **Report Generator** — the same symptom (growing if-else) arises from four completely different pressures. Correct diagnosis determines the correct solution.

## The Core Insight

Before prescribing a pattern, name the pressure. The symptom (if-else proliferation) is identical across all four files. The causes — and therefore the solutions — are completely different.

---

## Files

| File | Bucket | Pressure | Symptom | Solution |
|------|--------|----------|---------|----------|
| `bucket1_DataVariation.java` | Data Variation | Output format changes (json/csv/xml/html/tsv) | 5 format branches | Map registry — NOT Strategy |
| `bucket2_ObjectVariation.java` | Object Variation | Storage engine changes (MySQL/Mongo/S3/Redis) | 3 storage branches | Interface + Composition (DI) |
| `bucket3_BehaviorVariation.java` | Behavior Variation | Summarization algorithm changes | 5 algorithm branches | Strategy Pattern |
| `bucket4_RulesVariation.java` | Rules Variation | Access rules compose and grow | 6 rule branches | Specification Pattern |

---

## The Bucket Diagnostic Questions

Ask these questions when you see a growing if-else:

**1. Is the algorithm the same, only the output vocabulary changes?**
→ Data Variation. Use a Map or template. Do NOT add an interface hierarchy.
→ Example: json/csv/xml all traverse the same fields in the same order.

**2. Is the workflow stable, but a collaborating resource (DB, queue, API) changes?**
→ Object Variation. Extract an interface for the resource, inject it. The workflow class never changes again.
→ Example: validate → serialize → persist → log. Only "persist" changes.

**3. Does the computation itself differ between branches?**
→ Behavior Variation. Strategy Pattern. Encapsulate the algorithm, inject it, swap at runtime.
→ Example: brief vs visual vs narrative summaries compute fundamentally differently.

**4. Are the rules owned by a non-engineering team, composed with AND/OR, and changing independently?**
→ Rules Variation. Specification Pattern. Each rule is a named class; compose via and()/or()/not().
→ Example: access control requires "same dept AND premium tier" OR "admin" OR "public".

---

## Progression Inside Each File

Each file shows three stages:

- **v1 (Month 1)**: Two branches, correct as-is. Comments note "no pressure yet."
- **v2 (Month 7-9)**: Four to six branches, with inline comments marking each pain point and the pressure type.
- **v3 (Refactored)**: Pattern applied. Comments explain WHAT changed and WHY this specific pattern (not another) is correct.

---

## How to Run

Each file has a `main()` method in its public class. Compile and run individually:

```bash
# Compile
javac bucket1_DataVariation.java
javac bucket2_ObjectVariation.java
javac bucket3_BehaviorVariation.java
javac bucket4_RulesVariation.java

# Run
java bucket1_DataVariation
java bucket2_ObjectVariation
java bucket3_BehaviorVariation
java bucket4_RulesVariation
```

No external dependencies. All files are self-contained.

---

## Common Misdiagnosis

| What you see | Wrong call | Right call |
|---|---|---|
| Format branches (json/csv/xml) | Strategy Pattern | Data Variation → Map registry |
| Storage branches (mysql/mongo/s3) | Strategy Pattern | Object Variation → Interface + DI |
| Access rule branches | Strategy Pattern | Rules Variation → Specification |
| Algorithm branches (brief/visual/nlp) | Nothing needed | Behavior Variation → Strategy |

**The mistake**: reaching for Strategy Pattern for every if-else. Strategy is correct only when the *algorithm itself* is the variation axis.
