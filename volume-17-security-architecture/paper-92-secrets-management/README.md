# Secrets Management — A Credential's Lifecycle Is an Architecture Decision, Not a Config Value

**Continues Volume 17** — picking up right after Paper 91 (Broken Object-Level Authorization).

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

A secret hardcoded into source code has no separate identity per environment, no matter how many environments the code runs in (measured: a key extracted from source successfully authorized a real production charge). Externalizing secrets into a store with one distinct value per environment confines a leak's blast radius to exactly the environment it came from (measured: a leaked development secret failed to authorize a production charge). This paper then shows the overcorrection: rotating a secret as an atomic instant replacement, with no overlap window, which fails every legitimate in-flight caller still holding the previous value (measured: a legitimate, correctly-obtained secret failed a real charge attempt the moment rotation occurred).

## The Measurements

- **Hardcoded, shared (pressure)**: a key read directly out of source code authorizes a real production charge, because source and production share the identical literal.
- **Externalized, scoped (principle-applied)**: a leaked dev secret fails to authorize a production charge, because dev and prod were never the same value.
- **Externalized, instant rotation (wrong-direction)**: a legitimate batch job holding a secret fetched before rotation fails a real charge the instant rotation occurs, indistinguishable from an attacker using a stolen credential.

## The Finding

**A complete secrets architecture requires three properties together: no literal secret in code, a distinct value per real scope, and rotation with a grace period.** Missing any one produces a different, equally real failure — externalizing alone doesn't guarantee scoping, and scoping alone doesn't guarantee safe rotation.

## Code Examples in This Repo

| Example | What It Shows |
|---------|--------------|
| [`pressure/`](./pressure/) | Hardcoded secret — measured a source-leaked key authorizing a real production charge |
| [`principle-applied/`](./principle-applied/) | Environment-scoped external store — measured a leaked dev secret failing against prod |
| [`wrong-direction/`](./wrong-direction/) | Instant rotation, no grace period — measured a legitimate in-flight caller failing at rotation |

### How to Run

```bash
cd volume-17-security-architecture/paper-92-secrets-management/pressure
javac *.java && java Main

cd volume-17-security-architecture/paper-92-secrets-management/principle-applied
javac *.java && java Main

cd volume-17-security-architecture/paper-92-secrets-management/wrong-direction
javac *.java && java Main
```
