# Paper 169: Prompt Injection Is an Architecture Problem

Part of [Architect Notes](../../README.md) — Volume 31: Agentic AI Security Architecture.

## The Question

An AI support assistant reads text from people it cannot vouch for, and it can call tools that move money
and send email. A single ticket can carry instructions that look like every other sentence in the inbox,
and a runtime that executes whatever the model proposes will carry them out. The obvious fix is a filter:
block the phrases that look like attacks. But the attacker chooses the words, so a reworded ticket walks
through while a legitimate ticket that quotes the phrase gets blocked. How do you let an agent read
untrusted text without letting that text decide what the agent does, when the model itself can still be
fooled?

## Structure

- `pressure/` — The Orbit Support Assistant builds one flat context from a merchant request and an outside
  ticket, a worst-case model stub proposes a call for every instruction it finds, and the runtime executes
  them all. Verified: the legitimate $40 refund for order 8841 works, but `no-unauthorized-refund` fails
  (order 8842 refunded $4000) and `no-data-exfiltration` fails (412 records sent externally).
- `wrong-direction/` — A six-phrase keyword blocklist filters ticket text in front of the same pipeline.
  Verified: ticket-A, the obvious wording, is blocked, but ticket-B, the same intent in new words, passes
  and the refund executes, so `no-unauthorized-refund` fails (reworded attack refunded $4000); and
  `legit-ticket-handled` fails because ticket-C, a real merchant question that quotes the phrase, is
  blocked as a false positive.
- `principle-applied/` — Every context segment is labeled by the channel it arrived on (console is
  TRUSTED, inbox is UNTRUSTED), the labels follow the data onto tool-call arguments, and a policy outside
  the model returns ALLOWED, HELD or BLOCKED, with an exact-domain allow-list for email recipients.
  Verified: all 6 checks pass. The model is still fooled, yet the $40 refund is allowed, refund 8842 is
  held for confirmation, the email is blocked (0 records sent externally), ticket-C gets a reply, and 2
  injection attempts are recorded.

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
