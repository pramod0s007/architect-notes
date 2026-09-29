# Paper 138: Resource Requests/Limits & OOMKill Under Memory Pressure

Part of [Architect Notes](../../README.md) — Volume 25: Cloud-Native & Kubernetes Architecture.

## The Question

A pod keeps getting OOMKilled and restarted. The obvious fix is to raise its memory limit
until the restarts stop. Why is that often exactly the wrong move?

## Structure

- `pressure/` — PodA has a real, undetected memory leak. Its limit (250MB) is sized from
  steady-state only, so it gets OOMKilled and restarted every ~4 ticks. Verified: 15
  restarts over 60 ticks, node never under memory pressure, PodB never affected — a
  correctly-contained failure that's silently masking a real bug.
- `wrong-direction/` — The operator "fixes" the visible symptom by removing PodA's memory
  limit entirely. Restarts stop immediately. But the leak was never fixed, and with no
  ceiling, PodA's usage grows unchecked until it drags the whole node into memory pressure.
  Verified: node pressure at tick 34, PodB (an innocent, unrelated pod) degraded for 27 of
  60 ticks.
- `principle-applied/` — PodA's limit is sized from realistic peak usage (300MB, with
  headroom) instead of bare steady-state, and a restart-loop alert fires once PodA has been
  OOMKilled 3 times — flagging the likely leak for investigation without touching the
  ceiling. Verified: 8 restarts, alert fires after restart #3, node never under pressure.

## Running It

Each scenario is self-contained. From inside any of the three directories:

```bash
javac -Xlint:all *.java
java Main
```

## Full Write-Up

See [MEDIUM-DRAFT.md](../../../architect-notes-private/volume-25-cloud-native-kubernetes-architecture/paper-138-resource-requests-limits-oomkill/MEDIUM-DRAFT.md)
(private repo) for the full analysis, comparison table, and interview Q&A.
