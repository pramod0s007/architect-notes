import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The team's fix after the double-charge incident: the client now sends an
 * idempotency key with every request, and the server checks whether it has
 * seen that key before processing. This is the right instinct, but "check,
 * then act" is two separate steps, not one atomic operation -- if two
 * requests carrying the same key are both in flight before either one has
 * recorded the key as seen, both checks can see "not seen yet," and both
 * proceed to charge.
 */
public class PaymentService {
    private final Set<String> seenKeys = new HashSet<>();
    private final List<String> ledger = new ArrayList<>();

    public boolean hasBeenSeen(String idempotencyKey) {
        return seenKeys.contains(idempotencyKey);
    }

    public void markSeen(String idempotencyKey) {
        seenKeys.add(idempotencyKey);
    }

    public void charge(String orderId, double amount) {
        ledger.add(orderId + ":" + String.format("%.2f", amount));
    }

    public List<String> ledger() {
        return ledger;
    }
}
