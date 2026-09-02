// One node in a 5-node cluster. A network partition can prevent a node from
// reaching some of the others, simulated here directly as a set of node IDs it
// can currently communicate with -- standing in for a real network partition.
class Node {
    final String id;
    boolean believesItIsLeader = false;

    Node(String id) {
        this.id = id;
    }
}
