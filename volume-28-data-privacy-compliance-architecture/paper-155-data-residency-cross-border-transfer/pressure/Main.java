/**
 * PRESSURE: a company runs one central analytics warehouse, in one region (us-east), because that
 * was the simplest way to get a single dashboard across all customers worldwide. Every customer's
 * raw record -- regardless of which country or region that customer is legally associated with --
 * gets copied into that one warehouse so reporting can query it. A customer whose data is subject
 * to a data-residency requirement (their raw personal data must stay within their own region) has
 * their raw record copied into us-east anyway, because the analytics pipeline was never built to
 * ask "where is this customer's data allowed to physically live" -- it only asks "does this
 * customer exist, and do we want them in the dashboard."
 */
public class Main {
    public static void main(String[] args) {
        CustomerRecord[] customers = new CustomerRecord[] {
                new CustomerRecord("customer-301", "EU"),
                new CustomerRecord("customer-302", "US"),
                new CustomerRecord("customer-303", "EU"),
        };

        System.out.println("Nightly job: copy every customer's raw record into the central "
                + "analytics warehouse (region: us-east)");

        for (CustomerRecord customer : customers) {
            System.out.println("Copying raw record for " + customer.customerId
                    + " (residency zone: " + customer.residencyZone + ") into us-east warehouse");
        }

        System.out.println("Result: " + customers.length + " raw records now physically stored in "
                + "us-east, including customer-301 and customer-303, whose residency zone is EU. "
                + "The pipeline never asked where either customer's data was required to stay -- it "
                + "only asked whether the customer belonged in the dashboard, and every customer "
                + "does.");
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
