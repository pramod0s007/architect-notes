import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * PRINCIPLE APPLIED: every audit entry's hash is computed from both its own content and the
 * previous entry's hash, chaining every entry to the one before it. The only state-mutating
 * operation the production API exposes is append -- no update or delete method exists for normal
 * application code to call. currentHead(), size(), verify(), and verifyAgainstAnchor() are
 * read-only. copy() and two other methods below are clearly named and commented as
 * demonstration-only, existing solely to let this sample clone a chain and then simulate a
 * storage-level attacker (a direct disk edit, a rogue database admin); none of the three should
 * exist in real production code. Chain-only verification walks every entry from the start,
 * recomputing each entry's expected hash from its stored previous-hash and content, and confirming
 * that stored previous-hash actually matches the prior entry's real hash.
 * This catches an interior content edit (the edited entry's hash no longer matches its content)
 * and an interior deletion (the entry after the gap no longer links to anything actually present).
 * It does NOT catch two other attacks on its own: deleting the chain's current tail (the
 * remaining entries are still perfectly self-consistent with each other), or editing an entry and
 * then recomputing every hash after it to match (a full suffix rewrite -- also self-consistent).
 * Both of those require comparing the chain against an externally anchored checkpoint -- a
 * (entry count, head hash) pair recorded somewhere the attacker doesn't control at the time it was
 * recorded -- which this sample also demonstrates in its final two cases. That check, as built
 * here, verifies the chain against one specific, previously recorded snapshot -- it is an
 * exact-match comparison, not a protocol for an evolving log where new, legitimate entries keep
 * being appended after the checkpoint was taken. A real system needs the checkpoint verification
 * to advance (re-anchor at the new tail after confirming the old checkpoint's prefix is still
 * intact) each time it re-publishes a checkpoint, which this sample does not build.
 */
public class Main {
    private static final String GENESIS_HASH = "0".repeat(64);

    public static void main(String[] args) throws NoSuchAlgorithmException {
        AuditChain chain = new AuditChain();
        chain.append("alice", "VIEW_SSN", "customer-401");
        chain.append("bob", "VIEW_SSN", "customer-402");
        chain.append("carol", "VIEW_SSN", "customer-401");

        String anchoredHead = chain.currentHead();
        int anchoredCount = chain.size();

        System.out.println("-- Case 1: verify the untampered chain --");
        chain.verify();

        System.out.println();
        System.out.println("-- Case 2: an entry's content is edited directly in storage, hash left untouched --");
        AuditChain tamperedContent = chain.copy();
        tamperedContent.simulateStorageLevelContentEditForDemo(2, "bob");
        tamperedContent.verify();

        System.out.println();
        System.out.println("-- Case 3: an interior entry is removed directly from storage --");
        AuditChain tamperedInteriorDeletion = chain.copy();
        tamperedInteriorDeletion.simulateStorageLevelDeletionForDemo(1);
        tamperedInteriorDeletion.verify();

        System.out.println();
        System.out.println("-- Case 4: the chain's current tail entry is removed directly from storage --");
        AuditChain tamperedTailDeletion = chain.copy();
        tamperedTailDeletion.simulateStorageLevelDeletionForDemo(2);
        System.out.println("  Chain-only verify (no external checkpoint):");
        tamperedTailDeletion.verify();

        System.out.println();
        System.out.println("-- Case 5: same tail-deleted chain, now checked against the externally anchored checkpoint --");
        System.out.println("  Anchored verify (checkpoint recorded before the tampering):");
        tamperedTailDeletion.verifyAgainstAnchor(anchoredHead, anchoredCount);
    }

    static class AuditEntry {
        String actor;
        String action;
        String subject;
        String previousHash;
        String hash;

        AuditEntry(String actor, String action, String subject, String previousHash, String hash) {
            this.actor = actor;
            this.action = action;
            this.subject = subject;
            this.previousHash = previousHash;
            this.hash = hash;
        }

        AuditEntry copy() {
            return new AuditEntry(actor, action, subject, previousHash, hash);
        }
    }

    static class AuditChain {
        private final List<AuditEntry> entries = new ArrayList<>();

        void append(String actor, String action, String subject) throws NoSuchAlgorithmException {
            String previousHash = entries.isEmpty() ? GENESIS_HASH : entries.get(entries.size() - 1).hash;
            String hash = hashOf(previousHash, actor, action, subject);
            entries.add(new AuditEntry(actor, action, subject, previousHash, hash));
        }

        String currentHead() {
            return entries.isEmpty() ? GENESIS_HASH : entries.get(entries.size() - 1).hash;
        }

        int size() {
            return entries.size();
        }

        // DEMONSTRATION ONLY -- not part of the production API. Cloning a chain by value like this
        // exists solely so this sample can tamper with a copy while leaving the original intact
        // for comparison; a real AuditChain has no reason to expose a bulk copy operation.
        AuditChain copy() {
            AuditChain copy = new AuditChain();
            for (AuditEntry e : entries) {
                copy.entries.add(e.copy());
            }
            return copy;
        }

        // DEMONSTRATION ONLY -- not part of the production API. These two methods exist solely to
        // simulate what happens if something below the application layer (a direct storage edit, a
        // compromised disk, a rogue database admin) alters the underlying data. A real AuditChain
        // must not expose anything like these.
        void simulateStorageLevelContentEditForDemo(int index, String newActor) {
            entries.get(index).actor = newActor;
        }

        void simulateStorageLevelDeletionForDemo(int index) {
            entries.remove(index);
        }

        void verify() throws NoSuchAlgorithmException {
            String expectedPreviousHash = GENESIS_HASH;
            for (int i = 0; i < entries.size(); i++) {
                AuditEntry e = entries.get(i);
                if (!e.previousHash.equals(expectedPreviousHash)) {
                    System.out.println("  INVALID at entry " + i + ": stored previous-hash does not "
                            + "match the actual previous entry's hash -- an entry was removed or "
                            + "reordered between entry " + (i - 1) + " and entry " + i);
                    return;
                }
                String recomputedHash = hashOf(e.previousHash, e.actor, e.action, e.subject);
                if (!recomputedHash.equals(e.hash)) {
                    System.out.println("  INVALID at entry " + i + ": recomputed hash does not match "
                            + "the stored hash -- this entry's content was altered after it was "
                            + "written");
                    return;
                }
                expectedPreviousHash = e.hash;
            }
            System.out.println("  VALID: all " + entries.size() + " entries are chained correctly, "
                    + "no tampering detected");
        }

        // Verifies against one specific, previously recorded checkpoint (an exact entry-count and
        // head-hash match). This is a snapshot comparison, not an ongoing protocol for a log that
        // keeps growing after the checkpoint was taken -- calling this against a chain that has had
        // legitimate new entries appended since the checkpoint will also report INVALID, because
        // it has no way to distinguish "grew legitimately" from "tail was rewritten." A real system
        // needs to re-anchor (publish a new checkpoint) each time it accepts new entries as
        // legitimate, and verify new entries against the most recent checkpoint plus a check that
        // the checkpoint's own prefix is still intact.
        void verifyAgainstAnchor(String expectedHeadHash, int expectedCount) throws NoSuchAlgorithmException {
            String expectedPreviousHash = GENESIS_HASH;
            for (int i = 0; i < entries.size(); i++) {
                AuditEntry e = entries.get(i);
                if (!e.previousHash.equals(expectedPreviousHash)) {
                    System.out.println("  INVALID at entry " + i + ": stored previous-hash does not "
                            + "match the actual previous entry's hash -- an entry was removed or "
                            + "reordered between entry " + (i - 1) + " and entry " + i);
                    return;
                }
                String recomputedHash = hashOf(e.previousHash, e.actor, e.action, e.subject);
                if (!recomputedHash.equals(e.hash)) {
                    System.out.println("  INVALID at entry " + i + ": recomputed hash does not match "
                            + "the stored hash -- this entry's content was altered after it was "
                            + "written");
                    return;
                }
                expectedPreviousHash = e.hash;
            }
            if (entries.size() != expectedCount || !expectedPreviousHash.equals(expectedHeadHash)) {
                System.out.println("  INVALID: the chain is internally self-consistent, but its "
                        + "current length and head hash do not match the externally anchored "
                        + "checkpoint (expected " + expectedCount + " entries) -- entries were "
                        + "removed from the end, or the tail was rewritten, after the checkpoint "
                        + "was recorded");
                return;
            }
            System.out.println("  VALID: all " + entries.size() + " entries are chained correctly "
                    + "and match the externally anchored checkpoint, no tampering detected");
        }

        static String hashOf(String previousHash, String actor, String action, String subject)
                throws NoSuchAlgorithmException {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(canonicalize(previousHash, actor, action, subject).getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }

        // Length-prefixes each field before concatenating, so fields containing arbitrary
        // characters (including a literal delimiter) can't be shifted across a field boundary to
        // produce the same hash input from different underlying data.
        static String canonicalize(String... fields) {
            StringBuilder sb = new StringBuilder();
            for (String field : fields) {
                sb.append(field.length()).append(':').append(field);
            }
            return sb.toString();
        }
    }
}
