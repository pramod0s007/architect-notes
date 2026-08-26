public class Main {
    public static void main(String[] args) {
        PricingEngine engine = new PricingEngine();
        Order order = new Order("ORD-3", 100.006);
        engine.addAdjustment(new LineItem("loyalty", 5.0));
        engine.addAdjustment(new LineItem("promo", 10.0));

        FulfillmentService fulfillment = new FulfillmentService();
        fulfillment.reserveInventory(order, engine); // can no longer clear pricing's internal state

        System.out.println("Total AFTER fulfillment ran (still includes $15 off): "
                + engine.computeTotal(order));

        InvoiceGenerator invoiceGenerator = new InvoiceGenerator();
        System.out.println("Billing call:  " + invoiceGenerator.generate(order, new StandardRounding()));
        System.out.println("Refunds call:  " + invoiceGenerator.generate(order, new NoRounding()));
        System.out.println("Sales call:    " + invoiceGenerator.generate(order, new StandardRounding()));
    }
}
