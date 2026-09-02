// A trivially cheap object -- two ints. Allocating one is close to the
// cheapest possible operation the JVM can perform: a bump of the young-gen
// allocation pointer, nothing more.
class SmallObject {
    int x;
    int y;

    void set(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
