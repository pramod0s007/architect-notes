# Cryptographic Misuse — A Fast Hash Is a Feature Everywhere Except Here

**Continues Volume 17** — picking up right after Paper 92 (Secrets Management).

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

A fast, general-purpose hash with no salt lets identical passwords produce identical, directly comparable stored hashes (measured: two accounts with the same password produced byte-for-byte identical SHA-256 hashes). A unique salt per account combined with deliberate iteration eliminates that comparability and raises the real cost of every guess (measured: identical passwords produced completely different hashes, each costing a real, measured ~60-65ms to compute). This paper then shows the overcorrection: a salt that's present but shared as a single constant across every account, which fails the exact property salting exists to guarantee (measured: identical passwords produced identical hashes again, despite salting and iteration both being present).

## The Measurements

- **No salt (pressure)**: two different accounts with the identical password produce byte-for-byte identical SHA-256 hashes — comparable with zero cracking required.
- **Unique salt + iteration (principle-applied)**: identical passwords produce completely different hashes; each hash computation measured ~60-65ms across runs, a real per-guess cost.
- **Shared global salt + iteration (wrong-direction)**: identical passwords produce identical hashes again — the salt is present but contributes the same bytes to every account, so it changes nothing about comparability.

## The Finding

**"Uses a salt" and "salt is unique per record" are different claims, and only the second one is the actual security property.** A cryptographic primitive being present in a design is a weaker claim than that primitive being configured for the specific property the design needs — and the gap between the two is invisible to a review that only checks whether the word "salt" appears in the code.

## Code Examples in This Repo

| Example | What It Shows |
|---------|--------------|
| [`pressure/`](./pressure/) | No salt — measured two identical passwords producing identical, comparable hashes |
| [`principle-applied/`](./principle-applied/) | Unique salt + 100,000 iterations — measured different hashes and a real ~60-65ms per-guess cost |
| [`wrong-direction/`](./wrong-direction/) | Shared global salt + iteration — measured the comparability failure returning despite salting |

### How to Run

```bash
cd volume-17-security-architecture/paper-93-cryptographic-misuse/pressure
javac *.java && java Main

cd volume-17-security-architecture/paper-93-cryptographic-misuse/principle-applied
javac *.java && java Main

cd volume-17-security-architecture/paper-93-cryptographic-misuse/wrong-direction
javac *.java && java Main
```
