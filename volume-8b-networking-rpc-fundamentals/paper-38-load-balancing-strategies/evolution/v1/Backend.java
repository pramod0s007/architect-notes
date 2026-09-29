/** A backend server with a fixed processing capacity (requests it can finish per tick). */
public class Backend {
    final int id;
    final int capacity;
    int assigned;

    Backend(int id, int capacity) {
        this.id = id;
        this.capacity = capacity;
    }

    int ticksToFinish() {
        return (int) Math.ceil((double) assigned / capacity);
    }
}
