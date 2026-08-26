public class Main {
    public static void main(String[] args) {
        PricingEngine engine = new PricingEngine();
        Order order = new Order("ORD-1", 100.0);
        engine.pendingAdjustments.add(new LineItem("loyalty", 5.0));

        System.out.println("Total (v1, single team, no problem yet): " + engine.computeTotal(order));

        InvoiceGenerator invoiceGenerator = new InvoiceGenerator();
        System.out.println(invoiceGenerator.generate(order, false));
    }
}
