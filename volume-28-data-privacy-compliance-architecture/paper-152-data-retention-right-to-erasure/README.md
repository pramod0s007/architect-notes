# Paper 152: Data Retention & Right-to-Erasure

Part of [Architect Notes](../../README.md) — Volume 28: Data Privacy & Compliance Architecture.

## The Question

A user exercises their right to erasure (GDPR Article 17). Their data lives in three systems —
a primary database, an analytics warehouse, a search index. Deleting from the primary database
succeeds. Is the user actually erased?

## Structure

- `pressure/` — A single-table delete: only the primary database is touched. Verified: the primary
  DB is genuinely clean, but the user's data still fully exists in the two downstream systems that
  were never told about the request.
- `wrong-direction/` — Synchronous fan-out to all three systems in one pass, but with no durable
  tracking of failures. Verified: two of three targets succeed, the third fails during an outage,
  and the failure is only ever logged to a console — nothing durable distinguishes this user from
  a fully erased one once the process exits.
- `principle-applied/` — An in-memory model of an erasure manifest tracking every target's status
  (PENDING/DONE) — standing in for what a production version would need to persist. Verified: the
  first attempt leaves one target PENDING; a simulated retry pass, driven entirely by the manifest
  state, completes only that target; the user is reported fully forgotten only once every manifest
  entry reads DONE.

## Running It

Each scenario is self-contained:

```bash
javac -Xlint:all *.java
java Main
```

## Full Write-Up

See [MEDIUM-DRAFT.md](../../../architect-notes-private/volume-28-data-privacy-compliance-architecture/paper-152-data-retention-right-to-erasure/MEDIUM-DRAFT.md)
(private repo) for the full analysis, comparison table, and interview Q&A.
