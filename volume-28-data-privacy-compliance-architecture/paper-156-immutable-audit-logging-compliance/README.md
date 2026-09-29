# Paper 156: Immutable Audit Logging for Compliance

Part of [Architect Notes](../../README.md) — Volume 28: Data Privacy & Compliance Architecture.

## The Question

An audit log exists to answer "what actually happened" after the fact. If the log itself can be
quietly edited or deleted, can it still answer that question — or only "what does the log
currently claim happened"?

## Structure

- `pressure/` — An audit log stored as ordinary mutable rows in the same database as everything
  else. Verified: an entry recording an unauthorized SSN access is deleted before an incident
  investigation reaches the log, and nothing about the log's structure indicates a third entry
  ever existed or was removed.
- `wrong-direction/` — Every entry gets a hash computed from its own content, verified by
  recomputing that hash. Verified: an entry is edited in place and its hash is recomputed to match
  the edited content — the verification pass reports the log as fully `VALID`, identical to the
  result before the edit, because a per-entry hash only proves an entry agrees with itself right
  now, not that it's the entry originally written.
- `principle-applied/` — Each entry's hash is chained to the previous entry's hash, and the class
  exposes no update or delete method at all. Verified across 5 cases: an untampered chain verifies
  clean; a directly-edited interior entry is caught at the tampered entry itself (hash mismatch);
  a deleted interior entry is caught at the first surviving entry after the gap (previous-hash
  link mismatch); deleting the chain's current *tail* entry is **not** caught by a chain-only
  check — the remaining entries are still perfectly self-consistent, demonstrating a genuine
  blind spot; and the same tail-deleted chain, checked against a checkpoint (entry count + head
  hash) recorded before the tampering, is correctly flagged invalid — showing why a hash chain
  needs an externally anchored checkpoint, not just internal consistency, to catch a tail deletion
  or a fully recomputed suffix rewrite.

## Running It

Each scenario is self-contained:

```bash
javac -Xlint:all *.java
java Main
```

## Full Write-Up

See [MEDIUM-DRAFT.md](../../../architect-notes-private/volume-28-data-privacy-compliance-architecture/paper-156-immutable-audit-logging-compliance/MEDIUM-DRAFT.md)
(private repo) for the full analysis, comparison table, and interview Q&A.
