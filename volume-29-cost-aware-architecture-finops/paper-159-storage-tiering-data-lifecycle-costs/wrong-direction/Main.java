import java.util.*;

/**
 * WRONG-DIRECTION SCENARIO: recency-based storage tiering.
 *
 * The fix for age-based tiering's blind spot looks obvious: instead of moving an
 * object based on its age, move it based on whether it was RECENTLY accessed --
 * accessed within the last 30 days, keep it in HOT; otherwise, ARCHIVE it. This
 * still misfires in both directions, because recency of the single most recent
 * access is not the same signal as an object's own steady-state access frequency:
 * a regularly-retrieved object caught just outside the recency window gets moved
 * to ARCHIVE right before its next scheduled retrieval, and a rarely-retrieved
 * object caught just inside the recency window (because it happened to be touched
 * once, recently) gets kept in expensive HOT storage for a full month it does not
 * otherwise need.
 */
public class Main {

    static final double HOT_STORAGE_PER_GB_MONTH = 0.023;
    static final double HOT_RETRIEVAL_PER_GB = 0.0;

    static final double ARCHIVE_STORAGE_PER_GB_MONTH = 0.00099;
    static final double ARCHIVE_RETRIEVAL_PER_GB = 0.02;

    static final int RECENCY_WINDOW_DAYS = 30;

    static class StorageObject {
        String name;
        double sizeGB;
        int daysSinceLastAccess;
        double observedAccessesPerMonth; // this object's own historical average retrieval count

        StorageObject(String name, double sizeGB, int daysSinceLastAccess, double observedAccessesPerMonth) {
            this.name = name;
            this.sizeGB = sizeGB;
            this.daysSinceLastAccess = daysSinceLastAccess;
            this.observedAccessesPerMonth = observedAccessesPerMonth;
        }
    }

    static double monthlyCostHot(StorageObject o) {
        return o.sizeGB * HOT_STORAGE_PER_GB_MONTH + o.observedAccessesPerMonth * o.sizeGB * HOT_RETRIEVAL_PER_GB;
    }

    static double monthlyCostArchive(StorageObject o) {
        return o.sizeGB * ARCHIVE_STORAGE_PER_GB_MONTH + o.observedAccessesPerMonth * o.sizeGB * ARCHIVE_RETRIEVAL_PER_GB;
    }

    public static void main(String[] args) {
        List<StorageObject> objects = new ArrayList<>();
        // compliance-reports: retrieved twice a month, on an irregular schedule
        // tied to audit deadlines rather than a fixed calendar day -- this
        // month's snapshot happens to land 35 days after its most recent
        // retrieval, just outside the 30-day recency window and shortly
        // before its next one.
        objects.add(new StorageObject("compliance-reports", 500, 35, 2.0));
        // cold-logs: genuinely untouched for over a year.
        objects.add(new StorageObject("cold-logs", 2000, 400, 0.0));
        // occasional-audit-export: an auditor happened to pull this 5 days ago,
        // but its steady-state retrieval frequency is once every ~10 months.
        objects.add(new StorageObject("occasional-audit-export", 300, 5, 0.1));

        System.out.println("-- Recency-based lifecycle policy: keep in HOT if accessed within the last " + RECENCY_WINDOW_DAYS + " days, else ARCHIVE --");
        double totalRecencyPolicyCost = 0.0;
        for (StorageObject o : objects) {
            boolean keptHot = o.daysSinceLastAccess <= RECENCY_WINDOW_DAYS;
            double cost = keptHot ? monthlyCostHot(o) : monthlyCostArchive(o);
            totalRecencyPolicyCost += cost;
            System.out.printf("%-26s last-access=%3dd-ago observed-accesses=%.1f/mo -> %s, $%.3f/month%n",
                    o.name, o.daysSinceLastAccess, o.observedAccessesPerMonth,
                    keptHot ? "HOT" : "ARCHIVE", cost);
        }
        System.out.printf("Total under recency-based policy: $%.3f/month%n", totalRecencyPolicyCost);

        System.out.println();
        System.out.println("-- Two ways the recency window still gets it wrong --");
        StorageObject compliance = objects.get(0);
        double complianceArchive = monthlyCostArchive(compliance);
        double complianceHot = monthlyCostHot(compliance);
        System.out.printf("compliance-reports: 35 days since last access falls outside the window, so it is moved to ARCHIVE right before its next scheduled retrieval -- $%.3f/month instead of $%.3f/month if left in HOT%n",
                complianceArchive, complianceHot);

        StorageObject audit = objects.get(2);
        double auditHot = monthlyCostHot(audit);
        double auditArchive = monthlyCostArchive(audit);
        System.out.printf("occasional-audit-export: a single recent access keeps it in HOT for the whole month -- $%.3f/month instead of $%.3f/month if it had gone to ARCHIVE, even though its own steady-state frequency is only %.1f/month%n",
                auditHot, auditArchive, audit.observedAccessesPerMonth);
    }
}
