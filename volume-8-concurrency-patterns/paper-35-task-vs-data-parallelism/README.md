# Task vs Data Parallelism — Decision Tree

**Concept:** Task parallelism (different operations, overlapped) vs. data parallelism (same operation, partitioned data) — and why picking the wrong one can silently produce wrong output

---

## Read the Full Article on Medium

[Task vs Data Parallelism — Decision Tree](https://medium.com/@replytopramods.aws/task-vs-data-parallelism-decision-tree-452d413ff49f)

---

## What This Paper Is About

"Just parallelize it" isn't a plan — there are two structurally different shapes of concurrent work. Data parallelism applies the same operation across independent data partitions (`parallelStream`, `ForkJoinPool`). Task parallelism overlaps different, heterogeneous operations across a stream of items (a pipeline, one pool per stage). This paper shows both correctly, and shows what happens when you apply data-parallel thinking to a dependency chain: not a slowdown, silently wrong output.

## The Pressure / The Wrong Direction

Treating three dependent pipeline stages (decode → resize → watermark) as three independent "parallel" tasks — each reading the original raw input instead of the previous stage's output — compiles, runs, and produces a result that's silently different from the correct sequential output. No exception. Verified: `encoded(watermarked(image1.raw))` instead of `encoded(watermarked(resized(decoded(image1.raw))))`.

## The Principle

**The test is whether the concurrency is across partitions of the same operation, or across different operations.** Same operation, independent data → data parallelism. Different operations, overlapped across a stream → task parallelism. A dependency chain within a single item is neither, and forcing it into either shape produces wrong results, not just wasted effort.

## Pros

- Data parallelism (`parallelStream`) needs zero coordination code — the JVM partitions and merges for you
- Task parallelism (per-stage pools) lets genuinely different work overlap, improving throughput on a continuous stream
- Both are measurable and verifiable — correctness checks catch a wrong split immediately

## Cons

- Data parallelism only fits work with zero cross-item dependency — forcing a dependency chain into it produces silently wrong output
- Task parallelism needs one pool per stage and careful chaining — more moving parts than a straight sequential call
- Neither is worth the complexity below the volume where sequential processing is already fast enough

## When NOT to Reach for Either

- Low volume, sequential is already fast enough
- A true dependency chain for a single unit of work — no parallelism changes that for one item
- CPU-bound work already saturating available cores — more parallel tasks beyond core count adds overhead, not throughput

## Code Examples in This Repo

| Example | Domain | What It Shows |
|---------|--------|--------------|
| [`evolution/`](./evolution/) | Image processing pipeline (decode/resize/watermark/encode) | v1: sequential, one image (fine) -> v2: data-parallelizing dependent steps, silently wrong output (measured) -> v3: correct data parallelism (1000 images, `parallelStream`) and correct task parallelism (20 images through a 4-stage pipeline, one pool per stage) |

### How to Run

```bash
cd volume-8-concurrency-patterns/paper-35-task-vs-data-parallelism/evolution/v1
javac *.java && java Main

cd volume-8-concurrency-patterns/paper-35-task-vs-data-parallelism/evolution/v2
javac *.java && java Main

cd volume-8-concurrency-patterns/paper-35-task-vs-data-parallelism/evolution/v3
javac *.java && java Main
```
