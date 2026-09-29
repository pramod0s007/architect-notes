# Paper 161: AI/LLM Inference Cost-Routing

Part of [Architect Notes](../../README.md) — Volume 29: Cost-Aware Architecture (FinOps for Engineers).

## The Question

A large language model costs far more per query than a smaller one, and every query category has its
own historical accuracy on the cheap model — routing every query to the large model regardless of that
history means paying the large-model premium even for categories whose own record projects a much
cheaper route. The obvious fix looks like routing by a proxy such as query word count: short queries to
the cheap model, long queries to the expensive one. But word count and a category's own historical
small-model accuracy are two different signals, and a category where they diverge can end up costing
more under the proxy than under no routing at all. How do you route each query category to the
candidate route — direct to the large model, or a small-model attempt with a large-model retry on
failure — with the lowest modeled cost for that category's own history, including a disclosed fallback
for a brand-new category with too few historical samples to route by?

## Structure

- `pressure/` — Every query, in every category, is routed to the large model uniformly. Verified:
  `simple-faq` and `complex-analysis` both cost $2.00 for 100 queries, $4.00 total. `simple-faq` is
  assigned a 98% historical small-model accuracy in this demonstration's data, so its modeled cascade
  cost ($0.24) is far below its $2.00 uniform cost; `complex-analysis` is assigned a 5% historical
  small-model accuracy, so its cascade route would in fact cost more ($2.10) than direct-large — the
  uniform policy is not itself an overspend for that category.
- `wrong-direction/` — A word-count threshold (20 words) is used as a proxy for a category's own
  historical small-model accuracy: queries under the threshold go to the small model first, others go
  direct to the large model. Verified: `simple-faq`'s queries happen to be long (45 words) despite a
  high historical small-model accuracy, so they're routed direct-large at $2.00 — the same overspend
  the uniform policy already had. `complex-analysis`'s queries happen to be short (15 words) despite a
  low historical small-model accuracy, so they're routed to the cascade at $2.10 — a modeled cost
  higher than direct-large's $2.00 for that same category. Total: $4.10, worse than the pressure
  scenario's $4.00 uniform baseline for the same two categories.
- `principle-applied/` — Each category's own historical small-model accuracy is used to project the
  modeled cost of both candidate routes, and the route with the lower modeled cost is selected per
  category; a category with a sample count below a disclosed cutoff (10) is routed direct-large
  regardless of its recorded rate. Verified: `simple-faq` selects the cascade ($0.24, over 500 samples
  at 98% accuracy); `complex-analysis` selects direct-large ($2.00, over 400 samples at 5% accuracy);
  `new-diagnostic-category`, with only 3 historical samples, is routed direct-large ($0.40) under the
  disclosed cutoff rather than off its 3-sample 100% rate. Total: $2.64.

## Running It

Each scenario is self-contained:

```bash
javac -Xlint:all *.java
java Main
```

## Full Write-Up

See [MEDIUM-DRAFT.md](../../../architect-notes-private/volume-29-cost-aware-architecture-finops/paper-161-ai-llm-inference-cost-routing/MEDIUM-DRAFT.md)
(private repo) for the full analysis, comparison table, and interview Q&A.
