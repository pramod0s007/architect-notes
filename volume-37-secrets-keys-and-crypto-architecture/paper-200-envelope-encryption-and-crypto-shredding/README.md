# Paper 200: Envelope Encryption and Crypto-Shredding

Part of [Architect Notes](../../README.md) — Volume 37: Secrets, Keys and Crypto Architecture.

## The Question

A commerce platform keeps every merchant's records in a primary store, a search index, an analytics export and
backups. One merchant leaves, and its contract says all of its data must go. The export is append-only and the
backups are immutable, so a delete reaches half the copies. Disk encryption is switched on, yet one key opens
every merchant's rows for anything running on the host, and a stolen backup plus that key opens all of them.
Rotating the key would mean rewriting data that two copies refuse to accept. How do you arrange keys so that
deleting one tenant is a single operation, a leak stays inside one tenant, and a rotation leaves the data alone?

## Structure

- `pressure/` — One data key, `dk-all-v1`, seals 24 entries (3 tenants, 2 records each, 4 copies). A merchant's
  delete reaches the primary store and the index but not the append-only export or the immutable backup, a key
  from a crash dump plus a backup copy opens every tenant, and the rotation can re-encrypt only the two copies
  that accept changes. Verified: the legitimate read of m4417's payout works, but
  `destroyed-tenant-key-makes-all-copies-unreadable` fails (4 of 8 m3012 entries still open),
  `leak-exposes-one-tenant-at-most` fails (3 tenants exposed by one key) and
  `kek-rotation-rewraps-without-reencrypting-data` fails (8 entries re-encrypted, 12 still open under the old
  key).
- `wrong-direction/` — Volume encryption: each of the four copies is one sealed image holding every tenant's
  rows under a single master key, `vol-master-v1`, which the key service releases to the gateway host at boot.
  Verified: the payout read works and an intruder holding a copy of the image and a key of their own is denied,
  but `destroyed-tenant-key-makes-all-copies-unreadable` fails (4 of 8 m3012 rows still open),
  `ciphertext-bound-to-tenant` fails (session m4417 received 1 row of another tenant) and
  `leak-exposes-one-tenant-at-most` fails (3 tenants exposed by one key).
- `principle-applied/` — Each tenant gets its own data key, kept only in wrapped form in a key service's own
  tables, with every ciphertext bound to its tenant, an authorized and audited unwrap, a rotation that
  re-wraps and a shred that destroys. Verified: all 6 checks pass. Destroying `dk-m3012-v1` leaves 0 of 8 m3012
  entries open, a backup copy plus one worker's key opens 2 of 6 entries and 1 tenant, the rotation re-wraps 4
  keys and rewrites 0 data entries, 4 of 4 live rows still read, and 13 of 13 unwrap attempts are audited, 2 of
  them denied.

## Interactive Explorer

Download [`explorer/explorer.html`](explorer/explorer.html) and open it in a browser. It replays the real
output of the three programs as a timeline you can step through event by event, shows each check turning pass
or fail, and sets the three designs side by side. It is a single self-contained file that works offline.
GitHub displays HTML files as source, so download the file rather than opening it on the repository page.

## Running It

Each scenario is self-contained; run these commands inside `pressure/`, `wrong-direction/` or
`principle-applied/`:

```bash
javac -Xlint:all *.java
java Main
```

## Read the Full Article on Medium

_Article coming soon on Medium._
