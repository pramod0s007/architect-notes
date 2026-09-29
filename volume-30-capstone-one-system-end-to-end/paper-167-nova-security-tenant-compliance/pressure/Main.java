/**
 * PRESSURE: Nova's support tool looks up a customer's PII (email, phone) whenever a support
 * agent opens a case, and logs the retrieved value in the audit log exactly as it was retrieved
 * from the customer record -- in full, unredacted, for every lookup, regardless of why the
 * agent is looking, and regardless of which data-residency region the customer's record and the
 * agent are each associated with. Nothing in this call path checks a stated purpose for the
 * lookup, and nothing in this call path checks whether an agent in one region is authorized to
 * access a customer record associated with a different region. A stated purpose is accepted as
 * a parameter but never read for any decision -- the same as a lookup with no purpose at all.
 */
public class Main {

    public static void main(String[] args) {
        CustomerRecord customer = new CustomerRecord(
                "cust-9001", "priya.raman@example.com", "+1-555-0199", "eu-west");

        System.out.println("-- Nova support tool: customer lookup, no purpose check, no residency check --");
        System.out.println();

        // Lookup 1: a legitimate billing dispute lookup by an agent in the same region.
        System.out.println("Lookup 1 -- agent in eu-west, stated purpose: billing_dispute (accepted, not checked by code):");
        supportLookup(customer, "agent-eu-1", "eu-west", "billing_dispute");
        System.out.println();

        // Lookup 2: an agent simply browsing the record with no stated purpose at all.
        System.out.println("Lookup 2 -- agent in eu-west, no stated purpose at all:");
        supportLookup(customer, "agent-eu-2", "eu-west", null);
        System.out.println();

        // Lookup 3: an agent in a different region than the customer's own residency region.
        System.out.println("Lookup 3 -- agent in us-east, customer's own record is eu-west, stated purpose: billing_dispute:");
        supportLookup(customer, "agent-us-1", "us-east", "billing_dispute");
        System.out.println();

        System.out.println("Result: all 3 lookups above wrote the customer's real email and phone number,");
        System.out.println("unredacted, to the audit log -- the same for a stated purpose, no stated");
        System.out.println("purpose at all, and a cross-region access with no residency gate applied.");
    }

    /**
     * Retrieves the customer record and logs it, with no purpose check and no residency check.
     * The purpose parameter is accepted but never read anywhere in this method -- it makes no
     * difference to what this method does, whether it is a recognized value, an unrecognized
     * value, or null.
     */
    static void supportLookup(CustomerRecord customer, String agentId, String agentRegion, String purpose) {
        String logLine = "AUDIT LOG: agent=" + agentId + " agentRegion=" + agentRegion
                + " customer=" + customer.customerId
                + " email=" + customer.email
                + " phone=" + customer.phone
                + " customerRegion=" + customer.residencyRegion;
        System.out.println("  " + logLine);
    }

    /** A modeled customer record with a data-residency region attached. */
    static class CustomerRecord {
        final String customerId;
        final String email;
        final String phone;
        final String residencyRegion;

        CustomerRecord(String customerId, String email, String phone, String residencyRegion) {
            this.customerId = customerId;
            this.email = email;
            this.phone = phone;
            this.residencyRegion = residencyRegion;
        }
    }
}
