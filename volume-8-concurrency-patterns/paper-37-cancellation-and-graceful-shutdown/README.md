# Cancellation & Graceful Shutdown

**Concept:** Cooperative cancellation (interruption is a request, not a command) — and the two ways code silently ignores it

---

## Read the Full Article on Medium

[Cancellation & Graceful Shutdown](https://medium.com/@replytopramods.aws/cancellation-graceful-shutdown-e765116a60c3)

---

## What This Paper Is About

`Thread.interrupt()` doesn't stop a thread — it sets a flag and, if the thread is blocked in an interruptible call, throws `InterruptedException`. Nothing after that is automatic. This paper shows a worker that genuinely cannot be cancelled (measured: it keeps processing jobs after being interrupted), the two independent bugs that cause it, and the fix — plus a demonstration that swallowing the exception without restoring the flag hides the interrupt from code further up the call stack.

## The Pressure: A Worker That Ignores shutdownNow()

`while (true)` never checks anything an interrupt could change, and the `catch (InterruptedException e) {}` block discards the signal entirely. Measured: after calling `interrupt()`, the worker went on to process 5 more jobs fed to its queue — indistinguishable from a worker that was never interrupted.

## The Principle

**Interruption is cooperative — a thread has to check for it and choose to stop.** Two things make a worker actually cancellable: the loop condition checks `isInterrupted()`, and the catch block restores the interrupted status (which catching the exception clears as a side effect) before stopping.

## Pros

- A cooperating worker responds to `shutdownNow()` within one blocking call's latency, not never
- Restoring interrupted status preserves the signal for any caller further up the stack that also checks it
- The escalation sequence (`shutdown()` → `awaitTermination()` → `shutdownNow()`) gives well-behaved tasks a bounded chance to finish before force-interrupting stuck ones

## Cons

- Cooperative cancellation requires every layer of code to actually cooperate — one swallowed exception anywhere breaks it
- Checking cancellation status has to happen at safe points, not mid-transaction or mid-cleanup
- `shutdownNow()` still can't force a genuinely uncooperative task to stop — verified in this paper's own broken example

## When NOT to Add Cancellation Checks

- Short, non-blocking loops — a flag check between fast iterations is already sufficient
- Code that must run to completion regardless (a commit, a cleanup step) — check before entering, not inside
- One-shot tasks with no loop — nothing to cooperate with beyond a single check, if that

## Code Examples in This Repo

| Example | Domain | What It Shows |
|---------|--------|--------------|
| [`evolution/`](./evolution/) | Background job worker | v1: non-blocking loop with a checked flag, stops promptly -> v2: `while(true)` + swallowed `InterruptedException`, measurably un-cancellable -> v3: fixed cooperative worker (stops within one `take()` call) plus a live demonstration that swallowing without restoring hides the interrupt from the caller |

### How to Run

```bash
cd volume-8-concurrency-patterns/paper-37-cancellation-and-graceful-shutdown/evolution/v1
javac *.java && java Main

cd volume-8-concurrency-patterns/paper-37-cancellation-and-graceful-shutdown/evolution/v2
javac *.java && java Main

cd volume-8-concurrency-patterns/paper-37-cancellation-and-graceful-shutdown/evolution/v3
javac *.java && java Main
```
