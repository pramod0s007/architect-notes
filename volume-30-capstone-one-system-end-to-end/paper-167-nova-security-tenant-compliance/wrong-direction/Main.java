/**
 * WRONG-DIRECTION: instead of leaving the lookup entirely unguarded, a masking fix is applied to
 * what the support agent's screen displays -- the email and phone number shown on screen are
 * partially redacted (e.g. "p***@example.com"). This closes the specific gap of an agent's own
 * screen showing the full unredacted value. But the masking is applied only at display time,
 * after the value has already been retrieved -- the underlying audit-log call still receives
 * and writes the customer's real, unredacted email and phone number, exactly as before. Purpose
 * is still not checked, and the agent's region is still not checked against the customer's own
 * residency region. A stated purpose is accepted as a parameter but never read for any decision
 * -- the same as a lookup with no purpose at all.
 */
public class Main {

    public static void main(String[] args) {
        CustomerRecord customer = new CustomerRecord(
                "cust-9001", "priya.raman@example.com", "+1-555-0199", "eu-west");

        System.out.println("-- Nova support tool: display masking added, log call left unchanged --");
        System.out.println();

        System.out.println("Lookup 1 -- agent in eu-west, stated purpose: billing_dispute (accepted, not checked by code):");
        supportLookup(customer, "agent-eu-1", "eu-west", "billing_dispute");
        System.out.println();

        System.out.println("Lookup 2 -- agent in eu-west, no stated purpose at all:");
        supportLookup(customer, "agent-eu-2", "eu-west", null);
        System.out.println();

        System.out.println("Lookup 3 -- agent in us-east, customer's own record is eu-west, stated purpose: billing_dispute:");
        supportLookup(customer, "agent-us-1", "us-east", "billing_dispute");
        System.out.println();

        System.out.println("Result: the agent's own screen now shows a masked value for all 3 lookups, but");
        System.out.println("the audit log entries above still contain the customer's real, unredacted email");
        System.out.println("and phone number -- the masking fix never reached the logging call, and purpose");
        System.out.println("and residency are still not checked at all.");
    }

    /**
     * Retrieves the customer record, masks what's shown on screen, but logs the raw value
     * unchanged. The purpose parameter is accepted but never read anywhere in this method -- it
     * makes no difference to what this method does, whether it is a recognized value, an
     * unrecognized value, or null.
     */
    static void supportLookup(CustomerRecord customer, String agentId, String agentRegion, String purpose) {
        String displayedEmail = maskEmail(customer.email);
        String displayedPhone = maskPhone(customer.phone);
        System.out.println("  Agent screen shows: email=" + displayedEmail + " phone=" + displayedPhone);

        // The masking above only changed what's displayed -- this log call is untouched from the
        // pressure scenario and still writes the real, unredacted values.
        String logLine = "AUDIT LOG: agent=" + agentId + " agentRegion=" + agentRegion
                + " customer=" + customer.customerId
                + " email=" + customer.email
                + " phone=" + customer.phone
                + " customerRegion=" + customer.residencyRegion;
        System.out.println("  " + logLine);
    }

    static String maskEmail(String email) {
        int at = email.indexOf('@');
        if (at <= 1) {
            return email;
        }
        return email.charAt(0) + "***" + email.substring(at);
    }

    static String maskPhone(String phone) {
        if (phone.length() < 4) {
            return phone;
        }
        return "***-" + phone.substring(phone.length() - 4);
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
