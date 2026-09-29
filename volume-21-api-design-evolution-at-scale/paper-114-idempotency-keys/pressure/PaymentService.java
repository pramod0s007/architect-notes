import java.util.ArrayList;
import java.util.List;

/**
 * A minimal payment charge endpoint with no concept of a retry at all. Every
 * call to charge() unconditionally appends a new ledger entry -- there is
 * nothing here to tell two calls with the same intent apart from two calls
 * that genuinely mean to charge twice.
 */
public class PaymentService {
    private final List<String> ledger = new ArrayList<>();

    public void charge(String orderId, double amount) {
        ledger.add(orderId + ":" + String.format("%.2f", amount));
    }

    public List<String> ledger() {
        return ledger;
    }
}
