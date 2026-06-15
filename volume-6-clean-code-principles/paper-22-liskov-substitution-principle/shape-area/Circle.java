/**
 * Circle — immutable shape backed by a radius.
 * Fully substitutable for Shape; area() is always consistent.
 */
public class Circle implements Shape {

    private final double radius;

    public Circle(double radius) {
        if (radius <= 0) throw new IllegalArgumentException("Radius must be positive");
        this.radius = radius;
    }

    @Override
    public double area() {
        return Math.PI * radius * radius;
    }

    @Override
    public String describe() {
        return String.format("Circle(radius=%.2f)", radius);
    }
}
