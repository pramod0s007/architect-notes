// A different team, a different quarter. They see a public list on an engine they don't own
// and assume clearing it before reservation is safe local cleanup.
class FulfillmentService {
    void reserveInventory(Order order, PricingEngine engine) {
        engine.pendingAdjustments.clear(); // content coupling: reaching into PricingEngine's internals
        System.out.println("Reserved inventory for " + order.getId());
    }
}
