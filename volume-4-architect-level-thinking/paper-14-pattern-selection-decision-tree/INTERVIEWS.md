# Interview Discussion

## Question

How do you decide which design pattern to use?

## Weak Answer

I pick the pattern that fits the UML diagram.

## Strong Answer

I identify what is changing (data, object graph, behavior, rules), estimate growth rate, choose the smallest abstraction on the decision tree, and defer patterns until pressure is visible in code review metrics.

## Questions To Ask

- What breaks if we add one more variant?
- Can we delete this abstraction in a week if wrong?

## Key Insight

The decision tree is about **pressure classification**, not pattern memorization.
