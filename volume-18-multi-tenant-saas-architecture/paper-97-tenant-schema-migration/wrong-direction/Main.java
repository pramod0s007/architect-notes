import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Main {
    static final int SIGNUP_BONUS = 100;
    static final int BATCH_SIZE = 2;

    static void migrateTenant(Map<String, Integer> creditBalance, String tenantId) {
        creditBalance.merge(tenantId, SIGNUP_BONUS, Integer::sum);
    }

    public static void main(String[] args) {
        List<String> tenants = List.of("T1", "T2", "T3", "T4", "T5");
        Map<String, Integer> creditBalance = new HashMap<>();
        for (String t : tenants) creditBalance.put(t, 0);

        // Overcorrection: checkpointing exists -- this isn't the pressure
        // case's mistake. But the checkpoint is only recorded once an
        // entire BATCH of 2 tenants finishes, not after each individual
        // tenant. Batches: [T1,T2], [T3,T4], [T5].
        Set<String> checkpointedBatches = new HashSet<>();
        List<List<String>> batches = new ArrayList<>();
        for (int i = 0; i < tenants.size(); i += BATCH_SIZE) {
            batches.add(tenants.subList(i, Math.min(i + BATCH_SIZE, tenants.size())));
        }

        System.out.println("First run: processing in batches of " + BATCH_SIZE + ", checkpointing per BATCH...");
        outer:
        for (List<String> batch : batches) {
            List<String> doneInBatch = new ArrayList<>();
            for (String t : batch) {
                if (batch.equals(List.of("T3", "T4")) && doneInBatch.size() == 1) {
                    System.out.println("  CRASH mid-batch, after T3 but before T4 -- batch [T3, T4] never checkpointed");
                    break outer;
                }
                migrateTenant(creditBalance, t);
                doneInBatch.add(t);
            }
            checkpointedBatches.add(batch.toString());
        }

        System.out.println();
        System.out.println("Restart: checkpointed batches = " + checkpointedBatches + " -- anything else restarts from scratch.");
        for (List<String> batch : batches) {
            if (checkpointedBatches.contains(batch.toString())) {
                continue; // whole batch already done, skip entirely
            }
            for (String t : batch) {
                migrateTenant(creditBalance, t); // re-runs the ENTIRE incomplete batch, including T3 again
            }
        }

        System.out.println();
        for (String t : tenants) {
            System.out.println("  " + t + ": credit balance = " + creditBalance.get(t));
        }

        boolean t3Corrupted = creditBalance.get("T3") == 200;
        boolean othersCorrect = creditBalance.get("T1") == 100 && creditBalance.get("T2") == 100
            && creditBalance.get("T4") == 100 && creditBalance.get("T5") == 100;
        System.out.println();
        System.out.println(t3Corrupted && othersCorrect
            ? "CONFIRMED FAILURE: T1, T2, T4, and T5 are all correct -- checkpointing genuinely helped. "
              + "T3 still received the bonus twice, because it was processed inside the same batch as T4, "
              + "and the crash happened after T3 but before that batch was checkpointed as complete. The "
              + "checkpoint granularity (2 tenants) doesn't match the real unit of idempotency (1 tenant), "
              + "so restarting the incomplete batch replays work that was already, correctly, done."
            : "Unexpected -- investigate.");
    }
}
