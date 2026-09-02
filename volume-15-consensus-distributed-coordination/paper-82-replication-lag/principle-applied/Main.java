public class Main {
    public static void main(String[] args) {
        int primaryLsn = 5;
        String primaryBio = "Staff Engineer, loves distributed systems";
        System.out.println("User A writes a new bio to the primary. Primary is now at LSN=" + primaryLsn + ".");
        System.out.println("The write records requiredLsn=" + primaryLsn + " for User A's next read.");

        ReadRouter router = new ReadRouter();

        // Scenario 1: one lagging replica, one caught-up replica.
        Replica r1 = new Replica("R1", 3, "Backend Engineer");
        Replica r2 = new Replica("R2", 6, primaryBio);
        System.out.println();
        System.out.println("Candidates: R1 (replicatedLsn=3), R2 (replicatedLsn=6).");
        String result1 = router.route(new Replica[]{r1, r2}, primaryLsn, primaryBio);
        System.out.println("Read result: \"" + result1 + "\"");

        // Scenario 2: no replica has caught up at all -- falls back to primary.
        Replica r3 = new Replica("R3", 2, "Backend Engineer");
        Replica r4 = new Replica("R4", 4, "Backend Engineer");
        System.out.println();
        System.out.println("A second read, candidates: R3 (replicatedLsn=2), R4 (replicatedLsn=4) -- neither caught up.");
        String result2 = router.route(new Replica[]{r3, r4}, primaryLsn, primaryBio);
        System.out.println("Read result: \"" + result2 + "\"");

        boolean bothFresh = result1.contains(primaryBio) && result2.contains(primaryBio);
        System.out.println();
        System.out.println(bothFresh
            ? "FIXED: both reads returned the fresh bio -- one by finding a replica that had actually "
              + "caught up, the other by falling back to the primary when none had. User A never sees "
              + "their own write disappear, regardless of which replica round-robin would have picked."
            : "Unexpected -- investigate.");
    }
}
