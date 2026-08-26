// Same flag as v1. Now three teams call it, each guessing what the flag means.
class InvoiceGenerator {
    String generate(Order order, boolean skipTaxRounding) {
        double total = order.getTotal();
        if (!skipTaxRounding) {
            total = Math.round(total * 100) / 100.0;
        }
        return "Invoice[" + order.getId() + "] total=" + total;
    }
}
