# Interview Discussion

## Question

When should you use Builder instead of a constructor or factory?

## Weak Answer

Whenever there are many fields.

## Strong Answer

When construction has many optional parameters, invariants must be validated together, and call sites need readable, ordered assembly — especially for requests and configuration.

## Questions To Ask

- Is the built object immutable?
- Where does validation live?
- Could a simple factory method suffice?

## Key Insight

Builder solves **construction readability**, not runtime behavior variation.
