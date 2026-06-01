package evolution;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * EVOLUTION v3 — Stacked Proxies: Protection + Caching
 *
 * Domain: Document Access Service
 *
 * Two proxies stacked, each with one responsibility:
 *   CachingDocumentProxy  — caches results, reduces DB calls
 *   DocumentServiceProxy  — enforces access control
 *   DocumentServiceImpl   — pure business logic
 *
 * Composition: Caching wraps the secured service.
 *   new CachingDocumentProxy(
 *       new DocumentServiceProxy(
 *           new DocumentServiceImpl()))
 *
 * Each layer has one job. Changing cache TTL affects CachingDocumentProxy only.
 * Changing auth rules affects DocumentServiceProxy only.
 * DocumentServiceImpl never changes for infrastructure reasons.
 *
 * The caller gets DocumentService — does not know about either proxy.
 */
public class v3_CachingLayer {

    // Domain types
    record User(String username, Set<String> roles) {
        boolean hasRole(String role) { return roles.contains(role); }
    }

    record Document(String id, String title, String content) {}

    // ---------------------------------------------------------------
    // Interface
    // ---------------------------------------------------------------
    interface DocumentService {
        Document   getDocument(String docId, User user);
        void       saveDocument(Document doc, User user);
        void       deleteDocument(String docId, User user);
        String     getDocumentMetadata(String docId, User user);
    }

    // ---------------------------------------------------------------
    // Layer 3 — pure business logic (identical to v2)
    // ---------------------------------------------------------------
    static class DocumentServiceImpl implements DocumentService {
        @Override
        public Document getDocument(String docId, User user) {
            System.out.println("  [DocumentServiceImpl] DB fetch: " + docId);
            return new Document(docId, "Budget Report Q4", "Confidential content for " + docId);
        }

        @Override
        public void saveDocument(Document doc, User user) {
            System.out.println("  [DocumentServiceImpl] DB save: " + doc.id());
        }

        @Override
        public void deleteDocument(String docId, User user) {
            System.out.println("  [DocumentServiceImpl] DB delete: " + docId);
        }

        @Override
        public String getDocumentMetadata(String docId, User user) {
            System.out.println("  [DocumentServiceImpl] DB metadata: " + docId);
            return "title=Budget Report Q4, created=2026-01-01, pages=42";
        }
    }

    // ---------------------------------------------------------------
    // Layer 2 — protection proxy (identical to v2)
    // ---------------------------------------------------------------
    static class DocumentServiceProxy implements DocumentService {
        private final DocumentService delegate;

        DocumentServiceProxy(DocumentService delegate) {
            this.delegate = delegate;
        }

        private void requireRole(User user, String op, String... roles) {
            for (String role : roles) {
                if (user.hasRole(role)) {
                    System.out.println("  [ProtectionProxy] " + user.username()
                            + " authorized for " + op);
                    return;
                }
            }
            throw new SecurityException("[ProtectionProxy] " + user.username()
                    + " denied for " + op);
        }

        @Override public Document   getDocument(String id, User u) { requireRole(u, "getDocument", "ADMIN", "READER"); return delegate.getDocument(id, u); }
        @Override public void       saveDocument(Document d, User u) { requireRole(u, "saveDocument", "ADMIN"); delegate.saveDocument(d, u); }
        @Override public void       deleteDocument(String id, User u) { requireRole(u, "deleteDocument", "ADMIN"); delegate.deleteDocument(id, u); }
        @Override public String     getDocumentMetadata(String id, User u) { requireRole(u, "getDocumentMetadata", "ADMIN", "READER"); return delegate.getDocumentMetadata(id, u); }
    }

    // ---------------------------------------------------------------
    // Layer 1 — caching proxy (outer layer — checked first)
    // ---------------------------------------------------------------
    static class CachingDocumentProxy implements DocumentService {
        private final DocumentService delegate;

        // Simple in-memory cache — in production, use Caffeine or Redis
        private final Map<String, Document> documentCache  = new HashMap<>();
        private final Map<String, String>   metadataCache  = new HashMap<>();

        CachingDocumentProxy(DocumentService delegate) {
            this.delegate = delegate;
        }

        @Override
        public Document getDocument(String docId, User user) {
            if (documentCache.containsKey(docId)) {
                System.out.println("  [CachingProxy] HIT for document: " + docId);
                return documentCache.get(docId);
            }

            System.out.println("  [CachingProxy] MISS for document: " + docId + " — delegating");
            Document doc = delegate.getDocument(docId, user);
            documentCache.put(docId, doc);
            return doc;
        }

        @Override
        public void saveDocument(Document doc, User user) {
            // Write-through: update cache and persist
            delegate.saveDocument(doc, user);
            documentCache.put(doc.id(), doc);   // keep cache in sync
            metadataCache.remove(doc.id());     // invalidate stale metadata
            System.out.println("  [CachingProxy] Cache updated for: " + doc.id());
        }

        @Override
        public void deleteDocument(String docId, User user) {
            // Invalidate before delete (fail-safe: if delete fails, cache is already gone)
            documentCache.remove(docId);
            metadataCache.remove(docId);
            System.out.println("  [CachingProxy] Cache evicted for: " + docId);
            delegate.deleteDocument(docId, user);
        }

        @Override
        public String getDocumentMetadata(String docId, User user) {
            if (metadataCache.containsKey(docId)) {
                System.out.println("  [CachingProxy] HIT for metadata: " + docId);
                return metadataCache.get(docId);
            }

            System.out.println("  [CachingProxy] MISS for metadata: " + docId + " — delegating");
            String meta = delegate.getDocumentMetadata(docId, user);
            metadataCache.put(docId, meta);
            return meta;
        }

        public int documentCacheSize() { return documentCache.size(); }
        public int metadataCacheSize() { return metadataCache.size(); }
    }

    // ---------------------------------------------------------------
    // Factory — stacks both proxies transparently
    // ---------------------------------------------------------------
    static DocumentService createDocumentService() {
        return new CachingDocumentProxy(
                new DocumentServiceProxy(
                        new DocumentServiceImpl()));
    }

    // ---------------------------------------------------------------
    // Main
    // ---------------------------------------------------------------
    public static void main(String[] args) {
        System.out.println("=== v3: Two Stacked Proxies (Caching + Protection) ===\n");

        DocumentService service = createDocumentService();

        User admin  = new User("alice", Set.of("ADMIN", "READER"));
        User reader = new User("bob",   Set.of("READER"));
        User guest  = new User("carol", Set.of("GUEST"));

        System.out.println("--- First fetch (MISS: goes all the way to DB) ---");
        Document doc1 = service.getDocument("DOC-001", admin);
        System.out.println("  Result: " + doc1.title());

        System.out.println("\n--- Second fetch (HIT: cache returns without auth check) ---");
        Document doc2 = service.getDocument("DOC-001", admin);
        System.out.println("  Result: " + doc2.title());

        System.out.println("\n--- Reader fetches (also hits cache) ---");
        Document doc3 = service.getDocument("DOC-001", reader);
        System.out.println("  Result: " + doc3.title());

        System.out.println("\n--- Guest blocked before reaching cache ---");
        try {
            service.getDocument("DOC-001", guest);
        } catch (SecurityException e) {
            System.out.println("  " + e.getMessage());
        }

        System.out.println("\n--- Admin saves (write-through + cache invalidation) ---");
        service.saveDocument(doc1, admin);

        System.out.println("\n--- Fetch after save (MISS: cache was invalidated) ---");
        service.getDocument("DOC-001", admin);

        System.out.println("\n--- Admin deletes (evict before delete) ---");
        service.deleteDocument("DOC-001", admin);

        System.out.println();
        System.out.println("=== Summary ===");
        System.out.println("CachingDocumentProxy:  one responsibility — cache management");
        System.out.println("DocumentServiceProxy:  one responsibility — access control");
        System.out.println("DocumentServiceImpl:   one responsibility — business logic");
        System.out.println();
        System.out.println("Each proxy layer is independently testable.");
        System.out.println("Removing caching: swap CachingDocumentProxy for the delegate directly.");
        System.out.println("Callers never change — they hold a DocumentService reference.");
    }
}
