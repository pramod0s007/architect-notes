/**
 * Models the downstream service's remaining request-handling capacity per tick, after
 * whatever steady baseline traffic it's already committed to serving. serve(demand) returns
 * how many of the offered requests it can actually handle this tick; the rest fail (and, in
 * these scenarios, get retried).
 */
public class Downstream {
    private final int spareCapacityPerTick;

    public Downstream(int spareCapacityPerTick) {
        this.spareCapacityPerTick = spareCapacityPerTick;
    }

    public int serve(int demand) {
        return Math.min(demand, spareCapacityPerTick);
    }
}
