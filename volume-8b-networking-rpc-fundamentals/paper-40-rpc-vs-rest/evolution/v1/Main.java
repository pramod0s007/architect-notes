public class Main {
    public static void main(String[] args) {
        // Server v1 encodes an order total under the key "totalPrice".
        JsonMessage fromServerV1 = new JsonMessage();
        fromServerV1.put("totalPrice", 149.99);

        // Client code written against server v1's contract.
        double totalFromV1 = readTotal(fromServerV1);
        System.out.println("Client reading server v1's response: totalPrice = " + totalFromV1);

        // Server v2 renames the field from "totalPrice" to "totalAmount" — a routine-looking
        // rename, same value, same meaning, just a clearer name.
        JsonMessage fromServerV2 = new JsonMessage();
        fromServerV2.put("totalAmount", 149.99);

        // The SAME unchanged client code, now talking to server v2.
        double totalFromV2 = readTotal(fromServerV2);
        System.out.println("Same client reading server v2's response: totalPrice = " + totalFromV2);

        System.out.println();
        System.out.println("CONFIRMED FAILURE: the client's field lookup is by string key (\"totalPrice\"). "
            + "Server v2 renamed that key to \"totalAmount\" with the value completely unchanged, but the "
            + "client's get(\"totalPrice\") now returns null, and the client defaults a missing total to "
            + "0.0 rather than crash. The order total silently becomes $0.00 — no exception, no log line, "
            + "nothing to alert anyone that the contract drifted.");
    }

    // Client-side reader: pulls the total out of a JSON-shaped message by its field name.
    static double readTotal(JsonMessage message) {
        Object value = message.get("totalPrice");
        return value == null ? 0.0 : (double) value;
    }
}
