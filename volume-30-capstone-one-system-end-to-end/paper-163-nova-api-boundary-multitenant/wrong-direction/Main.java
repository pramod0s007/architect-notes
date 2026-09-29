import java.util.ArrayList;
import java.util.List;

/**
 * WRONG DIRECTION: instead of reusing one field for two meanings, Nova gives each tenant its own
 * dedicated endpoint definition, so each tenant's response shape can mean whatever that tenant
 * needs. Tenant-A's endpoint keeps itemCount as units; a newer tenant's endpoint uses itemCount
 * as packages. Both compute correctly for their own tenant. The cost shows up when a field has
 * to be added to every one of these endpoint definitions, and again when Nova onboards another
 * tenant.
 */
public class Main {
    static final int UNITS_PER_PACKAGE = 12;
    static final double PRICE_PER_UNIT_DOLLARS = 5.00;

    public static void main(String[] args) {
        int totalUnits = 120;
        int packageCount = totalUnits / UNITS_PER_PACKAGE;

        List<String> endpointDefinitions = new ArrayList<>();
        endpointDefinitions.add("tenant-A-orders-endpoint (itemCount = units)");
        endpointDefinitions.add("tenant-B-orders-endpoint (itemCount = packages)");

        System.out.println("-- Nova order API, one dedicated endpoint definition per tenant --");
        System.out.println("Order: " + totalUnits + " total units, " + packageCount + " packages of "
                + UNITS_PER_PACKAGE + " units each.");
        System.out.println();

        int itemCountTenantA = totalUnits;
        double billingTenantA = itemCountTenantA * PRICE_PER_UNIT_DOLLARS;
        System.out.printf("Tenant-A's endpoint: itemCount = %d (units), billing = $%.2f -- correct for tenant-A%n",
                itemCountTenantA, billingTenantA);

        int itemCountTenantB = packageCount;
        System.out.printf("Tenant-B's endpoint: itemCount = %d (packages) -- correct for tenant-B's own use of the field%n",
                itemCountTenantB);
        System.out.println();

        System.out.println("Current endpoint definitions Nova has to keep in sync: " + endpointDefinitions.size());
        for (String e : endpointDefinitions) {
            System.out.println("  - " + e);
        }
        System.out.println();

        System.out.println("Now a new field, weightKg, needs to be added to the order response.");
        System.out.println("It has to be added separately to every one of the " + endpointDefinitions.size()
                + " endpoint definitions above, one at a time.");
        System.out.println();

        System.out.println("Now Nova onboards tenant-C, its own dedicated endpoint definition:");
        endpointDefinitions.add("tenant-C-orders-endpoint (itemCount = units, its own convention)");
        System.out.println("Endpoint definitions to keep in sync: " + endpointDefinitions.size()
                + " (grew by exactly 1, purely from onboarding one more tenant)");
        System.out.println();

        System.out.println("Result: each tenant's own endpoint computes correctly for that tenant, but the");
        System.out.println("number of endpoint definitions Nova has to keep in sync grows by one for every");
        System.out.println("tenant it onboards -- at Nova's own disclosed minimum of 50 concurrent tenants");
        System.out.println("(established in Paper 162), that is 50 separate endpoint definitions to update");
        System.out.println("every time a field like weightKg needs to be added.");
    }
}
