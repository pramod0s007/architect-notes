/**
 * Stands in for a shared, evolving piece of business configuration -- the
 * current tax rate finance can update at any time.
 */
public class TaxPolicy {
    private static double rate = 0.08;

    public static double currentRate() {
        return rate;
    }

    public static void setRate(double newRate) {
        rate = newRate;
    }
}
