/**
 * Stands in for a shared, evolving piece of business configuration -- the
 * current tax rate finance can update at any time. Nothing about this class
 * is wrong on its own; the danger is entirely in who is allowed to depend
 * on "the current value" versus who needs a frozen, permanent answer.
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
