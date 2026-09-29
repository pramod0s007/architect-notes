/**
 * PRINCIPLE APPLIED: one shared endpoint for every tenant. The existing itemCount field's meaning
 * is never repurposed -- it stays "total units" permanently. The packaging feature is exposed as
 * a brand-new, additive field, packageCount, alongside it. A tenant whose response parser
 * tolerates unrecognized fields reads only itemCount and is unaffected by the addition; a tenant
 * that wants package-level data reads the new field. Adding a further field later touches this
 * one shared endpoint definition, regardless of how many tenants Nova has onboarded.
 */
public class Main {
    static final int UNITS_PER_PACKAGE = 12;
    static final double PRICE_PER_UNIT_DOLLARS = 5.00;

    public static void main(String[] args) {
        int totalUnits = 120;
        int packageCount = totalUnits / UNITS_PER_PACKAGE;

        System.out.println("-- Nova order API, one shared endpoint, additive-only field evolution --");
        System.out.println("Order: " + totalUnits + " total units, " + packageCount + " packages of "
                + UNITS_PER_PACKAGE + " units each.");
        System.out.println();

        System.out.println("Shared endpoint response fields:");
        System.out.println("  itemCount = " + totalUnits + " (meaning: total units, unchanged since before the");
        System.out.println("              packaging feature existed)");
        System.out.println("  packageCount = " + packageCount + " (new, additive field -- only appears once the");
        System.out.println("                 packaging feature ships, itemCount is untouched by its addition)");
        System.out.println();

        System.out.println("Tenant-A's billing client reads only itemCount, exactly as before the packaging");
        System.out.println("feature shipped:");
        double billingTenantA = totalUnits * PRICE_PER_UNIT_DOLLARS;
        System.out.printf("  itemCount * $%.2f/unit = $%.2f -- unchanged, correct%n", PRICE_PER_UNIT_DOLLARS,
                billingTenantA);
        System.out.println();

        System.out.println("A tenant that wants package-level display reads the new packageCount field:");
        System.out.println("  packageCount = " + packageCount + " -- available without touching itemCount");
        System.out.println();

        int endpointDefinitionCount = 1;
        System.out.println("Adding a further field, weightKg, touches exactly " + endpointDefinitionCount
                + " endpoint definition -- the same one, regardless of how many tenants Nova has onboarded.");
        System.out.println();

        System.out.println("Result: every tenant is served from the same endpoint. A tenant whose response parser");
        System.out.println("tolerates unrecognized fields reads only the field it already understood, at its");
        System.out.println("already-established meaning, and is unaffected by the field being added. Nova's");
        System.out.println("maintenance surface for adding a field stays at " + endpointDefinitionCount
                + " endpoint definition no matter how many of its");
        System.out.println("disclosed minimum of 50 concurrent tenants (Paper 162) are onboarded.");
    }
}
