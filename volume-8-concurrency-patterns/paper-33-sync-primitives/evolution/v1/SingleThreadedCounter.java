class SingleThreadedCounter {
    private int count = 0;

    void increment() {
        count++;
    }

    int get() {
        return count;
    }
}
