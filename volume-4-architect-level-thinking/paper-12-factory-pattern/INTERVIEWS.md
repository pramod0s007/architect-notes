# Interview Discussion

## Question

What is the difference between Factory and Strategy?

## Weak Answer

They are the same — both use interfaces.

## Strong Answer

Factory decides **which object to create**. Strategy decides **which behavior to run** on an already-constructed object.

## Questions To Ask

- Is variation at construction time or runtime behavior?
- Does the caller need to know concrete types?
- Is a registry or DI container a better fit than a custom factory?

## Key Insight

Factory answers **who to instantiate**; Strategy answers **what algorithm to execute**.
