/** A single queued message. "poison" messages always fail processing, no matter how many times retried. */
public class Message {
    private final String id;
    private final boolean poison;

    public Message(String id, boolean poison) {
        this.id = id;
        this.poison = poison;
    }

    public String getId() {
        return id;
    }

    /** Simulates processing this message. Poison messages always throw; others always succeed. */
    public void process() {
        if (poison) {
            throw new RuntimeException("processing failed for " + id);
        }
    }
}
