/**
 * PRESSURE: Nova's order API has one shared endpoint serving every tenant. A packaging feature
 * ships: orders can now be grouped into fixed-size packages. The existing `itemCount` field is
 * reused to carry the new "package count" concept instead of adding a new field, because it
 * looks like the natural place to put it -- same field name, same endpoint, no schema change
 * anyone has to announce.
 */
public class Main {
    static final int UNITS_PER_PACKAGE = 12;
    static final double PRICE_PER_UNIT_DOLLARS = 5.00;

    public static void main(String[] args) {
        int totalUnits = 120;
        int packageCount = totalUnits / UNITS_PER_PACKAGE;

        System.out.println("-- Nova order API, single endpoint, single field reused for two meanings --");
        System.out.println("Order: " + totalUnits + " total units, packaged as " + packageCount + " packages of "
                + UNITS_PER_PACKAGE + " units each.");
        System.out.println();

        System.out.println("Before the packaging feature ships:");
        int itemCountBefore = totalUnits;
        double billingBefore = itemCountBefore * PRICE_PER_UNIT_DOLLARS;
        System.out.println("  API response field itemCount = " + itemCountBefore + " (meaning: total units)");
        System.out.printf("  Tenant-A billing client: itemCount * $%.2f/unit = $%.2f%n",
                PRICE_PER_UNIT_DOLLARS, billingBefore);
        System.out.println();

        System.out.println("After the packaging feature ships (same endpoint, same field name, no version change):");
        int itemCountAfter = packageCount;
        double billingAfter = itemCountAfter * PRICE_PER_UNIT_DOLLARS;
        System.out.println("  API response field itemCount = " + itemCountAfter + " (meaning: total packages)");
        System.out.println("  Tenant-A's billing client code is unchanged -- it still reads itemCount and");
        System.out.println("  still multiplies by the per-unit price, because nothing told it the field's");
        System.out.println("  meaning changed.");
        System.out.printf("  Tenant-A billing client: itemCount * $%.2f/unit = $%.2f%n",
                PRICE_PER_UNIT_DOLLARS, billingAfter);
        System.out.println();

        double shortfall = billingBefore - billingAfter;
        System.out.println("Result: no request failed, no field was missing, no exception was thrown.");
        System.out.printf("Tenant-A's billing dropped from $%.2f to $%.2f for the exact same order --%n",
                billingBefore, billingAfter);
        System.out.printf("a $%.2f shortfall, entirely because itemCount now means packages instead of%n", shortfall);
        System.out.println("units, and the same field name and the same endpoint gave no signal that its");
        System.out.println("meaning had changed.");
    }
}
