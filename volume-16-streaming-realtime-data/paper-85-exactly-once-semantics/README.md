# Exactly-Once Semantics — Deduplication Is Solving the Wrong Half of the Problem

**Continues Volume 16** — picking up right after Paper 84 (Partitioning Strategy).

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

At-least-once delivery redelivering a message after a consumer crash is the stream working correctly, not a defect (measured: a consumer crash before offset commit produced a genuine redelivery of the same messageId). A consumer with no memory of prior processing applies that redelivery a second time (measured: one logical $10 charge became a $20 deduction). Deduplicating by the producer's stable messageId fixes this without breaking anything (measured: a redelivered charge was correctly skipped while a genuinely distinct charge with the same content was still applied). This paper then shows the overcorrection: deduplicating by the event's content instead of its identity, which silently drops legitimate distinct events that happen to look alike (measured: a real second charge was dropped because its content matched an already-seen event).

## The Measurements

- **No dedup (pressure)**: a redelivered messageId is applied twice — one logical $10 charge becomes a $20 deduction.
- **Dedup by messageId (principle-applied)**: the redelivery is skipped and a genuinely distinct second charge — same account, same amount, different messageId — is still correctly applied.
- **Dedup by content hash (wrong-direction)**: the true redelivery is caught correctly, then a real, separate charge with its own messageId is also dropped because its content matches an already-seen event.

## The Finding

**Deduplication requires keying on the event's stable identity, never on its content.** A messageId-based dedup store tells "redelivered" apart from "new" without needing the event's payload to be unique. A content-based dedup store cannot make that distinction — and in most real domains, distinct events with identical payloads are routine, not rare.

## Code Examples in This Repo

| Example | What It Shows |
|---------|--------------|
| [`pressure/`](./pressure/) | No dedup — measured a redelivered charge applied twice |
| [`principle-applied/`](./principle-applied/) | Dedup by messageId — measured the redelivery correctly skipped, a real second charge correctly applied |
| [`wrong-direction/`](./wrong-direction/) | Dedup by content hash — measured a real, distinct charge silently dropped |

### How to Run

```bash
cd volume-16-streaming-realtime-data/paper-85-exactly-once-semantics/pressure
javac *.java && java Main

cd volume-16-streaming-realtime-data/paper-85-exactly-once-semantics/principle-applied
javac *.java && java Main

cd volume-16-streaming-realtime-data/paper-85-exactly-once-semantics/wrong-direction
javac *.java && java Main
```
