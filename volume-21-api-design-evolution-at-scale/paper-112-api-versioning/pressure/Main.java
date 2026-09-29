/**
 * Demonstrates: an in-place semantic change to an existing field ("total"
 * now includes tax, same field name, same endpoint, no version bump)
 * silently breaks a client that still applies its own tax on top -- no
 * crash, no error, just a wrong price shown to the customer.
 */
public class Main {
    public static void main(String[] args) {
        double subtotal = 100.00;
        OldClient client = new OldClient();

        System.out.println("=== Before the change: original v1 contract ===");
        OrderServiceV1 originalServer = new OrderServiceV1();
        double originalServerTotal = originalServer.getTotal(subtotal);
        double originalDisplayed = client.renderPriceToCustomer(originalServerTotal);
        System.out.println("Server 'total' field: " + fmt(originalServerTotal) + " (subtotal only, excludes tax)");
        System.out.println("Client displays to customer: " + fmt(originalDisplayed) + " (client added its own 8% tax)");

        System.out.println();
        System.out.println("=== After the in-place change: same field, new meaning, no version bump ===");
        OrderServiceV1Modified modifiedServer = new OrderServiceV1Modified();
        double modifiedServerTotal = modifiedServer.getTotal(subtotal);
        double modifiedDisplayed = client.renderPriceToCustomer(modifiedServerTotal);
        System.out.println("Server 'total' field: " + fmt(modifiedServerTotal) + " (now already includes 8% tax)");
        System.out.println("Client displays to customer: " + fmt(modifiedDisplayed) + " (client still adds its own 8% tax on top)");

        System.out.println();
        System.out.println("CONFIRMED FAILURE: the customer was silently overcharged $"
                + fmt(modifiedDisplayed - originalDisplayed) + " because the server changed what \"total\""
                + " means without changing its name or bumping any version. The client never crashed,"
                + " never saw an error -- it just double-applied tax, because nothing told it the field's"
                + " meaning had changed underneath it.");
    }

    static String fmt(double value) {
        return String.format("%.2f", value);
    }
}
