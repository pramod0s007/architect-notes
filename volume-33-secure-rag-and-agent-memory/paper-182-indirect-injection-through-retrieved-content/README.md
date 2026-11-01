# Paper 182: Indirect Injection Through Retrieved Content

Part of [Architect Notes](../../README.md) — Volume 33: Secure RAG and Agent Memory.

## The Question

An assistant answers questions from the company wiki and can also send email. Tenant and permission
filters already decide which pages it may retrieve, so the index looks clean. Then one ordinary editor adds
a sentence to a runbook asking for the on-call roster to be forwarded to an outside address, and the next
retrieval carries that sentence into the prompt beside the real question. Wrapping retrieved text in tags
and stripping known attack phrases at index time do not stop a reworded sentence, and they delete a
legitimate step that happens to use the same words. How do you defend a RAG-based agent against prompt
injection hidden in indexed documents?

## Structure

- `pressure/` — The Orbit Insights assistant pastes the top three retrieved wiki chunks into its prompt as
  system knowledge, with the editor and tier dropped, a worst-case model stub proposes a call for every
  instruction it finds, and the runtime executes them all. Verified: the summary email the caller asked for
  and the grounded answer work, but `retrieved-instruction-not-executed` fails (38 roster entries sent to an
  outside address), `provenance-labels-on-every-chunk` fails (0 of 3 chunks labeled) and
  `side-effect-from-untrusted-chunk-held` fails (0 of 1 risky calls held).
- `wrong-direction/` — A five-phrase strip runs at index time, and the three retrieved chunks are wrapped in
  `<retrieved>` tags under a data-only instruction, in front of the same pipeline. Verified: doc-104#1, the
  obvious wording, is removed, but doc-102#2, the same intent written as an audit rule, passes, so
  `retrieved-instruction-not-executed` fails (38 roster entries sent to an outside address),
  `provenance-labels-on-every-chunk` fails (0 of 3) and `side-effect-from-untrusted-chunk-held` fails
  (0 of 1); and `legit-grounded-answer-works` fails (1 of 2 steps) because doc-103#1, a real pager step
  that uses a banned phrase, is removed as a false positive.
- `principle-applied/` — Every chunk carries a source, tier and editor label from the index to the
  tool-call gate, a cheap detector quarantines a low-trust page for human review, and a policy outside the
  model returns ALLOWED or HELD from the origin of the instruction. Verified: all 6 checks pass. The model
  is still fooled, yet the summary email the caller asked for is allowed, the roster mail is held for
  approval (0 entries sent outside), doc-104#1 is quarantined with 1 review task open, each of the 3 chunks
  reaches the reader with its source badge, and the answer keeps 2 of 2 required steps.

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
