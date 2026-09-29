# GraphQL N+1 — Fixing Duplicate Calls Isn't Fixing the N+1 Problem

**Pattern:** Naive per-field resolver (1 query for the list + N round trips, one per resolved field) → per-request memoization (removes duplicate keys, but provides zero benefit when a query's keys happen to all be distinct — the normal case for most feeds) → collect-then-batch fetching, DataLoader-style (exactly 1 round trip for the whole query, independent of how much duplication exists)

**Volume 21** (API Design & Evolution at Scale) — Paper 116.

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

The N+1 problem is structural: resolving a list of N objects, each with a nested field requiring its own fetch, costs 1 round trip for the list plus up to N more — one per object — because each field resolver runs independently, with no visibility into its siblings (verified: an 8-post feed with author fields selected costs 1 + 8 round trips). Memoizing lookups within the request is a real, correct fix for duplicate keys, but its benefit depends entirely on how much duplication a given query's data happens to contain — an equally realistic feed where every post has a different author sees zero improvement from memoization (verified: 6 round trips for 6 distinct authors, identical to the unmemoized resolver). The real fix collects every distinct key the entire query needs before fetching anything, then issues one batched fetch for all of them — a DataLoader-style approach that costs exactly 1 round trip whether the query's keys are 100% duplicated or 0% (verified: 1 round trip for both the repeated-author feed and the fully-distinct-author feed).

## The Pressure: One Round Trip Per Resolved Field

A GraphQL field resolver does exactly what the execution model asks: resolve this field, for this object, right now. For a list of N objects each needing a nested fetch, that's N independent round trips — not because any single resolver is buggy, but because the execution model gives resolvers no visibility into the other objects being resolved in the same query.

## The Principle

**Fixing N+1 means changing when a fetch happens, not just reducing how many times an identical fetch repeats.** Memoization is a real optimization, but it only removes round trips for keys that recur within a request — it does nothing for a query whose keys are all distinct, which is not an edge case, it's the ordinary shape of most real feeds. This is a different concern from the pagination consistency problem covered earlier in this series (Paper 113): that paper is about a single query returning inconsistent results under concurrent writes; this one is about the cost of resolving nested fields within an otherwise-correct query, and it appears even with zero write traffic. The actual fix collects every distinct key the whole query will need before fetching anything, then issues a single batched call for all of them.

## Pros

- A batched fetch costs a fixed 1 round trip regardless of how many objects are in the list or how much their related-entity keys overlap
- Removes the need to reason about which specific inputs a caching layer will or won't help with — the fix no longer depends on the data's shape
- Matches how most production GraphQL frameworks already solve this (a DataLoader-equivalent), so adopting the pattern usually means using existing tooling rather than hand-rolling new infrastructure

## Cons

- Requires restructuring resolution to a two-phase "collect keys, then fetch" model, which is a bigger change than adding a cache
- The underlying datastore must actually support an efficient batched fetch (e.g., `WHERE id IN (...)`) for the batching to pay off — batching into N individual calls behind the scenes provides no benefit
- Introduces a window (the "batch tick") during which keys are collected before the fetch fires, which needs to be understood correctly in an asynchronous execution model to avoid over- or under-batching

## When NOT to Batch

- The nested field is cheap and already indexed for point lookups, and query volume is low enough that the round-trip cost isn't a real bottleneck
- The GraphQL server or client library already provides transparent batching/caching by default — hand-rolling a duplicate layer risks subtle double-caching bugs
- The nested fetch genuinely cannot be expressed as a batched call against the underlying datastore (e.g., calling N independent third-party APIs with no bulk endpoint) — batching the *call site* doesn't help if the *downstream system* still processes it one at a time

## Code Examples in This Repo

| Example | Domain | What It Shows |
|---------|--------|--------------|
| [`pressure`](./pressure) | Naive per-field resolver | 1 + N round trips to resolve a list's nested field, one per object |
| [`wrong-direction`](./wrong-direction) | Per-request memoization | Real improvement when keys repeat; zero improvement on an equally realistic feed with no repeats |
| [`principle-applied`](./principle-applied) | Collect-then-batch (DataLoader-style) | Exactly 1 round trip for the whole query, independent of duplication |

### How to Run

```bash
cd volume-21-api-design-evolution-at-scale/paper-116-graphql-n-plus-1/pressure
javac *.java && java Main

cd volume-21-api-design-evolution-at-scale/paper-116-graphql-n-plus-1/wrong-direction
javac *.java && java Main

cd volume-21-api-design-evolution-at-scale/paper-116-graphql-n-plus-1/principle-applied
javac *.java && java Main
```
