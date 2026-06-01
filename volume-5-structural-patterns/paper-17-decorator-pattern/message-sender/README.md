# Message Sender — Decorator Pattern

## What This Demonstrates

Decorator Pattern applied to message sending. `PlainMessageSender` transmits
a message as-is. Three decorator classes — `CompressedMessageSender`,
`EncryptedMessageSender`, and `LoggedMessageSender` — each wrap any
`MessageSender` and add one capability. They stack in any order at construction
time with no modifications to each other or to the base class.

**Pressure: Combinatorial inheritance explosion** — with 3 capabilities
(compression, encryption, logging), inheritance produces 2^3 = 8 subclasses:
`Plain`, `Compressed`, `Encrypted`, `Logged`, `CompressedEncrypted`,
`CompressedLogged`, `EncryptedLogged`, `CompressedEncryptedLogged`. A fourth
capability (signing) would require 16. Decorator produces 3 wrapper classes total,
regardless of how many combinations callers assemble.

## Class Diagram

```
<<interface>>
MessageSender
+ send(message: String, recipient: String): void
        △
        |
   ──────────────────────────────────────────────────
   |               |              |                  |
PlainMessage  Compressed     Encrypted         Logged
Sender        MessageSender  MessageSender     MessageSender
send()        - delegate:    - delegate:       - delegate:
→ transmit      MessageSender  MessageSender     MessageSender
              send()         send()            send()
              → compress     → encrypt         → log before
              → delegate       → delegate        → delegate
                .send()          .send()           .send()
                                                 → log after
```

## Decorator Chain

```
Logged → Encrypted → Compressed → Plain
  │           │           │          │
  │  log      │  encrypt  │ compress │ transmit
  │  start    │  payload  │ payload  │
  │           │           │          │
  └──────────>└──────────>└─────────>└──────> (network)
  │
  └── log end (full message length before transformation)
```

The outermost decorator (`LoggedMessageSender`) runs first — so logging captures
the original message length before compression or encryption have changed it. This
is the critical ordering insight: **outermost decorator runs first**.

## Decorator Stack Examples

```java
// Plain — no decoration
MessageSender plain = new PlainMessageSender();

// Full stack: Logged → Encrypted → Compressed → Plain
MessageSender full = new LoggedMessageSender(
    new EncryptedMessageSender(
        new CompressedMessageSender(
            new PlainMessageSender())));

// Logged only — internal messages need no encryption
MessageSender internal = new LoggedMessageSender(new PlainMessageSender());
```

## Design Decisions

- **Outermost decorator runs first — Logging wraps everything** — `LoggedMessageSender`
  is the outermost layer, so it logs the original message length before the payload
  is compressed or encrypted. If logging were innermost, it would see only the
  compressed/encrypted bytes — a much less useful log entry.
- **Each decorator holds exactly one `delegate: MessageSender`** — decorators are
  unaware of each other's existence. `EncryptedMessageSender` does not know whether
  it wraps a `CompressedMessageSender` or a `PlainMessageSender`. This is what
  makes arbitrary stacking possible.
- **No abstract decorator base class** — each decorator implements `MessageSender`
  directly and holds the delegate as a constructor argument. With only one interface
  method this is simpler than introducing an abstract intermediary.
- **Order changes meaning** — `Logged → Compressed → Encrypted` logs pre-encryption
  size; `Encrypted → Compressed → Logged` logs post-encryption size. Neither is wrong
  — the correct stack depends on what the log is for. This is an explicit design
  decision at the composition site, not hidden inside any decorator.

## How to Run

```bash
cd volume-5-structural-patterns/paper-17-decorator-pattern/message-sender
javac decorator/messagesender/*.java && java decorator.messagesender.Main
```

Expected output:

```
=== Plain sender ===
[SEND] To: alice@example.com | Message: Hello World

=== Logged + Encrypted + Compressed ===
[LOG]  Sending 36 chars to bob@example.com
[COMP] Compressed 36 → 28 chars
[ENC]  Encrypted: ENC(...)
[SEND] To: bob@example.com | Message: ENC(...)
[LOG]  Sent successfully

=== Logged only (no encryption for internal messages) ===
[LOG]  Sending 13 chars to monitor@internal.com
[SEND] To: monitor@internal.com | Message: Internal ping
[LOG]  Sent successfully
```

## When to Apply

- Multiple orthogonal capabilities (logging, compression, encryption, retry,
  metrics) need to be applied to the same operation in varying combinations.
- The set of combinations is not fixed at design time and may grow.
- You want each capability independently testable and independently removable.

## When NOT to Apply

- Only one capability is needed and no variation is expected — a plain wrapper
  method or a direct call is simpler.
- Capabilities are not truly orthogonal (e.g., encryption depends on compression
  order for correctness in a protocol-specific way) — explicit pipeline logic may
  be clearer than a flexible decorator stack.
