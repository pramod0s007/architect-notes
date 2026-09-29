# Paper 139: Rolling Updates & PodDisruptionBudgets

Part of [Architect Notes](../../README.md) — Volume 25: Cloud-Native & Kubernetes Architecture.

## The Question

If your rolling update's `maxUnavailable` is configured safely, and a routine node drain
is also configured safely, how can the two combine into an outage neither one caused alone?

## Structure

- `pressure/` — A safely-paced rolling update (`maxUnavailable=2` of 10) never drops below
  the 7-replica floor on its own. A concurrent, unrelated node drain evicts 3 more replicas
  with no PodDisruptionBudget to check current availability. Verified: fleet drops to 5 of
  10 available, 600 dropped requests, even though both operations were individually correct.
- `wrong-direction/` — A PDB is added with `minAvailable` set to 100% of replicas, reasoning
  "maximum protection." But Deployment-driven pod replacement bypasses the Eviction API
  entirely, so the PDB never governed the actual risk. Verified: dropped requests fall to 0
  (because the deploy alone was already safe), but the node drain is denied on every attempt
  — 26 denials, 0 of 3 pods ever evicted — permanently blocking maintenance.
- `principle-applied/` — The PDB is sized to the real floor (`minAvailable=7`), so it adapts
  to whatever else has already reduced availability. Verified: dropped requests fall to 100
  (from 600), and the drain completes (3 of 3 evicted, vs. 0 of 3) — a large, honest
  improvement, though not a perfect one: a PDB is a point-in-time check, not a lookahead, so
  a tight, back-to-back deploy-wave cadence can still produce one small residual dip.

## Running It

Each scenario is self-contained. From inside any of the three directories:

```bash
javac -Xlint:all *.java
java Main
```

## Full Write-Up

See [MEDIUM-DRAFT.md](../../../architect-notes-private/volume-25-cloud-native-kubernetes-architecture/paper-139-rolling-updates-blue-green-canary-pdb/MEDIUM-DRAFT.md)
(private repo) for the full analysis, comparison table, and interview Q&A.
