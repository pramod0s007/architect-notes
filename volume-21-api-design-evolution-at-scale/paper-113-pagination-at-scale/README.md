# Pagination at Scale — Offset Pagination Assumes a List That Holds Still

**Pattern:** OFFSET/LIMIT pagination against a live, changing table (silently duplicates or skips rows) → single-field cursor pagination (fixes drift from inserts, but silently drops rows tied on the sort field) → composite cursor (sort field + unique tie-breaker) with a strict total order and no ties possible

**Volume 21** (API Design & Evolution at Scale) — Paper 113.

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

OFFSET/LIMIT pagination asks "skip N rows, take M," recomputed against whatever the live row order happens to be at the moment of the request — not a stable position. A single row inserted between two page requests is enough to duplicate or skip a row, because "offset 3" no longer points at the same row it pointed at a moment ago (verified: one duplicate event shown across two pages after a single insert). Switching to a cursor built from the last row's sort value correctly stops inserts from causing drift, but the fix only works if that sort field is unique: when two rows share the exact same value — a completely ordinary batch-insert or same-millisecond scenario — the row tied with the cursor's boundary value is silently and permanently excluded (verified: 1 of 8 events never returned across a full traversal). The real fix pairs the sort field with an already-unique field (a primary key) into a composite cursor, producing a strict total order with no possible ties (verified: all 8 events, including the tied pair, returned exactly once).

## The Pressure: A Single Insert Shifts Every Later Offset

OFFSET/LIMIT never asks for a stable position — it asks for a count of rows to skip, recomputed against the live table every time. If nothing changes between two page requests this is harmless; if even one row is inserted or removed anywhere before the page boundary, "skip N" silently means something different than it meant a moment ago.

## The Principle

**A cursor is only as safe as the uniqueness of the field it's built from.** Cursor pagination fixes the specific failure of OFFSET/LIMIT — drift caused by concurrent inserts — by tracking a position instead of a count. But if the cursor field can tie between two rows, the cursor inherits a new failure: a row exactly equal to the cursor's boundary value is excluded by a "strictly after" filter, not because it was already returned, but because equality isn't after. The fix is a composite cursor: the sort field plus a field that's already guaranteed unique, so the pair can never tie.

## Pros

- A composite `(sort field, unique id)` cursor survives both concurrent inserts and duplicate sort-field values, with no known failure mode against those two specific problems (clock skew across distributed writers, cursor tampering, and re-sorting the underlying table are separate concerns this paper doesn't address)
- Cursor pagination in general scales far better than OFFSET/LIMIT on large tables, because it doesn't require the database to count and discard N rows on every request
- The fix requires no new infrastructure — just an additional field already present on almost every real table (a primary key)

## Cons

- Cursor pagination cannot jump directly to an arbitrary page number — only "next" and "previous" are well-defined, because a cursor is a position relative to a specific row
- A composite cursor is slightly more complex to encode/decode across an API boundary (two values instead of one) than a single-field cursor
- If the sort field is already provably unique on its own, a composite adds no protection it didn't already have — the fix is specifically needed when the sort field can tie

## When NOT to Use Cursor Pagination

- The dataset is small, static, or genuinely read-only for the lifetime of the request — no drift exists to protect against, and OFFSET/LIMIT's simplicity (direct page-number jumps, total page counts) is a real advantage
- Random access to an arbitrary page number is a hard product requirement — cursors don't support "jump to page 40" at all
- The sort key is already provably unique on its own — a single-field cursor is safe without a composite in that narrow case

## Code Examples in This Repo

| Example | Domain | What It Shows |
|---------|--------|--------------|
| [`pressure`](./pressure) | OFFSET/LIMIT on a live feed | A single insert between two page requests duplicates a row across pages |
| [`wrong-direction`](./wrong-direction) | Single-field cursor | Fixes insert-drift, but silently and permanently drops a row tied with the cursor's boundary value |
| [`principle-applied`](./principle-applied) | Composite cursor | `(sort field, unique id)` produces a strict total order — every row returned exactly once, regardless of ties |

### How to Run

```bash
cd volume-21-api-design-evolution-at-scale/paper-113-pagination-at-scale/pressure
javac *.java && java Main

cd volume-21-api-design-evolution-at-scale/paper-113-pagination-at-scale/wrong-direction
javac *.java && java Main

cd volume-21-api-design-evolution-at-scale/paper-113-pagination-at-scale/principle-applied
javac *.java && java Main
```
