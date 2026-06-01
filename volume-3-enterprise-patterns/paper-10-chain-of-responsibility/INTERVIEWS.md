# Interview Discussion

## Question

When should Chain of Responsibility be used?

## Weak Answer

For any multi-step process.

## Strong Answer

When a request must pass through ordered handlers that may process, enrich, or terminate the flow independently.

## Questions To Ask

- Must stages be reorderable?
- Can a stage short-circuit the pipeline?
- Should handlers stay loosely coupled?

## Key Insight

Chain models **flow**, not business rules (Specification) or encapsulated operations (Command).
