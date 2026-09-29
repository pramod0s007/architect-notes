# Paper 153: PII Tokenization & Masking

Part of [Architect Notes](../../README.md) — Volume 28: Data Privacy & Compliance Architecture.

## The Question

A customer's full SSN and card number are stored in a database. Is encrypting them at rest enough
to stop a support engineer, answering an unrelated billing question, from seeing both in full on
screen?

## Structure

- `pressure/` — Plain fields, rendered in full on every screen. Verified: a support engineer
  handling a billing question sees the customer's full SSN and card number, with no distinction
  based on why they're looking.
- `wrong-direction/` — Fields encrypted at rest, but decrypted in full for any authenticated
  caller. Verified: the database is genuinely harder to exploit, but the same support engineer
  still sees both full values on screen — encryption at rest protects a different boundary than
  the one being crossed here.
- `principle-applied/` — Masked by default (precomputed, never derived by decrypting); full
  reveal requires an explicit, checked `view-full-pii` permission, which then decrypts the SSN and
  resolves the card number's opaque token through a separate token vault; every reveal (granted or
  denied) is logged with caller, role, ticket, and fields involved. Verified across three cases: a
  billing-support engineer never needs a reveal; the same engineer is denied when they explicitly
  request one (decrypt/detokenize never called); a fraud-investigation engineer holding the
  permission is granted one (decrypt/detokenize called for the only time in the run) — each
  outcome logged.

## Running It

Each scenario is self-contained:

```bash
javac -Xlint:all *.java
java Main
```

## Full Write-Up

See [MEDIUM-DRAFT.md](../../../architect-notes-private/volume-28-data-privacy-compliance-architecture/paper-153-pii-tokenization-masking/MEDIUM-DRAFT.md)
(private repo) for the full analysis, comparison table, and interview Q&A.
