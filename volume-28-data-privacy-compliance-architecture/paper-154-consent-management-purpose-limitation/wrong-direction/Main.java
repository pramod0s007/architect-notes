/**
 * WRONG DIRECTION: the fix for pressure's "no consent tracking at all" bug is a single boolean,
 * marketingOptOut, checked before sending. This is real progress over pressure -- a customer who
 * has explicitly asked not to be contacted is now honored. But the flag defaults to false (not
 * opted out) for every customer, including customers who were never asked the question at all --
 * silence is still being treated as consent, just phrased as "hasn't said no" instead of "has an
 * account." And the flag is a single bit covering every marketing channel and purpose at once:
 * a customer who agreed to product-update emails but never agreed to promotional SMS, or who wants
 * email for one product line but not another, has no way to express that distinction -- the system
 * can only say yes-to-everything or no-to-everything, per customer, for all of marketing.
 */
public class Main {
    public static void main(String[] args) {
        Customer[] customers = new Customer[] {
                new Customer("customer-201", "created an account to complete a support ticket", false),
                new Customer("customer-202", "explicitly opted in to promotional email", false),
                new Customer("customer-204", "explicitly opted out of all marketing contact", true),
        };

        System.out.println("Marketing team runs campaign: \"send promo email to all customers who "
                + "haven't opted out\"");

        int emailsSent = 0;
        for (Customer customer : customers) {
            if (customer.marketingOptOut) {
                System.out.println("Skipping " + customer.customerId + " -- marketingOptOut=true");
                continue;
            }
            System.out.println("Sending promo email to " + customer.customerId
                    + " (marketingOptOut=false; history: " + customer.accountHistory + ")");
            emailsSent++;
        }

        System.out.println("Result: " + emailsSent + " promotional emails sent. customer-204, who "
                + "explicitly opted out, was correctly skipped -- real progress over pressure. But "
                + "customer-201, who only ever created an account for a support ticket and was never "
                + "asked about marketing at all, still received the email, because marketingOptOut "
                + "defaults to false for anyone who hasn't explicitly said no -- silence is still "
                + "consent, just one negation removed from pressure's version of the same assumption. "
                + "And there is no way, with a single boolean, to have opted in to one marketing "
                + "channel or purpose while remaining un-consented for another.");
    }

    static class Customer {
        String customerId;
        String accountHistory;
        boolean marketingOptOut;

        Customer(String customerId, String accountHistory, boolean marketingOptOut) {
            this.customerId = customerId;
            this.accountHistory = accountHistory;
            this.marketingOptOut = marketingOptOut;
        }
    }
}
