// A genuinely expensive object to construct: a large backing array. The JVM
// has to allocate and zero this much memory every time one is created --
// real, measurable work, not a hypothetical.
class Buffer {
    final double[] data;

    Buffer(int size) {
        this.data = new double[size];
    }
}
