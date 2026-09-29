# Query Optimizer & Cost-Based Plan Selection — An Index Isn't Automatically the Fast Path

**Pattern:** Always use the index for an equality filter (a 950-of-1000-row query costs 9505 estimated units, versus a full scan's 1000) → always full-scan instead (the 950-row query is fixed at 1000, but a different, highly selective 50-of-1000-row query now costs 1000 instead of the index's 505) → cost-based selection, estimating both plans from real statistics per query (both queries get the objectively cheaper plan — 1000 and 505 respectively)

**Volume 23** (Database Internals for Architects) — Paper 126.

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

An index turns "find matching rows" into one random-access lookup per matching row, a cost that scales with how many rows match — while a full scan turns the same question into one linear pass over the whole table, a cost that scales with table size regardless of match count. Neither is universally cheaper: on a 1,000-row table with a skewed status column (950 "active", 50 "inactive"), always using the index for a filter costs 9505 estimated units for the low-selectivity "active" query, nearly ten times a full scan's 1000 (verified). Switching to a fixed "always full scan" rule fixes that query, but the same rule now makes the highly selective "inactive" query (only 50 of 1,000 rows) cost 1000 instead of the index's 505 (verified) — the same class of failure, just relocated onto a different query. The actual fix estimates both plans' real cost from real statistics — the index's own matching-row count — for each specific query, and picks whichever is genuinely cheaper (verified: both queries get the objectively cheaper plan, something neither fixed rule managed for both at once).

## The Pressure: Always Using the Index Ignores Selectivity

An index being available for a column says nothing about whether using it is the cheaper choice for a specific filter — that depends entirely on how many rows the filter matches. A filter that matches most of the table turns an index-based plan into almost as many random-access operations as there are rows, which is dramatically more expensive than one linear pass over the same rows.

## The Principle

**Whether an index or a full scan is cheaper depends entirely on selectivity, and a query optimizer's job is estimating that from real statistics for each specific query — not applying the same fixed rule to every query it sees.** An index-scan plan's cost scales with the number of matching rows; a full-scan plan's cost scales with total table size, independent of how selective the filter is. Neither plan dominates the other in general — which one wins flips depending on where a given query's selectivity falls, and that has to be estimated per query using real, current statistics (like the index's own matching-row count), not assumed once and applied everywhere.

## Pros

- Gets the cheapest available plan for every query, verified across two queries with opposite selectivity (950/1000 vs 50/1000), where either fixed rule only wins for one of the two
- The cost estimate is computed from real statistics gathered at query time (the index's own matching-row count), not from an assumption baked in ahead of time about which plans tend to be faster
- Degrades gracefully as data distribution shifts — a column that used to be low-selectivity and becomes high-selectivity (or vice versa) is re-evaluated on its own terms every time, with no rule to go stale

## Cons

- Requires the optimizer to maintain accurate, reasonably fresh statistics (row counts, value distributions) — stale statistics can make a correct cost formula produce a wrong estimate anyway
- The cost model itself (the relative weight given to random vs. sequential access, for instance) has to reasonably reflect the real storage engine's actual behavior, or the "cheaper" plan on paper won't be cheaper in practice
- More work per query than a fixed rule — estimating multiple plans' costs has its own (much smaller) overhead, though it's negligible next to the cost difference it's protecting against

## When a Fixed Rule Is Still Acceptable

- The table's data distribution is well understood, stable, and never expected to change — e.g. a lookup table where one column is always highly selective by design
- Query volume and variety are low enough that manually verifying the one plan the fixed rule produces is genuinely cheaper is realistic and gets re-checked when the schema changes
- The system is small enough, or the cost difference is small enough, that neither wrong-plan scenario meaningfully affects users — this removes the entire motivation for cost-based selection in the first place

## Code Examples in This Repo

| Example | Domain | What It Shows |
|---------|--------|--------------|
| [`pressure`](./pressure) | Always use the index | A 950-of-1000-row query costs 9505 estimated units via the index versus a full scan's 1000 |
| [`wrong-direction`](./wrong-direction) | Always full-scan | Fixes the 950-row query (cost 1000), but a different 50-of-1000-row query now costs 1000 instead of the index's 505 |
| [`principle-applied`](./principle-applied) | Cost-based selection | Both queries get the objectively cheaper plan — full scan (1000) for the 950-row query, index scan (505) for the 50-row query |

### How to Run

```bash
cd volume-23-database-internals-for-architects/paper-126-query-optimizer-cost-based-plans/pressure
javac *.java && java Main

cd volume-23-database-internals-for-architects/paper-126-query-optimizer-cost-based-plans/wrong-direction
javac *.java && java Main

cd volume-23-database-internals-for-architects/paper-126-query-optimizer-cost-based-plans/principle-applied
javac *.java && java Main
```
