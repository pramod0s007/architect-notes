# Defense in Depth — Counting Layers Is Not the Same as Verifying They're Independent

**Closes Volume 17** — Security Architecture for Staff Engineers, following on from Paper 94 (Injection and the Trust Boundary).

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

A single security control, however carefully implemented, is the system's entire defense the moment it has any bug at all (measured: one flawed, case-insensitive authorization check let an attacker read another user's confidential data with nothing else in place to catch it). A second, genuinely independently-implemented layer can catch exactly the mistake the first layer's real bug lets through, without requiring either layer to be bug-free (measured: the first check's real bug fired unchanged, and a second, case-sensitive check still denied access). This paper then shows the overcorrection: multiple checks that share the same underlying logic, which provide no more real protection than a single check regardless of how many named layers they occupy (measured: three identically-implemented checks all made the identical wrong decision on the identical input).

## The Measurements

- **Single check (pressure)**: one case-insensitive authorization check lets an attacker with a different, independently-registered account read another user's data.
- **Two independent checks (principle-applied)**: the same buggy check still approves the attacker, but a second, case-sensitive check with different logic correctly denies access.
- **Three identical checks (wrong-direction)**: gateway, service-layer, and data-access checks all run the same case-insensitive logic and all approve the attacker identically.

## The Finding

**The question that determines whether a defense-in-depth claim is real is not "how many checks exist" but "would a plausible bug in one check actually be caught by the others."** Checks that share the same implementation share the same blind spot, and counting them as separate layers is counting appearances, not verifying protection.

## Code Examples in This Repo

| Example | What It Shows |
|---------|--------------|
| [`pressure/`](./pressure/) | Single check — measured a real bug producing total compromise |
| [`principle-applied/`](./principle-applied/) | Two independently-implemented checks — measured the second layer catching the first's real bug |
| [`wrong-direction/`](./wrong-direction/) | Three identically-implemented checks — measured all three failing identically |

### How to Run

```bash
cd volume-17-security-architecture/paper-95-defense-in-depth/pressure
javac *.java && java Main

cd volume-17-security-architecture/paper-95-defense-in-depth/principle-applied
javac *.java && java Main

cd volume-17-security-architecture/paper-95-defense-in-depth/wrong-direction
javac *.java && java Main
```
