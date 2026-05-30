# Decision Tree Walkthroughs

## Behavior branch

**Symptom:** Growing `if (type)` for algorithms.

**Tree:** Behavior → Strategy.

**Paper:** 04 + `code-samples/strategy/encryption-example`

## Rules branch

**Symptom:** Composable eligibility filters.

**Tree:** Rules → Specification.

**Paper:** 09 + `code-samples/specification/product-search`

## Interaction branch

**Symptom:** Pairwise type interactions (collision engine).

**Tree:** Behavior → Visitor **or** Lookup (Paper 08) depending on matrix stability.

## Object branch

**Symptom:** Many `new` for channel/storage/payment types.

**Tree:** Object → Factory (Paper 12).

## Construction branch

**Symptom:** 12-parameter constructors.

**Tree:** Object → Builder (Paper 11).
