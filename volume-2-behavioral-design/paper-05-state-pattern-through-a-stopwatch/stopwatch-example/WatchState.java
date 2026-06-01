/**
 * State-specific behavior for {@link StopWatch}.
 * See: volume-2/.../paper-05-state-pattern-through-a-stopwatch
 */
public interface WatchState {

    void start(StopWatch watch);

    void pause(StopWatch watch);

    void resume(StopWatch watch);

    void stop(StopWatch watch);

    String name();
}
