# Blameless Postmortems — Beyond the Single Root Cause

**Continues Volume 12** — picking up right after Paper 63 (Alerting: Symptom-Based vs. Cause-Based).

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

A postmortem that forces naming exactly one root cause fixes exactly one contributing factor, even when an incident genuinely had several (measured: 1 of 3 real factors remediated, incident recurs when an unfixed factor — the alert threshold — fails again). A contributing-factors timeline that tracks and remediates every critical factor prevents that recurrence (measured: both critical factors fixed, no recurrence). This paper then shows the overcorrection: documenting every conceivable factor without prioritizing which are actually critical, which produces more total documentation but still misses the fix that mattered, because it has to compete on equal footing with trivial factors for finite remediation capacity.

## The Measurements

- **Single root cause**: 1 factor named and fixed; 2 real contributing factors never addressed; incident recurs.
- **Contributing-factors timeline, prioritized**: both critical factors fixed; non-critical factor correctly deprioritized; incident does not recur.
- **Unprioritized timeline (wrong direction)**: 8 factors documented (more than either other approach), only 3 remediated by list order, critical factor at position 7 never reached; incident recurs anyway.

## The Finding

**Thorough documentation and effective prioritization are different properties of a postmortem process.** A process can excel at capturing every contributing factor and still fail to prevent a recurrence, if nothing distinguishes which of those factors are actually critical against real, finite remediation capacity.

## Code Examples in This Repo

| Example | What It Shows |
|---------|--------------|
| [`pressure/`](./pressure/) | Single-root-cause postmortem — measured 1 of 3 real factors fixed, incident recurs |
| [`principle-applied/`](./principle-applied/) | Contributing-factors timeline, prioritized by criticality — measured both critical factors fixed, no recurrence |
| [`wrong-direction/`](./wrong-direction/) | Unprioritized contributing-factors list — measured more total documentation but the critical fix still missed due to capacity limits |

### How to Run

```bash
cd volume-12-production-engineering-observability/paper-64-blameless-postmortems/pressure
javac *.java && java Main

cd volume-12-production-engineering-observability/paper-64-blameless-postmortems/principle-applied
javac *.java && java Main

cd volume-12-production-engineering-observability/paper-64-blameless-postmortems/wrong-direction
javac *.java && java Main
```
