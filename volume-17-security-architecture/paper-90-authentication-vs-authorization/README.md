# Authentication vs Authorization — Being Logged In Is Not Being Allowed

**Opens Volume 17** — Security Architecture for Staff Engineers, following on from Volume 16 (Streaming & Real-Time Data Architecture).

---

## Read the Full Article on Medium

_Article coming soon on Medium._

---

## What This Paper Is About

A system can authenticate correctly and still have zero access control, if nothing ever checks whether the authenticated identity is authorized for the specific resource requested (measured: a genuinely valid session retrieved another user's private data by changing one ID). An authorization check that compares a resource against the session's own server-established identity closes that gap completely (measured: the identical request pattern was correctly denied once the comparison used the session's real identity). This paper then shows the overcorrection: an authorization check that compares a resource against a value the request itself supplies, which provides zero real protection even though the check logic looks correct (measured: an authorization branch ran, matched successfully, and still returned another user's private data).

## The Measurements

- **Authentication only (pressure)**: a genuinely authenticated user retrieves another user's order by changing one ID — no authorization check exists at all.
- **Authorization against server-established identity (principle-applied)**: the identical request is correctly denied, because the check compares against `session.authenticatedUserId`, a value the request cannot influence.
- **Authorization against client-claimed identity (wrong-direction)**: an authorization check runs and matches successfully — and still returns another user's data, because it compares against a value the request itself supplies.

## The Finding

**Whether an authorization check is real depends on which side of the comparison the attacker controls, not whether a check exists.** A check comparing a resource against a server-established identity is a real security boundary. A check comparing a resource against a client-supplied claim about identity looks identical in code review and protects nothing.

## Code Examples in This Repo

| Example | What It Shows |
|---------|--------------|
| [`pressure/`](./pressure/) | Authentication only — measured any authenticated user reading any other user's data |
| [`principle-applied/`](./principle-applied/) | Authorization against session identity — measured correct denial of cross-user access |
| [`wrong-direction/`](./wrong-direction/) | Authorization against client-claimed identity — measured a check that ran, matched, and protected nothing |

### How to Run

```bash
cd volume-17-security-architecture/paper-90-authentication-vs-authorization/pressure
javac *.java && java Main

cd volume-17-security-architecture/paper-90-authentication-vs-authorization/principle-applied
javac *.java && java Main

cd volume-17-security-architecture/paper-90-authentication-vs-authorization/wrong-direction
javac *.java && java Main
```
