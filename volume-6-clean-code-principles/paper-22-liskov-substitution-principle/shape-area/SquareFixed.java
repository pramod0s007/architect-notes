/**
 * SquareFixed — a square that implements Shape directly.
 *
 * No Rectangle inheritance; no overridden setters; no contract surprises.
 * area() is always side * side.  Fully LSP-compliant.
 */
public class SquareFixed implements Shape {

    private final double side;

    public SquareFixed(double side) {
        if (side <= 0) throw new IllegalArgumentException("Side must be positive");
        this.side = side;
    }

    @Override
    public double area() {
        return side * side;
    }

    @Override
    public String describe() {
        return String.format("SquareFixed(side=%.2f)", side);
    }
}
