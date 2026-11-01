import java.util.*;
import java.util.regex.*;

// Principle applied: every chunk carries the acl it was indexed with, and the retriever filters on it inside the query,
// before ranking, then re-checks the fetched chunks on live permissions and logs each decision. The model is the same
// disclosed worst case, so every defense comes from the retriever, the index and authz. Tenant and trust are unused here.
public class Main {
    record Chunk(String docId, String chunkId, String tenant, Set<String> acl, String trust, int version, String text) {
        String id() { return docId + "/" + chunkId; }
    }
    record Decision(String user, List<Chunk> pool, List<Chunk> fetched, List<Chunk> denied, List<Chunk> returned) {}

    static final int K = 3, FETCH = K + 2;
    static final Pattern FIGURE = Pattern.compile("\\d[\\d-]*( percent| days)?");
    static final List<String> FACTS = List.of("2027-03-01", "12 percent", "90 days", "18000", "7 percent");
    static final List<String> RENEWAL = FACTS.subList(0, 4);   // the four facts held in doc-103
    static final Map<String, String> TITLES = new LinkedHashMap<>();
    static final Map<String, Set<String>> LIVE = new LinkedHashMap<>();     // authz: who may read each document right now
    static final Map<String, Set<String>> GROUPS = new LinkedHashMap<>();   // user -> groups, also authz
    static final List<Chunk> INDEX = new ArrayList<>();
    static final List<String> LOG = new ArrayList<>();
    static int seq = 0;

    static Set<String> set(String... items) { return new LinkedHashSet<>(List.of(items)); }
    static void doc(String docId, String title, String groups, String... texts) {
        TITLES.put(docId, title);
        LIVE.put(docId, set(groups.split(",")));
        for (int i = 0; i < texts.length; i++) {
            INDEX.add(new Chunk(docId, "c" + (i + 1), "m3012", set(groups.split(",")), "internal", 1, texts[i]));
        }
    }

    static boolean mayRead(String user, Set<String> acl) { return !Collections.disjoint(GROUPS.get(user), acl); }
    static List<Chunk> unreadable(String user, List<Chunk> chunks) { return chunks.stream().filter(c -> !mayRead(user, LIVE.get(c.docId()))).toList(); }
    static String ids(List<Chunk> chunks) { return chunks.isEmpty() ? "none" : String.join(" ", chunks.stream().map(Chunk::id).toList()); }
    static Set<String> words(String s) { return new LinkedHashSet<>(List.of(s.toLowerCase(Locale.ROOT).split("[^a-z0-9]+"))); }

    // The toy similarity: how many distinct lowercase words the query and the chunk share.
    static int score(String query, Chunk c) {
        Set<String> shared = words(query);
        shared.retainAll(words(c.text()));
        return shared.size();
    }
    static List<Chunk> rank(String query, List<Chunk> pool, int n) {
        return pool.stream().sorted(Comparator.comparingInt((Chunk c) -> -score(query, c)).thenComparing(Chunk::id)).limit(n).toList();
    }

    // The whole control: the acl filter sits inside the query, so ranking and top-k only ever see permitted chunks.
    static Decision retrieve(String user, String query) {
        List<Chunk> pool = INDEX.stream().filter(c -> mayRead(user, c.acl())).toList();
        List<Chunk> fetched = rank(query, pool, FETCH);   // over-fetch, so one denied chunk does not leave a slot empty
        List<Chunk> denied = unreadable(user, fetched);   // re-check on live permissions, because the index copy can be stale
        List<Chunk> returned = fetched.stream().filter(c -> !denied.contains(c)).limit(K).toList();
        LOG.add(user + " | " + query.length() + " chars | scored " + pool.size() + " | fetched " + ids(fetched) + " | denied " + ids(denied) + " | returned " + ids(returned));
        return new Decision(user, pool, fetched, denied, returned);
    }

    static String answer(String query, List<Chunk> context) {
        if (query.startsWith("list")) {
            List<String> figures = FIGURE.matcher(String.join(" ", context.stream().map(Chunk::text).toList())).results().map(MatchResult::group).toList();
            return figures.isEmpty() ? "no figures in the context" : String.join(", ", figures);
        }
        return String.join("; ", context.stream().map(c -> query.startsWith("quote") ? c.text() + " (" + TITLES.get(c.docId()) + ", " + c.docId() + ")" : c.text()).toList());
    }

    static Set<String> factsIn(String text) { return new LinkedHashSet<>(FACTS.stream().filter(text::contains).toList()); }
    // Scans the whole corpus on live permissions, not the retrieved context, so the check does not depend on the mechanism.
    static Set<String> restrictedFor(String user) {
        Set<String> out = new LinkedHashSet<>();
        unreadable(user, INDEX).forEach(c -> out.addAll(factsIn(c.text())));
        return out;
    }

    static void section(String title) { System.out.println("-- " + title + " --"); }
    static void event(String src, String dst, String msg, String verdict) { System.out.println(String.format(Locale.ROOT, "[%02d] %s -> %s : %s%s", ++seq, src, dst, msg, verdict == null ? "" : " => " + verdict)); }
    static void check(String name, boolean pass, String detail) { System.out.println("CHECK " + name + " : " + (pass ? "PASS" : "FAIL") + "  (" + detail + ")"); }

    public static void main(String[] args) {
        GROUPS.put("rep-ana", set("support")); GROUPS.put("lead-omar", set("support", "contracts"));
        doc("doc-101", "Shipping Times", "support", "standard shipping takes 5 business days and express takes 2");
        doc("doc-102", "Returns Policy", "support", "customers may return items within 30 days for a refund to the original card");
        doc("doc-103", "Renewal Terms Agreement", "contracts", "renewal terms: the agreement renews on 2027-03-01 and the uplift is 12 percent",
            "notice period for this renewal is 90 days and an early exit costs 18000");
        doc("doc-104", "Renewal Handling Note", "support", "renewal questions are answered by the account team");
        doc("doc-105", "Pricing Exceptions Memo", "support,contracts", "this merchant holds a 7 percent discount through the renewal");
        doc("doc-106", "Billing Checks", "support", "check the invoice and the billing profile before you answer a billing question");
        doc("doc-107", "Gift Card Rules", "support", "gift cards never expire and can be used at any storefront");
        doc("doc-108", "Storefront Admin Tips", "support", "reset a storefront password from the admin console");
        LIVE.put("doc-105", set("contracts"));   // tightened after indexing: the index still holds the old acl at version 1
        String[][] script = {{"rep-ana", "rep-ana asks for a quote", "quote the notice period from the renewal terms agreement"},
            {"rep-ana", "rep-ana asks for a summary", "what are the renewal terms and notice period for this merchant"},
            {"rep-ana", "rep-ana asks for the figures", "list the dates and amounts from the renewal terms and notice period"},
            {"lead-omar", "lead-omar asks for a quote", "quote the notice period from the renewal terms agreement"}};
        List<Decision> decisions = new ArrayList<>();
        Set<String> leaked = new LinkedHashSet<>(), leadFacts = new LinkedHashSet<>();
        long open = 0, kept = 0, after = 0;   // for rep-ana: best score unfiltered vs permitted, usable slots after a post-filter
        section("Setup");
        event("indexer", "index", INDEX.size() + " chunks stored, each with its acl and version", "DONE");
        event("authz", "indexer", "doc-105 acl tightened to " + LIVE.get("doc-105") + ", index sync pending", "PENDING");
        for (int i = 0; i < script.length; i++) {
            String user = script[i][0], title = script[i][1], query = script[i][2], groups = String.join(", ", GROUPS.get(user));
            section(title);
            event("insights", "retriever", user + " asks \"" + query + "\"", null);
            event("retriever", "authz", "expand " + user + "'s delegated token", "OK (groups: " + groups + ")");
            Decision d = retrieve(user, query);
            decisions.add(d);
            event("retriever", "index", "rank only chunks whose acl meets [" + groups + "], fetch " + FETCH + " for " + K + " slots", null);
            event("index", "retriever", d.pool().size() + " of " + INDEX.size() + " chunks scored, fetched: " + ids(d.fetched()), null);
            event("retriever", "authz", "re-check the " + FETCH + " fetched chunks on live permissions", d.denied().isEmpty()
                ? "ALLOWED (all " + FETCH + " still readable)"
                : "DENIED (" + String.join("; ", d.denied().stream().map(c -> c.id() + ": acl is now " + LIVE.get(c.docId()) + ", index holds v" + c.version()).toList()) + ")");
            int bad = unreadable(user, d.returned()).size();
            event("retriever", "insights", d.returned().size() + " chunks join the model context: " + ids(d.returned()),
                bad > 0 ? "EXPOSED (" + bad + " chunks " + user + " may not read)" : "OK (all readable by " + user + ")");
            event("retriever", "authz", "decision recorded: scored " + d.pool().size() + ", fetched " + d.fetched().size() + ", denied " + d.denied().size() + ", returned " + d.returned().size(), "RECORDED");
            String text = answer(query, d.returned());
            Set<String> restricted = restrictedFor(user);
            restricted.retainAll(factsIn(text));
            String shape = query.startsWith("quote") ? "quotes " : query.startsWith("list") ? "lists the figures in " : "restates ";
            event("insights", "caller", "answer " + shape + d.returned().size() + " chunks", restricted.isEmpty()
                ? "DELIVERED (" + factsIn(text).size() + " contract facts, none restricted)"
                : "LEAKED (" + String.join(", ", restricted) + " reached " + user + ")");
            leaked.addAll(restricted);
            if (GROUPS.get(user).contains("contracts")) {
                leadFacts.addAll(factsIn(text));
            }
            if (user.equals("rep-ana")) {
                open += score(query, rank(query, INDEX, 1).get(0));
                kept += score(query, d.returned().get(0));
                after += K - unreadable(user, rank(query, INDEX, K)).size();
            }
        }
        long inContext = decisions.stream().mapToLong(d -> unreadable(d.user(), d.returned()).size()).sum();
        long outside = decisions.stream().mapToLong(d -> d.pool().stream().filter(c -> !mayRead(d.user(), c.acl())).count()).sum();
        long filled = decisions.stream().mapToLong(d -> d.returned().size() - unreadable(d.user(), d.returned()).size()).sum();
        long logged = decisions.stream().filter(d -> LOG.stream().anyMatch(e -> e.startsWith(d.user() + " | ") && e.endsWith("returned " + ids(d.returned())))).count();
        System.out.println("METRIC restricted_chunks_in_context = " + inContext);
        System.out.println("METRIC restricted_facts_delivered = " + leaked.size());
        System.out.println("METRIC chunks_denied_on_fetch = " + decisions.stream().mapToLong(d -> d.denied().size()).sum());
        System.out.println("METRIC slots_filled_if_filtered_after_top_k = " + after);
        System.out.println("METRIC best_score_unfiltered_vs_permitted = " + open + " vs " + kept);
        check("restricted-chunk-never-in-context", inContext == 0, inContext + " chunks the caller may not read reached the model across " + decisions.size() + " questions");
        check("restricted-facts-absent-from-answer", leaked.isEmpty(), leaked.size() + " of " + restrictedFor("rep-ana").size() + " restricted facts reached rep-ana");
        check("acl-prefilter-before-ranking", outside == 0, outside + " of " + decisions.stream().mapToLong(d -> d.pool().size()).sum() + " scored chunks fell outside the caller's stored acl");
        check("topk-filled-from-permitted-only", filled == decisions.size() * K, filled + " of " + decisions.size() * K + " top-k slots hold a chunk the caller may read");
        check("retrieval-decision-logged", logged == decisions.size(), logged + " of " + decisions.size() + " retrievals have a record naming fetched, denied and returned chunks");
        check("legit-permitted-answer-works", leadFacts.containsAll(RENEWAL), RENEWAL.stream().filter(leadFacts::contains).count() + " of " + RENEWAL.size() + " renewal facts reached lead-omar");
    }
}
