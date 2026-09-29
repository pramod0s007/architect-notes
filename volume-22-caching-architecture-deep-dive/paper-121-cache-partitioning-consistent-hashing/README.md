# Cache Partitioning & Consistent Hashing — Balance and Low Remap Cost Are Separate Claims

**Pattern:** Modulo hashing (perfectly balanced for a fixed node count, but 806 of 1000 keys remap on a single node addition) → consistent hashing, one ring point per node (fixes the resize storm — only 300 of 1000 keys remap — but introduces a real 6.0x load skew that exists even with zero resizes) → consistent hashing with virtual nodes, 2000 points per physical node (keeps the low remap cost — 268 of 1000 — and cuts the load skew to 1.28x)

**Volume 22** (Caching Architecture Deep Dive) — Paper 121.

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

Partitioning a cache across multiple nodes means deciding which node owns each key, and that decision has to survive the cluster changing size. Plain modulo hashing (`hash(key) % nodeCount`) gives perfectly even load for a fixed node count, but ties every key's assignment to the total node count itself, so adding a single node remaps almost everything (verified: 806 of 1000 keys). Consistent hashing fixes that by putting nodes and keys on a ring and assigning each key to the next node clockwise — resizing only touches keys near the changed node's position (verified: only 300 of 1000 keys remapped). But a ring with just one point per physical node has its own separate failure: a handful of essentially random points doesn't divide a circle into equal arcs, so load can be badly skewed with zero resizes involved (verified: 6.0x skew, 73 to 438 keys, at a stable 4-node count). The real fix — giving each physical node many virtual points instead of one — gets both properties at once: balanced load (verified: skew drops to 1.28x with 2,000 points per node) and cheap resizing (verified: 268 of 1000 keys remapped, still far below modulo's 806).

## The Pressure: Modulo Hashing Remaps Almost Everything on Resize

`hash(key) % nodeCount` is perfectly even for a fixed node count, but the node count is baked into every single key's assignment. Adding or removing even one node changes the divisor for every key at once, not just the keys that logically belong near the changed node — turning a routine scale-up into a mass cache-miss event.

## The Principle

**A ring with too few sample points can't be balanced, no matter how correct the assignment rule on top of it is.** Consistent hashing's resize-cost fix comes from the assignment rule itself ("next node clockwise") and holds regardless of how many points are on the ring. The load-imbalance failure is a sampling-density problem sitting one layer below that rule: a handful of random points will always carve unequal arcs out of a circle, the same way four random cuts rarely produce four equal slices — no amount of correctness in the assignment logic fixes a ring that's too sparse to average out. Giving each node many points instead of one raises the sample size until the law of large numbers evens out each node's total share, without touching the assignment rule at all.

## Pros

- Virtual nodes get balanced load and cheap resizing simultaneously — not a tradeoff, because the resize property never depended on ring density in the first place
- The fix requires no change to the assignment algorithm itself (still "next node clockwise on the ring") — only more points per node, which is a configuration change, not a redesign
- Scales down gracefully: nodes with more capacity can simply be given proportionally more virtual points, providing a natural mechanism for heterogeneous node sizing

## Cons

- A ring with thousands of points per node uses more memory and takes slightly longer to build than a single-point ring — real but usually negligible compared to the cost of the imbalance it fixes
- Choosing the right number of virtual points per node requires either measurement or a rule of thumb — too few and the skew persists, though there's no risk of "too many" beyond memory and build-time cost
- Doesn't address load imbalance caused by uneven request rates on individual keys (hot keys) — this fixes key *count* balance, not necessarily request-rate balance if some keys are dramatically hotter than others

## When a Single Ring Point Per Node Is Still Acceptable

- The cluster is large enough that the law of large numbers already applies at the physical-node level, even with one point each
- Perfect load balance isn't actually required for the workload, and the operational simplicity of a smaller ring is a real advantage
- Nodes are already roughly homogeneous in a way where key-count imbalance doesn't translate into meaningfully uneven load

## Code Examples in This Repo

| Example | Domain | What It Shows |
|---------|--------|--------------|
| [`pressure`](./pressure) | Modulo hashing | Perfectly even at 4 nodes, but 806 of 1000 keys remap after adding a 5th node |
| [`wrong-direction`](./wrong-direction) | Consistent hashing, 1 point/node | Only 300 of 1000 keys remap, but a 6.0x load skew exists even before any resize |
| [`principle-applied`](./principle-applied) | Consistent hashing, 2000 points/node | 268 of 1000 keys remap, and load skew drops to 1.28x |

### How to Run

```bash
cd volume-22-caching-architecture-deep-dive/paper-121-cache-partitioning-consistent-hashing/pressure
javac *.java && java Main

cd volume-22-caching-architecture-deep-dive/paper-121-cache-partitioning-consistent-hashing/wrong-direction
javac *.java && java Main

cd volume-22-caching-architecture-deep-dive/paper-121-cache-partitioning-consistent-hashing/principle-applied
javac *.java && java Main
```
