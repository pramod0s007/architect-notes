import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<RefundRequest> requests = Arrays.asList(
            new RefundRequest("ORD-1", 25.00, "damaged item", 25.00),
            // The model produced a negative amount for this one -- a malformed
            // or hallucinated value that should never reach a real refund call.
            new RefundRequest("ORD-2", -50.00, "wrong item shipped", 50.00),
            new RefundRequest("ORD-3", 15.00, "late delivery", 15.00)
        );

        System.out.println("Processing model-generated refund requests with no validation:");
        double netCustomerImpact = 0;
        for (RefundRequest r : requests) {
            netCustomerImpact += r.amount;
            System.out.println("  " + r.orderId + ": applied refund of $" + r.amount + " (\"" + r.reason + "\")");
        }

        System.out.println();
        System.out.println("Net amount refunded to customers: $" + netCustomerImpact);
        System.out.println(netCustomerImpact < (25.00 + 15.00)
            ? "CONFIRMED FAILURE: ORD-2's negative amount was applied directly -- a \"refund\" of -$50 "
              + "is a $50 CHARGE to the customer, not a refund. The model's malformed output was used "
              + "exactly as produced, with nothing checking whether a refund amount actually makes sense "
              + "before it moved real money."
            : "Unexpected -- investigate.");
    }
}
