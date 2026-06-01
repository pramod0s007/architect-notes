package evolution;

import java.util.Set;

/**
 * EVOLUTION v1 — No Proxy: Business Logic Mixed with Access Control
 *
 * Domain: Document Access Service
 *
 * DocumentService has hardcoded role checks at the top of every method.
 * Access control is mixed with business logic. The service knows about roles,
 * users, and authorization policy — none of which is its core responsibility.
 *
 * Problems:
 * 1. Every new method must re-implement the same role check (copy-paste).
 * 2. If the authorization rule changes (e.g., add "MANAGER" role),
 *    every method must be updated.
 * 3. DocumentService cannot be tested for its core logic without
 *    providing a valid user context.
 * 4. The authorization logic is not reusable across other services.
 */
public class v1_NoProxy {

    // Domain types
    record User(String username, Set<String> roles) {
        boolean hasRole(String role) { return roles.contains(role); }
    }

    record Document(String id, String title, String content) {}

    // ---------------------------------------------------------------
    // DocumentService — business logic polluted with auth checks
    // ---------------------------------------------------------------
    static class DocumentService {

        /**
         * Auth check at the top — repeated in every method.
         * If policy changes (allow MANAGER, deny READ_ONLY), update 4 places.
         */
        public Document getDocument(String docId, User requestingUser) {
            // HARDCODED ACCESS CONTROL — mixed into business logic
            if (!requestingUser.hasRole("ADMIN") && !requestingUser.hasRole("READER")) {
                throw new SecurityException("Access denied: " + requestingUser.username()
                        + " lacks required role for getDocument");
            }

            // Actual business logic: fetch document
            System.out.println("  [DocumentService] Fetching document: " + docId);
            return new Document(docId, "Budget Report Q4", "Confidential content...");
        }

        public void saveDocument(Document doc, User requestingUser) {
            // Auth check copy-pasted and slightly different — ADMIN only for save
            if (!requestingUser.hasRole("ADMIN")) {
                throw new SecurityException("Access denied: " + requestingUser.username()
                        + " lacks ADMIN role for saveDocument");
            }

            // Actual business logic: persist document
            System.out.println("  [DocumentService] Saving document: " + doc.id());
        }

        public void deleteDocument(String docId, User requestingUser) {
            // Auth check copy-pasted again — same pattern, third time
            if (!requestingUser.hasRole("ADMIN")) {
                throw new SecurityException("Access denied: " + requestingUser.username()
                        + " lacks ADMIN role for deleteDocument");
            }

            // Actual business logic: remove document
            System.out.println("  [DocumentService] Deleting document: " + docId);
        }

        public String getDocumentMetadata(String docId, User requestingUser) {
            // Fourth copy — READER or ADMIN
            if (!requestingUser.hasRole("ADMIN") && !requestingUser.hasRole("READER")) {
                throw new SecurityException("Access denied: " + requestingUser.username()
                        + " for getDocumentMetadata");
            }

            System.out.println("  [DocumentService] Fetching metadata: " + docId);
            return "title=Budget Report Q4, created=2026-01-01, pages=42";
        }
    }

    // ---------------------------------------------------------------
    // Main — show the mixed concerns
    // ---------------------------------------------------------------
    public static void main(String[] args) {
        System.out.println("=== v1: No Proxy — Auth Mixed with Business Logic ===\n");

        var service = new DocumentService();

        User admin  = new User("alice", Set.of("ADMIN", "READER"));
        User reader = new User("bob",   Set.of("READER"));
        User guest  = new User("carol", Set.of("GUEST"));

        System.out.println("--- Admin user ---");
        Document doc = service.getDocument("DOC-001", admin);
        System.out.println("  Got: " + doc.title());
        service.saveDocument(doc, admin);

        System.out.println("\n--- Reader user ---");
        Document rdoc = service.getDocument("DOC-001", reader);
        System.out.println("  Got: " + rdoc.title());
        try {
            service.saveDocument(rdoc, reader); // reader cannot save
        } catch (SecurityException e) {
            System.out.println("  Blocked (correct): " + e.getMessage());
        }

        System.out.println("\n--- Guest user (no roles) ---");
        try {
            service.getDocument("DOC-001", guest);
        } catch (SecurityException e) {
            System.out.println("  Blocked (correct): " + e.getMessage());
        }

        System.out.println();
        System.out.println("DocumentService has 4 methods. Each has an auth check.");
        System.out.println("The authorization policy is scattered across 4 places.");
        System.out.println("Changing 'READER' to 'VIEWER' requires 4 edits.");
        System.out.println("The service cannot be unit-tested without a valid User.");
        System.out.println("v2 extracts all auth into a Proxy.");
    }
}
