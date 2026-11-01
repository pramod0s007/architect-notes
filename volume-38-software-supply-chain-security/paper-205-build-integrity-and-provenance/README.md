# Paper 205: Build Integrity and Provenance

Part of [Architect Notes](../../README.md) — Volume 38: Software Supply Chain Security.

## The Question

A release pipeline builds a gateway and uploads the file together with a record of where it came from.
The registry checks that the record matches the file and stores both. But the job that built the file
also wrote the record, so the two agree even after someone alters one build step. Signing the record
does not repair that when the signing key sits inside the same job. And a statement written by a separate,
honest party can still describe a tampered build, because it reports what ran. How do you make a
registry trust the answer to "which commit, built where, by what" when the evidence can be written by
the very thing it describes?

## Structure

- `pressure/` — The build job packs the workspace and writes its own metadata, and the registry accepts
  an upload when the metadata digest equals the bytes. An intruder alters one build step before release
  1.4.2. Verified: release 1.4.1 is accepted, but `provenance-binds-artifact-to-source-commit` fails (1
  stored artifact does not rebuild from a reviewed commit), `provenance-written-outside-build-steps`
  fails (2 of 2 stored statements were written by the job) and `tampered-build-detected-by-rebuild`
  fails (the tampered 1.4.2 bytes are stored, and only a clean rebuild by hand noticed). 8 events, 2
  uploads accepted, 0 refused.
- `wrong-direction/` — The job signs its own statement as its last step, and the registry verifies the
  signature and the digest. Verified: an upload from outside, signed with another key, is rejected, but
  the altered step runs inside the job, so the job signs the altered bytes and the registry accepts them.
  The same three checks fail: `provenance-binds-artifact-to-source-commit` (1 stored artifact does not
  rebuild from a reviewed commit), `provenance-written-outside-build-steps` (2 of 2 statements written by
  the job) and `tampered-build-detected-by-rebuild`. 9 events, 2 accepted, 1 refused.
- `principle-applied/` — An isolated builder runs the steps, a separate attestor holding a key the steps
  never see signs a statement of commit, builder and artifact digest, and the registry applies five
  rules in order of cost: signature, builder allow-list, artifact digest, reviewed commit, rebuild.
  Verified: all 5 checks pass. The altered 1.4.2 is signed truthfully and still refused by the rebuild
  (41d1e76a472e against the statement's b5bdc7578148), four more uploads are each refused by exactly one
  rule, and the restored 1.4.2 is accepted. 12 events, 2 accepted, 5 refused, 3 rebuilds, 0 accepted
  without a source match.

## Interactive Explorer

Download [`explorer/explorer.html`](explorer/explorer.html) and open it in a browser. It replays the
captured output of the three programs event by event, shows each check change between pass and fail,
and sets the three designs side by side. The file is self-contained and works offline. GitHub shows
HTML as source, so save the file first instead of opening it on the repository page.

## Running It

Each scenario is self-contained; run these commands inside `pressure/`, `wrong-direction/` or
`principle-applied/`:

```bash
javac -Xlint:all *.java
java Main
```

## Read the Full Article on Medium

_Article coming soon on Medium._
