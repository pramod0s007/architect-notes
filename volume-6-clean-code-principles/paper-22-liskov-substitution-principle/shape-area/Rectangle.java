/**
 * Rectangle — mutable shape with independent width and height.
 * setWidth/setHeight let callers adjust dimensions independently.
 * Fully substitutable for Shape.
 */
public class Rectangle implements Shape {

    protected int width;
    protected int height;

    public Rectangle(int width, int height) {
        this.width  = width;
        this.height = height;
    }

    public void setWidth(int width)   { this.width  = width;  }
    public void setHeight(int height) { this.height = height; }

    public int getWidth()  { return width;  }
    public int getHeight() { return height; }

    @Override
    public double area() {
        return (double) width * height;
    }

    @Override
    public String describe() {
        return String.format("Rectangle(w=%d, h=%d)", width, height);
    }
}
