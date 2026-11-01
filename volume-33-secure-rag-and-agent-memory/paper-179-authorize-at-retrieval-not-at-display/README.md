# Paper 179: Authorize at Retrieval, Not at Display

Part of [Architect Notes](../../README.md) — Volume 33: Secure RAG and Agent Memory.

## The Question

A support assistant answers questions from a retrieval index built over every document a merchant has,
including contract terms. The interface hides the link to documents a user may not open, so the design
review signs off. But the model has already read the text, and a rep with no contracts access receives an
accurate summary of the renewal date, the uplift, the notice period and the exit cost. Screening the
answer for restricted titles and terms does not hold either: a paraphrase, or a list of bare figures,
carries the content past every word on the list while a legitimate reader gets blocked. Where does the
permission check have to sit so that a document the user may not read never reaches the model's context at
all, and what does that placement cost?

## Structure

- `pressure/` — Nine chunks are indexed as text only, the retriever ranks all of them for every question,
  the top 3 join the model's context, and only afterwards does the permission service hide links. A stub
  model quotes, restates or lists what it was given. Verified: `lead-omar`'s legitimate answer works
  (`legit-permitted-answer-works` passes, 4 of 4 renewal facts), but `restricted-chunk-never-in-context`
  fails (7 chunks the caller may not read reached the model), `restricted-facts-absent-from-answer` fails
  (5 of 5 facts reached `rep-ana`), `acl-prefilter-before-ranking` fails (9 of 36 scored chunks were
  unreadable by the caller) and `retrieval-decision-logged` fails (0 of 4 records name a chunk).
- `wrong-direction/` — A 12-entry watch list screens the answer text for restricted titles, ids and
  terms, in front of the same retrieval. Verified: the first two answers are blocked, but only because the
  quote format adds a title and one chunk happens to use the word agreement; the bare figures in the third
  answer pass, so `restricted-facts-absent-from-answer` fails (4 of 5 facts reached `rep-ana`);
  `restricted-chunk-never-in-context` fails (7 chunks) and `acl-prefilter-before-ranking` fails (9 of 36)
  because retrieval is unchanged; and `legit-permitted-answer-works` fails (0 of 4 renewal facts reached
  `lead-omar`) because his own answer is blocked as a false positive.
- `principle-applied/` — Every chunk is indexed with its ACL and a version, and the retriever filters
  inside the query by the caller's delegated groups before ranking, over-fetches 5 chunks for 3 slots,
  re-checks them on live permissions and logs ids and counts, never text. A pending permission change
  leaves one stale chunk in the index. Verified: all 6 checks pass. The stale chunk is scored in all three
  of the rep's questions and denied on fetch each time, 0 restricted chunks and 0 of 5 restricted facts
  reach the rep, 12 of 12 top-k slots hold readable chunks, 4 of 4 retrievals are logged, and the lead
  still gets 4 of 4 renewal facts. The recall cost is reported too: best score 14 unfiltered against 7
  permitted.

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
