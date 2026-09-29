import java.util.HashMap;
import java.util.Map;

/**
 * PRINCIPLE APPLIED: PII is masked by default and full values are gated behind an explicit,
 * checked permission -- not behind "are you an authenticated user of this application" the way
 * the wrong-direction scenario left it. The masked view is precomputed and stored separately from
 * the sensitive full values, so rendering the masked default never touches the encrypted SSN or
 * the card token at all -- masking is not "decrypt then hide most of it," it is "never decrypt
 * unless reveal is both requested and authorized." The card number is additionally tokenized, not
 * just encrypted: the record holds an opaque token, and only an authorized reveal calls out to a
 * separate token vault to resolve the token back to the real PAN -- a second boundary beyond
 * decryption, and the boundary PCI-DSS scope reduction for card data actually depends on.
 * Revealing either full value requires the caller to hold a specific "view-full-pii" permission,
 * checked at the moment of the reveal request. This model authorizes strictly by role; it does
 * not model authentication, session validity, or ticket/case-purpose binding -- those are assumed
 * preconditions here, not something this code checks. Every full-value reveal, when granted, is
 * logged with caller, role, ticket id, and which fields were shown; every denied reveal attempt is
 * logged the same way. These are simulated audit events (printed to stdout, not written to a real
 * audit store) -- see the draft's caveats on what a production audit log still needs.
 */
public class Main {
    public static void main(String[] args) {
        CustomerRecord record = new CustomerRecord(
                "customer-4471",
                "ENC(984-12-3456)", "***-**-3456",
                "TOK-CARD-7e21", "************1111");

        TokenVault vault = new TokenVault();
        vault.register("TOK-CARD-7e21", "4111111111111111");

        System.out.println("-- Case 1: billing-support engineer, billing ticket --");
        handleViewRequest(record, vault, "support-agent-devi", "billing-support", "TICKET-8821", false);

        System.out.println();
        System.out.println("-- Case 2: billing-support engineer explicitly requests a full reveal anyway --");
        handleViewRequest(record, vault, "support-agent-devi", "billing-support", "TICKET-8821", true);

        System.out.println();
        System.out.println("-- Case 3: fraud-investigation engineer, fraud case, requests full reveal --");
        handleViewRequest(record, vault, "fraud-agent-omar", "fraud-investigation", "CASE-2201", true);
    }

    private static void handleViewRequest(CustomerRecord record, TokenVault vault, String caller,
            String role, String ticketId, boolean requestsFullReveal) {
        System.out.println(caller + " (role: " + role + ") opens the record. Default view is masked "
                + "(precomputed, no decrypt, no detokenize call made):");
        System.out.println("  SSN: " + record.maskedSsnDisplay);
        System.out.println("  Card number: " + record.maskedCardDisplay);

        if (!requestsFullReveal) {
            System.out.println(caller + " did not request a full reveal -- masked view is sufficient "
                    + "for this ticket, no elevated permission needed, encrypted SSN and card token "
                    + "were never touched.");
            return;
        }

        boolean callerHasViewFullPiiPermission = roleGrantsViewFullPii(role);
        if (!callerHasViewFullPiiPermission) {
            System.out.println(caller + " requested a full reveal but does NOT hold view-full-pii -- "
                    + "request DENIED. Encrypted SSN and card token were never decrypted or resolved.");
            System.out.println("Audit event: outcome=DENIED, caller=" + caller + ", role=" + role
                    + ", ticket=" + ticketId + ", requestedFields=[ssn,cardNumber], "
                    + "timestamp=2026-03-14T10:22:00Z");
            return;
        }

        String decryptedSsn = decryptForDisplay(record.encryptedSsn);
        String decryptedCard = vault.detokenize(record.cardToken);

        System.out.println(caller + " requested a full reveal and holds view-full-pii -- request "
                + "GRANTED. Decrypting SSN and resolving card token now, only because both the "
                + "permission check and the reveal request happened:");
        System.out.println("  SSN: " + decryptedSsn);
        System.out.println("  Card number: " + decryptedCard);
        System.out.println("Audit event: outcome=GRANTED, caller=" + caller + ", role=" + role
                + ", ticket=" + ticketId + ", fieldsShown=[ssn,cardNumber], "
                + "timestamp=2026-03-14T10:22:00Z");
    }

    private static boolean roleGrantsViewFullPii(String role) {
        return role.equals("fraud-investigation");
    }

    private static String decryptForDisplay(String encryptedValue) {
        return encryptedValue.substring(4, encryptedValue.length() - 1);
    }

    /** Simulated token vault: resolves an opaque card token back to the real PAN. In production
     * this is a separate, more tightly access-controlled service, not an in-process map. */
    static class TokenVault {
        private final Map<String, String> tokenToPan = new HashMap<>();

        void register(String token, String pan) {
            tokenToPan.put(token, pan);
        }

        String detokenize(String token) {
            return tokenToPan.get(token);
        }
    }

    static class CustomerRecord {
        String customerId;
        String encryptedSsn;
        String maskedSsnDisplay;
        String cardToken;
        String maskedCardDisplay;

        CustomerRecord(String customerId, String encryptedSsn, String maskedSsnDisplay,
                String cardToken, String maskedCardDisplay) {
            this.customerId = customerId;
            this.encryptedSsn = encryptedSsn;
            this.maskedSsnDisplay = maskedSsnDisplay;
            this.cardToken = cardToken;
            this.maskedCardDisplay = maskedCardDisplay;
        }
    }
}
