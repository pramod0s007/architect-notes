import java.util.*;
import java.util.stream.Collectors;

/**
 * Wrong direction: put "only for merchant 3012" into every query and prompt, and let code review confirm
 * that every call site passes the tenant filter. The filter value still comes from the request, and a blank
 * value still means no filter. Same toy similarity and same worst-case model stub as the pressure.
 */
public class Main {
    record Chunk(String docId, String tenant, String text) {}
    record Hit(Chunk chunk, int score) {}
    record Req(String label, String tokenTenant, String bodyTenant) {}
    record Delivery(String request, String tokenTenant, Hit hit) {}

    static final String HINT = "only for merchant 3012: ";
    static final String QUESTION = "what is our payout schedule and chargeback reserve";
    static final List<String> REVIEWED_CALL_SITES = List.of("ask", "draft-answer", "find-similar");
    static final List<Chunk> STORE = List.of(
        new Chunk("doc-101", "m3012", "refund policy: refunds within 30 days of delivery, store credit after that"),
        new Chunk("doc-102", "m3012", "payout schedule: every two weeks, chargeback reserve is 5 percent of volume"),
        new Chunk("doc-103", "m3012", "shipping rules: free shipping over 50 dollars, carrier pickup every weekday"),
        new Chunk("doc-104", "m3012", "return rules: items can be returned within 30 days of delivery, refunded to the card"),
        new Chunk("doc-105", "m4417", "refund policy: refunds within 60 days of delivery, restocking fee waived on loyalty orders"),
        new Chunk("doc-106", "m4417", "what is our payout schedule and chargeback reserve? weekly payouts, 12 percent reserve"),
        new Chunk("doc-107", "m4417", "shipping rules: free shipping over 80 dollars, same day courier on loyalty orders"));
    static final List<String> RAN = new ArrayList<>();       // tenant argument of every query that ran
    static final List<Delivery> LEDGER = new ArrayList<>();  // every chunk that reached the model's context
    static int seq = 0;

    static int score(String query, String text) {
        List<String> wanted = Arrays.asList(query.toLowerCase(Locale.ROOT).split("[^a-z0-9]+"));
        return (int) Arrays.stream(text.toLowerCase(Locale.ROOT).split("[^a-z0-9]+")).distinct()
            .filter(w -> !w.isEmpty() && wanted.contains(w)).count();
    }

    // Review reads code, so it can confirm that each call site passes an argument. It cannot see the value
    // that arrives at run time, and a blank value still means no filter.
    static List<Hit> search(String query, String tenant, int k) {
        RAN.add(tenant);
        return STORE.stream()
            .filter(c -> tenant.isBlank() || c.tenant().equals(tenant))
            .map(c -> new Hit(c, score(query, c.text())))
            .filter(h -> h.score() > 0)
            .sorted(Comparator.comparingInt(Hit::score).reversed().thenComparing(h -> h.chunk().docId()))
            .limit(k)
            .toList();
    }

    static boolean crossed(Delivery d) { return !d.hit().chunk().tenant().equals(d.tokenTenant()); }

    // The retriever takes the filter value from the request body, as every reviewed call site does.
    static void handle(Req r) {
        String body = r.bodyTenant().isBlank() ? "(blank)" : r.bodyTenant();
        event("rep-ana", "retriever", r.label() + " request, token says " + r.tokenTenant() + ", body says " + body, null);
        String why = r.bodyTenant().isBlank() ? " (blank means no filter)"
            : r.bodyTenant().equals(r.tokenTenant()) ? "" : " (the value came from the request body)";
        event("retriever", "index", "search tenant=" + body + " with the rule prepended", "ALLOWED" + why);
        List<Hit> hits = search(HINT + QUESTION, r.bodyTenant(), 2);
        List<Delivery> got = hits.stream().map(h -> new Delivery(r.label(), r.tokenTenant(), h)).toList();
        LEDGER.addAll(got);
        long other = got.stream().filter(Main::crossed).count();
        // The harness stamps LEAKED from each chunk's tenant tag. Neither the index nor the stub looks at it.
        event("index", "insights", hits.stream().map(h -> h.chunk().docId() + " (" + h.chunk().tenant() + ") score "
            + h.score()).collect(Collectors.joining(", ")),
            other == 0 ? "OK" : "LEAKED (chunks from another tenant: " + other + " of " + hits.size() + ")");
        event("insights", "rep-ana", "answer repeats its context, first chunk: \"" + hits.get(0).chunk().text() + "\"",
            other == 0 ? "DELIVERED" : "LEAKED");
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
        section("Setup: a prompt rule and a reviewed argument");
        event("retriever", "index", "every query and prompt now starts with the rule \"" + HINT.trim() + "\"", "INFO");
        event("retriever", "index", "review of " + REVIEWED_CALL_SITES.size() + " call sites: each passes a tenant argument",
            "APPROVED");

        section("The reviewed draft-answer path works");
        handle(new Req("draft-answer", "m3012", "m3012"));
        section("An older client leaves the field out");
        handle(new Req("older-client", "m3012", ""));
        section("A caller names someone else's tenant");
        handle(new Req("other-tenant", "m3012", "m4417"));

        long other = LEDGER.stream().filter(Main::crossed).count();
        long tenantless = RAN.stream().filter(String::isBlank).count();
        long named = LEDGER.stream().filter(d -> d.request().equals("other-tenant") && crossed(d)).count();
        System.out.println("METRIC call_sites_reviewed = " + REVIEWED_CALL_SITES.size());
        System.out.println("METRIC chunks_returned = " + LEDGER.size());
        System.out.println("METRIC cross_tenant_chunks = " + other);
        System.out.println("METRIC tenantless_queries_run = " + tenantless);
        check("legit-same-tenant-search-works",
            LEDGER.stream().anyMatch(d -> d.request().equals("draft-answer") && d.hit().chunk().docId().equals("doc-102")),
            "the reviewed draft-answer path got doc-102", "the reviewed draft-answer path did not get doc-102");
        check("no-cross-tenant-results", other == 0, "all " + LEDGER.size() + " returned chunks belong to the caller's tenant",
            other + " of " + LEDGER.size() + " returned chunks belong to another tenant");
        check("query-without-tenant-rejected", tenantless == 0, "no query ran without a tenant",
            "queries with no tenant that ran against every tenant: " + tenantless);
        check("tenant-from-token-not-request", named == 0, "the request naming m4417 got only m3012 chunks",
            "the request naming m4417 got chunks from m4417: " + named);
    }
}
