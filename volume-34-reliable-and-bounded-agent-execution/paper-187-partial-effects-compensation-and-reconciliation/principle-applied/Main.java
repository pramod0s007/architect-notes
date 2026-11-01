import java.util.*;

// Intent first, one declared compensation per tool, compensations with their own keys, and a read-only reconciler.
public class Main {
    record Effect(String run, String step, String tool, int order, int dollars, int t) { String id() { return run + "/" + step; } }

    static int seq = 0;
    static void section(String title) { System.out.println("-- " + title + " --"); }
    static void event(String src, String dst, String msg, String verdict) {
        System.out.println(String.format(Locale.ROOT, "[%02d] %s -> %s : %s => %s", ++seq, src, dst, msg, verdict));
    }
    static void log(String msg, String verdict) { event("orchestrator", "runlog", msg, verdict); }
    static void check(String name, boolean pass, String detail) {
        System.out.println("CHECK " + name + " : " + (pass ? "PASS" : "FAIL") + "  (" + detail + ")");
    }

    static final Map<Integer, Integer> credits = new LinkedHashMap<>(); // Meridian: order -> dollars issued
    static final List<Integer> spent = new ArrayList<>();               // credits the merchant already used
    static final List<String> mailbox = new ArrayList<>();              // what the merchant was emailed
    static final Map<Integer, Integer> books = new LinkedHashMap<>();   // finance ledger: order -> dollars booked
    static final Map<Effect, String> effects = new LinkedHashMap<>();   // ledger of intent: PLANNED, DONE or FAILED
    static final Set<String> keys = new LinkedHashSet<>();              // ledger of intent: compensations already run
    static final List<String> flags = new ArrayList<>();                // work handed to a person
    static final List<Integer> drift = new ArrayList<>();               // orders the reconciler reported
    static final List<String> landed = new ArrayList<>();               // harness record of what really completed, newest first
    static boolean ledgerUp = false;

    // One declared compensation per tool, true when the effect was really undone. A run refuses to start without one.
    static final Map<String, java.util.function.Predicate<Effect>> DECLARED = Map.of("credit", Main::reverseCredit, "email", Main::correct,
        "ledger-post", e -> books.remove(e.order()) != null); // not exercised below; lookup only, never iterated

    static boolean reverseCredit(Effect e) {
        boolean free = !spent.contains(e.order());
        if (free) credits.remove(e.order());
        event("gateway", "meridian", e.id() + "/comp reverse credit " + e.order(), free ? "COMPENSATED" : "REFUSED (credit already spent)");
        return free;
    }
    static boolean correct(Effect e) {
        mailbox.add("correction " + e.order());
        event("gateway", "mailer", e.id() + "/comp correction email for order " + e.order(), "SENT");
        return false; // an email cannot be unsent, so the caller flags a person
    }

    static void compensate(Effect e) {
        String key = e.id() + "/comp"; // run id and step id, never the wording of a message
        if (!keys.add(key)) { event("orchestrator", "gateway", "replay " + key + " after restart", "DEDUPED (key already used)"); return; }
        if (!DECLARED.get(e.tool()).test(e)) {
            flags.add(e.id() + " order " + e.order());
            log(e.id() + " cannot be undone, person flagged", "ESCALATED");
        }
    }

    static List<Effect> done(String run) {
        return effects.keySet().stream().filter(e -> e.run().equals(run) && effects.get(e).equals("DONE")).toList();
    }
    static List<String> undone(String run) { // steps that hold a compensation key, in the order they ran
        return keys.stream().filter(k -> k.startsWith(run + "/")).map(k -> k.split("/")[1]).toList();
    }

    static void unwind(String run) { // reads the ledger of intent, not local memory, so a restarted process unwinds the same way
        List<Effect> steps = new ArrayList<>(done(run));
        Collections.reverse(steps);
        steps.forEach(Main::compensate);
    }

    static void perform(Effect e) {
        String msg = e.id() + " " + e.tool() + " order " + e.order() + " $" + e.dollars() + " (t=" + e.t() + ")";
        if (e.tool().equals("ledger-post") && !ledgerUp) {
            effects.put(e, "FAILED");
            event("gateway", "ledger", msg + ", ledger down", "OUTAGE"); // stamped by the harness
            throw new IllegalStateException(e.id() + " ledger unavailable");
        }
        switch (e.tool()) {
            case "credit" -> { credits.put(e.order(), e.dollars()); event("gateway", "meridian", msg, "EXECUTED"); }
            case "email" -> {
                mailbox.add("notice " + e.order()); event("gateway", "mailer", msg, "SENT");
                spent.add(e.order()); // scripted: the merchant reads the notice and spends the credit
                event("meridian", "meridian",
                    "merchant applied credit " + e.order() + " to an invoice, $" + e.dollars() + " spent (t=" + (e.t() + 1) + ")", "DONE");
            }
            default -> { books.put(e.order(), e.dollars()); event("gateway", "ledger", msg, "RECORDED"); }
        }
        landed.add(0, e.id());
        effects.put(e, "DONE");
    }

    static String run(String id, int order, int dollars, int t, int tPost) {
        List<Effect> steps = List.of(new Effect(id, "s1", "credit", order, dollars, t),
            new Effect(id, "s2", "email", order, dollars, t + 1), new Effect(id, "s3", "ledger-post", order, dollars, tPost));
        for (Effect e : steps) if (!DECLARED.containsKey(e.tool())) throw new IllegalStateException("undeclared " + e.tool());
        steps.forEach(e -> effects.put(e, "PLANNED"));
        log("intent recorded for " + id + ": " + steps.size() + " effects planned, all compensations declared", "RECORDED");
        String status = "DONE";
        try {
            steps.forEach(Main::perform);
        } catch (IllegalStateException ex) {
            status = "FAILED";
            section("Compensation, last effect first");
            unwind(id);
        }
        long open = flags.stream().filter(f -> f.startsWith(id + "/")).count();
        log(id + " closed " + status + ": " + undone(id).size() + " compensations recorded, " + open + " flags open", "RECORDED");
        return status;
    }

    // Read-only: compares Meridian with the ledger of intent and the books. It reports and fixes nothing.
    static void reconcile() {
        credits.forEach((order, dollars) -> {
            boolean intended = effects.keySet().stream().anyMatch(e -> e.tool().equals("credit") && e.order() == order);
            boolean ok = intended && books.containsKey(order);
            if (!ok) drift.add(order);
            String why = ok ? "matches intent and ledger" : intended ? "has no ledger entry" : "has no intent entry";
            event("runlog", intended ? "ledger" : "meridian", "credit " + order + " $" + dollars + " " + why, ok ? "VERIFIED" : "DETECTED");
        });
    }

    public static void main(String[] args) {
        credits.put(5103, 45); // legacy credit issued before the ledger of intent existed
        section("Run 0417: intent first, then effects");
        String closed = run("run-0417", 5101, 60, 12, 21);
        section("A restart replays the unwind");
        int before = mailbox.size() + flags.size();
        unwind("run-0417");
        int extra = mailbox.size() + flags.size() - before;
        section("Run 0418: a complete run"); ledgerUp = true; // the ledger is back
        run("run-0418", 5102, 25, 30, 33);
        section("Reconciler (read-only)");
        reconcile();

        String want = String.join(" then ", landed.stream().filter(s -> s.startsWith("run-0417/")).map(s -> s.split("/")[1]).toList());
        String got = String.join(" then ", undone("run-0417"));
        // a spent credit still stands even if Meridian's record of it is gone
        int standing = (credits.containsKey(5101) || spent.contains(5101) ? 1 : 0) + (mailbox.contains("notice 5101") ? 1 : 0);
        long flagged = flags.stream().filter(f -> f.startsWith("run-0417/")).count();
        List<Integer> unbooked = credits.keySet().stream().filter(o -> !books.containsKey(o)).toList();
        long found = unbooked.stream().filter(drift::contains).count();
        int dollars = drift.stream().mapToInt(credits::get).sum(), full = done("run-0418").size(), comps = undone("run-0418").size();
        System.out.println("METRIC compensations_recorded = " + keys.size());
        System.out.println("METRIC flags_open = " + flags.size());
        System.out.println("METRIC drift_dollars_reported = " + dollars);
        check("partial-run-compensated-in-reverse", got.equals(want), "compensated " + got + ", newest completion first is " + want);
        check("irreversible-effect-flagged-not-hidden", flagged >= standing && closed.equals("FAILED"),
            standing + " effects still stand and " + flagged + " are flagged, run closed " + closed);
        check("compensation-is-idempotent", extra == 0, "the replay added " + extra + " emails and flags after " + keys.size() + " compensations");
        check("reconciler-finds-unrecorded-drift", found == unbooked.size(),
            found + " of " + unbooked.size() + " unbooked credits reported, $" + dollars + " in total");
        check("legit-complete-run-needs-no-compensation", full == 3 && comps == 0 && !drift.contains(5102),
            "run-0418 finished " + full + " of 3 effects with " + comps + " compensations, drift reported for it: " + drift.contains(5102));
    }
}
