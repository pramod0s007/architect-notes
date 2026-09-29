import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * PRINCIPLE APPLIED: exponential backoff PLUS jitter -- same BASE_DELAY_TICKS=4 as the
 * wrong-direction scenario, so this is an apples-to-apples comparison that isolates jitter
 * as the only variable that changed. This is a discrete, minimum-one-tick adaptation of AWS's
 * well-known "full jitter" formula (Brooker, "Exponential Backoff And Jitter"): each client
 * picks its actual delay at random from 1 tick up to the current exponential cap, instead of
 * every client following the identical deterministic schedule. (The canonical continuous-time
 * formula allows a zero delay; a discrete per-tick model needs a minimum of one tick for the
 * delay to be schedulable at all, so the cap is sampled as 1..cap rather than 0..cap here.)
 * This desynchronizes the herd: the 50 clients' first retries spread out across a window
 * instead of landing on a single tick, so far fewer of them collide with the same slice of
 * spare capacity at once. A fixed random seed is used so this run is exactly reproducible --
 * the specific spread shown is this seeded run's outcome, not a guarantee that no random draw
 * could ever re-synchronize some clients; the claim is that jitter reduces the likelihood and
 * magnitude of synchronized collisions, not that it makes them structurally impossible.
 */
public class Main {
    private static final int TOTAL_CLIENTS = 50;
    private static final int SPARE_CAPACITY_PER_TICK = 10;
    private static final int BASE_DELAY_TICKS = 4;
    private static final int MAX_TICKS = 100;
    private static final long RANDOM_SEED = 42L;

    public static void main(String[] args) {
        Downstream downstream = new Downstream(SPARE_CAPACITY_PER_TICK);
        Random rnd = new Random(RANDOM_SEED);

        // Each pending client tracked as {attemptCount, nextRetryTick}.
        List<int[]> pending = new ArrayList<>();
        for (int i = 0; i < TOTAL_CLIENTS; i++) {
            int cap = BASE_DELAY_TICKS; // attempt 1 cap
            int delay = rnd.nextInt(cap) + 1;
            pending.add(new int[]{1, delay});
        }

        long totalAttempts = 0;
        int stormTicks = 0;
        int drainTick = -1;

        for (int tick = 1; tick <= MAX_TICKS && !pending.isEmpty(); tick++) {
            List<int[]> arriving = new ArrayList<>();
            for (int[] c : pending) {
                if (c[1] == tick) {
                    arriving.add(c);
                }
            }
            if (arriving.isEmpty()) {
                continue;
            }

            int demand = arriving.size();
            totalAttempts += demand;
            int succeeded = downstream.serve(demand);
            int stillFailed = demand - succeeded;
            if (demand > succeeded) {
                stormTicks++;
            }

            System.out.println("Tick " + tick + ": " + demand + " client" + (demand == 1 ? " retries" : "s retry")
                    + ", downstream serves " + succeeded + ", " + stillFailed
                    + " fail and reschedule with backoff+jitter");

            pending.removeAll(arriving);
            for (int i = succeeded; i < demand; i++) {
                int[] c = arriving.get(i);
                int nextAttempt = c[0] + 1;
                int cap = BASE_DELAY_TICKS * (1 << (nextAttempt - 1));
                int delay = rnd.nextInt(cap) + 1;
                pending.add(new int[]{nextAttempt, tick + delay});
            }

            if (pending.isEmpty()) {
                drainTick = tick;
            }
        }

        System.out.println();
        System.out.println("Total retry attempts issued: " + totalAttempts + ", ticks downstream was over capacity: "
                + stormTicks + ", tick fully drained: " + drainTick);
    }
}
