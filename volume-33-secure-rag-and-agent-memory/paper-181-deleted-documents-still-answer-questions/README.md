# Paper 181: Deleted Documents Still Answer Questions

Part of [Architect Notes](../../README.md) — Volume 33: Secure RAG and Agent Memory.

## The Question

A merchant asks Halcyon Commerce to delete a contract and its ticket history. The source rows are removed on
Monday, yet on Wednesday the assistant that answers questions from them still quotes the termination clause,
because the text lives on in a chunk store, a vector index and an answer cache that nobody told. The obvious
fixes are to hide the citation in the interface when the source id no longer resolves and to sweep stale rows
at night. Both leave a window open for hours, and the sweep never touches the cache it was not written for.
How do you honor a delete request in a RAG pipeline that has embeddings and caches, and know that it worked?

## Structure

- `pressure/` — The knowledge base deletes doc-101 and doc-102, but nothing tells the chunk store, the vector
  index or the answer cache, so Orbit Insights keeps answering from derived copies. Verified: the live payout
  question works, but `deleted-source-never-retrieved` fails (4 of 5 answers quoted a deleted source),
  `deletion-reaches-every-derived-copy` fails (12 copies remain: 5 chunks, 5 vectors, 2 cache entries),
  `cache-entry-purged` fails (2 cache entries still cite a deleted document) and
  `deletion-receipts-complete` fails (0 of 3 stores sent a receipt).
- `wrong-direction/` — The interface hides a citation card when its source id no longer resolves, and a
  nightly job removes chunks and vectors with unresolved sources. The job was written for two stores and never
  touches the answer cache. Verified: the card is hidden but the quote is already in the answer, the window is
  17 hours, `deleted-source-never-retrieved` fails (3 of 5 answers quoted a deleted source),
  `deletion-reaches-every-derived-copy` fails (2 copies remain, both in the answer cache),
  `cache-entry-purged` fails (2 cache entries still cite a deleted document) and
  `deletion-receipts-complete` fails (2 of 3 stores sent a receipt).
- `principle-applied/` — Every derived copy is written through one path that records lineage to its source,
  a delete fans out to each store and each store returns a receipt that is compared with an independent audit
  scan, and a liveness gate checks every retrieval candidate, cache hits included, against the source of
  record. Verified: all 5 checks pass. 0 of 5 answers quoted a deleted source, the gate blocked 3 candidates
  during the 3-hour gap before the fan-out, all 12 derived copies were purged with 3 of 3 receipts matching the
  audit scan, and the live payout question still answers.

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
