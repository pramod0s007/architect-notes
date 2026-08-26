import java.util.ArrayList;
import java.util.List;

// Still a public mutable field. Eight months in, a second team exists and does not know this is here.
class PricingEngine {
    public List<LineItem> pendingAdjustments = new ArrayList<>();

    public double computeTotal(Order order) {
        double total = order.getSubtotal();
        for (LineItem item : pendingAdjustments) {
            total -= item.getAdjustment();
        }
        return total;
    }
}
