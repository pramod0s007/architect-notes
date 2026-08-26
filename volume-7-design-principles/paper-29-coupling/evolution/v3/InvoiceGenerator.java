// Fixed: caller states what it wants by name, not by an opaque flag.
class InvoiceGenerator {
    String generate(Order order, TaxRoundingPolicy policy) {
        double total = policy.apply(order.getTotal());
        return "Invoice[" + order.getId() + "] total=" + total;
    }
}
