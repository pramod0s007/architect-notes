# Paper 190: Release Gates for Prompts, Models and Tools

Part of [Architect Notes](../../README.md) — Volume 35: Continuous Assurance for AI Systems.

## The Question

Three things set how a support assistant behaves, and none of them goes through the release process that code does: the system prompt saved in an admin console, a model alias such as `latest` that the provider can move, and the tool schemas other teams publish. Each change is live for every tenant at once. A reviewer who reads the prompt diff covers the first and misses the other two. When a customer asks what was running last Tuesday, the console holds only today's values and there is no earlier release to return to. How should prompt, model and tool changes be pinned, tested and staged, so that a bad one is blocked or contained and any past release can be reproduced?

## Structure

- `pressure/` — The admin console keeps one live value for the prompt, the model label and the tool schema, and every save reaches all 5 tenants. A product manager edits the prompt, the provider moves `latest` from m1 to m2, and a tool owner raises the refund cap from 50 to 5000. The after-the-fact replay ends with 2 of 3 scenarios failing, and the morning traffic pays 800 dollars where the baseline pays 200.
  Verified: `unpinned-alias-rejected` fails (the live model is the alias latest, which meant m1 at tick 0 and m2 now), `failing-effect-suite-blocks-release` fails (2 of 3 changes shipped while the replay failed), `canary-regression-rolls-back` fails (the regression reached 5 of 5 tenants at once), `override-needs-second-approver` fails (3 of 3 changes had no second approver) and `past-release-reproducible` fails (0 of 6 ticks can be reproduced). `legit-clean-release-ships` passes for the clean edit v8 on m1.
- `wrong-direction/` — A senior reviewer reads each prompt diff and tries it in three playground chats on `latest`. The scripted reviewer rejects a harmless edit over the word "anything", approves the calm one, and never sees the alias move or the tool hotfix that skips review.
  Verified: `unpinned-alias-rejected` fails (m1 at review, m2 now), `failing-effect-suite-blocks-release` fails (2 of 3 changes shipped while the replay failed), `canary-regression-rolls-back` fails (5 of 5 tenants at once, nothing watches after review), `override-needs-second-approver` fails (2 of 3 changes had no second approver) and `past-release-reproducible` fails (0 of 7 ticks, the review log holds prompt labels only). `legit-clean-release-ships` passes for the reviewed edit v8 on m1.
- `principle-applied/` — A release is a manifest of the prompt-body digest, an exact model id and the tool-schema digest. A pin rule rejects `latest`, an effect-suite gate blocks regressions and critical failures, a one-tenant canary rolls back on a guardrail breach, and an override needs a second approver and never covers a critical failure.
  Verified: all 6 checks pass. R2 is rejected for naming the alias and 0 of 5 tenants follow the alias move. R4 is blocked, overridden by a second approver, alerted on the canary tenant and rolled back after reaching 1 of 5 tenants. R5 is refused as a critical failure, and 55 of 55 tenant-ticks match the registry.

## Interactive Explorer

Download [`explorer/explorer.html`](explorer/explorer.html) and open it in a browser. It replays the captured output of the three programs as a timeline you can step through event by event, shows each check turning pass or fail, and lines the three designs up side by side. It is one self-contained file that works offline. GitHub displays HTML files as source, so download the file instead of opening it on the repository page.

## Running It

Each scenario is self-contained; run these commands inside `pressure/`, `wrong-direction/` or `principle-applied/`:

```bash
javac -Xlint:all *.java
java Main
```

## Read the Full Article on Medium

_Article coming soon on Medium._
