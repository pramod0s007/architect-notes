/**
 * PRESSURE: a user exercises their right to erasure (GDPR Article 17). The system honors the
 * request the way most first implementations do -- delete the user's row from the primary
 * database and call it done. The primary database is genuinely, immediately clean. But the
 * user's data was never only in the primary database: it was also copied into an analytics
 * warehouse (for reporting) and a search index (for lookup) by earlier, unrelated pipelines.
 * Deleting only the primary record leaves those two copies fully intact -- the user is "deleted"
 * in the one place anyone checks, and still fully present in two places nobody thought to check.
 */
public class Main {
    public static void main(String[] args) {
        String userId = "user-4471";

        System.out.println("Right-to-erasure request received for " + userId);

        boolean primaryDbHasUser = true;
        boolean analyticsWarehouseHasUser = true;
        boolean searchIndexHasUser = true;

        System.out.println("Before deletion -- primary DB: " + primaryDbHasUser
                + ", analytics warehouse: " + analyticsWarehouseHasUser
                + ", search index: " + searchIndexHasUser);

        primaryDbHasUser = deleteFromPrimaryDatabase(userId);

        System.out.println("Deletion executed against the primary database only.");
        System.out.println("After deletion -- primary DB: " + primaryDbHasUser
                + ", analytics warehouse: " + analyticsWarehouseHasUser
                + ", search index: " + searchIndexHasUser);

        boolean userDataStillExistsSomewhere = analyticsWarehouseHasUser || searchIndexHasUser;
        int downstreamSystemsStillHoldingData = (analyticsWarehouseHasUser ? 1 : 0) + (searchIndexHasUser ? 1 : 0);

        System.out.println("Result: primary DB reports the user erased, but the user's data still "
                + "exists in " + downstreamSystemsStillHoldingData + " downstream system(s) that were "
                + "never told about the deletion request. Erasure confirmed to the user, but not "
                + "actually true: " + userDataStillExistsSomewhere);
    }

    private static boolean deleteFromPrimaryDatabase(String userId) {
        System.out.println("Deleting " + userId + " from primary database... done.");
        return false;
    }
}
