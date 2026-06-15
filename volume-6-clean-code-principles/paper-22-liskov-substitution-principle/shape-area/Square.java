/**
 * Square — extends Rectangle, but violates LSP.
 *
 * A square's sides must stay equal, so setWidth/setHeight both update
 * both dimensions.  This breaks the Rectangle contract: any code that
 * sets width and height independently and then checks area() will get
 * a wrong answer when handed a Square.
 *
 * // LSP violation: testArea(Rectangle r) fails when r is actually a Square
 */
public class Square extends Rectangle {

    public Square(int side) {
        super(side, side);
    }

    // LSP violation: overrides parent behaviour in a way callers don't expect
    @Override
    public void setWidth(int width) {
        super.setWidth(width);
        super.setHeight(width);   // silently changes height too
    }

    // LSP violation: overrides parent behaviour in a way callers don't expect
    @Override
    public void setHeight(int height) {
        super.setWidth(height);   // silently changes width too
        super.setHeight(height);
    }

    @Override
    public String describe() {
        return String.format("Square(side=%d) [extends Rectangle]", width);
    }
}
