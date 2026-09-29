/**
 * PRESSURE: a marketing team wants to send a promotional email about a new product to "all our
 * customers." The only signal the system has about a customer is whether they have an account at
 * all -- there is no record of what the customer actually agreed to when they signed up. A
 * customer who created an account purely to complete a support ticket (and was never asked about,
 * let alone agreed to, marketing contact) is indistinguishable, in this system, from a customer who
 * explicitly opted in to promotional email. "Has an account" is being treated as "consented to
 * being contacted for any purpose," because that's the only bit of information the system tracks --
 * not because anyone decided that was an acceptable substitute for actual consent.
 */
public class Main {
    public static void main(String[] args) {
        Customer[] customers = new Customer[] {
                new Customer("customer-201", "created an account to complete a support ticket"),
                new Customer("customer-202", "created an account and explicitly opted in to promotional email"),
                new Customer("customer-203", "created an account during a free trial signup"),
        };

        System.out.println("Marketing team runs campaign: \"send promo email to all our customers\"");

        int emailsSent = 0;
        for (Customer customer : customers) {
            System.out.println("Sending promo email to " + customer.customerId
                    + " (account history: " + customer.accountHistory + ")");
            emailsSent++;
        }

        System.out.println("Result: " + emailsSent + " promotional emails sent to " + customers.length
                + " customers, including customer-201, who only ever agreed to account creation for "
                + "a support ticket and was never asked about marketing contact at all. The system has "
                + "no record of what any customer actually consented to -- 'has an account' was the "
                + "only signal it ever tracked, and it was reused here as if it meant 'consented to "
                + "marketing.'");
    }

    static class Customer {
        String customerId;
        String accountHistory;

        Customer(String customerId, String accountHistory) {
            this.customerId = customerId;
            this.accountHistory = accountHistory;
        }
    }
}
