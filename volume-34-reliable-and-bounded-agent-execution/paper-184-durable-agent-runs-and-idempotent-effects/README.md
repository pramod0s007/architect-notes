# Paper 184: Durable Agent Runs and Idempotent Effects

Part of [Architect Notes](../../README.md) — Volume 34: Reliable and Bounded Agent Execution.

## The Question

An overnight agent works through a batch of refunds and customer emails. Midway through, its worker is
rescheduled, the orchestrator restarts the run, and the model, which remembers nothing, plans the batch
again from the top. Finished refunds are paid a second time and finished emails go out again, although
every call in the trace is valid. The obvious fix is a prompt rule that tells the model to skip anything
it already did, but the model can only compare wording, and wording changes between attempts while two
different steps can share the same words. How do you let a run crash halfway and resume without repeating
a finished effect, when the model's context is the one place that does not survive the restart?

## Structure

- `pressure/` — The Orbit Ops Agent keeps a run's plan and progress only in the model's context. Worker 1
  dies at t=7 with step s3 unacknowledged, the run restarts on worker 2 with an empty context, and the
  model replans from step one while the gateway mints a new request id for every attempt. Verified: the
  uninterrupted run-0416 works, but `effect-applied-exactly-once` fails (6 refunds, $230 paid against $135
  intended, 4 emails against 3 intended), `resume-skips-completed-steps` fails (2 of 2 completed steps sent
  again after the restart) and `idempotency-key-stable-across-retries` fails (3 of 3 re-sent steps went
  out under a new request id).
- `wrong-direction/` — A prompt rule tells the Ops Agent to skip any action whose description is already
  in the history, and the history even survives the crash. Verified: the rule skips the one verbatim
  repeat, but it also skips a legitimate second $20 credit to order 5101 and misses the two reworded
  repeats. `effect-applied-exactly-once` fails (4 refunds for 4 intended, yet 1 repeated and 1 missing:
  $145 paid against $135), `resume-skips-completed-steps` fails (1 of 2 completed steps sent again),
  `idempotency-key-stable-across-retries` fails (2 of 3 re-proposed steps changed wording, so their
  identity changed) and `legit-uninterrupted-run-works` fails (only 1 of 2 refunds reached order 5101).
- `principle-applied/` — The plan is persisted before the first effect, each step writes STARTED with a key
  and then DONE, and the key is the run id, the step id and a digest of the canonical action, never the
  free text. A resumed worker reads the log, skips finished steps without asking the model to replan, and
  re-issues only the in-flight step under its logged key. Verified: all 5 checks pass. Run-0417 resumes
  from the log, Meridian answers the s3 re-issue with DEDUPED (key applied at t=6), a reworded late copy of
  s3 is deduped too, the run ends with 4 refunds ($135) and 3 emails, and the uninterrupted run-0416 still
  applies both $20 credits.

## Interactive Explorer

Download [`explorer/explorer.html`](explorer/explorer.html) and open it in a browser. It replays the real
output of the three programs as an animated timeline you can step through event by event, shows each
check turning pass or fail, and compares the three designs side by side. It is one self-contained file
that works offline. GitHub displays HTML files as source, so download the file rather than opening it on
the repository page.

## Running It

Each scenario is self-contained; run these commands inside `pressure/`, `wrong-direction/` or
`principle-applied/`:

```bash
javac -Xlint:all *.java
java Main
```

## Read the Full Article on Medium

_Article coming soon on Medium._
