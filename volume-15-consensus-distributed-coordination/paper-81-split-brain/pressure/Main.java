public class Main {
    public static void main(String[] args) {
        PrimaryNode p1 = new PrimaryNode("P1");
        PrimaryNode p2 = new PrimaryNode("P2");

        System.out.println("P1 is the current primary. A partial network issue means the failover "
            + "controller's health-check path to P1 is down -- but P1's own clients reach it over a "
            + "completely separate network path, which is still fully up.");

        boolean healthCheckerCanReachP1 = false;
        System.out.println("Health checker can reach P1: " + healthCheckerCanReachP1);
        System.out.println("Real clients can still reach P1: true (different network path entirely)");

        System.out.println();
        System.out.println("After 3 failed health checks, the controller promotes P2 to primary -- "
            + "WITHOUT verifying P1 actually stopped, only that the controller can't see it anymore.");
        System.out.println("P2 is now also accepting writes as primary.");

        // P1's own clients, unaware anything changed, keep writing to P1.
        p1.write("100");
        // Meanwhile, clients routed to the newly-promoted P2 write there instead.
        p2.write("250");

        System.out.println();
        System.out.println("P1's recorded value: " + p1.currentValue());
        System.out.println("P2's recorded value: " + p2.currentValue());
        System.out.println(!p1.currentValue().equals(p2.currentValue())
            ? "CONFIRMED FAILURE: two primaries are simultaneously accepting writes for the same "
              + "dataset. P1 was never actually stopped -- the controller only lost contact with it. "
              + "\"I can't reach it\" and \"it stopped\" are different facts, and the controller acted "
              + "as if they were the same one."
            : "Unexpected -- investigate.");
    }
}
