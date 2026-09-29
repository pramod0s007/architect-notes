/**
 * WRONG DIRECTION: the fix for pressure's "PII sits in plain text where anyone can see it" bug is
 * encryption at rest -- the SSN and card number are now stored as a simulated encrypted
 * representation in the database, so anyone who dumps the raw database file, a backup, or a
 * replication stream sees that representation, not plaintext -- provided the encryption keys and
 * anyone capable of decrypting are kept separate from that storage layer. Encryption at rest, done
 * with real key management, is meaningful protection against exactly that threat. But it solves a
 * different problem than the one the pressure scenario actually exposed: the support engineer
 * never reads the raw database file. They read the application's rendered screen, and the
 * application still decrypts both fields in full and displays them to any authenticated caller,
 * with no check at all on whether this caller's role or this ticket's purpose has anything to do
 * with needing the decrypted value. Encryption at rest protects the data from someone with
 * database access; it does nothing to protect the data from someone with ordinary application
 * access at the wrong permission level -- which is exactly who was looking at the screen in the
 * pressure scenario, and still is here.
 */
public class Main {
    public static void main(String[] args) {
        String supportEngineer = "support-agent-devi";
        String supportEngineerRole = "billing-support";
        String ticketReason = "billing question about a duplicate charge";

        System.out.println("Support engineer " + supportEngineer + " (role: " + supportEngineerRole
                + ") opens customer-4471's record to handle: " + ticketReason);

        EncryptedCustomerRecord record = new EncryptedCustomerRecord(
                "customer-4471", "ENC(984-12-3456)", "ENC(4111111111111111)");

        System.out.println("Stored at rest: SSN=" + record.encryptedSsn
                + ", card=" + record.encryptedCardNumber
                + " (simulated encrypted representation -- protects the database itself, assuming keys are managed separately)");

        String decryptedSsn = decryptForDisplay(record.encryptedSsn);
        String decryptedCard = decryptForDisplay(record.encryptedCardNumber);

        System.out.println("Screen renders decrypted values with no role check on the decrypt-for-display path:");
        System.out.println("  SSN: " + decryptedSsn);
        System.out.println("  Card number: " + decryptedCard);

        System.out.println("Result: " + supportEngineer + " still saw the full SSN and full card "
                + "number despite holding only the " + supportEngineerRole + " role, because "
                + "decryption for display was never conditioned on the caller's role at all -- "
                + "encryption at rest was real progress against a database or backup leak, and "
                + "zero progress against this exact exposure.");
    }

    private static String decryptForDisplay(String encryptedValue) {
        return encryptedValue.substring(4, encryptedValue.length() - 1);
    }

    static class EncryptedCustomerRecord {
        String customerId;
        String encryptedSsn;
        String encryptedCardNumber;

        EncryptedCustomerRecord(String customerId, String encryptedSsn, String encryptedCardNumber) {
            this.customerId = customerId;
            this.encryptedSsn = encryptedSsn;
            this.encryptedCardNumber = encryptedCardNumber;
        }
    }
}
