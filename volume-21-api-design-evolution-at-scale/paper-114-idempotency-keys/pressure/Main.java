/**
 * Simulates the single most common reason a client retries a request it
 * already sent: the response never arrived, not because the request
 * failed, but because the network dropped the response on the way back.
 * The server successfully processed the first attempt; the client has no
 * way to know that, and does the only safe-seeming thing it can -- retries.
 */
public class Main {
    public static void main(String[] args) {
        PaymentService service = new PaymentService();

        System.out.println("=== Client sends: charge $49.99 for order ORD-1 ===");
        service.charge("ORD-1", 49.99);
        System.out.println("Server processes the charge and prepares a response...");

        System.out.println();
        System.out.println("=== The response is lost in transit (server processed it; client never saw that) ===");
        System.out.println("Client sees a timeout, not a success -- from the client's side, this looks identical "
                + "to \"the request never arrived at all.\"");

        System.out.println();
        System.out.println("=== Client does the only safe-seeming thing: retries the exact same charge ===");
        service.charge("ORD-1", 49.99);

        System.out.println();
        System.out.println("Ledger for order ORD-1: " + service.ledger());
        double total = 0.0;
        int count = 0;
        for (String entry : service.ledger()) {
            if (entry.startsWith("ORD-1:")) {
                total += Double.parseDouble(entry.substring("ORD-1:".length()));
                count++;
            }
        }
        System.out.println("CONFIRMED FAILURE: order ORD-1 was charged " + count + " times for a total of "
                + String.format("%.2f", total) + " -- the customer intended one $49.99 purchase, and was charged "
                + "for it twice, because the server had no way to recognize the retry as \"the same request,\" "
                + "not \"a second, independent charge.\"");
    }
}
