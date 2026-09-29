/**
 * PRINCIPLE APPLIED: the team keeps the StatefulSet's stable identity guarantee (fixing the
 * pressure scenario's reschedule-loses-everything problem), but now deliberately configures
 * `persistentVolumeClaimRetentionPolicy: { whenScaled: Delete }` for this specific workload --
 * a per-replica local cache that's fully rebuildable and never needs to survive a scale-down.
 * On scale-down, ordinals 1 and 2's PVCs are actually deleted, not silently retained. On
 * scale-up, they get brand-new, empty PVCs -- there is no "old data" to silently reattach to,
 * so there's no staleness ambiguity to get wrong, and no retained PVC object left orphaned
 * (whether the underlying storage is actually released and stops being billed further
 * depends on the PersistentVolume's reclaim policy and provisioner, not on this field alone).
 * The trade-off is explicit and intentional: scaling down this workload really does throw
 * away that replica's cache, on purpose, because that's the correct behavior for disposable,
 * rebuildable local state -- a real database or anything that can't be rebuilt would instead
 * use `whenScaled: Retain` deliberately, not by default.
 */
public class Main {
    private static final int MAX_ORDINALS = 3;
    private static final int TOTAL_TICKS = 40;

    public static void main(String[] args) {
        boolean[] running = {true, true, true};
        boolean[] pvcExists = {true, true, true};
        int[] lastWrittenTick = new int[MAX_ORDINALS];
        int riskyReattachments = 0;
        long totalStalenessTicks = 0;

        for (int tick = 1; tick <= TOTAL_TICKS; tick++) {
            if (tick == 10) {
                running[1] = false;
                running[2] = false;
                pvcExists[1] = false;
                pvcExists[2] = false;
                System.out.println("Tick " + tick + ": scale down 3 -> 1 (ordinals 1, 2's PVCs are deleted per "
                        + "whenScaled=Delete -- no retained PVC left behind)");
            } else if (tick == 20) {
                for (int ord = 1; ord <= 2; ord++) {
                    // pvcExists is false here, so this is a brand-new PVC, not a reattachment --
                    // there is no prior data to be stale relative to.
                    pvcExists[ord] = true;
                }
                running[1] = true;
                running[2] = true;
                System.out.println("Tick " + tick + ": scale up 1 -> 3 (ordinals 1, 2 get brand-new, empty PVCs -- "
                        + "no old data, so no staleness risk)");
            } else if (tick == 30) {
                running[1] = false;
                running[2] = false;
                pvcExists[1] = false;
                pvcExists[2] = false;
                System.out.println("Tick " + tick + ": scale down 3 -> 1 again (ordinals 1, 2's PVCs deleted again)");
            }

            for (int ord = 0; ord < MAX_ORDINALS; ord++) {
                if (running[ord]) {
                    lastWrittenTick[ord] = tick;
                }
            }
        }

        int orphanedPvcs = 0;
        for (int ord = 0; ord < MAX_ORDINALS; ord++) {
            if (pvcExists[ord] && !running[ord]) {
                orphanedPvcs++;
            }
        }

        System.out.println();
        System.out.println("Risky stale reattachments: " + riskyReattachments + ", total staleness ticks silently "
                + "absorbed: " + totalStalenessTicks + ", orphaned (retained, unattached) PVCs at end of run: "
                + orphanedPvcs);
    }
}
