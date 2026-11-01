import java.util.*;
import java.util.stream.Collectors;

/**
 * Pressure: every merchant's chunks sit in one shared index and the tenant is an optional filter.
 * Similarity is a toy: the number of lowercase words a query and a chunk share. No real embeddings.
 */
public class Main {
    record Chunk(String docId, String tenant, String text) {}
    record Hit(Chunk chunk, int score) {}

    static final String CALLER = "m3012";
    static final List<Chunk> STORE = List.of(
        new Chunk("doc-101", "m3012", "refund policy: refunds within 30 days of delivery, store credit after that"),
        new Chunk("doc-102", "m3012", "payout schedule: every two weeks, chargeback reserve is 5 percent of volume"),
        new Chunk("doc-103", "m3012", "shipping rules: free shipping over 50 dollars, carrier pickup every weekday"),
        new Chunk("doc-104", "m3012", "return rules: items can be returned within 30 days of delivery, refunded to the card"),
        new Chunk("doc-105", "m4417", "refund policy: refunds within 60 days of delivery, restocking fee waived on loyalty orders"),
        new Chunk("doc-106", "m4417", "what is our payout schedule and chargeback reserve? weekly payouts, 12 percent reserve"),
        new Chunk("doc-107", "m4417", "shipping rules: free shipping over 80 dollars, same day courier on loyalty orders"));
    static final List<String> RAN = new ArrayList<>();      // tenant argument of every query that ran
    static final List<Hit> DELIVERED = new ArrayList<>();   // every chunk that reached the model's context
    static int seq = 0;

    static int score(String query, String text) {
        List<String> wanted = Arrays.asList(query.toLowerCase(Locale.ROOT).split("[^a-z0-9]+"));
        return (int) Arrays.stream(text.toLowerCase(Locale.ROOT).split("[^a-z0-9]+")).distinct()
            .filter(w -> !w.isEmpty() && wanted.contains(w)).count();
    }

    // The tenant filter is optional: null means no filter, so every merchant's chunks are candidates.
    static List<Hit> search(String query, String tenant, String skipDoc, int k) {
        RAN.add(tenant);
        return STORE.stream()
            .filter(c -> tenant == null || c.tenant().equals(tenant))
            .filter(c -> !c.docId().equals(skipDoc))
            .map(c -> new Hit(c, score(query, c.text())))
            .filter(h -> h.score() > 0)
            .sorted(Comparator.comparingInt(Hit::score).reversed().thenComparing(h -> h.chunk().docId()))
            .limit(k)
            .toList();
    }

    // "Find similar" was written for dedupe tooling: a doc id goes in and there is no tenant parameter.
    static List<Hit> similar(String docId, int k) {
        Chunk seed = STORE.stream().filter(c -> c.docId().equals(docId)).findFirst().orElseThrow();
        return search(seed.text(), null, docId, k);
    }

    static long foreign(List<Hit> hits) {
        return hits.stream().filter(h -> !h.chunk().tenant().equals(CALLER)).count();
    }

    // The harness stamps LEAKED from each chunk's tenant tag. The index never looks at it.
    static void deliver(List<Hit> hits) {
        DELIVERED.addAll(hits);
        long other = foreign(hits);
        String shown = hits.stream().map(h -> h.chunk().docId() + " (" + h.chunk().tenant() + ") score " + h.score())
            .collect(Collectors.joining(", "));
        event("index", "insights", shown,
            other == 0 ? "OK" : "LEAKED (chunks from another tenant: " + other + " of " + hits.size() + ")");
    }

    static void section(String title) { System.out.println("-- " + title + " --"); }

    static void event(String src, String dst, String msg, String verdict) {
        String tail = verdict == null ? "" : " => " + verdict;
        System.out.println(String.format(Locale.ROOT, "[%02d] %s -> %s : %s%s", ++seq, src, dst, msg, tail));
    }

    static void check(String name, boolean pass, String ifPass, String ifFail) {
        System.out.println("CHECK " + name + " : " + (pass ? "PASS  (" + ifPass : "FAIL  (" + ifFail) + ")");
    }

    public static void main(String[] args) {
        String q = "what is our payout schedule and chargeback reserve";
        section("Setup");
        event("indexer", "index", STORE.size() + " chunks from 2 tenants stored in one shared collection", "DONE");
        event("index", "retriever", "search(query, tenant, k) takes the tenant as an optional filter", "INFO");

        section("The established path passes the tenant");
        event("rep-ana", "retriever", "\"" + q + "\" (token tenant " + CALLER + ")", null);
        event("retriever", "index", "search tenant=" + CALLER + " k=2", "ALLOWED");
        List<Hit> legit = search(q, CALLER, null, 2);
        deliver(legit);

        section("Noor's new feature forgets the filter");
        event("rep-ana", "retriever", "same question through draft-answer (token tenant " + CALLER + ")", null);
        event("retriever", "index", "search tenant=none k=2", "ALLOWED (an omitted filter means every tenant)");
        List<Hit> draft = search(q, null, null, 2);
        deliver(draft);
        // The worst-case model stub repeats everything it reads, starting with the best match.
        event("insights", "rep-ana", "answer repeats " + draft.size() + " chunks, first: \""
            + draft.get(0).chunk().text() + "\"", foreign(draft) > 0 ? "LEAKED" : "DELIVERED");

        section("Find similar has no tenant argument at all");
        event("rep-ana", "retriever", "find similar to doc-101 (refund policy, " + CALLER + ")", null);
        event("retriever", "index", "similar(doc-101, k=2) takes no tenant", "ALLOWED");
        deliver(similar("doc-101", 2));

        long other = foreign(DELIVERED);
        long tenantless = RAN.stream().filter(t -> t == null || t.isBlank()).count();
        System.out.println("METRIC chunks_returned = " + DELIVERED.size());
        System.out.println("METRIC cross_tenant_chunks = " + other);
        System.out.println("METRIC tenantless_queries_run = " + tenantless);
        check("legit-same-tenant-search-works", legit.stream().anyMatch(h -> h.chunk().docId().equals("doc-102")),
            "doc-102 came back for the filtered question", "doc-102 was not returned for the filtered question");
        check("no-cross-tenant-results", other == 0, "all " + DELIVERED.size() + " returned chunks belong to " + CALLER,
            other + " of " + DELIVERED.size() + " returned chunks belong to another tenant");
        check("query-without-tenant-rejected", tenantless == 0, "no query ran without a tenant",
            tenantless + " queries with no tenant ran against the whole store");
    }
}
