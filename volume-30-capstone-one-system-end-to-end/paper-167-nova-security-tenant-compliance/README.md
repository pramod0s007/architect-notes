# Paper 167: Securing Nova Across Tenant and Compliance Boundaries

Part of [Architect Notes](../../README.md) — Volume 30: Capstone: One System, End to End.

This is the sixth paper of Volume 30, continuing directly from Nova, the fictional multi-tenant
order/checkout and fulfillment platform carried through Papers 162 (data-layer architecture), 163
(API boundary evolution), 164 (order-to-fulfillment write path), 165 (read-path caching), and 166
(circuit-breaker re-verification). This paper moves to Nova's customer-support tool — specifically,
the lookup path a support agent uses to view a customer's contact details (email, phone) while
working a case. It ties to Volume 17 (Security Architecture) and Volume 28 (Data Privacy &
Compliance), applying both volumes' distinctions — display-layer control versus enforcement-layer
control, and purpose limitation/data-residency as specific, checkable obligations — to one concrete
lookup path.

## The Question

Nova's support tool retrieves a customer's real email and phone number on every lookup and writes
that value, unredacted, to an audit log — regardless of why the agent is looking, and regardless of
which region the agent and the customer's own record are each associated with. A masking fix that
redacts what the agent's own screen displays closes that specific exposure — but does it close the
gap in what gets logged? And does it check anything about why the lookup is happening, or whether
the requesting agent is even authorized to access a customer record in a different region?

## Structure

- `pressure/` — No purpose check, no residency check: every lookup writes the customer's real email
  and phone, unredacted, to the audit log — the same for a stated purpose, no purpose at all, and a
  cross-region access.
- `wrong-direction/` — Adds display-only masking (`maskEmail`/`maskPhone`) so the agent's own screen
  shows a redacted value. Verified: the underlying audit-log call is untouched — it's built from
  `customer.email`/`customer.phone` directly, the exact same expressions as the pressure scenario —
  so the log still receives the real, unredacted value for every lookup, and purpose/residency are
  still not checked at all.
- `principle-applied/` — Composes three independent controls: tokenization (the audit log receives
  only an opaque token, never the real value — verified the program has no method that reads the
  vault's real-value map back out), a purpose gate (refuses a lookup outright if no recognized
  purpose is supplied), and a residency gate (refuses a lookup outright if the agent's region
  doesn't match the customer's residency region). Verified: a recognized-purpose, same-region
  lookup is allowed through and logs only tokens; a no-purpose lookup and a cross-region lookup are
  each refused before any PII is touched.

## Running It

Each scenario is self-contained:

```bash
javac -Xlint:all *.java
java Main
```

## Full Write-Up

See [MEDIUM-DRAFT.md](../../../architect-notes-private/volume-30-capstone-one-system-end-to-end/paper-167-nova-security-tenant-compliance/MEDIUM-DRAFT.md)
(private repo) for the full analysis, comparison table, and interview Q&A — including why masking
what's displayed doesn't fix what's logged, and why closing one gap can look like the whole problem
is solved while an identical exposure continues at a different point in the same call path.
