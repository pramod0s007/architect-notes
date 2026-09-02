# Context Window Budgeting — What Survives a Hard Token Ceiling

**Continues Volume 14** — picking up right after Paper 74 (Model Cascading).

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

Naive tail truncation can delete the most important part of a prompt if it happens to be placed last (measured: the user's entire question — 22 of 22 words — was cut when a 215-word prompt was truncated to a 190-word budget). Reserving the essential components (system prompt and current query) first and only truncating negotiable content (oldest conversation history) fixes this completely (measured: both essentials fully intact, only the single oldest history turn dropped). This paper then shows the overcorrection: treating conversation history as untouchable and truncating the system prompt instead, which silently deletes safety/behavioral constraints (measured: the system prompt's safety constraint was completely removed).

## The Measurements

- **Tail truncation**: 215-word prompt truncated to 190 words — the user's entire question is cut, the model never sees it.
- **Reserve essentials, drop oldest history**: 187 of 190 words used — system prompt and user query both fully intact, only 1 of 5 history turns dropped.
- **Never truncate history (wrong direction)**: system prompt cut from 53 to 28 words — its safety constraint completely removed, while full history and full query are preserved.

## The Finding

**A context budget isn't one number to hit — it's a priority order over which components are negotiable, decided before any truncation happens.** Conversation history is usually the safest thing to truncate; system instructions are frequently the component with the least room for degradation, because they encode the behavioral and safety constraints the system is supposed to enforce.

## Code Examples in This Repo

| Example | What It Shows |
|---------|--------------|
| [`pressure/`](./pressure/) | Naive tail truncation — measured the user's entire question cut from the prompt |
| [`principle-applied/`](./principle-applied/) | Reserve system prompt + query, drop oldest history — measured both essentials fully intact |
| [`wrong-direction/`](./wrong-direction/) | Never truncate history, shrink system prompt instead — measured the safety constraint silently deleted |

### How to Run

```bash
cd volume-14-ai-system-design-serving/paper-75-context-window-budgeting/pressure
javac *.java && java Main

cd volume-14-ai-system-design-serving/paper-75-context-window-budgeting/principle-applied
javac *.java && java Main

cd volume-14-ai-system-design-serving/paper-75-context-window-budgeting/wrong-direction
javac *.java && java Main
```
