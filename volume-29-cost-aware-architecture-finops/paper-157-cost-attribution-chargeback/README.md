# Paper 157: Cost Attribution & Chargeback

Part of [Architect Notes](../../README.md) — Volume 29: Cost-Aware Architecture (FinOps for Engineers).

## The Question

A shared cluster's cloud bill arrives itemized by node, not by team. When the total jumps, finance
asks which team's usage the increase belongs to — but nothing in the raw billing data distinguishes
one team's consumption from another's. How do you split a shared resource's cost across its
consumers in a way that both reconciles exactly to the real bill and reflects what each consumer
actually measured, rather than an easy-to-count proxy that happens to add up to the right total?

## Structure

- `pressure/` — A cluster's monthly bill, itemized per node with no per-team breakdown at all.
  Verified: the total jumps from $2,100 to $2,700 and there is no way to say which team's pods
  drove the increase, because the billing data was never captured with a team dimension.
- `wrong-direction/` — Cost split evenly by pod count per node. Verified: a checkout pod using
  85.7% of a node's measured CPU-seconds is charged only 25% of that node's cost, because it
  happens to share the node with three other, lighter pods — the split total matches the bill
  exactly, which is precisely what makes the misallocation easy to miss.
- `principle-applied/` — Each node's cost is decomposed into a CPU-priced portion and a
  memory-priced portion under a disclosed 70/30 policy, and each portion is divided among that
  node's pods in proportion to that pod's own measured usage of that specific dimension. Verified
  across 4 nodes including one fully idle node (zero measured usage in both dimensions): the idle
  node's entire cost routes to an explicit `unallocated (idle capacity)` bucket instead of dividing
  by zero, and the whole allocation reconciles exactly to the total bill ($3000.00 charged against
  a $3000.00 bill).

## Running It

Each scenario is self-contained:

```bash
javac -Xlint:all *.java
java Main
```

## Full Write-Up

See [MEDIUM-DRAFT.md](../../../architect-notes-private/volume-29-cost-aware-architecture-finops/paper-157-cost-attribution-chargeback/MEDIUM-DRAFT.md)
(private repo) for the full analysis, comparison table, and interview Q&A.
