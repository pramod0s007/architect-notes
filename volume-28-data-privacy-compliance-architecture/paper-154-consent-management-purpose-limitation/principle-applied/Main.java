import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PRINCIPLE APPLIED: consent is tracked per purpose, not as one bit per customer, and the default
 * for any (customer, purpose) pair with no record at all is "no consent" -- not "no objection
 * recorded yet, so proceed" the way wrong-direction's single boolean defaulted. Each explicit
 * consent decision is its own record: which purpose it covers, whether it was actually granted (a
 * record can exist and still record an explicit decline, not just silence), when it takes effect,
 * an optional expiry, and whether it has since been revoked. A grant only counts if all of these
 * hold, evaluated fresh as of the check time: the record says granted=true; its effective date has
 * already passed (a future-dated grant hasn't taken effect yet); it hasn't been revoked as of now
 * (a revocation dated in the future hasn't taken effect yet either); and it hasn't expired as of
 * now. Consent for one purpose (email-marketing) never implies consent for a different purpose
 * (sms-marketing) even for the same customer. This sample keeps one hand-seeded record per
 * (customer, purpose) pair for clarity -- it does not model an append-only event history or how to
 * resolve multiple overlapping records for the same purpose, which a production consent registry
 * needs.
 */
public class Main {
    public static void main(String[] args) {
        String now = "2026-06-01";
        ConsentStore store = new ConsentStore();

        store.add("customer-201", new ConsentRecord("service-notifications", true, "2025-01-10", null, null));
        store.add("customer-202", new ConsentRecord("email-marketing", true, "2025-03-01", null, null));
        store.add("customer-205", new ConsentRecord("email-marketing", true, "2024-01-01", "2026-01-01", null));
        store.add("customer-206", new ConsentRecord("email-marketing", true, "2025-02-01", null, "2025-09-01"));
        store.add("customer-207", new ConsentRecord("email-marketing", true, "2025-04-01", null, null));
        store.add("customer-207", new ConsentRecord("sms-marketing", true, "2025-04-01", null, null));
        store.add("customer-208", new ConsentRecord("email-marketing", false, "2025-05-01", null, null));
        store.add("customer-209", new ConsentRecord("email-marketing", true, "2026-09-01", null, null));

        String[] customerIds = new String[] {
                "customer-201", "customer-202", "customer-205", "customer-206", "customer-207",
                "customer-208", "customer-209"
        };

        System.out.println("-- Campaign A: email-marketing --");
        runCampaign(store, customerIds, "email-marketing", now);

        System.out.println();
        System.out.println("-- Campaign B: sms-marketing --");
        runCampaign(store, customerIds, "sms-marketing", now);
    }

    private static void runCampaign(ConsentStore store, String[] customerIds, String purpose, String now) {
        int sent = 0;
        for (String customerId : customerIds) {
            ConsentDecision decision = store.checkConsent(customerId, purpose, now);
            if (decision.allowed) {
                System.out.println("Sending to " + customerId + " for purpose=" + purpose
                        + " -- " + decision.reason);
                sent++;
            } else {
                System.out.println("Skipping " + customerId + " for purpose=" + purpose
                        + " -- " + decision.reason);
            }
        }
        System.out.println("Campaign for purpose=" + purpose + " reached " + sent + " of "
                + customerIds.length + " customers.");
    }

    static class ConsentDecision {
        boolean allowed;
        String reason;

        ConsentDecision(boolean allowed, String reason) {
            this.allowed = allowed;
            this.reason = reason;
        }
    }

    static class ConsentStore {
        private final Map<String, List<ConsentRecord>> byCustomer = new HashMap<>();

        void add(String customerId, ConsentRecord record) {
            byCustomer.computeIfAbsent(customerId, k -> new ArrayList<>()).add(record);
        }

        ConsentDecision checkConsent(String customerId, String purpose, String now) {
            List<ConsentRecord> records = byCustomer.get(customerId);
            ConsentRecord match = null;
            if (records != null) {
                for (ConsentRecord record : records) {
                    if (record.purpose.equals(purpose)) {
                        match = record;
                        break;
                    }
                }
            }

            if (match == null) {
                return new ConsentDecision(false, "no consent record for purpose=" + purpose
                        + " -- default is no consent, not implied consent");
            }
            if (!match.granted) {
                return new ConsentDecision(false, "consent for purpose=" + purpose
                        + " was explicitly recorded as declined on " + match.grantedAt);
            }
            if (now.compareTo(match.grantedAt) < 0) {
                return new ConsentDecision(false, "consent for purpose=" + purpose
                        + " is dated " + match.grantedAt + " and has not taken effect yet (now=" + now + ")");
            }
            if (match.revokedAt != null && now.compareTo(match.revokedAt) >= 0) {
                return new ConsentDecision(false, "consent for purpose=" + purpose
                        + " was granted on " + match.grantedAt + " but revoked on " + match.revokedAt
                        + ", effective as of now=" + now);
            }
            if (match.expiresAt != null && now.compareTo(match.expiresAt) > 0) {
                return new ConsentDecision(false, "consent for purpose=" + purpose
                        + " expired on " + match.expiresAt + " (now=" + now + ")");
            }
            return new ConsentDecision(true, "active consent for purpose=" + purpose
                    + " granted on " + match.grantedAt);
        }
    }

    static class ConsentRecord {
        String purpose;
        boolean granted;
        String grantedAt;
        String expiresAt;
        String revokedAt;

        ConsentRecord(String purpose, boolean granted, String grantedAt, String expiresAt, String revokedAt) {
            this.purpose = purpose;
            this.granted = granted;
            this.grantedAt = grantedAt;
            this.expiresAt = expiresAt;
            this.revokedAt = revokedAt;
        }
    }
}
