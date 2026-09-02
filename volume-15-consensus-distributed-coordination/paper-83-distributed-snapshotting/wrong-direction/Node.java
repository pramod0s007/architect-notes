// One node's real, live account balance. A "snapshot" records this value at
// whatever moment it's asked for -- the question this paper is about is
// whether that moment was chosen correctly relative to messages in flight
// between nodes.
class Node {
    final String id;
    int balance;

    Node(String id, int balance) {
        this.id = id;
        this.balance = balance;
    }
}
