/**
 * BillingCalculator — charges tenants based on floor-plan area.
 *
 * The method calculateFloorCharge() is written entirely against the
 * Shape interface — it should work for any Shape substitutor.
 * The inner testArea() helper exposes the LSP violation when a Square
 * is passed in place of a Rectangle.
 */
public class BillingCalculator {

    private static final double GST_RATE = 0.10;

    /** Calculate the monthly floor charge including tax. */
    public double calculateFloorCharge(Shape shape, double ratePerSqMeter) {
        double base = shape.area() * ratePerSqMeter;
        return base + base * GST_RATE;
    }

    /**
     * Demonstrates the LSP violation: a caller that mutates a Rectangle's
     * dimensions to verify area() will get a wrong result for a Square.
     *
     * Expected: w=5, h=4  →  area = 20
     * Actual with Square:  setWidth(5) also sets height to 5 → area = 25
     */
    public void testArea(Rectangle r) {
        r.setWidth(5);
        r.setHeight(4);
        double expected = 20.0;
        double actual   = r.area();
        System.out.printf("  %s → area=%.0f  (expected=%.0f)  %s%n",
                r.describe(), actual, expected,
                actual == expected ? "PASS" : "FAIL — LSP violated!");
    }
}
