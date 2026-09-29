/**
 * PRINCIPLE APPLIED: erasure as a tracked workflow instead of a single fire-and-forget pass. This
 * is a deterministic, single-process, in-memory model: the "manifest" here is three boolean
 * fields, not a persisted record, and "Attempt 2" is invoked from the same main method as Attempt
 * 1, not by a genuinely separate retry worker running later against restored state. It stands in
 * for what a real implementation would need: a manifest persisted before any deletion is
 * attempted (naming every target and marking each PENDING), a per-target status that survives a
 * process restart, and a retry pass that reads that persisted status rather than in-memory state
 * to know what still needs doing. A target that fails stays PENDING in this model, exactly as it
 * would need to in a persisted one, so a later retry pass -- real or simulated -- knows exactly
 * what still needs to happen and only that; the user is only reported as "fully forgotten" once
 * every target in the manifest reads DONE.
 */
public class Main {
    private static boolean primaryStatusDone = false;
    private static boolean analyticsStatusDone = false;
    private static boolean searchIndexStatusDone = false;
    private static boolean searchIndexAvailableThisAttempt = false;

    public static void main(String[] args) {
        String userId = "user-4471";

        System.out.println("Right-to-erasure request received for " + userId);
        System.out.println("Erasure manifest created before any deletion is attempted: "
                + "primary=PENDING, analytics=PENDING, searchIndex=PENDING");

        runErasureAttempt(userId, "Attempt 1");
        printManifestStatus();

        boolean fullyForgottenAfterAttempt1 = primaryStatusDone && analyticsStatusDone && searchIndexStatusDone;
        System.out.println("Fully forgotten after Attempt 1: " + fullyForgottenAfterAttempt1
                + " -- manifest still has a PENDING target, so the user is not yet reported as erased.");

        System.out.println("Retry pass runs later, driven by the manifest -- it only re-attempts "
                + "targets still marked PENDING (primary and analytics are skipped; they are already DONE).");
        searchIndexAvailableThisAttempt = true;
        runErasureAttempt(userId, "Attempt 2 (retry, search index only)");
        printManifestStatus();

        boolean fullyForgottenAfterAttempt2 = primaryStatusDone && analyticsStatusDone && searchIndexStatusDone;
        System.out.println("Result: fully forgotten after Attempt 2: " + fullyForgottenAfterAttempt2
                + ". Every manifest entry reads DONE, so -- and only so -- the user is now reported "
                + "as fully erased. At no point between Attempt 1 and Attempt 2 was the user "
                + "incorrectly reported as fully erased.");
    }

    private static void runErasureAttempt(String userId, String label) {
        if (!primaryStatusDone) {
            System.out.println(label + ": deleting " + userId + " from primary database... succeeded, "
                    + "manifest updated to primary=DONE");
            primaryStatusDone = true;
        }
        if (!analyticsStatusDone) {
            System.out.println(label + ": deleting " + userId + " from analytics warehouse... succeeded, "
                    + "manifest updated to analytics=DONE");
            analyticsStatusDone = true;
        }
        if (!searchIndexStatusDone) {
            if (searchIndexAvailableThisAttempt) {
                System.out.println(label + ": deleting " + userId + " from search index... succeeded, "
                        + "manifest updated to searchIndex=DONE");
                searchIndexStatusDone = true;
            } else {
                System.out.println(label + ": deleting " + userId + " from search index... FAILED "
                        + "(index cluster unreachable during a maintenance window), manifest stays "
                        + "searchIndex=PENDING for a later retry pass to pick up");
            }
        }
    }

    private static void printManifestStatus() {
        System.out.println("Manifest status: primary=" + (primaryStatusDone ? "DONE" : "PENDING")
                + ", analytics=" + (analyticsStatusDone ? "DONE" : "PENDING")
                + ", searchIndex=" + (searchIndexStatusDone ? "DONE" : "PENDING"));
    }
}
