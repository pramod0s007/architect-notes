// Identical to the pressure case's Buffer -- still a large backing array,
// still genuinely expensive to allocate. What changes is how many times an
// instance actually gets constructed.
class Buffer {
    final double[] data;

    Buffer(int size) {
        this.data = new double[size];
    }
}
