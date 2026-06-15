/**
 * Worker — fat interface combining operational and biological concerns.
 *
 * ISP violation: eat() and sleep() are meaningless for robotic workers.
 * Forcing robots to implement biological methods leads to either
 * no-op bodies or runtime exceptions — both mask real intent.
 */
public interface Worker {

    /** Perform the assigned work task. */
    void work();

    /** Take a meal break. */
    void eat();

    /** Rest during a sleep cycle. */
    void sleep();

    /** Return the worker's hourly billing rate. */
    double getHourlyRate();

    /** Return true if the worker is currently on a break. */
    boolean isOnBreak();
}
