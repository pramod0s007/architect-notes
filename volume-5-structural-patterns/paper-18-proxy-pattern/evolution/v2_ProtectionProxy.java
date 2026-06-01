package evolution;

import java.util.Set;

/**
 * EVOLUTION v2 — Protection Proxy
 *
 * Domain: Document Access Service
 *
 * DocumentServiceProxy handles all access control.
 * DocumentServiceImpl has ZERO authorization code — pure business logic.
 *
 * Benefits:
 * 1. Auth policy is in one place (the proxy). Change once, applies everywhere.
 * 2. DocumentServiceImpl can be tested without any user context.
 * 3. The interface (DocumentService) is unchanged — callers don't know about the proxy.
 * 4. The proxy can be swapped for a no-op proxy in tests.
 */
public class v2_ProtectionProxy {

    // Domain types
    record User(String username, Set<String> roles) {
        boolean hasRole(String role) { return roles.contains(role); }
    }

    record Document(String id, String title, String content) {}

    // ---------------------------------------------------------------
    // Interface — callers depend on this only
    // ---------------------------------------------------------------
    interface DocumentService {
        Document   getDocument(String docId, User user);
        void       saveDocument(Document doc, User user);
        void       deleteDocument(String docId, User user);
        String     getDocumentMetadata(String docId, User user);
    }

    // ---------------------------------------------------------------
    // Implementation — ZERO auth code, pure business logic
    // ---------------------------------------------------------------
    static class DocumentServiceImpl implements DocumentService {

        @Override
        public Document getDocument(String docId, User user) {
            System.out.println("  [DocumentServiceImpl] Fetching: " + docId);
            return new Document(docId, "Budget Report Q4", "Confidential content...");
        }

        @Override
        public void saveDocument(Document doc, User user) {
            System.out.println("  [DocumentServiceImpl] Saving: " + doc.id());
        }

        @Override
        public void deleteDocument(String docId, User user) {
            System.out.println("  [DocumentServiceImpl] Deleting: " + docId);
        }

        @Override
        public String getDocumentMetadata(String docId, User user) {
            System.out.println("  [DocumentServiceImpl] Fetching metadata: " + docId);
            return "title=Budget Report Q4, created=2026-01-01, pages=42";
        }
    }

    // ---------------------------------------------------------------
    // Protection Proxy — handles ALL access control in one place
    // ---------------------------------------------------------------
    static class DocumentServiceProxy implements DocumentService {
        private final DocumentService delegate;

        DocumentServiceProxy(DocumentService delegate) {
            this.delegate = delegate;
        }

        // Authorization policy — defined ONCE, applied to all methods
        private void requireRole(User user, String operation, String... allowedRoles) {
            for (String role : allowedRoles) {
                if (user.hasRole(role)) {
                    System.out.println("  [Proxy] " + user.username()
                            + " authorized for " + operation + " (role: " + role + ")");
                    return;
                }
            }
            throw new SecurityException("[Proxy] Access denied: " + user.username()
                    + " lacks required role for " + operation
                    + ". Required: " + String.join(" or ", allowedRoles));
        }

        @Override
        public Document getDocument(String docId, User user) {
            requireRole(user, "getDocument", "ADMIN", "READER");
            return delegate.getDocument(docId, user);
        }

        @Override
        public void saveDocument(Document doc, User user) {
            requireRole(user, "saveDocument", "ADMIN");
            delegate.saveDocument(doc, user);
        }

        @Override
        public void deleteDocument(String docId, User user) {
            requireRole(user, "deleteDocument", "ADMIN");
            delegate.deleteDocument(docId, user);
        }

        @Override
        public String getDocumentMetadata(String docId, User user) {
            requireRole(user, "getDocumentMetadata", "ADMIN", "READER");
            return delegate.getDocumentMetadata(docId, user);
        }
    }

    // ---------------------------------------------------------------
    // Factory — callers get the proxy-wrapped service
    // ---------------------------------------------------------------
    static DocumentService createDocumentService() {
        return new DocumentServiceProxy(new DocumentServiceImpl());
    }

    // ---------------------------------------------------------------
    // Main
    // ---------------------------------------------------------------
    public static void main(String[] args) {
        System.out.println("=== v2: Protection Proxy ===\n");

        // Callers receive DocumentService — they don't know it's a proxy
        DocumentService service = createDocumentService();

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
            service.saveDocument(rdoc, reader);
        } catch (SecurityException e) {
            System.out.println("  Blocked: " + e.getMessage());
        }

        System.out.println("\n--- Guest user ---");
        try {
            service.getDocument("DOC-001", guest);
        } catch (SecurityException e) {
            System.out.println("  Blocked: " + e.getMessage());
        }

        System.out.println();
        System.out.println("DocumentServiceImpl: 0 auth checks, pure logic.");
        System.out.println("DocumentServiceProxy: all auth in requireRole() — 1 place.");
        System.out.println("Adding MANAGER role access: edit requireRole() calls only.");
        System.out.println();
        System.out.println("v3 adds a CachingDocumentProxy wrapping this secured service.");
    }
}
