import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.stream.*;

// The gateway alone writes escaped, hash-chained entries; checkpoints go to an anchor store, raw payloads to a vault.
// The model is the scripted stub and the suite plays the insider. The checks read the stores, not the verdict labels.
public class Main {
    record Refund(int order, int amount, String subject, String approver, String customer, String policy) {
        String truth() { return approver.isEmpty() ? "own authority" : "approved by " + approver; }
        String payload() { return "order=" + order + ";amount=" + amount + ";customer=" + customer; }
    }

    static final List<Refund> DAY = List.of(
        new Refund(8841, 40, "ops-4", "", "ana@shop.example", "policy@p3"),
        new Refund(8850, 120, "ops-4", "", "ben@shop.example", "policy@p3"),
        new Refund(8852, 310, "ops-9", "fin-2", "cy@shop.example", "policy@p4"),
        new Refund(8860, 95, "ops-9", "fin-2", "di@shop.example", "policy@p4"),
        new Refund(8861, 30, "ops-4", "", "eli@shop.example", "policy@p4"));
    static final List<Refund> LEDGER = new ArrayList<>();        // Meridian's record of executed refunds
    static final List<String> AUDIT = new ArrayList<>();         // auditlog: one line per entry
    static final Map<Integer, String> ANCHOR = new TreeMap<>();  // anchor: a separate store, checkpoints only
    static final Map<String, String> VAULT = new TreeMap<>();    // raw payloads, erased when retention ends
    static String head = "genesis";
    static int seq = 0, entries = 0;

    static String sha(String s) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8))); }
        catch (java.security.NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }

    // Escape the separator, both newline characters and the escape character itself.
    static String esc(String s) { return s.replace("%", "%25").replace("|", "%7C").replace("\n", "%0A").replace("\r", "%0D"); }
    // The auditlog's only access rule: the gateway may append, nobody else.
    static boolean write(String principal, String line) { return principal.equals("gateway") && AUDIT.add(line); }
    static String body(String line) { return line.substring(0, line.lastIndexOf('|')); }
    static List<String[]> rows() { return AUDIT.stream().map(l -> l.split("\\|", -1)).toList(); }

    // Gateway: one entry per decision. The raw payload goes to the vault; the entry keeps digest and pointer.
    static void append(String subject, String chain, String tool, String payload, String decision, String policy) {
        int n = AUDIT.size() + 1;
        VAULT.put("vault/" + n, payload);
        String body = Stream.of("" + n, subject, chain, tool, sha(payload), decision, policy, "vault/" + n).map(Main::esc).collect(Collectors.joining("|"));
        head = sha(head + body);
        write("gateway", body + "|" + head);
        entries++;
        event("gateway", "auditlog", "seq " + n + " " + esc(tool) + " " + chain + " " + policy + " hash " + head.substring(0, 8), decision);
        if (n % 3 == 0) { ANCHOR.put(n, head); event("gateway", "anchor", "checkpoint seq " + n + " hash " + head.substring(0, 8), "RECORDED"); }
    }

    // Recompute every hash from genesis, the way anyone who can read the log can.
    static List<String> seal(List<String> lines) {
        List<String> out = new ArrayList<>();
        String prev = "genesis";
        for (String line : lines) { prev = sha(prev + body(line)); out.add(body(line) + "|" + prev); }
        return out;
    }

    // The chain catches edits that keep the old hashes; the anchor store catches rewrites and truncation.
    static String verify(List<String> lines) {
        List<String> sealed = seal(lines);
        for (int i = 0; i < lines.size(); i++) if (!sealed.get(i).equals(lines.get(i))) return "chain broken at seq " + (i + 1);
        for (var a : ANCHOR.entrySet()) {
            if (a.getKey() > lines.size()) return "truncated before anchored seq " + a.getKey();
            if (!lines.get(a.getKey() - 1).endsWith("|" + a.getValue())) return "anchor mismatch at seq " + a.getKey();
        }
        return "ok";
    }

    // The lawyer's question, answered from the stored entries and Meridian's ledger only.
    static List<String> reconstruct() {
        List<String> report = new ArrayList<>();
        for (Refund r : LEDGER) {
            for (String[] f : rows()) {
                if (f.length != 9 || !f[4].equals(sha(r.payload()))) continue;
                String[] chain = f[2].split(">");
                report.add(r.order() + " " + f[1] + " " + (chain.length > 2 ? "approved by " + chain[2] : "own authority") + " " + f[6]);
            }
        }
        return report;
    }

    static String seen(String result) { return result.equals("ok") ? "VERIFIED" : "DETECTED (" + result + ")"; }
    static void section(String title) { System.out.println("-- " + title + " --"); }

    static void event(String src, String dst, String msg, String verdict) {
        System.out.println(String.format(Locale.ROOT, "[%02d] %s -> %s : %s => %s", ++seq, src, dst, msg, verdict));
    }

    static void check(String name, boolean pass, String detail) {
        System.out.println("CHECK " + name + " : " + (pass ? "PASS" : "FAIL") + "  (" + detail + ")");
    }

    public static void main(String[] args) {
        section("Setup: only the gateway can write the audit store");
        boolean agentWrote = write("agent", "7|ops-4|ops-4>ops-agent|refund|0|ALLOWED|policy@p4|vault/7|0");
        event("agent", "auditlog", "append a line directly", agentWrote ? "ACCEPTED" : "REJECTED (only the gateway may write)");
        section("The day: five refunds and one hostile tool name");
        for (Refund r : DAY) {
            boolean within = r.amount() <= (r.policy().equals("policy@p3") ? 200 : 50);
            LEDGER.add(r);
            String chain = r.subject() + ">ops-agent" + (within ? "" : ">" + r.approver());
            append(r.subject(), chain, "refund", r.payload(), within ? "ALLOWED" : "APPROVED", r.policy());
        }
        String tool = "refund\nrefund ok order=9999 amount=500|APPROVED";
        String decision = tool.equals("refund") ? "ALLOWED" : "DENIED";
        event("agent", "gateway", "tool name \"" + tool.replace("\n", "<LF>") + "\"", decision.equals("DENIED") ? "DENIED (not a registered tool)" : decision);
        append("ops-4", "ops-4>ops-agent", tool, tool, decision, "policy@p4");
        section("Scheduled verification and the lawyer's question");
        event("suite", "auditlog", "scheduled verification at tick 7: " + AUDIT.size() + " entries, " + ANCHOR.size() + " anchors", seen(verify(AUDIT)));
        List<String> report = reconstruct();
        List<String> truth = LEDGER.stream().map(r -> r.order() + " " + r.subject() + " " + r.truth() + " " + r.policy()).toList();
        long own = report.stream().filter(s -> s.contains("own authority")).count();
        event("suite", "auditlog", "rebuild who acted for whom: " + own + " own authority, " + (report.size() - own) + " person-approved",
            report.equals(truth) ? "VERIFIED (matches the truth table)" : "MISSED (differs from the truth table)");
        section("Tampering is evident");
        List<String> edited = new ArrayList<>(AUDIT);
        edited.set(1, edited.get(1).replace("ops-4>ops-agent", "ops-4>ops-agent>fin-2"));
        String[][] attacks = {
            {"insider edits seq 2 to read approved by fin-2, hashes untouched", verify(edited)},
            {"insider edits seq 2 and recomputes every later hash", verify(seal(edited))},
            {"insider deletes the last two entries", verify(AUDIT.subList(0, 4))}};
        for (String[] a : attacks) event("suite", "auditlog", a[0], a[1].equals("ok") ? "MISSED" : seen(a[1]));
        section("Retention: erase a payload, keep the chain");
        event("suite", "gateway", "retention job erases the payload behind vault/1", VAULT.remove("vault/1") != null ? "REDACTED" : "MISSED");
        String after = verify(AUDIT);
        event("suite", "auditlog", "scheduled verification at tick 8", seen(after));

        String store = String.join("\n", AUDIT);
        long named = rows().stream().filter(f -> f.length == 9 && f[2].startsWith(f[1] + ">ops-agent") && f[6].startsWith("policy@")).count();
        long copies = DAY.stream().filter(r -> store.contains(r.customer())).count();
        long linked = rows().stream().filter(f -> VAULT.containsKey(f[7]) && sha(VAULT.get(f[7])).equals(f[4])).count();
        long physical = store.split("\n", -1).length;
        long covered = LEDGER.stream().filter(r -> rows().stream().anyMatch(f -> f[4].equals(sha(r.payload())))).count();
        long correct = truth.stream().filter(report::contains).count();
        System.out.println("METRIC refunds_in_ledger = " + LEDGER.size());
        System.out.println("METRIC customer_emails_in_audit_store = " + copies);
        System.out.println("METRIC refunds_attributed_correctly = " + correct);
        check("event-names-subject-and-actor", named == AUDIT.size(), named + " of " + AUDIT.size() + " entries carry subject, actor chain and policy version");
        check("tampering-detected-by-chain-verification", after.equals("ok") && Arrays.stream(attacks).noneMatch(a -> a[1].equals("ok")),
            "edit, rewrite and truncation: " + Arrays.stream(attacks).map(a -> a[1]).collect(Collectors.joining("; ")));
        check("agent-cannot-write-its-own-audit", !agentWrote && AUDIT.size() == entries, "agent append rejected, " + (AUDIT.size() - entries) + " foreign lines");
        check("payload-stored-as-digest-not-copy", copies == 0 && linked == VAULT.size() && after.equals("ok"),
            copies + " emails in the store, " + linked + " of " + VAULT.size() + " vault payloads match, " + (AUDIT.size() - VAULT.size()) + " erased, chain after erasure " + after);
        check("forged-newline-cannot-fake-entry", physical == entries, entries + " entries in " + physical + " physical lines");
        check("no-gap-in-audit-window", covered == LEDGER.size(), covered + " of " + LEDGER.size() + " refunds have an entry in the audit store");
        check("legit-refund-works", LEDGER.size() == DAY.size(), LEDGER.size() + " of " + DAY.size() + " refunds executed with the gateway in the path");
        check("legit-incident-reconstruction-works", report.equals(truth), correct + " of " + LEDGER.size() + " refunds attributed correctly");
    }
}
