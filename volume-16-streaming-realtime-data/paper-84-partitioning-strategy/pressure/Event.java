// One event on the stream. partitionKey decides which partition (and
// therefore which single consumer) processes it -- and every event sharing a
// key MUST land on the same partition, or per-key ordering breaks entirely.
class Event {
    final String partitionKey;
    final String payload;

    Event(String partitionKey, String payload) {
        this.partitionKey = partitionKey;
        this.payload = payload;
    }
}
