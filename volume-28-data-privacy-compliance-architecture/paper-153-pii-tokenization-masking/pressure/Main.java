/**
 * PRESSURE: a support engineer opens a customer record to help with a billing question. The
 * support tooling was built to show "everything about this customer" on one screen, because that
 * was the simplest way to build it and support engineers asked for "all the context." The customer's
 * full Social Security Number and full payment card number are stored as plain fields on the
 * customer record, so they render in full, in plain text, on that screen -- regardless of whether
 * this particular support engineer's job (answering a billing question) has anything to do with
 * needing the customer's SSN at all. Any authenticated support engineer, for any ticket, sees the
 * full raw value of both fields, and nothing about this access is distinguished from viewing the
 * customer's name or email. (This sample assumes authentication itself already succeeded and
 * focuses on what happens after that -- it is not a model of the authentication step.)
 */
public class Main {
    public static void main(String[] args) {
        String supportEngineer = "support-agent-devi";
        String ticketReason = "billing question about a duplicate charge";

        System.out.println("Support engineer " + supportEngineer + " opens customer-4471's record "
                + "to handle: " + ticketReason);

        CustomerRecord record = new CustomerRecord("customer-4471", "984-12-3456", "4111111111111111");

        System.out.println("Screen renders full customer record:");
        System.out.println("  SSN: " + record.ssn);
        System.out.println("  Card number: " + record.cardNumber);

        System.out.println("Result: " + supportEngineer + " saw the customer's full SSN and full "
                + "card number while handling a billing question that never required either value, "
                + "and no record exists anywhere that this exposure happened.");
    }

    static class CustomerRecord {
        String customerId;
        String ssn;
        String cardNumber;

        CustomerRecord(String customerId, String ssn, String cardNumber) {
            this.customerId = customerId;
            this.ssn = ssn;
            this.cardNumber = cardNumber;
        }
    }
}
