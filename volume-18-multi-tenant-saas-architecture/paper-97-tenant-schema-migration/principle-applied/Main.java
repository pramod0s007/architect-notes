import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Main {
    static final int SIGNUP_BONUS = 100;

    // A durable checkpoint records exactly which tenants are done, right
    // after each one finishes -- the smallest real unit of idempotency in
    // this migration. A restart skips anything already checkpointed.
    static void migrateTenant(Map<String, Integer> creditBalance, Set<String> checkpointed, String tenantId) {
        if (checkpointed.contains(tenantId)) {
            return; // already migrated, do not reapply
        }
        creditBalance.merge(tenantId, SIGNUP_BONUS, Integer::sum);
        checkpointed.add(tenantId);
    }

    public static void main(String[] args) {
        List<String> tenants = List.of("T1", "T2", "T3", "T4", "T5");
        Map<String, Integer> creditBalance = new HashMap<>();
        for (String t : tenants) creditBalance.put(t, 0);
        Set<String> checkpointed = new HashSet<>();

        System.out.println("First run: processing tenants in order, checkpointing each as it completes...");
        List<String> firstRunProcessed = new ArrayList<>();
        for (String t : tenants) {
            if (firstRunProcessed.size() == 3) {
                System.out.println("  CRASH after processing " + firstRunProcessed + " (all checkpointed)");
                break;
            }
            migrateTenant(creditBalance, checkpointed, t);
            firstRunProcessed.add(t);
        }

        System.out.println();
        System.out.println("Restart: checkpoint shows " + checkpointed + " already done -- skip them, resume from there.");
        for (String t : tenants) {
            migrateTenant(creditBalance, checkpointed, t);
        }

        System.out.println();
        for (String t : tenants) {
            System.out.println("  " + t + ": credit balance = " + creditBalance.get(t));
        }

        boolean allCorrect = tenants.stream().allMatch(t -> creditBalance.get(t) == 100);
        System.out.println();
        System.out.println(allCorrect
            ? "FIXED: every tenant has exactly one signup bonus applied, regardless of when the crash "
              + "happened. The restart resumed exactly where the crash left off, because the checkpoint "
              + "granularity -- one tenant at a time -- matches the actual unit the migration needs to "
              + "be idempotent about."
            : "Unexpected -- investigate.");
    }
}
