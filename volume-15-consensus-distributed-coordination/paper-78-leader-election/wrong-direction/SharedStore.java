// The shared store every leader writes to. It accepts a write from whoever
// sends one, with no way to check whether the sender's leadership is still
// valid at the moment the write arrives -- no fencing mechanism at all.
class SharedStore {
    private int value;
    private String lastWrittenBy;

    void write(String nodeId, int newValue) {
        this.value = newValue;
        this.lastWrittenBy = nodeId;
    }

    int currentValue() {
        return value;
    }

    String lastWriter() {
        return lastWrittenBy;
    }
}
