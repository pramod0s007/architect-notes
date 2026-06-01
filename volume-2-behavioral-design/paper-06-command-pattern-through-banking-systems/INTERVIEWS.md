# Interview Discussion

## Question

When should Command Pattern be used?

## Weak Answer

Whenever you need undo.

## Strong Answer

When operations must be encapsulated as objects that can be executed, stored, reversed, or scheduled independently.

## Questions To Ask

- Must operations be logged or replayed?
- Is undo a first-class requirement?
- Does the invoker stay stable while operations grow?
- Can behavior be queued or retried?

## Key Insight

Command Pattern is not about undo alone.

It is about **behavior encapsulation**.
