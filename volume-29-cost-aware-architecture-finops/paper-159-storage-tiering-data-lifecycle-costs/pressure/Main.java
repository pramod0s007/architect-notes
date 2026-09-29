import java.util.*;

/**
 * PRESSURE SCENARIO: age-based storage tiering.
 *
 * A lifecycle policy moves every object older than 90 days to the ARCHIVE tier,
 * on the theory that "old data is cold data." Age alone says nothing about how
 * often an object is actually retrieved -- and retrieval from ARCHIVE is priced
 * per GB, unlike HOT, where retrieval is free. An old object that is still
 * retrieved regularly can cost MORE in ARCHIVE than it would have cost to simply
 * leave in HOT, once retrieval charges are counted.
 */
public class Main {

    static final double HOT_STORAGE_PER_GB_MONTH = 0.023;
    static final double HOT_RETRIEVAL_PER_GB = 0.0;

    static final double ARCHIVE_STORAGE_PER_GB_MONTH = 0.00099;
    static final double ARCHIVE_RETRIEVAL_PER_GB = 0.02;

    static final int AGE_THRESHOLD_DAYS = 90;

    static class StorageObject {
        String name;
        double sizeGB;
        int ageDays;
        double observedAccessesPerMonth; // this object's own historical average retrieval count

        StorageObject(String name, double sizeGB, int ageDays, double observedAccessesPerMonth) {
            this.name = name;
            this.sizeGB = sizeGB;
            this.ageDays = ageDays;
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
        objects.add(new StorageObject("compliance-reports", 500, 120, 2.0));
        objects.add(new StorageObject("cold-logs", 2000, 200, 0.0));
        objects.add(new StorageObject("occasional-audit-export", 300, 100, 0.1));

        System.out.println("-- Age-based lifecycle policy: move to ARCHIVE once an object is older than " + AGE_THRESHOLD_DAYS + " days --");
        double totalArchivePolicyCost = 0.0;
        for (StorageObject o : objects) {
            boolean movedToArchive = o.ageDays > AGE_THRESHOLD_DAYS;
            double cost = movedToArchive ? monthlyCostArchive(o) : monthlyCostHot(o);
            totalArchivePolicyCost += cost;
            System.out.printf("%-26s age=%3dd size=%6.0fGB observed-accesses=%.1f/mo -> %s, $%.3f/month%n",
                    o.name, o.ageDays, o.sizeGB, o.observedAccessesPerMonth,
                    movedToArchive ? "ARCHIVE" : "HOT", cost);
        }
        System.out.printf("Total under age-based policy: $%.3f/month%n", totalArchivePolicyCost);

        System.out.println();
        System.out.println("-- What compliance-reports would have cost if left in HOT instead --");
        StorageObject compliance = objects.get(0);
        double archiveCost = monthlyCostArchive(compliance);
        double hotCost = monthlyCostHot(compliance);
        System.out.printf("compliance-reports in ARCHIVE: $%.3f/month (storage + %.1f retrievals/mo of %.0fGB)%n",
                archiveCost, compliance.observedAccessesPerMonth, compliance.sizeGB);
        System.out.printf("compliance-reports in HOT:     $%.3f/month (storage only, retrieval is free in HOT)%n", hotCost);
        System.out.printf("Moving this object to ARCHIVE at 90 days overpaid $%.3f/month, because age alone did not account for its own %.1f-per-month retrieval history%n",
                archiveCost - hotCost, compliance.observedAccessesPerMonth);
    }
}
