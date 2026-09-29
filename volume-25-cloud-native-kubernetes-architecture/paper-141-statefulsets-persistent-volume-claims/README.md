# Paper 141: StatefulSets & Persistent Volume Claims

Part of [Architect Notes](../../README.md) — Volume 25: Cloud-Native & Kubernetes Architecture.

## The Question

If a StatefulSet guarantees the same ordinal always gets the same PersistentVolumeClaim back,
why can a scaled-back-up replica end up silently serving badly outdated data?

## Structure

- `pressure/` — A Deployment-style workload with no per-replica identity or storage: every
  reschedule assigns a brand-new empty volume. Verified: 4 reschedule events cause 12 total
  replica-resets and 120 cumulative ticks of built-up local cache work thrown away.
- `wrong-direction/` — Migrating to a StatefulSet fixes the reschedule problem, but the team
  leaves `persistentVolumeClaimRetentionPolicy` at its default (`Retain`) and cycles replica
  count down and back up. Verified: 2 risky stale reattachments (22 total staleness ticks
  silently absorbed, no error or signal), and 2 orphaned, still-billed PVCs left retained at
  the end of the run.
- `principle-applied/` — The team deliberately configures `whenScaled: Delete` for this
  specific, fully-rebuildable per-replica cache workload. Verified: 0 risky reattachments, 0
  staleness ticks, 0 orphaned PVCs — not a universal win, but the correct, explicit trade-off
  for disposable state (a real database would instead choose `Retain`, on purpose).

## Running It

Each scenario is self-contained. From inside any of the three directories:

```bash
javac -Xlint:all *.java
java Main
```

## Full Write-Up

See [MEDIUM-DRAFT.md](../../../architect-notes-private/volume-25-cloud-native-kubernetes-architecture/paper-141-statefulsets-persistent-volume-claims/MEDIUM-DRAFT.md)
(private repo) for the full analysis, comparison table, and interview Q&A.
