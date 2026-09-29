# Idempotency Keys — A Retry Without a Key Is Just a Second Request

**Pattern:** No idempotency key (a retry after a lost response looks identical to a genuinely new request, causing a double charge) → idempotency key with a non-atomic check-then-act (the right instinct, but two concurrent requests can both pass the check before either records it) → atomic key reservation with cached-result replay (exactly one request can ever win the reservation, and every other one gets the winner's own result back)

**Volume 21** (API Design & Evolution at Scale) — Paper 114.

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

An idempotency key is a value the client generates once per logical operation and attaches to every attempt, including retries — it answers a question the server has no other way to answer: is this the same intent as a request I've already seen, or a genuinely new one? Without one, a lost response (the request succeeded, but the acknowledgment never arrived) is indistinguishable to the client from a lost request, and retrying — the only safe-seeming option — creates a second, independent charge (verified: $49.99 charged twice for one intended purchase). Adding an idempotency key with a "check if seen, then charge, then mark as seen" flow is the right instinct, but it's three separate steps: two genuinely concurrent requests carrying the identical key can both pass the check before either finishes acting on it, still producing a double charge (verified: same $49.99 double-charge, despite the idempotency key matching on both requests). The real fix makes reservation and result-recording a single atomic operation (`ConcurrentHashMap.putIfAbsent`), so exactly one request can ever win a given key, and every other request — whether racing concurrently or arriving long after the original completed — is handed back the winner's real result instead of being allowed to charge (verified: exactly one $49.99 charge across three separate requests sharing the same key).

## The Pressure: A Lost Response Looks Identical to a Lost Request

The most common reason a client retries a request isn't that the request failed — it's that the response never arrived. From the client's side, a lost response and a lost request are indistinguishable: both look like a timeout. Retrying is the correct instinct; the bug is that the server has no way to recognize the retry as a retry.

## The Principle

**Reserving an idempotency key and recording its result must be a single atomic operation, not a check followed by an act.** This is a different mechanism from message-queue redelivery idempotency: a queued message already carries a built-in identity (its ID or offset), so a consumer only has to notice it's seen that ID before. An HTTP retry carries no such identity — the client has to manufacture one on purpose. And a non-atomic "check, then act" sequence, even when the key genuinely matches, leaves a gap two concurrent requests can both pass through before either one records anything.

## Pros

- An atomic reservation (`putIfAbsent` or equivalent) makes it structurally impossible for two requests with the same key to both win — exactly one can ever succeed
- Storing the actual result (not just a "seen it" boolean) against the key lets every losing request — concurrent or much later — get back a useful, correct response instead of an error
- The fix requires no new infrastructure beyond a key-value store with an atomic compare-and-set operation, which most datastores already provide

## Cons

- The idempotency key store itself needs a retention/expiry policy — keeping every key forever is rarely necessary and costs storage indefinitely
- Clients must be trusted (or verified) to generate a genuinely unique key per logical operation; a client that reuses a key across unrelated operations will have the second operation silently treated as a duplicate of the first
- This pattern only protects a single service boundary — if that service itself calls other side-effecting operations downstream without its own idempotency guarantees, the double-execution risk simply moves downstream

## When NOT to Add Idempotency Keys

- The operation is naturally idempotent already — a `PUT` that fully overwrites a resource with the same payload produces the same end state regardless of retry count
- The operation has no side effect worth protecting — a read-only `GET`, or a duplicate whose worst-case cost is genuinely negligible
- The client and server share a trust domain where retries are already deduplicated further down the stack, so the risk this pattern protects against doesn't exist at this boundary

## Code Examples in This Repo

| Example | Domain | What It Shows |
|---------|--------|--------------|
| [`pressure`](./pressure) | No idempotency key | A lost response triggers a client retry that becomes a second, independent charge |
| [`wrong-direction`](./wrong-direction) | Non-atomic check-then-act | An idempotency key is checked correctly, but two concurrent requests can both pass the check before either records it |
| [`principle-applied`](./principle-applied) | Atomic reservation + cached replay | Exactly one request wins the reservation; every other request, racing or late, gets back the winner's own result |

### How to Run

```bash
cd volume-21-api-design-evolution-at-scale/paper-114-idempotency-keys/pressure
javac *.java && java Main

cd volume-21-api-design-evolution-at-scale/paper-114-idempotency-keys/wrong-direction
javac *.java && java Main

cd volume-21-api-design-evolution-at-scale/paper-114-idempotency-keys/principle-applied
javac *.java && java Main
```
