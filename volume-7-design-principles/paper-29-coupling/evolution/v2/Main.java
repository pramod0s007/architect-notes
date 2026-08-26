public class Main {
    public static void main(String[] args) {
        // --- Content coupling incident ---
        PricingEngine engine = new PricingEngine();
        Order order = new Order("ORD-2", 100.006);
        engine.pendingAdjustments.add(new LineItem("loyalty", 5.0));
        engine.pendingAdjustments.add(new LineItem("promo", 10.0));

        FulfillmentService fulfillment = new FulfillmentService();
        fulfillment.reserveInventory(order, engine); // clears adjustments mid-checkout

        System.out.println("Total AFTER fulfillment ran (should include $15 off, does not): "
                + engine.computeTotal(order));

        // --- Control coupling incident ---
        InvoiceGenerator invoiceGenerator = new InvoiceGenerator();
        System.out.println("Billing call:  " + invoiceGenerator.generate(order, false));
        System.out.println("Refunds call:  " + invoiceGenerator.generate(order, true));

        boolean isWholesaleOrder = true; // sales team's guess — wrong, wholesale still needs rounding
        System.out.println("Sales call:    " + invoiceGenerator.generate(order, isWholesaleOrder));
    }
}
