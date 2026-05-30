# Interview Discussion

## Question

When should Visitor Pattern be used?

## Weak Answer

When you have many classes.

## Strong Answer

When operations must run across a stable object hierarchy and interactions depend on concrete types (double dispatch).

## Questions To Ask

- Is the object set relatively stable?
- Are operations growing faster than object types?
- Can you accept adding a new visitor per operation?
- Would a lookup table be simpler?

## Key Insight

Visitor is about **object interaction structure**, not diagram aesthetics.
