# Cloud File Storage — Evaluated with Quality Attribute Scenarios

**Evaluation method:** Quality Attribute Scenarios (QAS) — closing the loop with Paper 52

**Closes Volume 10's independent-blueprint arc** — picking up right after Paper 57 (Social Feed System).

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

Storing every version of an edited file in full costs exactly what it looks like (measured: 5 versions of a 100,000-byte file with tiny edits cost 500,000 bytes). Content-defined chunking — splitting a file into pieces at boundaries determined by a rolling hash over local content, not fixed byte offsets — fixes this by only paying for chunks that actually changed (measured: a 5-byte insertion left 516 of 517 chunks completely unaffected). This paper then shows the overcorrection: fixed-size chunking, which looks identical in shape but fails catastrophically the moment an edit isn't a pure append, because every downstream chunk boundary shifts.

## The Measurements

- **No chunking**: 500,000 bytes stored for 5 versions of a 100,000-byte file with small edits (5x the file size).
- **Content-defined chunking**: a 5-byte insertion near the start of a 100,000-byte file needed only 1 new chunk out of 517 — 100,166 total bytes stored for both versions, not 200,005.
- **Fixed-size chunking**: the identical insertion needed all 782 of 782 chunks — 200,005 bytes stored, functionally identical to no deduplication at all.

## The QAS Finding

**A chunking implementation that chunks and dedups can still deliver zero storage benefit if chunk boundaries are fixed byte offsets instead of content-determined** — a boundary-shift failure that only shows up for edits that aren't pure appends, which is exactly why the Response Measure has to specify the real edit scenario, not a convenient one.

## Code Examples in This Repo

| Example | What It Shows |
|---------|--------------|
| [`pressure/`](./pressure/) | No deduplication — measured 5x storage cost for 5 versions with tiny edits |
| [`principle-applied/`](./principle-applied/) | Content-defined chunking — measured 1 new chunk out of 517 for a mid-file insertion |
| [`wrong-direction/`](./wrong-direction/) | Fixed-size chunking — measured all 782 chunks registering as new for the identical insertion |

### How to Run

```bash
cd volume-10-system-design-blueprints/paper-58-cloud-file-storage/pressure
javac *.java && java Main

cd volume-10-system-design-blueprints/paper-58-cloud-file-storage/principle-applied
javac *.java && java Main

cd volume-10-system-design-blueprints/paper-58-cloud-file-storage/wrong-direction
javac *.java && java Main
```
