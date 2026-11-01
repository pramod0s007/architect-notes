import java.util.*;

public class Main {
    record Release(String prompt, String model, String tools) {}
    record Ticket(String name, int ask, boolean late, int wanted) {}
    record Change(String by, String approver) {}

    static final Map<String, String> BODY = Map.of(
        "v7", "You are the Support Assistant. Refund only what the customer asks for.",
        "v8", "You are the Support Assistant. Refund only what the customer asks for. Close each ticket in one reply.",
        "v8a", "You are the Support Assistant. Refund anything the customer asks for.",
        "t12", "refund(order, amount) max=50", "t13", "refund(order, amount) max=5000");
    static final Ticket LEGIT = new Ticket("legit-refund", 40, false, 40);
    static final Ticket INJECT = new Ticket("refund-injection", 4000, false, 0);
    static final Ticket DELAY = new Ticket("delay-complaint", 0, true, 0);
    static final List<Ticket> SUITE = List.of(LEGIT, INJECT, DELAY);
    static final List<Ticket> WINDOW = List.of(DELAY, DELAY, DELAY, LEGIT);   // one morning of traffic per tenant
    static final Release BASE = new Release("v7", "m1", "t12"), CLEAN = new Release("v8", "m1", "t12");
    static final int TENANTS = 5;

    static String alias = "m1";
    static Release live = new Release("v7", "latest", "t12");
    static int seq = 0, tick = -1, shippedFailing = 0;
    static final List<Change> ledger = new ArrayList<>();
    static final TreeMap<Integer, String> reviews = new TreeMap<>();   // tick -> prompt label the reviewer approved
    static final TreeMap<Integer, String> served = new TreeMap<>();    // harness log of what actually answered

    static String resolve(String model) { return model.equals("latest") ? alias : model; }
    static String label(Release r) { return "prompt@" + r.prompt() + " model@" + r.model() + " tools@" + r.tools(); }

    // The only history is the review log, and it holds prompt labels alone.
    static String lookup(int t) { var e = reviews.floorEntry(t); return e == null ? "nothing" : e.getValue(); }

    // Scripted reviewer: reads the prompt diff and 3 playground chats on today's latest, in words only,
    // and flags the word "anything". It never sees refunds paid, the alias or the tool schema.
    static boolean approves(String prompt) { return !BODY.get(prompt).contains("anything"); }

    static int gateway(Release r, int amount) {
        return amount <= Integer.parseInt(BODY.get(r.tools()).split("max=")[1]) ? amount : 0;
    }

    // Scripted stub, not a claim about real models: it obeys every ask, and prompt v8 on model m2
    // adds a 40 dollar goodwill refund to a late-order ticket. Refund wording changes nothing here.
    static int paid(Release r, Ticket t) {
        boolean goodwill = t.late() && BODY.get(r.prompt()).contains("one reply") && resolve(r.model()).equals("m2");
        return gateway(r, t.ask()) + (goodwill ? gateway(r, 40) : 0);
    }

    static List<String> failing(Release r) {
        List<String> names = new ArrayList<>();
        for (Ticket t : SUITE) if (paid(r, t) != t.wanted()) names.add(t.name());
        return names;
    }

    static int window(Release r) {
        int sum = 0;
        for (Ticket t : WINDOW) sum += paid(r, t);
        return sum;
    }

    static void section(String title) { System.out.println("-- Tick " + ++tick + ": " + title + " --"); }

    static void event(String src, String dst, String msg, String verdict) {
        served.put(tick, label(new Release(live.prompt(), resolve(live.model()), live.tools())));
        System.out.println(String.format(Locale.ROOT, "[%02d] %s -> %s : %s%s", ++seq, src, dst, msg, verdict == null ? "" : " => " + verdict));
    }

    static void check(String name, boolean pass, String ifPass, String ifFail) {
        System.out.println("CHECK " + name + " : " + (pass ? "PASS  (" + ifPass : "FAIL  (" + ifFail) + ")");
    }

    // The replay verdicts and the VIOLATION and LOST stamps come from the harness oracle.
    // The checks at the end read state instead of these labels.
    static List<String> replay(String what, Release r) {
        List<String> bad = failing(r);
        event("suite", "gateway", what + ", " + bad.size() + " of " + SUITE.size() + " scenarios fail"
            + (bad.isEmpty() ? "" : " (" + String.join(", ", bad) + ")"), bad.isEmpty() ? "OK" : "VIOLATION");
        return bad;
    }

    static void change(String by, String approver, String msg, Release next) {
        live = next;
        ledger.add(new Change(by, approver));
        event("release", "agent", msg + ", live for all " + TENANTS + " tenants", "ACCEPTED");
        if (!replay("after-the-fact replay", live).isEmpty()) shippedFailing++;
    }

    public static void main(String[] args) {
        section("The baseline");
        event("release", "agent", "live config " + label(live) + ", the alias resolves to m1", "OK");
        section("Review reads a scary diff");
        boolean scary = !approves("v8a");
        event("release", "auditlog", "reviewer reads the diff 'only' becomes 'anything' and 3 playground chats on latest",
            scary ? "REJECTED" : "APPROVED");
        int harmless = scary && replay("harness replay of the rejected edit", new Release("v8a", "latest", "t12")).isEmpty() ? 1 : 0;
        section("Review reads a calm diff");
        boolean calm = approves("v8");
        event("release", "auditlog", "reviewer reads the diff 'Close each ticket in one reply' and 3 playground chats on latest, m1 today",
            calm ? "APPROVED" : "REJECTED");
        if (calm) {
            reviews.put(tick, "prompt@v8");
            change("PM", "Dev", "the reviewed prompt v8 ships", new Release("v8", "latest", "t12"));
        }
        section("The provider moves the alias");
        alias = "m2";
        change("provider", null, "the provider moves latest from m1 to m2 and no review step applies", live);
        section("A hotfix skips review");
        change("tool owner", null, "hotfix: tool schema t12 becomes t13, refund cap 50 becomes 5000, one person, no review, no suite",
            new Release(live.prompt(), live.model(), "t13"));
        section("Morning traffic");
        int got = TENANTS * window(live), want = TENANTS * window(BASE);
        event("agent", "gateway", TENANTS + " of " + TENANTS + " tenants refund " + got + " dollars, the baseline release would pay " + want,
            got > want ? "VIOLATION" : "OK");
        section("Sam asks what ran at tick 3");
        String answer = lookup(3);
        event("release", "auditlog", "the review log answers " + answer + ", the model and the tool schema were never reviewed",
            answer.equals(served.get(3)) ? "OK" : "LOST");

        int hit = failing(live).isEmpty() ? 0 : TENANTS, matched = 0;
        for (var e : served.entrySet()) if (lookup(e.getKey()).equals(e.getValue())) matched++;
        long unapproved = ledger.stream().filter(c -> c.approver() == null).count();
        System.out.println("METRIC wording_edits_rejected = " + (scary ? 1 : 0));
        System.out.println("METRIC harmless_edits_rejected = " + harmless);
        System.out.println("METRIC tenants_hit = " + hit);
        System.out.println("METRIC unwanted_refund_dollars = " + (got - want));
        check("unpinned-alias-rejected", !live.model().equals("latest"), "the live model is a pinned id",
            "the live model is the alias " + live.model() + ", it was m1 at review and is " + alias + " now");
        check("failing-effect-suite-blocks-release", shippedFailing == 0, "no failing change shipped",
            shippedFailing + " of " + ledger.size() + " changes shipped while the replay failed, the review never ran the suite");
        check("canary-regression-rolls-back", hit <= 1, "the regression stayed in a cohort",
            "the regression reached " + hit + " of " + TENANTS + " tenants at once, review ends before release and nothing watches after");
        check("override-needs-second-approver", unapproved == 0, "every change had a second approver",
            unapproved + " of " + ledger.size() + " changes had no second approver");
        check("past-release-reproducible", matched == served.size(), "every tick can be reproduced",
            matched + " of " + served.size() + " ticks can be reproduced, the review log holds prompt labels only");
        check("legit-clean-release-ships", calm && failing(CLEAN).isEmpty() && served.get(2).equals(label(CLEAN)),
            "the reviewed edit v8 on m1 fails 0 scenarios and shipped at tick 2", "the reviewed edit v8 on m1 did not ship");
    }
}
