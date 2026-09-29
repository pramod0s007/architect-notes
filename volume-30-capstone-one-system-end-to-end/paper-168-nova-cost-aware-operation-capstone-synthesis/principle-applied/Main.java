/**
 * PRINCIPLE-APPLIED: a unified event taxonomy closes the gap the other two designs leave open.
 * Every operation emits exactly one structured OperationEvent carrying its tenant, mechanism,
 * modeled cost delta, and health status together as fields on the same object. Because cost
 * and mechanism are fields on the same event rather than entries in two independently-populated
 * logs, a diagnostic pass over the recorded events can group modeled cost by tenant and mechanism
 * and identify which specific tenant/mechanism combination contributed the largest share of a
 * modeled cost total, without relying on any assumption about matching list position across two
 * separate logs.
 */
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Main {

    public static void main(String[] args) {
        List<OperationEvent> events = new ArrayList<>();

        System.out.println("-- Nova cost tracking: unified OperationEvent, cost and mechanism correlated per event --");
        System.out.println();

        System.out.println("Operation 1 -- tenant-a, CACHE mechanism, cache miss (modeled cost: 40):");
        events.add(recordEvent("req-1", "tenant-a", "CACHE", "MISS", 40));
        System.out.println();

        System.out.println("Operation 2 -- tenant-a, RESILIENCE mechanism, breaker open, after repeated failures (modeled cost: 5):");
        events.add(recordEvent("req-2", "tenant-a", "RESILIENCE", "OPEN", 5));
        System.out.println();

        System.out.println("Operation 3 -- tenant-b, CACHE mechanism, cache hit (modeled cost: 2):");
        events.add(recordEvent("req-3", "tenant-b", "CACHE", "HIT", 2));
        System.out.println();

        long totalCost = events.stream().mapToLong(e -> e.costDelta).sum();
        System.out.println("Total modeled cost across all 3 events: " + totalCost);
        System.out.println();

        Map.Entry<String, Long> topContributor = diagnoseTopContributor(events);
        System.out.println("Diagnostic: grouping modeled cost by tenant+mechanism across the recorded events:");
        for (Map.Entry<String, Long> entry : costByTenantMechanism(events).entrySet()) {
            System.out.println("  " + entry.getKey() + " = " + entry.getValue());
        }
        System.out.println();
        System.out.println("Top contributor: " + topContributor.getKey() + " at " + topContributor.getValue()
                + " out of " + totalCost + " total modeled cost.");
        System.out.println();

        System.out.println("Result: because each event's cost and mechanism fields live on the same recorded");
        System.out.println("object, the diagnostic above attributes a specific share of the modeled cost total");
        System.out.println("to a specific tenant/mechanism combination directly from the recorded events --");
        System.out.println("no assumption about matching order across two separate logs was needed.");
    }

    /** Records one unified event carrying tenant, mechanism, health status, and cost together. */
    static OperationEvent recordEvent(String requestId, String tenantId, String mechanism,
                                       String healthStatus, long costDelta) {
        OperationEvent event = new OperationEvent(requestId, tenantId, mechanism, healthStatus, costDelta);
        System.out.println("  Event recorded: requestId=" + requestId + " tenant=" + tenantId
                + " mechanism=" + mechanism + " status=" + healthStatus + " costDelta=" + costDelta);
        return event;
    }

    /** Groups modeled cost by tenant+mechanism key, summing costDelta across matching events. */
    static Map<String, Long> costByTenantMechanism(List<OperationEvent> events) {
        Map<String, Long> totals = new TreeMap<>();
        for (OperationEvent event : events) {
            String key = event.tenantId + "/" + event.mechanism;
            totals.merge(key, event.costDelta, Long::sum);
        }
        return totals;
    }

    /** Returns the tenant+mechanism key with the largest summed modeled cost. */
    static Map.Entry<String, Long> diagnoseTopContributor(List<OperationEvent> events) {
        return costByTenantMechanism(events).entrySet().stream()
                .max(Comparator.comparingLong(Map.Entry::getValue))
                .orElseThrow();
    }

    /** A single unified event: one operation's tenant, mechanism, health status, and cost together. */
    static class OperationEvent {
        final String requestId;
        final String tenantId;
        final String mechanism;
        final String healthStatus;
        final long costDelta;

        OperationEvent(String requestId, String tenantId, String mechanism, String healthStatus, long costDelta) {
            this.requestId = requestId;
            this.tenantId = tenantId;
            this.mechanism = mechanism;
            this.healthStatus = healthStatus;
            this.costDelta = costDelta;
        }
    }
}
