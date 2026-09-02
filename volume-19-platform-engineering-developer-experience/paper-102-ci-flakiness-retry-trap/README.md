# CI Flakiness and the Retry Trap — A Retry Can't Tell a Flake From a Bug

**Continues Volume 19** — picking up right after Paper 101 (The Paved Road Paradox).

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

Retrying any failing test until an attempt succeeds cannot distinguish a genuinely flaky test harness from a real, intermittent bug in the code under test (measured: a real race-condition bug passed a build after two failed attempts, identically to a genuine environmental flake). Reserving retries for a documented, reviewed allowlist of specific known-flaky tests protects legitimate flakes while giving every other test exactly one chance to prove itself (measured: the genuine flake still passed, while the real bug — never tagged — failed on its only attempt and blocked the build). This paper then shows the overcorrection: a shared retry budget across the whole build, which bounds total retries but says nothing about which specific tests are trusted to use them (measured: the real bug consumed budget from the same pool as the genuine flake and passed anyway).

## The Measurements

- **Retry until pass, no reasoning (pressure)**: a real race-condition bug (TestB) passes on its 3rd attempt, identically treated to a genuine flake (TestA) that passes on its 2nd — the build reports PASS with a real bug baked in.
- **Documented allowlist (principle-applied)**: TestA (tagged known-flaky) still passes after its allowed retry; TestB (never tagged) gets exactly one attempt, fails, and blocks the build.
- **Shared retry budget (wrong-direction)**: TestA uses 1 of 3 shared retries and passes; TestB draws the remaining 2 from the same pool and also passes on its 3rd attempt — the real bug slips through again.

## The Finding

**Bounding the total number of retries and bounding who is allowed to use them are two different controls.** A policy needs both — having only the first one produces the identical masking failure as having no policy at all.

## Code Examples in This Repo

| Example | What It Shows |
|---------|--------------|
| [`pressure/`](./pressure/) | Retry until pass — measured a real bug masked identically to a genuine flake |
| [`principle-applied/`](./principle-applied/) | Documented allowlist — measured the flake protected and the real bug caught |
| [`wrong-direction/`](./wrong-direction/) | Shared retry budget — measured the real bug borrowing the flake's retries to slip through |

### How to Run

```bash
cd volume-19-platform-engineering-developer-experience/paper-102-ci-flakiness-retry-trap/pressure
javac *.java && java Main

cd volume-19-platform-engineering-developer-experience/paper-102-ci-flakiness-retry-trap/principle-applied
javac *.java && java Main

cd volume-19-platform-engineering-developer-experience/paper-102-ci-flakiness-retry-trap/wrong-direction
javac *.java && java Main
```
