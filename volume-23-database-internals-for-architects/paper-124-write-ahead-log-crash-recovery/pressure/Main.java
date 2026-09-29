/**
 * Demonstrates that with no durability mechanism at all, every
 * acknowledged write is lost the instant the process crashes --
 * regardless of how many writes were acknowledged, or how long ago.
 */
public class Main {
    public static void main(String[] args) {
        Database db = new Database();

        int totalWrites = 50;
        int acknowledged = 0;
        for (int i = 0; i < totalWrites; i++) {
            if (db.put("key-" + i, "value-" + i)) {
                acknowledged++;
            }
        }
        System.out.println("Writes acknowledged: " + acknowledged);

        // Simulate a crash: the process dies, and a fresh instance comes
        // up in its place. Nothing about the old in-memory map survives,
        // because nothing about it was ever written anywhere durable.
        Database afterCrash = new Database();
        System.out.println("Rows recovered after crash: " + afterCrash.size());
    }
}
