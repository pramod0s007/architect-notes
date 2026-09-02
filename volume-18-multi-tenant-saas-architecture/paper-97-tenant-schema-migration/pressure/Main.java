import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    static final int SIGNUP_BONUS = 100;

    // Non-idempotent migration: crediting a bonus is an ADD, not a SET, and
    // the script keeps no record at all of which tenants it already
    // processed. A restart has no way to know where it left off, so it
    // starts from the very first tenant every time.
    static void migrateTenant(Map<String, Integer> creditBalance, String tenantId) {
        creditBalance.merge(tenantId, SIGNUP_BONUS, Integer::sum);
    }

    public static void main(String[] args) {
        List<String> tenants = List.of("T1", "T2", "T3", "T4", "T5");
        Map<String, Integer> creditBalance = new HashMap<>();
        for (String t : tenants) creditBalance.put(t, 0);

        // First run: crashes after T1, T2, T3 are done, before T4 starts.
        System.out.println("First run: processing tenants in order...");
        List<String> firstRunProcessed = new ArrayList<>();
        for (String t : tenants) {
            if (firstRunProcessed.size() == 3) {
                System.out.println("  CRASH after processing " + firstRunProcessed);
                break;
            }
            migrateTenant(creditBalance, t);
            firstRunProcessed.add(t);
        }

        // Restart: with no record of prior progress, it starts from T1 again.
        System.out.println();
        System.out.println("Restart: no checkpoint exists, script begins again from the first tenant.");
        for (String t : tenants) {
            migrateTenant(creditBalance, t);
        }

        System.out.println();
        for (String t : tenants) {
            System.out.println("  " + t + ": credit balance = " + creditBalance.get(t));
        }

        boolean corrupted = creditBalance.get("T1") == 200 && creditBalance.get("T2") == 200
            && creditBalance.get("T3") == 200 && creditBalance.get("T4") == 100 && creditBalance.get("T5") == 100;
        System.out.println();
        System.out.println(corrupted
            ? "CONFIRMED FAILURE: T1, T2, and T3 -- the tenants that were already successfully migrated "
              + "before the crash -- each received the signup bonus TWICE (200 instead of 100), because "
              + "the restart had no memory of what already happened and reapplied a non-idempotent "
              + "operation to tenants that didn't need it again."
            : "Unexpected -- investigate.");
    }
}
