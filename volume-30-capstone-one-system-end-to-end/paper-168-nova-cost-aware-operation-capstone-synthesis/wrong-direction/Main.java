/**
 * WRONG-DIRECTION: instead of leaving cost entirely unattributed, two independent logs are added
 * -- a cost log (a running list of modeled cost amounts) and a mechanism/health log (a running
 * list of which mechanism ran and its status) -- but the two logs are populated by two separate,
 * independently-called recording methods, and neither log's own entries carry any field
 * identifying which mechanism-log entry corresponds to which cost-log entry. In this specific,
 * sequential program, the two logs happen to stay in matching list position because this program's
 * control flow calls recordCost and recordMechanism back-to-back for every operation -- but that
 * alignment is enforced only by this program's current call order, not recorded in either log's
 * own data. If either log were later filtered, reordered, or queried on its own -- independently of
 * this specific call path -- nothing in the recorded entries themselves would let a reader recover
 * which cost-log entry belongs with which mechanism-log entry.
 */
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        List<Long> costLog = new ArrayList<>();
        List<String> mechanismLog = new ArrayList<>();

        System.out.println("-- Nova cost tracking: separate cost log and mechanism log, no shared correlation --");
        System.out.println();

        System.out.println("Operation 1 -- tenant-a, CACHE mechanism, cache miss (modeled cost: 40):");
        recordCost(costLog, 40);
        recordMechanism(mechanismLog, "tenant-a", "CACHE", "MISS");
        System.out.println();

        System.out.println("Operation 2 -- tenant-a, RESILIENCE mechanism, breaker open, after repeated failures (modeled cost: 5):");
        recordCost(costLog, 5);
        recordMechanism(mechanismLog, "tenant-a", "RESILIENCE", "OPEN");
        System.out.println();

        System.out.println("Operation 3 -- tenant-b, CACHE mechanism, cache hit (modeled cost: 2):");
        recordCost(costLog, 2);
        recordMechanism(mechanismLog, "tenant-b", "CACHE", "HIT");
        System.out.println();

        System.out.println("Cost log (in recorded order): " + costLog);
        System.out.println("Mechanism log (in recorded order): " + mechanismLog);
        System.out.println();
        System.out.println("Result: both logs exist and both are populated, and in this specific, sequential");
        System.out.println("program they stay in matching list position because recordCost and recordMechanism");
        System.out.println("are called back-to-back for every operation -- but that alignment is enforced only");
        System.out.println("by this program's current call order, not by anything recorded in either log's own");
        System.out.println("entries. Neither log entry carries a field identifying its counterpart in the other");
        System.out.println("log, so if either log were filtered, reordered, or queried independently of this");
        System.out.println("exact call path, nothing in the recorded data itself would recover the pairing.");
    }

    /** Appends the modeled cost of one operation to the cost log; records nothing else. */
    static void recordCost(List<Long> costLog, long modeledCost) {
        costLog.add(modeledCost);
        System.out.println("  Cost log entry appended: " + modeledCost);
    }

    /** Appends a mechanism/status entry to the mechanism log; carries no cost information at all. */
    static void recordMechanism(List<String> mechanismLog, String tenantId, String mechanism, String status) {
        String entry = tenantId + "/" + mechanism + "/" + status;
        mechanismLog.add(entry);
        System.out.println("  Mechanism log entry appended: " + entry);
    }
}
