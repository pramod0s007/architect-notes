import java.util.ArrayList;
import java.util.List;

/**
 * A real (if simplified) in-memory B-Tree, following the standard CLRS
 * insertion algorithm: every node holds between (t-1) and (2t-1) keys, a
 * full node is split before a key is ever inserted into it, and a split
 * root grows the tree's height by one. `nodeWrites` counts every node
 * that actually gets its on-disk contents rewritten as a proxy for real
 * disk I/O: a leaf's key insertion is one write, and a split rewrites
 * the original node, creates a new sibling node, and updates the parent
 * node -- three separate node writes for one split.
 */
public class BTree {
    private static class Node {
        List<Integer> keys = new ArrayList<>();
        List<Node> children = new ArrayList<>();
        boolean leaf;

        Node(boolean leaf) {
            this.leaf = leaf;
        }
    }

    private final int t; // minimum degree; max keys per node = 2t - 1
    private Node root;
    private long nodeWrites = 0;

    public BTree(int minimumDegree) {
        this.t = minimumDegree;
        this.root = new Node(true);
    }

    public long nodeWrites() {
        return nodeWrites;
    }

    public void insert(int key) {
        if (root.keys.size() == 2 * t - 1) {
            Node newRoot = new Node(false);
            newRoot.children.add(root);
            splitChild(newRoot, 0);
            nodeWrites++; // writing the newly created root node
            root = newRoot;
        }
        insertNonFull(root, key);
    }

    private void insertNonFull(Node node, int key) {
        int i = node.keys.size() - 1;
        if (node.leaf) {
            node.keys.add(0); // placeholder, will be overwritten below
            while (i >= 0 && key < node.keys.get(i)) {
                node.keys.set(i + 1, node.keys.get(i));
                i--;
            }
            node.keys.set(i + 1, key);
            nodeWrites++; // this leaf's contents were rewritten
        } else {
            while (i >= 0 && key < node.keys.get(i)) {
                i--;
            }
            i++;
            if (node.children.get(i).keys.size() == 2 * t - 1) {
                splitChild(node, i);
                if (key > node.keys.get(i)) {
                    i++;
                }
            }
            insertNonFull(node.children.get(i), key);
        }
    }

    /**
     * Splits the full child at index i of the given parent into two
     * nodes, promoting the median key up into the parent. Counts three
     * node writes: the original child (now holding only its left half),
     * the newly created right-half sibling, and the parent (which
     * gained the promoted key and a new child pointer).
     */
    private void splitChild(Node parent, int i) {
        Node fullChild = parent.children.get(i);
        Node newChild = new Node(fullChild.leaf);

        int median = fullChild.keys.get(t - 1);

        for (int j = t; j < 2 * t - 1; j++) {
            newChild.keys.add(fullChild.keys.get(j));
        }
        if (!fullChild.leaf) {
            for (int j = t; j < 2 * t; j++) {
                newChild.children.add(fullChild.children.get(j));
            }
            for (int j = 2 * t - 1; j > t - 1; j--) {
                fullChild.children.remove(j);
            }
        }
        for (int j = 2 * t - 2; j >= t - 1; j--) {
            fullChild.keys.remove(j);
        }

        parent.children.add(i + 1, newChild);
        parent.keys.add(i, median);

        nodeWrites += 3; // fullChild rewritten, newChild created, parent updated
    }

    /** Returns the number of node reads needed to find (or fail to find) a key. */
    public int lookupCost(int key) {
        return lookupCost(root, key);
    }

    private int lookupCost(Node node, int key) {
        int reads = 1; // reading this node counts as one disk read
        int i = 0;
        while (i < node.keys.size() && key > node.keys.get(i)) {
            i++;
        }
        if (i < node.keys.size() && key == node.keys.get(i)) {
            return reads;
        }
        if (node.leaf) {
            return reads;
        }
        return reads + lookupCost(node.children.get(i), key);
    }
}
