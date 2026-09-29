/**
 * A single domain event about one entity (e.g. a customer's order lifecycle).
 * sequenceNum is the order the event was produced in, not the order it will be processed in.
 */
public class Event {
    private final String entityId;
    private final String type;
    private final int sequenceNum;

    public Event(String entityId, String type, int sequenceNum) {
        this.entityId = entityId;
        this.type = type;
        this.sequenceNum = sequenceNum;
    }

    public String getEntityId() {
        return entityId;
    }

    public String getType() {
        return type;
    }

    public int getSequenceNum() {
        return sequenceNum;
    }

    @Override
    public String toString() {
        return "Event{" + entityId + ", " + type + ", seq=" + sequenceNum + "}";
    }
}
