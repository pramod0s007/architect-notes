public class Main {
    public static void main(String[] args) {
        PrimaryNode p1 = new PrimaryNode("P1");
        PrimaryNode p2 = new PrimaryNode("P2");
        FailoverController controller = new FailoverController();

        System.out.println("Same scenario: health-check path to P1 is down, but P1's own clients "
            + "still reach it fine over a separate path.");
        System.out.println("Before promoting P2, the controller must fence P1 over an INDEPENDENT "
            + "channel -- here, a shared storage-level access control the controller can still reach.");

        boolean fencingChannelReachable = true; // the fencing path is separate from the health-check path
        boolean promoted = controller.promoteNewPrimary(p1, p2, fencingChannelReachable);
        System.out.println("Fencing succeeded, P1's write access revoked: " + promoted);

        System.out.println();
        System.out.println("P1's own clients, still unaware anything changed, attempt to write:");
        boolean p1WriteAccepted = p1.write("100");
        System.out.println("  P1 write accepted: " + p1WriteAccepted);

        System.out.println("Clients now routed to P2 write there:");
        boolean p2WriteAccepted = p2.write("250");
        System.out.println("  P2 write accepted: " + p2WriteAccepted);

        System.out.println();
        System.out.println(!p1WriteAccepted && p2WriteAccepted
            ? "FIXED: P1's write was rejected -- its access was actually revoked before P2 was ever "
              + "promoted, not just assumed revoked because the controller lost contact. Exactly one "
              + "node accepts writes at any time, even though the health-check path never recovered."
            : "Unexpected -- investigate.");
    }
}
