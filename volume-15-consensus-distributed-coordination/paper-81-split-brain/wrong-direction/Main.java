public class Main {
    // The overcorrection: the pressure section's failover felt too trigger-happy,
    // so require many more consecutive failed checks before promoting -- surely
    // that gives transient network blips time to resolve, avoiding a needless
    // failover. It does nothing for a health-check path that's down for good
    // while the client path stays up, which is exactly the scenario here.
    static final int REQUIRED_FAILED_CHECKS = 10;

    public static void main(String[] args) {
        PrimaryNode p1 = new PrimaryNode("P1");
        PrimaryNode p2 = new PrimaryNode("P2");

        System.out.println("Same partial network issue as before: the health-check path to P1 is "
            + "down for good this time -- not a transient blip -- while P1's client path stays up.");
        System.out.println("This time the controller waits for " + REQUIRED_FAILED_CHECKS
            + " consecutive failed checks (up from 3) before acting, still with no fencing.");

        int failedChecks = 0;
        boolean healthCheckerCanReachP1 = false;
        while (!healthCheckerCanReachP1 && failedChecks < REQUIRED_FAILED_CHECKS) {
            failedChecks++;
        }

        System.out.println("Failed checks reached: " + failedChecks + " -- promoting P2, still without fencing P1.");

        p1.write("100");
        p2.write("250");

        System.out.println();
        System.out.println("P1's recorded value: " + p1.currentValue());
        System.out.println("P2's recorded value: " + p2.currentValue());
        System.out.println(!p1.currentValue().equals(p2.currentValue())
            ? "STILL FAILS, JUST LATER: waiting for 10 failed checks instead of 3 delayed the "
              + "promotion, but the health-check path was never coming back in this scenario, so the "
              + "threshold was always going to be reached eventually. The same unfenced promotion "
              + "produces the identical split-brain -- the timeout controls WHEN the bug happens, not "
              + "WHETHER it happens, because it never addressed the actual gap: promoting without ever "
              + "confirming the old primary had actually stopped."
            : "Unexpected -- investigate.");
    }
}
