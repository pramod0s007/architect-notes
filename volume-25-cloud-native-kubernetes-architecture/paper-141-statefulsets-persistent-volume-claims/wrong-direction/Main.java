/**
 * WRONG DIRECTION: the team migrates to a StatefulSet to get stable per-replica identity and
 * storage -- ordinal 0, 1, 2 each get their own PersistentVolumeClaim that survives reschedules,
 * fixing the pressure scenario's problem. But they scale down from 3 replicas to 1 for a
 * quiet period (a routine cost-saving move), then scale back up later, without configuring
 * `persistentVolumeClaimRetentionPolicy` at all -- which defaults to Retain. Ordinals 1 and 2's
 * PVCs are never deleted on scale-down; they just sit there, still billed, not attached to
 * anything. When the team scales back up, ordinals 1 and 2 reattach to those same retained
 * PVCs and immediately resume writing, without validating how old the data actually is. The
 * StatefulSet guarantee is about identity -- "ordinal 1 always gets the same PVC" -- not about
 * freshness, and nothing here checks whether "the same PVC" still reflects anything current.
 */
public class Main {
    private static final int MAX_ORDINALS = 3;
    private static final int TOTAL_TICKS = 40;

    public static void main(String[] args) {
        boolean[] running = {true, true, true};
        int[] lastWrittenTick = new int[MAX_ORDINALS];
        int riskyReattachments = 0;
        long totalStalenessTicks = 0;

        for (int tick = 1; tick <= TOTAL_TICKS; tick++) {
            if (tick == 10) {
                running[1] = false;
                running[2] = false;
                System.out.println("Tick " + tick + ": scale down 3 -> 1 (ordinals 1, 2 stop running; "
                        + "their PVCs are retained by default, not deleted)");
            } else if (tick == 20) {
                for (int ord = 1; ord <= 2; ord++) {
                    int staleness = tick - lastWrittenTick[ord];
                    totalStalenessTicks += staleness;
                    riskyReattachments++;
                }
                running[1] = true;
                running[2] = true;
                System.out.println("Tick " + tick + ": scale up 1 -> 3 (ordinals 1, 2 silently reattach to their "
                        + "retained PVCs and resume writing, with no check on how stale that data is)");
            } else if (tick == 30) {
                running[1] = false;
                running[2] = false;
                System.out.println("Tick " + tick + ": scale down 3 -> 1 again (ordinals 1, 2's PVCs retained "
                        + "and orphaned once more)");
            }

            for (int ord = 0; ord < MAX_ORDINALS; ord++) {
                if (running[ord]) {
                    lastWrittenTick[ord] = tick;
                }
            }
        }

        int orphanedPvcs = 0;
        for (int ord = 0; ord < MAX_ORDINALS; ord++) {
            if (!running[ord]) {
                orphanedPvcs++;
            }
        }

        System.out.println();
        System.out.println("Risky stale reattachments: " + riskyReattachments + ", total staleness ticks silently "
                + "absorbed: " + totalStalenessTicks + ", orphaned (retained, unattached) PVCs at end of run: "
                + orphanedPvcs);
    }
}
