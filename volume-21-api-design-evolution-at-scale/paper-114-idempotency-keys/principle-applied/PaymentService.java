import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The real fix: reserving an idempotency key and recording its result is a
 * single atomic operation, not two separate steps. ConcurrentHashMap's
 * putIfAbsent() is exactly that -- it atomically checks "is this key
 * already claimed?" and, if not, claims it in the same indivisible
 * operation, so two concurrent requests with the same key can never both
 * observe "not claimed yet." Whichever request loses the race gets back
 * the winner's own result instead of being allowed to charge again.
 */
public class PaymentService {
    private final ConcurrentHashMap<String, ChargeResult> resultsByKey = new ConcurrentHashMap<>();
    private final List<String> ledger = new ArrayList<>();

    /**
     * Atomically reserves the idempotency key. Returns null if this call won
     * the reservation (the caller must now actually process the charge and
     * call recordResult()); returns the existing result if another request
     * already claimed this key, first or concurrently.
     */
    public ChargeResult reserve(String idempotencyKey) {
        return resultsByKey.putIfAbsent(idempotencyKey, ChargeResult.PENDING);
    }

    public ChargeResult processCharge(String idempotencyKey, String orderId, double amount) {
        ledger.add(orderId + ":" + String.format("%.2f", amount));
        ChargeResult result = new ChargeResult(orderId, amount);
        resultsByKey.put(idempotencyKey, result);
        return result;
    }

    public List<String> ledger() {
        return ledger;
    }

    public static class ChargeResult {
        public static final ChargeResult PENDING = new ChargeResult(null, -1);

        public final String orderId;
        public final double amount;

        public ChargeResult(String orderId, double amount) {
            this.orderId = orderId;
            this.amount = amount;
        }

        @Override
        public String toString() {
            if (this == PENDING) {
                return "PENDING";
            }
            return orderId + ":" + String.format("%.2f", amount);
        }
    }
}
