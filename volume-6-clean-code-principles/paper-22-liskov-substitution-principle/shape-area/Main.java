import java.util.Arrays;
import java.util.List;

/** Demos the Square-extends-Rectangle LSP bug in billing, then the SquareFixed solution. */
public class Main {

    public static void main(String[] args) {
        BillingCalculator calc = new BillingCalculator();
        double rate = 50.0;

        System.out.println("=== VIOLATION: Square extends Rectangle ===");
        Rectangle rect   = new Rectangle(6, 4);
        Square    square = new Square(5);

        System.out.printf("Billing Rectangle %s → charge=$%.2f%n",
                rect.describe(), calc.calculateFloorCharge(rect, rate));
        System.out.printf("Billing Square    %s → charge=$%.2f%n",
                square.describe(), calc.calculateFloorCharge(square, rate));

        System.out.println("Area test — setWidth(5), setHeight(4), expect area=20:");
        calc.testArea(new Rectangle(1, 1));   // PASS
        calc.testArea(new Square(1));          // FAIL — LSP violated

        System.out.println();
        System.out.println("=== FIX: SquareFixed implements Shape directly ===");
        List<Shape> floorPlans = Arrays.asList(
                new Circle(3.5),
                new Rectangle(8, 5),
                new SquareFixed(6)
        );
        for (Shape s : floorPlans) {
            System.out.printf("  %s → area=%.2f m²  charge=$%.2f%n",
                    s.describe(), s.area(), calc.calculateFloorCharge(s, rate));
        }

        SquareFixed sf = new SquareFixed(5);
        System.out.printf("SquareFixed area check: %s → area=%.0f  PASS%n",
                sf.describe(), sf.area());
    }
}
