public class Main {
    public static void main(String[] args) {
        System.out.println("--- Without a schema registry: tag 1 gets reused ---");
        // Original schema: tag 1 = totalAmount (a currency value, e.g. dollars).
        WireMessage fromServerV2 = new WireMessage();
        fromServerV2.put(1, 149.99);
        OrderTotalStubV2 oldClient = new OrderTotalStubV2(fromServerV2);
        System.out.println("Old client (stub v2) reading server v2: totalAmount() = " + oldClient.totalAmount());

        // Server v3 removes totalAmount and, without checking history, reuses tag 1 for an
        // unrelated field: customerId (an integer identifier, not a currency value at all).
        WireMessage fromServerV3 = new WireMessage();
        fromServerV3.put(1, 4021.0); // customerId 4021, stored as a double just like before

        // The OLD client stub was never regenerated — it still thinks tag 1 is totalAmount.
        OrderTotalStubV2 oldClientAgainstV3 = new OrderTotalStubV2(fromServerV3);
        System.out.println("Old client (stub v2) reading server v3: totalAmount() = "
            + oldClientAgainstV3.totalAmount());

        System.out.println();
        System.out.println("CONFIRMED FAILURE: server v3 reused tag 1 for a completely different field "
            + "(customerId instead of totalAmount). The old client stub still reads tag 1 and still gets "
            + "a double back — no exception, no type mismatch, no missing-field null. It just confidently "
            + "reports an order total of $4021.00, which is actually customer #4021's ID. This is worse "
            + "than the REST rename failure: that one produced an obviously-wrong $0.00; this one produces "
            + "a plausible-looking wrong number.");

        System.out.println();
        System.out.println("--- With a schema registry enforcing retired tags ---");
        SchemaRegistry registry = new SchemaRegistry();
        registry.registerField(1, "totalAmount");
        registry.retireField(1); // totalAmount removed in v3 — tag 1 is now permanently retired

        try {
            registry.registerField(1, "customerId"); // the same mistake server v3 made above
            System.out.println("registerField(1, \"customerId\") succeeded — this should not happen");
        } catch (IllegalStateException e) {
            System.out.println("registerField(1, \"customerId\") rejected: " + e.getMessage());
        }

        System.out.println();
        System.out.println("FIXED: the registry remembers that tag 1 was retired and refuses to let a new "
            + "field claim it, at schema-definition time — before a single wire message is ever sent. "
            + "customerId has to take the next free tag instead, and the old client stub simply never "
            + "receives anything on tag 1 again once totalAmount is truly gone.");
    }
}
