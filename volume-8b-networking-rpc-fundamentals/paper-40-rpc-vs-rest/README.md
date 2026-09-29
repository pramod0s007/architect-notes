# RPC vs REST — Field Identity by Name vs by Number

**Pattern:** JSON name-keyed contracts → Protobuf-style tag-keyed contracts → Tag-reuse hazard + a schema registry guard

**Volume 8b** (Networking & RPC Fundamentals) — a short bridge volume between Volume 8 (Concurrency Patterns) and Volume 9 (Distributed Systems Resilience). Each paper here uses an independent example, in the pre-Volume-9 style.

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

A JSON-style message's field identity is its string key — renaming `totalPrice` to `totalAmount` on the server makes an unchanged client's `get("totalPrice")` return null, and a "safe" default-to-zero handler turns that into a silently zeroed order total (verified). A protobuf-style wire format instead ties identity to a numbered tag, not a name — the same rename replayed against tag-based lookup is a genuine no-op: an old, never-regenerated client stub still reads the correct value (verified). But tag-based identity has its own hazard: reusing a retired tag for an unrelated field produces a plausible-looking wrong value instead of an obviously-wrong null (verified: an old stub reports a customer ID as a dollar total) — fixed with a schema registry that refuses to let a retired tag be reused.

## The Pressure: A Rename Is a Delete-and-Add

JSON's field identity is its string key — correctness depends on both sides agreeing on exact spelling, forever.

## The Principle

**Decoupling a field's wire identity from its human-readable name makes renaming safe** — but only if the freed-up identity (the tag number) is never reassigned to something else. Tag-based systems trade "silent missing value" for the narrower but more dangerous "silent misidentification," and that trade only pays off if tag reuse is actually enforced, not just documented.

## Pros

- Tag-based identity makes field renames a true no-op at the wire level — no client regeneration required
- JSON remains simple and human-readable, valuable when client/server are always deployed together
- A schema registry turns "never reuse a retired tag" from a style-guide comment into an enforced runtime check

## Cons

- JSON's tolerant-reader defaults (null → 0, missing → default) can mask a broken contract as a legitimate empty value
- Tag reuse produces a plausible-looking wrong value, harder to detect than JSON's obviously-wrong null/zero
- Tag-based binary formats give up the human-readability JSON offers for free

## When NOT to Use

- Client and server are always deployed together from the same build — no version-skew window exists for either failure mode
- Human-readability of the wire payload matters more than rename-safety (e.g. debugging via browser dev tools)
- The team can't reliably enforce "never reuse a retired tag" — tag-based identity only helps if that discipline actually holds

## Code Examples in This Repo

| Example | Domain | What It Shows |
|---------|--------|--------------|
| [`evolution/v1`](./evolution/v1) | JSON-style order total message | String-key rename: client's lookup returns null, total silently defaults to $0.00 |
| [`evolution/v2`](./evolution/v2) | Protobuf-style order total message | Tag-based rename: old and new client stubs both read the correct value correctly |
| [`evolution/v3`](./evolution/v3) | Same tag-based message, tag reused | Unguarded tag reuse misreports a customerId as a dollar total; a SchemaRegistry rejects the reuse |

### How to Run

```bash
cd volume-8b-networking-rpc-fundamentals/paper-40-rpc-vs-rest/evolution/v1
javac *.java && java Main

cd volume-8b-networking-rpc-fundamentals/paper-40-rpc-vs-rest/evolution/v2
javac *.java && java Main

cd volume-8b-networking-rpc-fundamentals/paper-40-rpc-vs-rest/evolution/v3
javac *.java && java Main
```
