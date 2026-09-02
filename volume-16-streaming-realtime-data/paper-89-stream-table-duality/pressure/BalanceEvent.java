// One entry in the authoritative stream (the log). offset is the event's true
// position in that log -- the only thing that defines "order" for this
// stream. newBalanceCents is an absolute snapshot (SET semantics, not a
// delta), so which event was applied LAST for a given key genuinely changes
// the result -- order isn't just cosmetic here.
class BalanceEvent {
    final String accountId;
    final int offset;
    final int newBalanceCents;

    BalanceEvent(String accountId, int offset, int newBalanceCents) {
        this.accountId = accountId;
        this.offset = offset;
        this.newBalanceCents = newBalanceCents;
    }
}
