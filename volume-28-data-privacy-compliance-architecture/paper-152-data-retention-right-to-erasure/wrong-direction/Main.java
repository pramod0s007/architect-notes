/**
 * WRONG DIRECTION: the fix for pressure's "only deleted from the primary DB" bug is to fan the
 * deletion out to every known downstream system in one synchronous pass -- primary DB, analytics
 * warehouse, search index -- so nothing gets missed. This does reach every system. But it has no
 * durable record of what succeeded and what didn't: if one target is unreachable (a transient
 * outage, a maintenance window), the failure is only ever logged to the console and the process
 * moves on. There is no queue, no retry, and no per-user "still needs erasure in system X" state
 * anywhere durable. The moment that log line scrolls past, the only proof this user's erasure is
 * incomplete disappears with it -- from every other place in the system, this user now looks
 * exactly like a fully, successfully erased one.
 */
public class Main {
    public static void main(String[] args) {
        String userId = "user-4471";

        System.out.println("Right-to-erasure request received for " + userId);
        System.out.println("Fanning out deletion to all three known systems in one synchronous pass.");

        boolean primaryDeleted = deleteFromPrimaryDatabase(userId);
        boolean analyticsDeleted = deleteFromAnalyticsWarehouse(userId);
        boolean searchIndexDeleted = deleteFromSearchIndex(userId);

        if (!primaryDeleted || !analyticsDeleted || !searchIndexDeleted) {
            System.out.println("One or more deletions failed (see log lines above). No durable record "
                    + "of this failure exists anywhere -- it lives only in this console output.");
        }

        boolean erasureFullyComplete = primaryDeleted && analyticsDeleted && searchIndexDeleted;

        System.out.println("Result: primary DB (" + primaryDeleted + "), analytics warehouse ("
                + analyticsDeleted + "), search index (" + searchIndexDeleted + "). Erasure fully "
                + "complete: " + erasureFullyComplete + ". Once this process exits, nothing durable "
                + "distinguishes this user from a user whose erasure genuinely succeeded everywhere.");
    }

    private static boolean deleteFromPrimaryDatabase(String userId) {
        System.out.println("Deleting " + userId + " from primary database... succeeded.");
        return true;
    }

    private static boolean deleteFromAnalyticsWarehouse(String userId) {
        System.out.println("Deleting " + userId + " from analytics warehouse... succeeded.");
        return true;
    }

    private static boolean deleteFromSearchIndex(String userId) {
        System.out.println("Deleting " + userId + " from search index... FAILED (index cluster "
                + "unreachable during a maintenance window).");
        return false;
    }
}
