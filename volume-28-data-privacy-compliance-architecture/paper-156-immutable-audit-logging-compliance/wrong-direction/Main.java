import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * WRONG DIRECTION: the fix for pressure's "an entry can just be deleted with no trace" problem is
 * to give every entry a hash, computed from its own content, stored alongside it -- so tampering
 * can supposedly be detected by recomputing the hash and checking it still matches. That's real
 * progress in one narrow sense: if someone edits an entry's content but forgets to also update its
 * stored hash, the mismatch is now detectable. But each entry's hash is computed only from that
 * entry's own fields, with no reference to any other entry. An attacker (or an insider) who can
 * edit an entry can just as easily recompute and overwrite its stored hash to match the edited
 * content, and a per-entry hash check that only asks "does this entry's hash match this entry's
 * content" can't tell the difference between an entry that was always this way and one that was
 * quietly edited and re-hashed to match. The check passes either way, so it proves nothing about
 * whether the entry was ever tampered with -- only that it's internally self-consistent right now.
 */
public class Main {
    public static void main(String[] args) throws NoSuchAlgorithmException {
        AuditLog log = new AuditLog();

        log.append("alice", "VIEW_SSN", "customer-401");
        log.append("bob", "VIEW_SSN", "customer-402");
        log.append("carol", "VIEW_SSN", "customer-401");

        System.out.println("Audit log after three accesses, each with a self-computed hash:");
        printLog(log);

        System.out.println();
        System.out.println("Verification pass on the untampered log:");
        verify(log);

        System.out.println();
        System.out.println("Carol's access is edited in place to say 'bob' instead, and her entry's "
                + "stored hash is recomputed and overwritten to match the edited content:");
        log.tamperAndRehash(2, "bob");
        printLog(log);

        System.out.println();
        System.out.println("Verification pass on the tampered-and-rehashed log:");
        verify(log);

        System.out.println();
        System.out.println("Result: every entry's hash still matches its own content, because the "
                + "hash was recomputed from the edited content -- the same way it would have been "
                + "computed if the entry had genuinely said 'bob' from the start. The verification "
                + "pass reports the log as fully valid. A per-entry hash proves an entry is "
                + "internally consistent with itself; it proves nothing about whether that entry "
                + "is the same one that was originally written.");
    }

    private static void printLog(AuditLog log) {
        List<AuditEntry> entries = log.entries();
        for (int i = 0; i < entries.size(); i++) {
            AuditEntry e = entries.get(i);
            System.out.println("  [" + i + "] actor=" + e.actor + " action=" + e.action
                    + " subject=" + e.subject + " storedHash=" + e.storedHash.substring(0, 12) + "...");
        }
    }

    private static void verify(AuditLog log) throws NoSuchAlgorithmException {
        List<AuditEntry> entries = log.entries();
        boolean allValid = true;
        for (int i = 0; i < entries.size(); i++) {
            AuditEntry e = entries.get(i);
            String recomputed = AuditLog.hashOf(e.actor, e.action, e.subject);
            boolean matches = recomputed.equals(e.storedHash);
            if (!matches) {
                allValid = false;
            }
            System.out.println("  [" + i + "] hash matches stored value: " + matches);
        }
        System.out.println("  Overall: " + (allValid ? "VALID -- no tampering detected" : "TAMPERING DETECTED"));
    }

    static class AuditEntry {
        String actor;
        String action;
        String subject;
        String storedHash;

        AuditEntry(String actor, String action, String subject, String storedHash) {
            this.actor = actor;
            this.action = action;
            this.subject = subject;
            this.storedHash = storedHash;
        }
    }

    static class AuditLog {
        private final List<AuditEntry> entries = new ArrayList<>();

        void append(String actor, String action, String subject) throws NoSuchAlgorithmException {
            String hash = hashOf(actor, action, subject);
            entries.add(new AuditEntry(actor, action, subject, hash));
        }

        void tamperAndRehash(int index, String newActor) throws NoSuchAlgorithmException {
            AuditEntry e = entries.get(index);
            e.actor = newActor;
            e.storedHash = hashOf(e.actor, e.action, e.subject);
        }

        List<AuditEntry> entries() {
            return entries;
        }

        static String hashOf(String actor, String action, String subject) throws NoSuchAlgorithmException {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(canonicalize(actor, action, subject).getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }

        // Length-prefixes each field before concatenating, so fields containing arbitrary
        // characters can't be shifted across a field boundary to produce the same hash input from
        // different underlying data.
        static String canonicalize(String... fields) {
            StringBuilder sb = new StringBuilder();
            for (String field : fields) {
                sb.append(field.length()).append(':').append(field);
            }
            return sb.toString();
        }
    }
}
