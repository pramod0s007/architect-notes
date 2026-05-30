# Interview Discussion

## Question

When would you use a lookup table instead of polymorphism?

## Weak Answer

Lookup tables are always faster.

## Strong Answer

When the decision matrix is finite, stable, and benefits from data-driven configuration rather than type hierarchies.

## Questions To Ask

- How often do new pairs appear?
- Must non-developers configure behavior?
- Is performance predictable?
- Is the matrix sparse or dense?

## Key Insight

Architecture is choosing the **simplest structure** that survives expected growth.
