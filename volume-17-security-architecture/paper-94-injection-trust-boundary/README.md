# Injection and the Trust Boundary — When User Input Gets Parsed Instead of Compared

**Continues Volume 17** — picking up right after Paper 93 (Cryptographic Misuse).

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

Concatenating user input into a string that gets parsed as syntax lets an injected condition override the developer's intended logic entirely (measured: a lookup for one specific order returned every order in the system via a single injected OR condition). Comparing user input as a value instead of parsing it as syntax eliminates the vulnerability class entirely (measured: the identical malicious input returned zero records once compared as data). This paper then shows the overcorrection: denylisting specific dangerous substrings, which blocks the exact payload it was written for and nothing else (measured: changing one word's letter case defeated a working, tested filter completely).

## The Measurements

- **Concatenated, unfiltered (pressure)**: an injected `OR '1'='1'` condition makes a single-order lookup return every order in the system.
- **Compared as data (principle-applied)**: the identical malicious input returns zero records, because it's compared as a literal value with no parser to exploit.
- **Concatenated, denylist-filtered (wrong-direction)**: the exact original payload is blocked; a mixed-case variant (`oR` instead of `OR`) bypasses the filter and returns every order again.

## The Finding

**The real fix for injection is never "detect and block dangerous input" — it's "eliminate the interpretation step so that no input, dangerous-looking or not, can ever be parsed as anything other than a value."** A denylist treats a symptom of string concatenation into a parser; only removing that parsing step for user-controlled input closes the actual vulnerability class.

## Code Examples in This Repo

| Example | What It Shows |
|---------|--------------|
| [`pressure/`](./pressure/) | Concatenated, unfiltered — measured an injected OR condition exposing every record |
| [`principle-applied/`](./principle-applied/) | Compared as data — measured the identical payload returning zero records |
| [`wrong-direction/`](./wrong-direction/) | Denylist-filtered — measured a one-letter case change bypassing the filter completely |

### How to Run

```bash
cd volume-17-security-architecture/paper-94-injection-trust-boundary/pressure
javac *.java && java Main

cd volume-17-security-architecture/paper-94-injection-trust-boundary/principle-applied
javac *.java && java Main

cd volume-17-security-architecture/paper-94-injection-trust-boundary/wrong-direction
javac *.java && java Main
```
