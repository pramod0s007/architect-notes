public class Main {
    public static void main(String[] args) {
        // Server v1 encodes the order total under wire tag 1.
        WireMessage fromServerV1 = new WireMessage();
        fromServerV1.put(1, 149.99);

        OrderTotalStubV1 oldClient = new OrderTotalStubV1(fromServerV1);
        System.out.println("Old client (stub v1) reading server v1's message: totalPrice() = "
            + oldClient.totalPrice());

        // Server v2 renames the field in its .proto from total_price to total_amount, but the
        // wire tag is untouched — still tag 1. This is the routine, low-risk kind of rename.
        WireMessage fromServerV2 = new WireMessage();
        fromServerV2.put(1, 149.99);

        // The OLD, unregenerated client stub, never told about the rename, reading the NEW
        // server's message.
        OrderTotalStubV1 oldClientAgainstNewServer = new OrderTotalStubV1(fromServerV2);
        System.out.println("Old client (stub v1) reading server v2's message: totalPrice() = "
            + oldClientAgainstNewServer.totalPrice());

        // The new stub, regenerated from the renamed .proto, reading the same message.
        OrderTotalStubV2 newClient = new OrderTotalStubV2(fromServerV2);
        System.out.println("New client (stub v2) reading server v2's message: totalAmount() = "
            + newClient.totalAmount());

        System.out.println();
        System.out.println("FIXED: the wire identity of the field is its tag (1), not its accessor name. "
            + "Server v2 renamed totalPrice to totalAmount in its schema — a purely cosmetic change from "
            + "the wire's point of view — and the OLD client stub, never regenerated, still reads the "
            + "correct value because it was never looking up a name in the first place. Renaming is safe "
            + "as long as the tag number doesn't move.");
    }
}
