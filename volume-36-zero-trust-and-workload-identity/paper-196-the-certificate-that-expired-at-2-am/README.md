# Paper 196: The Certificate That Expired at 2 A.M.

Part of [Architect Notes](../../README.md) — Volume 36: Zero Trust and Workload Identity.

## The Question

A gateway presents a workload certificate to the payments service, and that certificate has an end date.
When the date passes, every call is refused at once: in the simulation, 200 refund calls are rejected over
five ticks, and the first page arrives only once calls are already failing, because renewal lived in a
calendar reminder owned by someone who had changed teams. The tempting repair at 2 a.m. is to switch the
expiry check off, which restores traffic and also admits expired certificates from anyone, a leaked copy
included. A longer lifetime only postpones the same failure and makes every leak worth more. How do you
keep an end date from becoming a scheduled outage without weakening the check that gives the certificate
its meaning?

## Structure

- `pressure/` — The gateway holds a 90-day certificate (2160 ticks at one tick per hour) that a person has
  to renew. A calendar reminder fires at t=2100 for an owner who changed teams and is missed, the
  certificate expires at t=2160 with 40 calls per tick arriving, and the on-call engineer needs until
  t=2165 to find an approver, get a new certificate and restart the gateway. Verified: every call before
  expiry is accepted (86400 of 86400) and the expired certificate is rejected, but four checks fail:
  `renewal-before-expiry` (expired at t=2160, replacement installed at t=2165),
  `expiry-alert-fires-before-outage` (first warning at t=2160, 0 ticks before expiry),
  `legit-renewal-no-downtime` (200 refund calls rejected over 5 ticks) and `staging-rehearses-expiry`
  (staging lifetime 8640 vs production 2160: 0 expiries in 2160 ticks).
- `wrong-direction/` — The first fix has three parts: a 365-day lifetime (8760 ticks), a calendar
  reminder, and, when the reminder is missed, the expiry check switched off at t=8762 to stop the page.
  A leaked copy of the gateway key and certificate is used at t=3000 with 5760 ticks of validity left. The
  reminder at t=8700 is missed, the certificate expires at t=8760, and once the check is off the expired
  certificate is accepted again. Verified: every call before expiry is accepted (350400 of 350400), but
  five checks fail: `renewal-before-expiry` (expired at t=8760, never replaced),
  `expired-cert-still-rejected` (the leaked copy is ACCEPTED at t=8770, 10 ticks after expiry),
  `leaked-key-window-bounded` (5760 ticks against a budget of 24), `expiry-alert-fires-before-outage`
  (first warning at t=8760, 0 ticks before expiry) and `legit-renewal-no-downtime` (80 refund calls
  rejected over 2 ticks).
- `principle-applied/` — Certificates live 24 ticks and renew with 8 ticks left, an inventory scan covers
  every workload, the expiry check stays on, and a second issuer key joins Meridian's trust bundle at t=4
  and starts signing at t=28, so both keys are accepted for an 8-tick overlap before issuer-key-1 is
  retired at t=36. Verified: all 8 checks pass. The Ops Agent's renewal is refused at t=16 because its
  enrollment credential lapsed, the scan alerts 6 ticks before its certificate expires against a 2-tick
  fix, and it renews at t=20 with 4 ticks left; 0 of 1968 calls are rejected over 48 ticks, the leaked
  copy is answered EXPIRED at t=24 (valid 14 ticks, budget 24), a replaced issuer-key-1 certificate is
  REJECTED after the retirement, and staging runs at the production lifetime, so 2 expiries happen in 48
  ticks.

## Interactive Explorer

Download [`explorer/explorer.html`](explorer/explorer.html) and open it in a browser. It replays the real
output of the three programs as a timeline you can step through event by event, shows each check turning
pass or fail, and lines the three designs up side by side. The file is self-contained and works offline.
GitHub displays HTML files as source, so download it rather than opening it on the repository page.

## Running It

Each folder is self-contained. Run these two commands inside `pressure/`, `wrong-direction/` or
`principle-applied/`; every run prints the same output, because the programs read no clock, no random
source and no network:

```bash
javac -Xlint:all *.java
java Main
```

## Read the Full Article on Medium

_Article coming soon on Medium._
