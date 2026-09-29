# Paper 159: Storage Tiering & Data Lifecycle Costs

Part of [Architect Notes](../../README.md) — Volume 29: Cost-Aware Architecture (FinOps for Engineers).

## The Question

A storage lifecycle policy moves data to a cheaper storage tier once it's old enough, on the theory
that old data is unlikely to be retrieved again. But a cheaper storage tier is usually priced with a
retrieval cost that a more expensive tier doesn't charge at all — so an old object that's still
retrieved regularly can end up costing *more* in the "cheaper" tier once retrieval charges are
counted. How do you tier a storage object in a way that accounts for what that specific object is
actually retrieved for, rather than a proxy (age, or recency of the single most recent access) that
happens to correlate with retrieval frequency for most objects but not all of them?

## Structure

- `pressure/` — An age-based lifecycle rule moves every object older than 90 days to the ARCHIVE
  tier. Verified: a set of compliance reports, 120 days old and retrieved twice a month by a
  recurring audit process, gets archived alongside genuinely cold data — and once retrieval charges
  are counted, archiving it costs $20.50/month versus $11.50/month if it had simply stayed in HOT,
  an overpay of $8.995/month that age alone had no way to detect.
- `wrong-direction/` — A recency-window rule keeps an object in HOT if it was accessed within the
  last 30 days, and archives it otherwise. Verified: this still misfires in both directions — the
  same compliance-reports object, retrieved twice a month but caught 35 days since its last access,
  gets archived right before its next scheduled retrieval, while a rarely-retrieved audit export
  that happened to be touched once, 5 days ago, gets kept in HOT for a full month it doesn't need,
  costing $6.90/month instead of the $0.897/month it would have cost in ARCHIVE.
- `principle-applied/` — Each object's own total monthly cost — storage plus expected retrieval —
  is projected across HOT, COOL, and ARCHIVE tiers, using that object's own observed access
  frequency, and the object is placed in whichever tier minimizes that projected total. Verified
  across all 4 objects including a pre-launch object with only 1 month of access history: the
  guarded object is held at a conservative HOT default rather than being tiered off a single thin
  month of zero-access data, and the other three objects' combined cost ($14.377/month) is
  $8.995/month cheaper than the age-based policy and $14.998/month cheaper than the recency-based
  policy on the same data.

## Running It

Each scenario is self-contained:

```bash
javac -Xlint:all *.java
java Main
```

## Full Write-Up

See [MEDIUM-DRAFT.md](../../../architect-notes-private/volume-29-cost-aware-architecture-finops/paper-159-storage-tiering-data-lifecycle-costs/MEDIUM-DRAFT.md)
(private repo) for the full analysis, comparison table, and interview Q&A.
