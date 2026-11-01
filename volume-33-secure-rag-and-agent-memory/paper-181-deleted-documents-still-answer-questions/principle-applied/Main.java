import java.util.*;
import java.util.stream.*;

// Principle applied: every derived copy gets a lineage row, a delete fans out with receipts, every read passes a gate.
public class Main {
    static final List<String> STORES = List.of("chunk-store", "vector-index", "answer-cache");
    static final List<String> GONE = List.of("doc-101", "doc-102"); // the harness's own record of what was deleted
    static final int DELETED = 81, FANOUT = 84; // hours since Friday 00:00: Mon 09:00 and Mon 12:00
    record Art(String store, String id, String docId, String text) {}
    record Row(String store, String id, String docId) {}
    record Receipt(String store, int listed, int purged) {}
    static final List<Art> derived = new ArrayList<>();
    static final List<Row> lineage = new ArrayList<>();
    static final Set<String> kb = new LinkedHashSet<>();
    static int seq = 0, asked = 0, leaks = 0, lookups = 0, dropped = 0;

    // Toy embedding: the words of four or more characters. Similarity is how many of them a query shares.
    static Set<String> words(String s) {
        return Arrays.stream(s.toLowerCase(Locale.ROOT).split("[^a-z0-9]+")).filter(w -> w.length() > 3)
            .collect(Collectors.toCollection(TreeSet::new));
    }
    static int score(String q, Art v) { return (int) words(v.text()).stream().filter(words(q)::contains).count(); }
    static List<Art> in(String store) { return derived.stream().filter(a -> a.store().equals(store)).toList(); }

    // The only write path: every derived copy gets a lineage row that names its source.
    static void add(Art a) { derived.add(a); lineage.add(new Row(a.store(), a.id(), a.docId())); }
    static void index(String doc, String... chunks) {
        kb.add(doc);
        for (int i = 0; i < chunks.length; i++) {
            String id = doc + "#" + (i + 1);
            add(new Art("chunk-store", id, doc, chunks[i]));
            add(new Art("vector-index", id, doc, String.join(" ", words(chunks[i]))));
        }
    }
    static List<Art> retrieve(String q) {
        List<Art> top = in("vector-index").stream().filter(v -> score(q, v) >= 2)
            .sorted(Comparator.comparingInt((Art v) -> -score(q, v)).thenComparing(Art::id)).limit(2).toList();
        return top.stream().flatMap(v -> in("chunk-store").stream().filter(c -> c.id().equals(v.id()))).toList();
    }
    static Art cached(String q) { return in("answer-cache").stream().filter(a -> a.id().equals(q)).findFirst().orElse(null); }

    // Read gate: ask the system of record, once per candidate, whether the source row still exists.
    static List<Art> gate(List<Art> found) { return found.stream().filter(a -> kb.contains(a.docId())).toList(); }
    static List<Art> serve(String q) {
        asked++;
        Art c = cached(q);
        List<Art> found = c != null ? List.of(c) : retrieve(q), kept = gate(found);
        int out = found.size() - kept.size();
        lookups += found.size();
        dropped += out;
        leaks += kept.stream().anyMatch(a -> GONE.contains(a.docId())) ? 1 : 0;
        event(c != null ? "cache" : "retriever", "insights",
            "\"" + q + "\" " + found.size() + " found, gate dropped " + out + ", " + kept.size() + " delivered",
            out > 0 ? "BLOCKED (source row gone)" : kept.isEmpty() ? "OK (no live source to quote)" : "DELIVERED");
        return kept;
    }

    // Fan-out: remove every artifact the lineage lists for the deleted documents and return a receipt.
    static Receipt purge(String store, List<String> docs) {
        List<Row> rows = lineage.stream().filter(r -> r.store().equals(store) && docs.contains(r.docId())).toList();
        int purged = 0;
        for (Row r : rows) purged += derived.removeIf(a -> a.store().equals(r.store()) && a.id().equals(r.id())) ? 1 : 0;
        lineage.removeAll(rows);
        return new Receipt(store, rows.size(), purged);
    }

    // Audit scan: counts the real stores by source id, independent of the lineage table.
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

        section("Friday: every derived copy is written through one path that records its lineage");
        index("doc-101", "Master services agreement between Halcyon Commerce and merchant m3012 for payment processing.",
            "Termination clause: either party may terminate with sixty days written notice.",
            "Early termination fee equals three months of committed processing volume.");
        index("doc-102", "Ticket 4411: merchant disputed the early termination fee and asked for a written waiver.",
            "Ticket 4417: support agreed to review the termination fee dispute with account management.");
        index("doc-103", "Payouts settle two business days after capture and land in the merchant bank account.");
        event("indexer", "index", "3 documents chunked and embedded, " + in("chunk-store").size() + " chunks and "
            + in("vector-index").size() + " vectors written, each with a lineage row", "DONE");
        Art top = retrieve(clause).get(0);
        add(new Art("answer-cache", clause, top.docId(), top.text()));
        event("insights", "cache", "Friday answer to the clause question cached, lineage row to doc-101", "RECORDED");
        add(new Art("answer-cache", "summarize the ticket history", "doc-102",
            "The merchant disputes the early termination fee."));
        event("indexer", "cache", "summary of the ticket history cached, lineage row to doc-102, lineage now holds "
            + lineage.size() + " rows", "RECORDED");

        section("Monday 09:00: the merchant deletes doc-101 and doc-102");
        GONE.forEach(kb::remove);
        Map<String, Integer> held = copies(GONE);
        event("kb", "indexer", "doc-101 and doc-102 deleted, fan-out queued for Mon 12:00", "ACCEPTED");

        section("Monday 10:00: the fan-out has not run yet, so the read gate answers for it");
        serve(clause);
        serve(notice);

        section("Monday 12:00: the fan-out walks the lineage and each store returns a receipt");
        Map<String, Integer> receipts = new LinkedHashMap<>();
        boolean matched = true;
        for (String s : STORES) {
            Receipt r = purge(s, GONE);
            boolean ok = r.listed() == r.purged() && r.purged() == held.get(s);
            matched &= ok;
            receipts.put(s, r.purged());
            event("indexer", s.equals("answer-cache") ? "cache" : "index", "purge " + s + ": lineage lists " + r.listed()
                + ", store removed " + r.purged() + ", audit scan found " + held.get(s), ok ? "VERIFIED" : "ALERTED");
        }
        boolean complete = matched && receipts.equals(held);
        event("indexer", "kb", receipts.size() + " receipts compared with the audit scan of " + sum(held) + " copies",
            complete ? "DONE (deletion complete)" : "ALERTED (deletion stays open)");

        section("Wednesday 10:00: Orbit answers again");
        serve(clause);
        serve(notice);
        List<Art> pay = serve(payout);

        Map<String, Integer> left = copies(GONE);
        int total = sum(left), cacheLeft = left.get("answer-cache");
        System.out.println("METRIC answers_quoting_deleted_sources = " + leaks);
        System.out.println("METRIC derived_copies_still_live = " + total);
        System.out.println("METRIC purge_lag_hours = " + (FANOUT - DELETED));
        System.out.println("METRIC liveness_lookups = " + lookups);
        check("deleted-source-never-retrieved", leaks == 0,
            leaks + " of " + asked + " answers quoted a deleted source, the gate dropped " + dropped);
        check("deletion-reaches-every-derived-copy", total == 0,
            total + " derived copies remain " + left + ", " + sum(receipts) + " purged");
        check("cache-entry-purged", cacheLeft == 0, cacheLeft + " cache entries still cite a deleted document");
        check("legit-live-document-works", pay.stream().anyMatch(h -> h.docId().equals("doc-103")),
            "payout answered from " + pay.size() + " live chunk");
        check("deletion-receipts-complete", complete,
            receipts.size() + " of " + STORES.size() + " stores sent a receipt that matches the audit scan");
    }
}
