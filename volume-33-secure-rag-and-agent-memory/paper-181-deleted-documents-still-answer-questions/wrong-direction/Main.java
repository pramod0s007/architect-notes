import java.util.*;
import java.util.stream.*;

// Wrong direction: hide a card when its source id does not resolve, and clean up at night. The card is hidden but the
// answer is already text, and the job only knows two of the three stores.
public class Main {
    static final List<String> STORES = List.of("chunk-store", "vector-index", "answer-cache");
    static final List<String> GONE = List.of("doc-101", "doc-102"); // the harness's own record of what was deleted
    static final int DELETED = 81, NIGHTLY = 98; // hours since Friday 00:00: Mon 09:00 and Tue 02:00
    record Art(String store, String id, String docId, String text) {}
    static final List<Art> derived = new ArrayList<>();
    static final Set<String> kb = new LinkedHashSet<>();
    static int seq = 0, asked = 0, leaks = 0;

    // Toy embedding: the words of four or more characters. Similarity is how many of them a query shares.
    static Set<String> words(String s) {
        return Arrays.stream(s.toLowerCase(Locale.ROOT).split("[^a-z0-9]+")).filter(w -> w.length() > 3)
            .collect(Collectors.toCollection(TreeSet::new));
    }
    static int score(String q, Art v) { return (int) words(v.text()).stream().filter(words(q)::contains).count(); }
    static List<Art> in(String store) { return derived.stream().filter(a -> a.store().equals(store)).toList(); }

    static void index(String doc, String... chunks) {
        kb.add(doc);
        for (int i = 0; i < chunks.length; i++) {
            String id = doc + "#" + (i + 1);
            derived.add(new Art("chunk-store", id, doc, chunks[i]));
            derived.add(new Art("vector-index", id, doc, String.join(" ", words(chunks[i]))));
        }
    }
    static List<Art> retrieve(String q) {
        List<Art> top = in("vector-index").stream().filter(v -> score(q, v) >= 2)
            .sorted(Comparator.comparingInt((Art v) -> -score(q, v)).thenComparing(Art::id)).limit(2).toList();
        return top.stream().flatMap(v -> in("chunk-store").stream().filter(c -> c.id().equals(v.id()))).toList();
    }
    static Art cached(String q) { return in("answer-cache").stream().filter(a -> a.id().equals(q)).findFirst().orElse(null); }

    // Fix one: the interface resolves each cited source id and hides the card when it does not resolve.
    // The answer text was composed from the chunk before any card is drawn. The harness stamps LEAKED from GONE.
    static List<Art> ask(String q) {
        asked++;
        Art c = cached(q);
        List<Art> hits = c != null ? List.of(c) : retrieve(q);
        List<String> docs = hits.stream().map(Art::docId).distinct().toList();
        boolean leak = docs.stream().anyMatch(GONE::contains);
        leaks += leak ? 1 : 0;
        String what = c != null ? "answered from cache: \"" + c.text() + "\""
            : hits.isEmpty() ? "found nothing" : "returned " + String.join(", ", hits.stream().map(Art::id).toList());
        event(c != null ? "cache" : "retriever", "insights", "\"" + q + "\" " + what,
            leak ? "LEAKED (source deleted Mon 09:00)" : hits.isEmpty() ? "OK (no source to quote)" : "DELIVERED");
        long hidden = docs.stream().filter(d -> !kb.contains(d)).count();
        if (!docs.isEmpty()) event("insights", "kb", "card check for cited sources " + String.join(", ", docs),
            hidden > 0 ? "MASKED (" + hidden + " of " + docs.size() + " cards hidden)" : "OK (all cards shown)");
        return hits;
    }

    // Fix two: a nightly job drops chunks and vectors whose source row is gone. It was written when there were two stores.
    static Map<String, Integer> nightly() {
        Map<String, Integer> removed = new LinkedHashMap<>();
        for (String s : List.of("chunk-store", "vector-index")) {
            int before = in(s).size();
            derived.removeIf(a -> a.store().equals(s) && !kb.contains(a.docId()));
            removed.put(s, before - in(s).size());
        }
        return removed;
    }

    static Map<String, Integer> copies(List<String> docs) {
        Map<String, Integer> m = new LinkedHashMap<>();
        for (String s : STORES) m.put(s, (int) in(s).stream().filter(a -> docs.contains(a.docId())).count());
        return m;
    }
    static int sum(Map<String, Integer> m) { return m.values().stream().mapToInt(Integer::intValue).sum(); }
    static void section(String title) { System.out.println("-- " + title + " --"); }
    static void event(String src, String dst, String msg, String verdict) {
        System.out.println(String.format(Locale.ROOT, "[%02d] %s -> %s : %s => %s", ++seq, src, dst, msg, verdict));
    }
    static void check(String name, boolean pass, String detail) {
        System.out.println("CHECK " + name + " : " + (pass ? "PASS" : "FAIL") + "  (" + detail + ")");
    }

    public static void main(String[] args) {
        String clause = "what is the termination clause in our contract";
        String notice = "How much notice does termination need, and what was the fee dispute about?";
        String payout = "when do payouts settle after capture";

        section("Friday: the same pipeline writes the same derived copies");
        index("doc-101", "Master services agreement between Halcyon Commerce and merchant m3012 for payment processing.",
            "Termination clause: either party may terminate with sixty days written notice.",
            "Early termination fee equals three months of committed processing volume.");
        index("doc-102", "Ticket 4411: merchant disputed the early termination fee and asked for a written waiver.",
            "Ticket 4417: support agreed to review the termination fee dispute with account management.");
        index("doc-103", "Payouts settle two business days after capture and land in the merchant bank account.");
        event("indexer", "index", "3 documents chunked and embedded, " + in("chunk-store").size() + " chunks and "
            + in("vector-index").size() + " vectors written", "DONE");
        Art top = retrieve(clause).get(0);
        derived.add(new Art("answer-cache", clause, top.docId(), top.text()));
        event("insights", "cache", "Friday answer to the clause question cached, cites doc-101", "RECORDED");
        derived.add(new Art("answer-cache", "summarize the ticket history", "doc-102",
            "The merchant disputes the early termination fee."));
        event("indexer", "cache", "summary of the ticket history cached, cites doc-102", "RECORDED");

        section("Monday 09:00: the merchant deletes, two band-aids are in place");
        GONE.forEach(kb::remove);
        Map<String, Integer> held = copies(GONE);
        event("kb", "indexer", "doc-101 and doc-102 deleted, nightly cleanup of unresolved sources set for Tue 02:00", "QUEUED");

        section("Monday 10:00: the cards are hidden, the text is not");
        ask(clause);
        ask(notice);

        section("Tuesday 02:00: the nightly cleanup runs");
        Map<String, Integer> receipts = nightly();
        event("indexer", "index", "nightly cleanup removed " + receipts.get("chunk-store") + " chunks and "
            + receipts.get("vector-index") + " vectors with unresolved sources",
            "DONE (window was " + (NIGHTLY - DELETED) + " hours)");

        section("Wednesday 10:00: Orbit answers again");
        ask(clause);
        ask(notice);
        List<Art> pay = ask(payout);

        Map<String, Integer> left = copies(GONE);
        int total = sum(left), cacheLeft = left.get("answer-cache");
        System.out.println("METRIC window_hours = " + (NIGHTLY - DELETED));
        System.out.println("METRIC answers_quoting_deleted_sources = " + leaks);
        System.out.println("METRIC derived_copies_still_live = " + total);
        check("deleted-source-never-retrieved", leaks == 0, leaks + " of " + asked + " answers quoted a deleted source");
        check("deletion-reaches-every-derived-copy", total == 0,
            total + " derived copies of deleted documents remain " + left);
        check("cache-entry-purged", cacheLeft == 0, cacheLeft + " cache entries still cite a deleted document");
        check("legit-live-document-works", pay.stream().anyMatch(h -> h.docId().equals("doc-103")),
            "payout answered from " + pay.size() + " live chunk");
        check("deletion-receipts-complete", receipts.equals(held),
            receipts.size() + " of " + held.size() + " stores sent a receipt");
    }
}
