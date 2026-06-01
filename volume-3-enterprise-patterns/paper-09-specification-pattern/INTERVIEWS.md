# Interview Discussion

## Question

When should Specification Pattern be used?

## Weak Answer

For all filtering logic.

## Strong Answer

When business rules must be composed, reused, and tested independently of persistence or API layers.

## Questions To Ask

- Are rules growing independently?
- Must rules combine with AND/OR?
- Should rules be unit-testable in isolation?

## Key Insight

Specification is about **rules variation**, not SQL builders alone.
