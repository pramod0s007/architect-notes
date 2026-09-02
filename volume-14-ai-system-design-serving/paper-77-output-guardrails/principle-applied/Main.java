import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<RefundRequest> requests = Arrays.asList(
            new RefundRequest("ORD-1", 25.00, "damaged item", 25.00),
            new RefundRequest("ORD-2", -50.00, "wrong item shipped", 50.00),
            new RefundRequest("ORD-3", 15.00, "late delivery", 15.00)
        );

        System.out.println("Processing model-generated refund requests, validated before use:");
        double netCustomerImpact = 0;
        int processed = 0;
        int rejected = 0;
        for (RefundRequest r : requests) {
            if (RefundValidator.isValid(r)) {
                netCustomerImpact += r.amount;
                processed++;
                System.out.println("  " + r.orderId + ": VALID -- applied refund of $" + r.amount);
            } else {
                rejected++;
                System.out.println("  " + r.orderId + ": REJECTED -- amount $" + r.amount
                    + " fails validation, flagged for manual review, NOT processed.");
            }
        }

        System.out.println();
        System.out.println("Net amount refunded to customers: $" + netCustomerImpact
            + " (" + processed + " processed, " + rejected + " rejected)");
        System.out.println((rejected == 1 && netCustomerImpact == 40.00)
            ? "FIXED: ORD-2's malformed negative amount was caught and rejected before it ever moved "
              + "money. The two genuinely valid requests were processed exactly as before."
            : "Unexpected -- investigate.");
    }
}
