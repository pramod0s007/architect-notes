# Paper 172: Sandboxes and Egress Control

Part of [Architect Notes](../../README.md) — Volume 31: Agentic AI Security Architecture.

## The Question

An AI support assistant holds a customer's email address, reads tickets from strangers, and writes replies
that a browser renders, loading every image on its own. A hidden instruction in one ticket asks for a
status image whose link carries the address, and a server the red team controls reads it from the request.
No tool did anything it was not allowed to do. The obvious fix is a filter that redacts email addresses
from the model's output, but the attacker re-encodes the data, routes it through a host the product has to
allow, and the filter also blanks the address a merchant is entitled to see. How do you make sure that when
the agent is tricked once, the data has nowhere to go?

## Structure

- `pressure/` — The Orbit Support Assistant has a general fetch tool, and its console renders every image
  the model writes. A worst-case model stub that always holds the buyer address obeys the instructions in
  two red-team tickets, and the simulated network delivers every request. Verified: the status API call
  works and a normal merchant request still shows the buyer address, but `no-data-leaves-sandbox` fails
  (the attacker decoded the buyer address from 3 requests), `egress-deny-by-default` fails (2 requests
  reached hosts outside the allow-list), `rendered-links-sanitized` fails (2 model-chosen images loaded by
  the browser) and `egress-attempts-logged` fails (0 log entries for 10 outbound attempts).
- `wrong-direction/` — A regular expression redacts email-shaped strings in every tool-call URL and in the
  reply, and nothing else changes. Verified: the plain-text address is redacted from the image link, but
  `no-data-leaves-sandbox` fails (the hex form still reaches the red team's tenant store, 2 requests
  decoded), `egress-deny-by-default` fails (2 requests reached hosts outside the allow-list),
  `rendered-links-sanitized` fails (2 images loaded), `egress-attempts-logged` fails (0 log entries for 10
  attempts), and `legit-reply-keeps-buyer-address` fails because the merchant's own reply loses the address.
- `principle-applied/` — Tool calls leave through one chokepoint: an egress gateway with a strict host
  parse, a deny-by-default destination allow-list, outbound payload inspection in four encodings, and a log
  that records verdict, host and size but never the payload. Replies are rewritten before rendering so only
  first-party images survive. The sandbox is modeled as that single chokepoint, not as real isolation.
  Verified: all 6 checks pass. The model is still fooled, yet 6 requests left the sandbox and the attacker
  decoded 0 of them, 0 requests reached unlisted hosts, the merchant kept the buyer address and the order
  image, and 10 outbound attempts produced 10 log entries with no payload in any of them.

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
