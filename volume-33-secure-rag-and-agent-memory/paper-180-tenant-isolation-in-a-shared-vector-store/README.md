# Paper 180: Tenant Isolation in a Shared Vector Store

Part of [Architect Notes](../../README.md) — Volume 33: Secure RAG and Agent Memory.

## The Question

To cut cost, a commerce platform moves every merchant's knowledge into one shared vector store. The tenant
id is an optional query filter, so any code path that omits it, and any "find similar" query, searches
every merchant's chunks. A new feature forgets the filter and the demo works beautifully: it returns the
best answer in the whole company, which belongs to another merchant. The obvious fix is a rule in the
prompt ("only for merchant 3012") plus code review that checks every call site passes the filter. But the
filter value comes from the request body, a blank value means no filter, and a model is asked to obey a
prompt rule, never forced to. How do you make isolation structural instead of a parameter every query must
remember, and how do you check that no result ever crosses a tenant line?

## Structure

- `pressure/` — Orbit Insights searches one shared collection of 7 chunks from 2 merchants. The tenant id
  is an optional filter, the draft-answer path leaves it out, and find-similar has no tenant argument at
  all, so a worst-case model stub repeats whatever ranks first. Verified: the filtered search for m3012
  returns doc-102, but `no-cross-tenant-results` fails (2 of 5 returned chunks belong to another tenant)
  and `query-without-tenant-rejected` fails (2 queries with no tenant ran against the whole store).
- `wrong-direction/` — The rule "only for merchant 3012:" is prepended to every query, and a review checks
  that all 3 call sites pass a tenant argument. The value still comes from the request body, and blank
  means no filter. Verified: the reviewed draft-answer path returns doc-102, but `no-cross-tenant-results`
  fails (2 of 4 returned chunks belong to another tenant), `query-without-tenant-rejected` fails (1 query
  with no tenant ran against every tenant) and `tenant-from-token-not-request` fails (the request naming
  m4417 got chunks from m4417).
- `principle-applied/` — The tenant is taken from the verified token, never from the request body. A
  tenant-scoped client is bound to one partition and none of its read paths takes a tenant argument, a
  request without a tenant is rejected, and a canary chunk per tenant is probed in both directions, by text
  and by id. Verified: all 5 checks pass. The draft-answer path returns doc-102, the forged body is ignored
  and alerted, 2 of 2 requests without a tenant are rejected, 0 of 6 returned chunks cross a tenant line,
  and 4 canary probes return nothing from the other tenant.

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
