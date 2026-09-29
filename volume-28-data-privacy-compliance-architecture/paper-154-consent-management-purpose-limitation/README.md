# Paper 154: Consent Management & Purpose Limitation

Part of [Architect Notes](../../README.md) — Volume 28: Data Privacy & Compliance Architecture.

## The Question

A marketing team wants to email "all our customers" about a new product. Is having an account,
or not having explicitly opted out, the same thing as having actually agreed to be marketed to?

## Structure

- `pressure/` — No consent tracking at all; "has an account" is treated as consent for any
  purpose. Verified: a customer who created an account solely to file a support ticket receives a
  promotional email they never agreed to.
- `wrong-direction/` — A single `marketingOptOut` boolean, defaulting to false. Verified: an
  explicit opt-out is now honored, but a never-asked customer still receives the email (silence
  still counts as consent), and the single bit can't distinguish one marketing purpose/channel
  from another.
- `principle-applied/` — Purpose-scoped consent records, defaulting to no consent when no record
  exists, with support for an explicit decline, a not-yet-effective grant, revocation, and expiry
  — all evaluated as of the check time. Verified across two campaigns and seven customers,
  exercising every distinct outcome: no record (default deny), active grant (included), expired
  grant (excluded), revoked grant (excluded, dated before the check time), explicit decline
  (excluded, distinct from no record at all), a not-yet-effective future grant (excluded), and
  purpose isolation (the same customer included in one campaign and excluded from another based on
  which purpose they actually consented to).

## Running It

Each scenario is self-contained:

```bash
javac -Xlint:all *.java
java Main
```

## Full Write-Up

See [MEDIUM-DRAFT.md](../../../architect-notes-private/volume-28-data-privacy-compliance-architecture/paper-154-consent-management-purpose-limitation/MEDIUM-DRAFT.md)
(private repo) for the full analysis, comparison table, and interview Q&A.
