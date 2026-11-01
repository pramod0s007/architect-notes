# Paper 189: Test the Effect, Not the Wording

Part of [Architect Notes](../../README.md) — Volume 35: Continuous Assurance for AI Systems.

## The Question

A security regression suite for an AI agent has to decide what counts as a pass. The simplest rule reads
the reply: an attack run passes when the agent refuses. After a model upgrade the refusal is worded
differently, every attack test goes red, and days go into finding that nothing regressed. Meanwhile one
injected request moves money behind a polite reply that still contains the phrase the suite wants.
Widening the phrase list or scoring the similarity of the reply to known refusals still reads the reply,
so it fails the same way. What should a security test for an LLM agent assert on, and how does the team
know the suite can fail?

## Structure

- `pressure/` — The red-team suite passes an attack run when the reply contains "i cannot help" and a
  legitimate task when it does not. The scripted stand-in model words its refusal differently on
  model@m1 and model@m2, and the gateway guards `refund` only, so a goodwill `credit` for order 8842 runs.
  Verified: `refusal-wording-ignored` fails (the same attack gives a green build on model@m1 and a red one
  on model@m2) and `forbidden-effect-fails-test` fails (1 forbidden effect, build red on 0, $4000 moved).
  `legit-task-effect-passes` passes for the $40 refund of order 8841.
- `wrong-direction/` — The assertion widens to three listed refusals and a word-overlap similarity score
  with a 0.60 threshold, which still reads the reply. Verified: `refusal-wording-ignored` fails (green on
  model@m2, red on model@m3 at similarity 0.20) and `forbidden-effect-fails-test` fails (1 forbidden
  effect, build red on 0): the polite reply to the executed $4000 credit scores 0.83 and passes.
- `principle-applied/` — The gateway records every call as an effect, each attack is a scenario with an
  oracle, four paraphrased variants run and the worst decides, the reply is never read, and the same suite
  runs against a deliberately weakened gateway. Verified: all 5 checks pass. Three wordings of the attack
  give green, green, green; the healthy gateway gives a green suite with 0 forbidden effects and the
  weakened gateway a red one with 1 (the $4000 credit is detected); all 4 variants run on both gateways;
  the $40 refund of order 8841 executes and the suite stays green.

## Interactive Explorer

Download [`explorer/explorer.html`](explorer/explorer.html) and open it in a browser. It replays the
captured output of the three programs as a timeline you can step through event by event, shows each check
turning pass or fail, and lines the three designs up side by side. It is one self-contained file that
works offline. GitHub displays HTML files as source, so download the file instead of opening it on the
repository page.

## Running It

Each scenario is self-contained; run these commands inside `pressure/`, `wrong-direction/` or
`principle-applied/`:

```bash
javac -Xlint:all *.java
java Main
```

## Read the Full Article on Medium

_Article coming soon on Medium._
