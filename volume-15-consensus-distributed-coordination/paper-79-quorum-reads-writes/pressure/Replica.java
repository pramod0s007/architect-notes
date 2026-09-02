// One replica's stored copy of a value, with a version number that increases
// on every write -- standing in for a real vector clock or timestamp, used
// here only to determine which of several responses is the most recent.
class Replica {
    final String id;
    String value;
    int version;

    Replica(String id, String value, int version) {
        this.id = id;
        this.value = value;
        this.version = version;
    }
}
