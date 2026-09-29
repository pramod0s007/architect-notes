# Distributed Transactions & Two-Phase Commit — Nothing Is Final Until Everyone Has Agreed

**Pattern:** No coordination across two services (payment debits successfully, inventory reservation then fails — customer charged, nothing reserved, the services disagree) → a compensating rollback (catch the failure, refund the payment — works only if the refund call itself succeeds; fails silently, leaving the same inconsistency plus a failed correction, if it doesn't) → two-phase commit (both participants tentatively hold and vote in a prepare phase; nothing commits anywhere until every vote is yes — a failed vote cleanly releases every hold, with no partial state ever left behind)

**Volume 23** (Database Internals for Architects) — Paper 129.

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

A single logical action that spans two independent services is not atomic just because the calling code happens to invoke both in sequence — each service commits its own action permanently and independently, with no shared notion of "both or neither." Without any coordination, a payment debit can succeed permanently while a dependent inventory reservation then fails, leaving the two services in states that no longer agree (verified: customer charged, nothing reserved, and neither service did anything wrong on its own terms). Catching that failure and manually reversing the first action looks like a fix, but the reversal is just another ordinary call with its own failure mode — it works when it works, and reproduces the exact same inconsistency, plus a failed correction attempt, the moment it doesn't (verified: a compensating refund succeeds in one case and fails in another, identical in every other respect). The actual fix is two-phase commit: every participant only tentatively holds its resource and votes during a prepare phase, and nothing becomes final anywhere until every vote is yes — a failed vote cleanly releases every hold that was already placed (verified: when a participant can't prepare, an already-prepared participant's hold is released and its state ends up exactly where it started).

## The Pressure: A Partial Commit Leaves Two Services Disagreeing

Two independently-committing calls across two services are not a transaction just because the code that invokes them runs in sequence. Each service commits its own action permanently the moment its own call returns, with nothing above them treating the pair as a single unit that either both happen or neither does.

## The Principle

**Nothing should become final, anywhere, until every participant has confirmed it's actually able to go through.** A prepare phase — a tentative hold plus a vote, with no permanent effect yet — is what makes an abort possible after the fact without needing a separate, unverified "undo" call. The guarantee isn't a compensating action remembered by the caller; it's a protocol every participant implements, where commit only ever happens after every vote is yes, and abort is a normal, first-class outcome, not a special case.

## Pros

- No partial commit is possible: a participant that votes yes is only ever finalized if every other participant also voted yes, verified across both a fully-succeeding case and a case where one participant fails to prepare
- The failure path requires no separate "undo" logic per action — `abort()` on a prepared-but-not-committed participant is a normal, first-class part of the same protocol, not a special-case compensating call
- Consistency holds regardless of *which* participant is the one that can't prepare — the protocol doesn't need to special-case "payment failed" versus "inventory failed"

## Cons

- Every participant has to support prepare/commit/abort — this is a real protocol contract each service must implement, not something a caller can add on its own by wrapping existing calls in try/catch
- A participant that has voted yes and is waiting for the coordinator's commit/abort decision has its resource held (unavailable to anyone else) for as long as that decision takes — this is the classic "blocking" cost of 2PC, distinct from the coordination benefit
- If the coordinator itself crashes after some participants have voted yes but before it tells anyone to commit or abort, those participants are stuck holding their resource with no way to know which way the vote is supposed to go until the coordinator recovers

## When a Compensating Rollback (Not Full 2PC) Is Still Acceptable

- The compensating action's own failure mode is independently monitored and alerted on (not just attempted and forgotten), so a failed compensation becomes a visible incident rather than a silent, permanent inconsistency
- The two actions are not both high-stakes and hard to reverse — e.g. one side is cheap to retry indefinitely, or the business already tolerates and reconciles occasional manual fixes for this specific flow
- Adding a real prepare/commit/abort contract to one of the participants isn't feasible (e.g. it's a third-party service with no such API) and the actual risk of the compensating step failing has been measured, not assumed

## Code Examples in This Repo

| Example | Domain | What It Shows |
|---------|--------|--------------|
| [`pressure`](./pressure) | No coordination | Payment debits successfully; inventory reservation then fails — customer charged, nothing reserved |
| [`wrong-direction`](./wrong-direction) | Compensating rollback | A refund on inventory failure works in one case and fails in another, leaving the same inconsistency |
| [`principle-applied`](./principle-applied) | Two-phase commit | Both participants prepare and vote first; a failed vote cleanly releases every hold, no partial state |

### How to Run

```bash
cd volume-23-database-internals-for-architects/paper-129-distributed-transactions-two-phase-commit/pressure
javac *.java && java Main

cd volume-23-database-internals-for-architects/paper-129-distributed-transactions-two-phase-commit/wrong-direction
javac *.java && java Main

cd volume-23-database-internals-for-architects/paper-129-distributed-transactions-two-phase-commit/principle-applied
javac *.java && java Main
```
