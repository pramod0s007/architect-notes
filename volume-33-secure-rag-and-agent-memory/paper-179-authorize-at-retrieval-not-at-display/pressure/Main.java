import java.util.*;
import java.util.regex.*;

// Pressure: chunks are indexed without access metadata, so every question ranks the whole index and the interface can
// only hide links. The model is the disclosed worst case: it quotes when asked to quote, lists figures when asked for
// figures, and otherwise restates every chunk it was handed. Facts and permissions are tracked by the harness.
public class Main {
    record Chunk(String docId, String chunkId, String tenant, String trust, int version, String text) {
        String id() { return docId + "/" + chunkId; }
    }

    static final int K = 3;
    static final Pattern FIGURE = Pattern.compile("\\d[\\d-]*( percent| days)?");
    static final List<String> FACTS = List.of("2027-03-01", "12 percent", "90 days", "18000", "7 percent");
    static final List<String> RENEWAL = FACTS.subList(0, 4);   // the four facts held in doc-103
    static final Map<String, String> TITLES = new LinkedHashMap<>();
    static final Map<String, Set<String>> ACL = new LinkedHashMap<>();       // lives in authz; the index never receives it
    static final Map<String, Set<String>> GROUPS = new LinkedHashMap<>();   // user -> groups, also authz
    static final List<Chunk> INDEX = new ArrayList<>();
    static final List<String> LOG = new ArrayList<>();
    static int seq = 0;

    static Set<String> set(String... items) { return new LinkedHashSet<>(List.of(items)); }

    static void doc(String docId, String title, String group, String... texts) {
        TITLES.put(docId, title);
        ACL.put(docId, set(group));
        for (int i = 0; i < texts.length; i++) {
            INDEX.add(new Chunk(docId, "c" + (i + 1), "m3012", "internal", 1, texts[i]));
        }
    }

    static boolean mayRead(String user, String docId) { return !Collections.disjoint(GROUPS.get(user), ACL.get(docId)); }

    static String ids(List<Chunk> chunks) { return String.join(" ", chunks.stream().map(Chunk::id).toList()); }

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

    static String answer(String query, List<Chunk> context) {
        if (query.startsWith("list")) {
            List<String> figures = new ArrayList<>();
            Matcher m = FIGURE.matcher(String.join(" ", context.stream().map(Chunk::text).toList()));
            while (m.find()) {
                figures.add(m.group());
            }
            return figures.isEmpty() ? "no figures in the context" : String.join(", ", figures);
        }
        boolean quote = query.startsWith("quote");
        return String.join("; ", context.stream().map(c -> quote ? c.text() + " (" + TITLES.get(c.docId()) + ", " + c.docId() + ")" : c.text()).toList());
    }

    static Set<String> factsIn(String text) { return new LinkedHashSet<>(FACTS.stream().filter(text::contains).toList()); }

    // Scans the whole corpus, not the retrieved context, so the check does not depend on the mechanism under test.
    static Set<String> restrictedFor(String user) {
        Set<String> out = new LinkedHashSet<>();
        INDEX.stream().filter(c -> !mayRead(user, c.docId())).forEach(c -> out.addAll(factsIn(c.text())));
        return out;
    }

    static void section(String title) { System.out.println("-- " + title + " --"); }

    static void event(String src, String dst, String msg, String verdict) { System.out.println(String.format(Locale.ROOT, "[%02d] %s -> %s : %s%s", ++seq, src, dst, msg, verdict == null ? "" : " => " + verdict)); }

    static void check(String name, boolean pass, String detail) { System.out.println("CHECK " + name + " : " + (pass ? "PASS" : "FAIL") + "  (" + detail + ")"); }

    public static void main(String[] args) {
        GROUPS.put("rep-ana", set("support"));
        GROUPS.put("lead-omar", set("support", "contracts"));
        doc("doc-101", "Shipping Times", "support", "standard shipping takes 5 business days and express takes 2");
        doc("doc-102", "Returns Policy", "support", "customers may return items within 30 days for a refund to the original card");
        doc("doc-103", "Renewal Terms Agreement", "contracts",
            "renewal terms: the agreement renews on 2027-03-01 and the uplift is 12 percent",
            "notice period for this renewal is 90 days and an early exit costs 18000");
        doc("doc-104", "Renewal Handling Note", "support", "renewal questions are answered by the account team");
        doc("doc-105", "Pricing Exceptions Memo", "contracts", "this merchant holds a 7 percent discount through the renewal");
        doc("doc-106", "Billing Checks", "support", "check the invoice and the billing profile before you answer a billing question");
        doc("doc-107", "Gift Card Rules", "support", "gift cards never expire and can be used at any storefront");
        doc("doc-108", "Storefront Admin Tips", "support", "reset a storefront password from the admin console");
        String[][] script = {{"rep-ana", "rep-ana asks for a quote", "quote the notice period from the renewal terms agreement"},
            {"rep-ana", "rep-ana asks for a summary", "what are the renewal terms and notice period for this merchant"},
            {"rep-ana", "rep-ana asks for the figures", "list the dates and amounts from the renewal terms and notice period"},
            {"lead-omar", "lead-omar asks for a quote", "quote the notice period from the renewal terms agreement"}};
        int inContext = 0, unreadable = 0;
        Set<String> leaked = new LinkedHashSet<>(), leadFacts = new LinkedHashSet<>();

        section("Setup");
        event("indexer", "index", INDEX.size() + " chunks stored with text only, no acl field", "DONE");
        for (int i = 0; i < script.length; i++) {
            String user = script[i][0], title = script[i][1], query = script[i][2];
            section(title);
            event("insights", "retriever", user + " asks \"" + query + "\"", null);
            event("retriever", "index", "rank all " + INDEX.size() + " chunks, no filter", null);
            List<Chunk> context = rank(query, INDEX, K);   // whoever asks, the whole index is ranked
            event("index", "retriever", "top " + K + ": " + ids(context), null);
            int bad = (int) context.stream().filter(c -> !mayRead(user, c.docId())).count();
            inContext += bad;
            unreadable += (int) INDEX.stream().filter(c -> !mayRead(user, c.docId())).count();
            // EXPOSED and LEAKED are harness labels: the retriever never asks who is calling.
            event("retriever", "insights", K + " chunks join the model context",
                bad > 0 ? "EXPOSED (" + bad + " chunks " + user + " may not read)" : "OK (" + user + " may read all " + K + ")");
            List<String> docs = context.stream().map(Chunk::docId).distinct().toList();
            List<String> hidden = docs.stream().filter(d -> !mayRead(user, d)).toList();
            event("insights", "authz", "may " + user + " open " + String.join(", ", docs) + "?",
                hidden.isEmpty() ? "ALLOWED (all links shown)" : "DENIED (links hidden: " + String.join(", ", hidden) + ")");
            String text = answer(query, context);
            Set<String> restricted = restrictedFor(user);
            restricted.retainAll(factsIn(text));
            String shape = query.startsWith("quote") ? "quotes " : query.startsWith("list") ? "lists the figures in " : "restates ";
            event("insights", "caller", "answer " + shape + K + " chunks", restricted.isEmpty()
                ? "DELIVERED (" + factsIn(text).size() + " contract facts, none restricted)"
                : "LEAKED (" + String.join(", ", restricted) + " reached " + user + ")");
            LOG.add(user + " asked " + query.length() + " chars, answered " + text.length() + " chars");
            leaked.addAll(restricted);
            if (GROUPS.get(user).contains("contracts")) {
                leadFacts.addAll(factsIn(text));
            }
        }

        long logged = LOG.stream().filter(e -> INDEX.stream().anyMatch(c -> e.contains(c.id()))).count();
        System.out.println("METRIC restricted_chunks_in_context = " + inContext);
        System.out.println("METRIC restricted_facts_delivered = " + leaked.size());
        check("restricted-chunk-never-in-context", inContext == 0,
            inContext + " chunks the caller may not read reached the model across " + script.length + " questions");
        check("restricted-facts-absent-from-answer", leaked.isEmpty(),
            leaked.size() + " of " + restrictedFor("rep-ana").size() + " restricted facts reached rep-ana");
        check("acl-prefilter-before-ranking", unreadable == 0,
            unreadable + " of " + script.length * INDEX.size() + " scored chunks were unreadable by the caller");
        check("retrieval-decision-logged", logged == LOG.size(), logged + " of " + LOG.size() + " records name a chunk");
        check("legit-permitted-answer-works", leadFacts.containsAll(RENEWAL),
            RENEWAL.stream().filter(leadFacts::contains).count() + " of " + RENEWAL.size() + " renewal facts reached lead-omar");
    }
}
