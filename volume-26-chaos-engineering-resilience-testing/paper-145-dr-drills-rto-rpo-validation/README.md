# Paper 145: Disaster Recovery Drills: RTO/RPO Validation

Part of [Architect Notes](../../README.md) — Volume 26: Chaos Engineering & Resilience Testing.

## The Question

A failover drill completed successfully. Does that mean the DR plan's RTO/RPO promises were met?

## Structure

- `pressure/` — The DR plan is reviewed on paper and never actually exercised. Verified: the plan
  documents a 2-minute replication lag, but the real lag has silently drifted to 22 minutes,
  undiscovered — hypothetically already 27 minutes past the 15-minute RTO and 17 minutes past the
  5-minute RPO, with nothing ever catching it.
- `wrong-direction/` — A failover drill is actually run, but only checks the binary question "did
  failover complete." Verified: failover completes (42-minute recovery time, 22-minute data loss),
  and the drill reports PASS — neither number is ever compared against the stated RTO/RPO.
- `principle-applied/` — Same failover, same 42-minute recovery time and 22-minute data loss, but
  the drill explicitly measures both against the stated objectives. Verified: RTO violated by 27
  minutes, RPO violated by 17 minutes, drill result FAIL — correctly reporting what the
  wrong-direction scenario's PASS concealed.

## Running It

Each scenario is self-contained:

```bash
javac -Xlint:all *.java
java Main
```

## Full Write-Up

See [MEDIUM-DRAFT.md](../../../architect-notes-private/volume-26-chaos-engineering-resilience-testing/paper-145-dr-drills-rto-rpo-validation/MEDIUM-DRAFT.md)
(private repo) for the full analysis, comparison table, and interview Q&A.
