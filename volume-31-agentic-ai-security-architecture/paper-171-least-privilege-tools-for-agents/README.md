# Paper 171: Least-Privilege Tools for Agents

Part of [Architect Notes](../../README.md) — Volume 31: Agentic AI Security Architecture.

## The Question

A commerce platform's support assistant answers merchants' order questions, and the quickest way to wire up
its tools was one `admin(command)` call into the operations console. That single tool can read any
merchant's orders, delete a catalog and refund any amount. The model that picks the commands reads text
nobody reviewed, so a confused or manipulated call is a matter of time, and a prompt that says "be careful"
changes nothing about what the tool can do. A longer list of forbidden verbs helps only against the verbs
someone remembered to write down. Every tool in the toolbox is authority an attacker can borrow. How do you
decide which tools an LLM agent gets, and what still holds when the model is wrong?

## Structure

- `pressure/` — The Halcyon support assistant holds one tool, `admin(command)`, which reaches five console
  verbs. A scripted worst-case model stub proposes four commands during a plain order-status task and the
  console runs all four. Verified: the legitimate question about order 5521 is answered ("shipped"), but
  `no-cross-merchant-access` fails (1 record of another merchant touched: order 5512, owned by m-377),
  `no-destructive-tool-exposed` fails (admin reaches 3 destructive verbs: delete, prune, reset),
  `tool-scope-matches-task` fails (the order-status task also ran delete and refund, and 6 listings were
  deleted) and `per-tool-limits-enforced` fails (largest refund $4000, against a rule of $100 per call and 2
  calls).
- `wrong-direction/` — The same pipeline with a longer fence: the prompt lists six forbidden verbs, and the
  console flags a listed verb as dangerous and refuses it. The model stub retries a refused goal with the
  next phrasing, as an agent loop does after an error. Verified: the legitimate question is still answered
  and `delete` is blocked (1 call blocked), but `no-destructive-tool-exposed` fails (admin still reaches 2
  destructive verbs the list never named: prune, reset), `tool-scope-matches-task` fails (the task also ran
  prune and refund), `no-cross-merchant-access` fails (1 record of another merchant touched) and
  `per-tool-limits-enforced` fails (largest refund $4000). The unlisted `prune` deleted 6 listings anyway.
- `principle-applied/` — A broker grants each task its own small toolbox, bound to the merchant from the
  login. A gateway outside the model checks every call against the toolbox, the merchant, a $100 refund cap
  and a budget of 2 refund calls, and a task with no safe tool goes to a person. Verified: all 6 checks
  pass. The same worst-case stub proposes 9 calls; 3 execute (the order lookup and two refunds, $140 in all)
  and 6 are refused. Order 5512 is denied to m-204, 0 destructive verbs are reachable, 0 listings are lost
  and the catalog cleanup is escalated to the support team.

## Interactive Explorer

Download [`explorer/explorer.html`](explorer/explorer.html) and open it in a browser. It replays the real
output of the three programs as a timeline you can step through one event at a time, marks each check as it
passes or fails, and puts the three designs side by side. It is a single self-contained file that works
offline. GitHub shows HTML files as source, so download the file instead of opening it on the repository
page.

## Running It

Each scenario is self-contained; run these commands inside `pressure/`, `wrong-direction/` or
`principle-applied/`:

```bash
javac -Xlint:all *.java
java Main
```

## Read the Full Article on Medium

_Article coming soon on Medium._
