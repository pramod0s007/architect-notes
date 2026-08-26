import java.util.ArrayList;
import java.util.List;

// Fixed: internals are private. Callers get a defensive copy, never the live list.
class PricingEngine {
    private final List<LineItem> pendingAdjustments = new ArrayList<>();

    void addAdjustment(LineItem item) {
        pendingAdjustments.add(item);
    }

    List<LineItem> viewAdjustments() {
        return List.copyOf(pendingAdjustments);
    }

    double computeTotal(Order order) {
        double total = order.getSubtotal();
        for (LineItem item : pendingAdjustments) {
            total -= item.getAdjustment();
        }
        return total;
    }
}
