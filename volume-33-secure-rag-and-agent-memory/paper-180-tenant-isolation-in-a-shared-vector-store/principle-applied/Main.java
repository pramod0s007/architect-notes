import java.util.*;
import java.util.function.Function;

/** Principle applied: the tenant is a partition address taken from the verified token, and no read path takes a
 *  tenant argument. Same toy similarity, worst-case model stub and corpus as the pressure, plus a canary per tenant. */
public class Main {
    record Chunk(String docId, String tenant, String text) {}
    record Hit(Chunk chunk, int score) {}
    record Delivery(String request, String tenant, Chunk chunk) {}

    static final String QUESTION = "what is our payout schedule and chargeback reserve";
    static final List<Chunk> CORPUS = List.of(
        new Chunk("doc-101", "m3012", "refund policy: refunds within 30 days of delivery, store credit after that"),
        new Chunk("doc-102", "m3012", "payout schedule: every two weeks, chargeback reserve is 5 percent of volume"),
        new Chunk("doc-103", "m3012", "shipping rules: free shipping over 50 dollars, carrier pickup every weekday"),
        new Chunk("doc-104", "m3012", "return rules: items can be returned within 30 days of delivery, refunded to the card"),
        new Chunk("doc-105", "m4417", "refund policy: refunds within 60 days of delivery, restocking fee waived on loyalty orders"),
        new Chunk("doc-106", "m4417", "what is our payout schedule and chargeback reserve? weekly payouts, 12 percent reserve"),
        new Chunk("doc-107", "m4417", "shipping rules: free shipping over 80 dollars, same day courier on loyalty orders"),
        new Chunk("canary-m3012", "m3012", "canary probe never serve outside m3012"),
        new Chunk("canary-m4417", "m4417", "canary probe never serve outside m4417"));
    // Stands in for signature checks at authz: the verified token is the only source of the tenant.
    static final Map<String, String> TOKENS = Map.of(
        "tok-rep-ana", "m3012", "tok-legacy", "", "tok-probe-m3012", "m3012", "tok-probe-m4417", "m4417");
    static final List<Delivery> LEDGER = new ArrayList<>();   // every chunk that reached the model's context
    static int seq = 0, probes = 0, attempts = 0, rejected = 0;

    static int score(String query, String text) {
        List<String> wanted = Arrays.asList(query.toLowerCase(Locale.ROOT).split("[^a-z0-9]+"));
        return (int) Arrays.stream(text.toLowerCase(Locale.ROOT).split("[^a-z0-9]+")).distinct()
            .filter(w -> !w.isEmpty() && wanted.contains(w)).count();
    }

    // A real store sits behind a boundary that feature code cannot import. Nested classes make private
    // porous inside one file, so this only simulates that boundary.
    static final class Store {
        private final Map<String, List<Chunk>> partitions = new LinkedHashMap<>();
        Store(List<Chunk> chunks) { chunks.forEach(c -> partitions.computeIfAbsent(c.tenant(), t -> new ArrayList<>()).add(c)); }
        // A blank or unknown tenant has no partition. That is an error, never "search everything".
        Handle open(String tenant) {
            List<Chunk> p = partitions.get(tenant);
            if (p == null) throw new IllegalArgumentException("tenant required");
            return new Handle(tenant, p);
        }
    }

    // A handle is bound to one partition and no method takes a tenant, so no call can widen the search.
    record Handle(String tenant, List<Chunk> partition) {
        Optional<Chunk> fetch(String docId) { return partition.stream().filter(c -> c.docId().equals(docId)).findFirst(); }
        List<Hit> search(String text, int k) {
            return partition.stream().map(c -> new Hit(c, score(text, c.text()))).filter(h -> h.score() > 0)
                .sorted(Comparator.comparingInt(Hit::score).reversed().thenComparing(h -> h.chunk().docId()))
                .limit(k).toList();
        }
        List<Hit> similar(String docId, int k) {
            return search(fetch(docId).map(Chunk::text).orElse(""), k + 1).stream()
                .filter(h -> !h.chunk().docId().equals(docId)).limit(k).toList();
        }
    }

    static String verify(String bearer) {
        return Optional.ofNullable(TOKENS.get(bearer)).orElseThrow(() -> new SecurityException("no verified tenant, so no client"));
    }
    static List<Delivery> served(String request) { return LEDGER.stream().filter(d -> d.request().startsWith(request)).toList(); }
    static long crossed(String request) { return served(request).stream().filter(d -> !d.chunk().tenant().equals(d.tenant())).count(); }

    // The harness stamps LEAKED from each chunk's tenant tag. The store never looks at it.
    static String deliver(String request, String tenant, List<Chunk> chunks) {
        chunks.forEach(c -> LEDGER.add(new Delivery(request, tenant, c)));
        long other = chunks.stream().filter(c -> !c.tenant().equals(tenant)).count();
        return other == 0 ? "OK" : "LEAKED (chunks from another tenant: " + other + " of " + chunks.size() + ")";
    }

    static void serve(Store store, String label, String bearer, String body, Function<Handle, List<Hit>> op) {
        if (TOKENS.getOrDefault(bearer, "").isBlank()) attempts++;
        try {
            String claim = verify(bearer);
            event("retriever", "authz", label + ": body field says " + body,
                "VERIFIED (tenant claim " + (claim.isEmpty() ? "none" : claim) + ")");
            if (!body.equals(claim)) event("retriever", "authz", "body field " + body + " differs from the claim",
                "ALERTED (body field ignored)");
            Handle handle = store.open(claim);   // opened from the claim, never from the body
            List<Hit> hits = op.apply(handle);
            String verdict = deliver(label, claim, hits.stream().map(Hit::chunk).toList());
            event("index", "insights", label + " searched the " + claim + " partition, " + handle.partition().size() + " of "
                + CORPUS.size() + " chunks: " + String.join(", ", hits.stream().map(h -> h.chunk().docId() + " ("
                + h.chunk().tenant() + ") score " + h.score()).toList()), verdict);
            // The worst-case model stub repeats the first chunk it reads.
            String first = hits.isEmpty() ? "no chunks" : "first chunk: \"" + hits.get(0).chunk().text() + "\"";
            event("insights", "rep-ana", "answer repeats its context, " + first, verdict.equals("OK") ? "DELIVERED" : "LEAKED");
        } catch (IllegalArgumentException | SecurityException e) {
            rejected++;
            event("retriever", e instanceof SecurityException ? "authz" : "index", label + " request", "REJECTED (" + e.getMessage() + ")");
        }
    }

    // A canary probe asks as one tenant for the other tenant's canary, by its text and by its doc id.
    static void probe(Store store, String token, String foreignDoc) {
        Handle handle = store.open(verify(token));
        String canary = CORPUS.stream().filter(c -> c.docId().equals(foreignDoc)).findFirst().orElseThrow().text();
        List<Chunk> got = new ArrayList<>(handle.search(canary, 2).stream().map(Hit::chunk).toList());
        handle.fetch(foreignDoc).ifPresent(got::add);
        probes += 2;
        event("retriever", "index", "canary probe as " + handle.tenant() + " for " + foreignDoc + " by text and by id, chunks back: "
            + got.size(), deliver("probe", handle.tenant(), got));
    }

    static void section(String title) { System.out.println("-- " + title + " --"); }
    static void event(String src, String dst, String msg, String verdict) {
        System.out.println(String.format(Locale.ROOT, "[%02d] %s -> %s : %s => %s", ++seq, src, dst, msg, verdict));
    }
    static void check(String name, boolean pass, String ifPass, String ifFail) {
        System.out.println("CHECK " + name + " : " + (pass ? "PASS  (" + ifPass : "FAIL  (" + ifFail) + ")");
    }

    public static void main(String[] args) {
        Store store = new Store(CORPUS);
        section("Setup: one partition per tenant, a canary chunk in each");
        event("indexer", "index", "stored " + CORPUS.size() + " chunks, one partition per tenant", "DONE");
        section("The established path, with the tenant taken from the token");
        serve(store, "draft-answer", "tok-rep-ana", "m3012", h -> h.search(QUESTION, 2));
        section("A forged body field, a legacy token and a missing token");
        serve(store, "forged-body", "tok-rep-ana", "m4417", h -> h.search(QUESTION, 2));
        serve(store, "legacy-client", "tok-legacy", "m3012", h -> h.search(QUESTION, 2));
        serve(store, "no-token", "", "m3012", h -> h.search(QUESTION, 2));
        section("Find similar and the canary probes");
        serve(store, "find-similar", "tok-rep-ana", "m3012", h -> h.similar("doc-101", 2));
        probe(store, "tok-probe-m3012", "canary-m4417");
        probe(store, "tok-probe-m4417", "canary-m3012");

        System.out.println("METRIC chunks_returned = " + LEDGER.size());
        System.out.println("METRIC cross_tenant_chunks = " + crossed(""));
        System.out.println("METRIC tenantless_queries_run = " + (attempts - rejected));
        System.out.println("METRIC queries_rejected = " + rejected);
        check("legit-same-tenant-search-works", served("draft-answer").stream().anyMatch(d -> d.chunk().docId().equals("doc-102")),
            "the draft-answer path got doc-102", "the draft-answer path did not get doc-102");
        check("no-cross-tenant-results", crossed("") == 0, "all " + LEDGER.size() + " returned chunks belong to the caller's tenant",
            crossed("") + " of " + LEDGER.size() + " returned chunks belong to another tenant");
        check("query-without-tenant-rejected", attempts > 0 && rejected == attempts,
            rejected + " of " + attempts + " requests without a tenant were rejected",
            "requests without a tenant that ran: " + (attempts - rejected) + " of " + attempts);
        check("tenant-from-token-not-request", !served("forged-body").isEmpty() && crossed("forged-body") == 0,
            "the request naming m4417 got only m3012 chunks, returned: " + served("forged-body").size(),
            "the request naming m4417 got chunks from another tenant: " + crossed("forged-body"));
        check("canary-cross-tenant-probe-clean", probes >= 4 && crossed("probe") == 0,
            probes + " probes, in both directions, by text and by id, returned no chunk from the other tenant",
            "chunks from the other tenant across " + probes + " probes: " + crossed("probe"));
    }
}
