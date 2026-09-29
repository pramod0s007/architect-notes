/**
 * WRONG DIRECTION: after seeing the retry storm, the team adds exponential backoff --
 * doubling the retry interval after every failure, starting from the same BASE_DELAY_TICKS=4
 * used in the principle-applied scenario (so the only variable that differs between the two
 * is the presence of jitter, not the base interval). This is a real, well-known mitigation,
 * and it does help -- but only half of the standard advice was applied. All 50 clients
 * started retrying at the exact same tick, and since every client follows the identical,
 * deterministic doubling schedule, they stay perfectly synchronized the entire time --
 * backoff without jitter doesn't desynchronize a herd, it just makes the herd wait longer
 * between identical, full-force stampedes. Each retry wave still slams into the same 10-unit
 * spare capacity all at once, so every wave is exactly as intense as the fixed-interval
 * version -- the only thing that changes is how far apart the waves are, which means the
 * last unlucky client now waits far longer for a response that used to arrive quickly.
 */
public class Main {
    private static final int TOTAL_CLIENTS = 50;
    private static final int SPARE_CAPACITY_PER_TICK = 10;
    private static final int BASE_DELAY_TICKS = 4;
    private static final int MAX_TICKS = 200;

    public static void main(String[] args) {
        Downstream downstream = new Downstream(SPARE_CAPACITY_PER_TICK);
        int pending = TOTAL_CLIENTS;
        int interval = BASE_DELAY_TICKS;
        int nextRetryTick = 1;
        long totalAttempts = 0;
        int stormTicks = 0;
        int drainTick = -1;

        for (int tick = 1; tick <= MAX_TICKS && pending > 0; tick++) {
            if (tick != nextRetryTick) {
                continue;
            }

            int demand = pending;
            totalAttempts += demand;
            int succeeded = downstream.serve(demand);
            int stillFailed = demand - succeeded;
            if (demand > succeeded) {
                stormTicks++;
            }

            System.out.println("Tick " + tick + ": " + demand + " clients retry simultaneously (backoff, no jitter), "
                    + "downstream serves " + succeeded + ", " + stillFailed + " fail, next synchronized retry in "
                    + (interval * 2) + " ticks");

            pending = stillFailed;
            interval *= 2;
            nextRetryTick = tick + interval;
            if (pending == 0) {
                drainTick = tick;
            }
        }

        System.out.println();
        System.out.println("Total retry attempts issued: " + totalAttempts + ", ticks downstream was over capacity: "
                + stormTicks + ", tick fully drained: " + drainTick);
    }
}
