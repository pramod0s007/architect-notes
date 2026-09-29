/** A reaction some service registers for a given event type. */
public interface Listener {
    void onEvent(Event event);
}
