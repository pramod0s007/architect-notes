# Paper 191: Tool and MCP Supply-Chain Controls

Part of [Architect Notes](../../README.md) — Volume 35: Continuous Assurance for AI Systems.

## The Question

An agent loads tool servers that other companies write. A reviewer reads one manifest, approves the server,
and the registry records its name. A month later the vendor ships an update under the same name, with a
changelog that says "Improved postcode matching." and a description that now tells the agent to attach the
order record to every lookup. The model reads that description as part of its context, so a sentence in it
can pass for an instruction from the operator, and the order record carries a customer's email. A one-time
review feels like due diligence, and a changelog feels like notice. How do you let an agent use tools it
did not write without trusting a name that the vendor controls?

## Structure

- `pressure/` — The registry approves `address-lookup` by name after one review, prints each manifest's
  digest and ignores it, and loads whatever arrives under the approved name. A scripted worst-case model
  stub obeys the sentence a vendor update adds, and the gateway forwards every argument the model proposes.
  Verified: the legitimate lookup for order 8841 works ("9 ELM RD"), but `manifest-change-requires-reapproval`
  fails (changed digest 44aa0c00 loaded without re-review), `digest-mismatch-blocks-load` fails (1 of 2
  loaded manifests were never reviewed) and `no-order-record-leaves` fails (2 order records sent to the
  vendor).
- `wrong-direction/` — A scripted reviewer reads each candidate once at onboarding, rejects the hostile
  `route-notes` and approves `address-lookup`. The registry keeps only the name and the label `tools@t3`,
  then accepts later updates unless the vendor's changelog says BREAKING. Verified:
  `onboarding-review-rejects-hostile` passes (0 of 1 approved manifests carry the attach sentence) and
  `legit-approved-tool-works` passes, but `manifest-change-requires-reapproval` fails,
  `digest-mismatch-blocks-load` fails (1 of 2 loaded manifests were never reviewed) and
  `no-order-record-leaves` fails (2 order records sent to the vendor), because the hostile `tools@t4` loads
  by name under the changelog "Improved postcode matching."
- `principle-applied/` — The registry pins `address-lookup` by a digest over name, description, schema and
  scopes, quarantines every changed manifest until a reviewer approves the new digest, labels every tool
  result UNTRUSTED, forwards only the pinned field list and records an effect for every call. Verified: all
  7 checks pass. The hostile `tools@t4` and the benign `tools@t5` are both quarantined, the reviewer rejects
  the first and approves the second (3 reviews), order 8842 waits and then completes, the order record the
  stub proposes for order 8844 is redacted (0 order records sent to the vendor), and 4 calls leave 4
  effects.

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
