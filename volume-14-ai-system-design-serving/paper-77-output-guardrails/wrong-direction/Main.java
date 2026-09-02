import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<RefundRequest> requests = Arrays.asList(
            new RefundRequest("ORD-1", 25.00, "damaged item", 25.00),
            new RefundRequest("ORD-2", -50.00, "wrong item shipped", 50.00),
            new RefundRequest("ORD-3", 15.00, "late delivery", 15.00)
        );

        System.out.println("Processing model-generated refund requests, auto-repairing invalid amounts:");
        double netCustomerImpact = 0;
        int autoRepaired = 0;
        for (RefundRequest r : requests) {
            double amount = AutoRepairValidator.repairedAmount(r);
            boolean wasRepaired = amount != r.amount;
            if (wasRepaired) autoRepaired++;
            netCustomerImpact += amount;
            System.out.println("  " + r.orderId + ": applied refund of $" + amount
                + (wasRepaired ? " (auto-repaired from $" + r.amount + ", no flag raised)" : ""));
        }

        System.out.println();
        System.out.println("Net amount refunded to customers: $" + netCustomerImpact
            + " (" + autoRepaired + " request auto-repaired, 0 flagged for review)");
        System.out.println(autoRepaired > 0
            ? "STILL FAILS, INVISIBLY: ORD-2's negative amount got sign-flipped and happened to land "
              + "within its order's refundable range, so a $50 refund was processed anyway. Nobody was "
              + "ever told this request was corrupted -- there's no flag, no review, no signal that the "
              + "model produced a nonsensical value in the first place. The request that most needed a "
              + "human to look at it is the one request in this run that generated zero visibility."
            : "Unexpected -- investigate.");
    }
}
