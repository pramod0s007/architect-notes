# Interview Discussion

## Question

When should Strategy Pattern be used?

## Weak Answer

When there are many if-else statements.

## Strong Answer

When behavior varies independently from the caller and is expected to grow over time.

## Questions To Ask

- What behavior is changing?
- How frequently does it change?
- Does the caller remain stable?
- Can behavior be isolated behind an interface?
- Is composition preferable to inheritance?

## Key Insight

Strategy Pattern is not about eliminating if-else.

It is about isolating behavior variation.
