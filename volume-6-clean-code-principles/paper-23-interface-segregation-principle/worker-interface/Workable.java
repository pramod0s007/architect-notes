/**
 * Workable — focused interface for any entity that can perform work.
 *
 * ISP fix: separates operational concerns from biological ones.
 * Both human employees and robots implement this; neither is forced
 * to provide methods that don't apply to them.
 */
public interface Workable {

    /** Perform the assigned work task. */
    void work();

    /** Return the worker's hourly billing rate. */
    double getHourlyRate();

    /** Return true if the worker is currently on a break. */
    boolean isOnBreak();
}
