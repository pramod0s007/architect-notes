import java.util.ArrayList;
import java.util.List;

// Single team owns pricing right now. A public mutable field looks harmless.
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
