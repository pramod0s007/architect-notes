import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * WRONG DIRECTION: the fix for pressure's "everything ends up in one region" bug is region-aware
 * writes -- a customer's record is now written to a primary store in their own residency zone
 * (EU customers to an EU store, US customers to a US store), which is real progress: the primary
 * write path now respects residency. But the nightly analytics job that pulls records into the
 * central us-east warehouse was never told about this change, because it's a separate pipeline
 * built and maintained independently of the write path. It still does exactly what it always did:
 * pull every record from every regional store and copy it into us-east, unconditionally. Fixing
 * where data is written doesn't fix what a completely separate downstream job does with it once
 * it's there.
 */
public class Main {
    public static void main(String[] args) {
        Map<String, List<CustomerRecord>> regionalStores = new HashMap<>();
        regionalStores.put("EU", new ArrayList<>());
        regionalStores.put("US", new ArrayList<>());

        CustomerRecord[] customers = new CustomerRecord[] {
                new CustomerRecord("customer-301", "EU"),
                new CustomerRecord("customer-302", "US"),
                new CustomerRecord("customer-303", "EU"),
        };

        for (CustomerRecord customer : customers) {
            regionalStores.get(customer.residencyZone).add(customer);
            System.out.println("Writing " + customer.customerId + " to the " + customer.residencyZone
                    + " primary store -- residency respected at write time");
        }

        System.out.println();
        System.out.println("Nightly analytics job runs, unaware of the regional write change:");
        int copied = 0;
        String[] zoneOrder = new String[] { "EU", "US" };
        for (String zone : zoneOrder) {
            for (CustomerRecord customer : regionalStores.get(zone)) {
                System.out.println("Copying raw record for " + customer.customerId
                        + " (residency zone: " + customer.residencyZone + ") from " + zone
                        + " primary store into us-east warehouse");
                copied++;
            }
        }

        System.out.println("Result: writes now correctly land in each customer's own residency "
                + "zone, but the analytics job copied all " + copied + " raw records into us-east "
                + "anyway, including customer-301 and customer-303's EU records -- the same "
                + "residency violation as pressure, just introduced by a different, unmodified "
                + "pipeline instead of the write path this fix actually changed.");
    }

    static class CustomerRecord {
        String customerId;
        String residencyZone;

        CustomerRecord(String customerId, String residencyZone) {
            this.customerId = customerId;
            this.residencyZone = residencyZone;
        }
    }
}
