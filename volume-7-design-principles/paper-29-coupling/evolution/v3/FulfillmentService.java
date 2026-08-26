// Fixed: fulfillment can inspect adjustments through a read-only view, but cannot mutate
// PricingEngine's internal state. The content-coupling incident is no longer reachable.
class FulfillmentService {
    void reserveInventory(Order order, PricingEngine engine) {
        int adjustmentCount = engine.viewAdjustments().size();
        System.out.println("Reserved inventory for " + order.getId()
                + " (pricing has " + adjustmentCount + " adjustments pending, untouched)");
    }
}
