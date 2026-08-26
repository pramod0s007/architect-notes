// Single team owns billing. A boolean flag looks like the simplest possible option.
class InvoiceGenerator {
    String generate(Order order, boolean skipTaxRounding) {
        double total = order.getTotal();
        if (!skipTaxRounding) {
            total = Math.round(total * 100) / 100.0;
        }
        return "Invoice[" + order.getId() + "] total=" + total;
    }
}
