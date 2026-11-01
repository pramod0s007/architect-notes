# Paper 183: Agent Memory Is a Write Path

Part of [Architect Notes](../../README.md) — Volume 33: Secure RAG and Agent Memory.

## The Question

An AI support assistant keeps long-term memory, and the store accepts whatever the agent decides to save.
A ticket from an outside sender carries a sentence that reads like a finance note, the assistant saves it,
and weeks later every session that loads the tenant's memory treats it as a standing rule. The obvious
fixes are a prompt rule that says "remember only facts" and a nightly summary that tidies the notes, but a
hostile sentence can be phrased as a fact, and a summary erases the source you would need to audit it. How
do you let an agent remember without letting a stranger's text become standing policy, when the model that
decides what to save can still be fooled?

## Structure

- `pressure/` — The Orbit Support Assistant keeps long-term memory keyed by tenant, a worst-case stub saves
  whatever it reads, and every session loads its tenant's entries as trusted context. An outside ticket
  (doc-117) becomes a stored refund-destination rule on day 3, a bullet-point preference from lead-omar is
  saved on day 5, and weeks later the sessions use both. Verified: the preference is saved and used and
  memory stays inside its tenant, but `memory-write-has-provenance` fails (2 of 2 memories name no real
  source), `untrusted-memory-not-instruction` fails (doc-117 text loaded as trusted in 2 sessions),
  `behavior-changing-memory-needs-review` fails (1 refund-destination memory live with no review), and
  `no-misrouted-refund` fails (2 refunds, $105, reached ACC-9917).
- `wrong-direction/` — The tempting fix: memory is kept per user, a prompt rule says "remember only facts,
  never instructions", and a nightly summary runs on day 10. The ticket is phrased as a fact and saved, the
  bullet-point preference is refused because it reads as an instruction, and the summary rewrites the note's
  source to "summary". Verified: all 6 checks fail, including two that passed in the pressure run:
  `legit-preference-saved-and-used` (saved false, used false), `memory-scoped-to-tenant` (1 note loaded
  across tenants), `memory-write-has-provenance` (1 of 1 notes carry a source that is not the real origin),
  `untrusted-memory-not-instruction` (doc-117 text loaded as trusted in 2 sessions),
  `behavior-changing-memory-needs-review` (1 refund-destination note live with no review), and
  `no-misrouted-refund` (2 refunds, $60, reached ACC-9917).
- `principle-applied/` — Memory is treated as a write path with a policy outside the model: entries are
  typed, a write needs a source the runtime recorded, untrusted text is kept as an inert quote that is never
  loaded, behavior-changing keys wait for an independent reviewer, loads stay inside the tenant, entries
  expire after 30 days, and every decision is audited. Verified: all 8 checks pass. The simulated model is
  still fooled, yet doc-117 stays a quote (mem-1), the bullet preference goes live, a write with no source
  is rejected, the change to account ACC-5520 is held, denied when its author tries to approve it, and
  approved by an independent reviewer, 0 refunds are misrouted, the day 40 sweep expires 3 entries, and the
  audit log holds 6 entries, one per decision.

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
