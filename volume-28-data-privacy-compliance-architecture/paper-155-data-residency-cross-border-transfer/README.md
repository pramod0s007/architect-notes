# Paper 155: Data Residency & Cross-Border Transfer

Part of [Architect Notes](../../README.md) — Volume 28: Data Privacy & Compliance Architecture.

## The Question

You fixed the write path so each customer's data lands in the right region. Does that mean no
copy of that customer's raw record can end up somewhere it isn't allowed to be?

## Structure

- `pressure/` — One central analytics warehouse, in one region, with no residency check anywhere.
  Verified: EU customers' raw records are copied into the us-east warehouse right alongside
  everyone else's, because the pipeline only asks whether a customer belongs in the dashboard,
  never where their data is allowed to physically live.
- `wrong-direction/` — Writes are made region-aware (each customer's record lands in a primary
  store in their own residency zone), which is real progress. Verified: the write path now
  respects residency, but a separate, unmodified nightly analytics job still pulls every record
  from every regional store into the same central us-east warehouse — the identical violation,
  reintroduced by a different pipeline that was never told the write path changed.
- `principle-applied/` — A single residency gate, checked by every consumer (write path and
  downstream jobs alike), defaulting to deny for a raw record crossing zones unless the
  destination matches the customer's own zone, only a non-identifying aggregate is requested, or
  a non-blank transfer approval reference is on file. Verified across 6 cases: two same-zone
  transfers that never actually cross a zone boundary (allowed), a cross-zone raw-record request
  with no approval reference (denied by default), a cross-zone aggregate-only request (allowed
  without needing an approval), a cross-zone raw-record request with a non-blank approval
  reference attached (allowed — the gate checks only that the reference is present and non-blank,
  not that it names a valid legal instrument), and a cross-zone raw-record request with a blank
  (whitespace-only) approval reference (denied — treated the same as no approval at all).

## Running It

Each scenario is self-contained:

```bash
javac -Xlint:all *.java
java Main
```

## Full Write-Up

See [MEDIUM-DRAFT.md](../../../architect-notes-private/volume-28-data-privacy-compliance-architecture/paper-155-data-residency-cross-border-transfer/MEDIUM-DRAFT.md)
(private repo) for the full analysis, comparison table, and interview Q&A.
