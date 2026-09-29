/**
 * PRINCIPLE-APPLIED: three independent controls compose to close all three gaps the other two
 * scenarios leave open. (1) Tokenization: the customer's real email and phone are never logged
 * directly -- only an opaque token stands in for each value in the audit log, with the real
 * value stored separately in a token vault. This control does not decide whether a lookup
 * proceeds; it applies to whichever lookup does proceed. (2) Purpose gate: a lookup is only
 * performed if the caller supplies one of a fixed set of recognized business purposes; a lookup
 * with no purpose, or an unrecognized one, is refused before any customer data is touched. This
 * checks only whether a supplied purpose string is recognized, not whether that stated purpose
 * is true. (3) Residency gate: a lookup is only performed if the requesting agent's own region
 * matches the customer record's residency region; a cross-region lookup is refused before any
 * PII (email/phone) is touched -- though the residency check itself does read the customer
 * record's non-PII residency-region field in order to make that comparison. Only a lookup that
 * passes both the purpose gate and the residency gate ever reaches the token vault, and even
 * then, the audit log receives only the token, never the underlying real value.
 */
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Main {

    static final Set<String> RECOGNIZED_PURPOSES = Set.of("billing_dispute", "fraud_investigation");

    public static void main(String[] args) {
        CustomerRecord customer = new CustomerRecord(
                "cust-9001", "priya.raman@example.com", "+1-555-0199", "eu-west");
        TokenVault vault = new TokenVault();
        vault.tokenize(customer);

        System.out.println("-- Nova support tool: tokenization + purpose gate + residency gate composed --");
        System.out.println();

        System.out.println("Lookup 1 -- agent in eu-west, purpose: billing_dispute (recognized), region matches:");
        supportLookup(customer, vault, "agent-eu-1", "eu-west", "billing_dispute");
        System.out.println();

        System.out.println("Lookup 2 -- agent in eu-west, purpose: none supplied:");
        supportLookup(customer, vault, "agent-eu-2", "eu-west", null);
        System.out.println();

        System.out.println("Lookup 3 -- agent in us-east, purpose: billing_dispute (recognized), region does not match:");
        supportLookup(customer, vault, "agent-us-1", "us-east", "billing_dispute");
        System.out.println();

        System.out.println("Result: Lookup 1 was allowed through both gates and the audit log received only");
        System.out.println("a token, never the real email or phone. Lookups 2 and 3 were each refused by one");
        System.out.println("of the two gates before the token vault was ever consulted, and neither the real");
        System.out.println("value nor its token appears in either refused lookup's log entry.");
    }

    /**
     * Runs the purpose gate and the residency gate, in that order, before consulting the token
     * vault at all. Only a lookup that passes both gates reaches the vault, and the audit log for
     * an allowed lookup receives the token the vault returns, never the underlying real value.
     */
    static void supportLookup(CustomerRecord customer, TokenVault vault, String agentId,
                               String agentRegion, String purpose) {
        if (purpose == null || !RECOGNIZED_PURPOSES.contains(purpose)) {
            System.out.println("  REFUSED (purpose gate): agent=" + agentId
                    + " purpose=" + purpose + " is not a recognized purpose -- no PII (email/phone) accessed");
            return;
        }
        if (!agentRegion.equals(customer.residencyRegion)) {
            System.out.println("  REFUSED (residency gate): agent=" + agentId + " agentRegion=" + agentRegion
                    + " does not match customerRegion=" + customer.residencyRegion + " -- no PII (email/phone) accessed");
            return;
        }

        String emailToken = vault.tokenFor(customer.customerId, "email");
        String phoneToken = vault.tokenFor(customer.customerId, "phone");
        String logLine = "AUDIT LOG: agent=" + agentId + " agentRegion=" + agentRegion
                + " customer=" + customer.customerId + " purpose=" + purpose
                + " emailToken=" + emailToken + " phoneToken=" + phoneToken;
        System.out.println("  ALLOWED: " + logLine);
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

    /**
     * A minimal token vault: tokenByCustomerField maps a customer-and-field key
     * (customerId + ":" + field) to an opaque token; realValueByToken separately maps that same
     * token to the real value. This program's only read method, tokenFor, looks up and returns
     * the token from tokenByCustomerField, never the real value -- this program contains no
     * method that reads from realValueByToken to convert a token back into the underlying real
     * value, so nothing in this scenario ever performs that conversion.
     */
    static class TokenVault {
        private final Map<String, String> tokenByCustomerField = new HashMap<>();
        private final Map<String, String> realValueByToken = new HashMap<>();
        private int nextTokenId = 1;

        void tokenize(CustomerRecord customer) {
            registerToken(customer.customerId, "email", customer.email);
            registerToken(customer.customerId, "phone", customer.phone);
        }

        private void registerToken(String customerId, String field, String realValue) {
            String key = customerId + ":" + field;
            String token = "TOK-" + String.format("%04d", nextTokenId++);
            tokenByCustomerField.put(key, token);
            realValueByToken.put(token, realValue);
        }

        String tokenFor(String customerId, String field) {
            return tokenByCustomerField.get(customerId + ":" + field);
        }
    }
}
