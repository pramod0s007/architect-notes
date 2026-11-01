import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

// The tempting fix: wrap the steps in try/catch and, on failure, run the inverse of every completed
// step in reverse order. Errors from the inverses are ignored, as in most first attempts.
public class Main {
    static int seq = 0;
    static void section(String title) { System.out.println("-- " + title + " --"); }
    static void event(String src, String dst, String msg, String verdict) {
        String tail = verdict == null ? "" : " => " + verdict;
        System.out.println(String.format(Locale.ROOT, "[%02d] %s -> %s : %s%s", ++seq, src, dst, msg, tail));
    }
    static void check(String name, boolean pass, String ifPass, String ifFail) {
        System.out.println("CHECK " + name + " : " + (pass ? "PASS  (" + ifPass : "FAIL  (" + ifFail) + ")");
    }

    static final Map<Integer, Integer> credits = new LinkedHashMap<>(); // Meridian: order -> dollars issued
    static final List<Integer> spent = new ArrayList<>();               // credits the merchant already used
    static final List<String> mailbox = new ArrayList<>();              // what the merchant was emailed
    static final Map<Integer, Integer> books = new LinkedHashMap<>();   // finance ledger: order -> dollars booked
    static final List<String> committed = new ArrayList<>();            // harness view of what reached downstream
    static final List<String> runlog = new ArrayList<>();               // the only record the runner keeps
    static final List<String> undoCalls = new ArrayList<>();            // every inverse the runner attempted
    static final List<String> compensations = new ArrayList<>();        // inverses that actually took effect
    static final List<String> flags = new ArrayList<>();                // nothing in this design flags anything
    static boolean ledgerUp = false;

    static void credit(int order, int dollars, int t) {
        credits.put(order, dollars);
        committed.add(order + ":s1");
        event("gateway", "meridian", "s1 credit order " + order + " $" + dollars + " (t=" + t + ")", "EXECUTED");
    }

    static void email(int order, int t) {
        mailbox.add("notice " + order);
        committed.add(order + ":s2");
        event("gateway", "mailer", "s2 email notice for order " + order + " (t=" + t + ")", "SENT");
    }

    // Scripted interleaving, not a runner step: the merchant reads the notice and spends the credit.
    static void merchantSpends(int order, int t) {
        spent.add(order);
        event("meridian", "meridian", "merchant applied credit " + order + " to an invoice, $" + credits.get(order) + " spent (t=" + t + ")", "DONE");
    }

    static void post(int order, int dollars, int t) {
        String msg = "s3 ledger-post order " + order + " $" + dollars + " (t=" + t + ")";
        if (!ledgerUp) {
            event("gateway", "ledger", msg + ", ledger down", "OUTAGE"); // stamped by the harness, not by the runner
            throw new IllegalStateException("s3 ledger unavailable");
        }
        books.put(order, dollars);
        committed.add(order + ":s3");
        event("gateway", "ledger", msg, "RECORDED");
    }

    // The inverse of a step. The downstream system decides whether an inverse is possible at all.
    static void undo(String step, int order, int t) {
        undoCalls.add(order + ":" + step);
        if (step.equals("s2")) {
            event("gateway", "mailer", "undo s2, recall the notice for order " + order + " (t=" + t + ")", "REFUSED (mailer has no recall)");
            throw new UnsupportedOperationException("no recall");
        }
        if (spent.contains(order)) {
            event("gateway", "meridian", "undo s1, reverse credit " + order + " (t=" + t + ")", "REFUSED (credit already spent)");
            throw new UnsupportedOperationException("credit spent");
        }
        credits.remove(order);
        compensations.add(order + ":s1");
        event("gateway", "meridian", "undo s1, reverse credit " + order + " (t=" + t + ")", "ROLLED_BACK");
    }

    static void run(String id, int order, int dollars, int t, int tPost) {
        event("orchestrator", "gateway", "start " + id + ", goodwill credit $" + dollars + " for order " + order, "STARTED");
        List<String> done = new ArrayList<>();
        try {
            credit(order, dollars, t);
            done.add("s1");
            email(order, t + 1);
            done.add("s2");
            merchantSpends(order, t + 2);
            post(order, dollars, tPost);
            done.add("s3");
            runlog.add(id + " completed");
            event("orchestrator", "runlog", id + " marked completed", "DONE");
        } catch (IllegalStateException e) {
            int tick = tPost + 1;
            for (int i = done.size() - 1; i >= 0; i--) {
                try {
                    undo(done.get(i), order, tick++);
                } catch (UnsupportedOperationException ignored) {
                    // the inverse failed: the loop moves on and nobody is told
                }
            }
            // ROLLED_BACK is this runner's own claim. It is written whether or not anything was undone.
            runlog.add(id + " rolled back cleanly");
            event("orchestrator", "runlog", id + " rolled back cleanly", "ROLLED_BACK");
        }
    }

    // A drift report is a run-log line that starts with "drift" and names the order.
    static boolean reported(int order) {
        return runlog.stream().anyMatch(l -> l.startsWith("drift") && l.contains("order " + order));
    }

    public static void main(String[] args) {
        section("Run 0417: the ledger is down, the runner rolls back");
        run("run-0417", 5101, 60, 12, 21);
        section("Run 0418: the ledger is back");
        ledgerUp = true;
        run("run-0418", 5102, 25, 30, 33);
        section("Morning tie-out (harness audit)");
        int issued = credits.values().stream().mapToInt(Integer::intValue).sum();
        int booked = books.values().stream().mapToInt(Integer::intValue).sum();
        List<Integer> unbooked = credits.keySet().stream().filter(o -> !books.containsKey(o)).toList();
        String names = unbooked.stream().map(String::valueOf).collect(Collectors.joining(", "));
        // MISSED is stamped by the harness audit. Nothing in the runner raised it.
        event("ledger", "meridian", "tie-out: credits issued $" + issued + ", booked $" + booked + ", no ledger entry for order " + names, "MISSED");

        long completed = committed.stream().filter(c -> c.startsWith("5101:")).count();
        int standing = (credits.containsKey(5101) || spent.contains(5101) ? 1 : 0) + (mailbox.contains("notice 5101") ? 1 : 0);
        long found = unbooked.stream().filter(Main::reported).count();
        int landed = (credits.containsKey(5102) ? 1 : 0) + (mailbox.contains("notice 5102") ? 1 : 0) + (books.containsKey(5102) ? 1 : 0);
        System.out.println("METRIC effects_standing = " + standing);
        System.out.println("METRIC undo_calls = " + undoCalls.size());
        System.out.println("METRIC undo_calls_refused = " + (undoCalls.size() - compensations.size()));
        System.out.println("METRIC unbooked_dollars = " + unbooked.stream().mapToInt(credits::get).sum());
        check("partial-run-compensated-in-reverse", compensations.size() == completed,
            "all " + completed + " completed steps compensated",
            compensations.size() + " of " + completed + " completed steps compensated; "
                + (undoCalls.size() - compensations.size()) + " undo calls were refused and ignored");
        check("irreversible-effect-flagged-not-hidden", flags.size() >= standing,
            "all " + standing + " standing effects flagged",
            standing + " effects still stand, " + flags.size() + " flagged; the run log line is: " + runlog.get(0));
        check("reconciler-finds-unrecorded-drift", found == unbooked.size(),
            "all " + unbooked.size() + " unbooked credits reported",
            found + " of " + unbooked.size() + " unbooked credits appear in any drift report");
        long undo18 = undoCalls.stream().filter(u -> u.startsWith("5102:")).count();
        check("legit-complete-run-needs-no-compensation", landed == 3 && undo18 == 0,
            "run-0418 landed 3 of 3 steps and no undo was called for it",
            "run-0418 landed " + landed + " of 3 steps, " + undo18 + " undo calls for it");
    }
}
