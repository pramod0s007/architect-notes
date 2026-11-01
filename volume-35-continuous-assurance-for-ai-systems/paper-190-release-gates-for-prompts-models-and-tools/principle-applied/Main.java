import java.util.*;

public class Main {
    record Release(String name, String prompt, String model, String tools) {}
    record Ticket(String name, int ask, boolean late, int wanted, boolean critical) {}
    record Slot(String tenant, int tick) {}
    record Row(String release, String by, String approver, boolean granted) {}

    static final Map<String, String> BODY = Map.of("v7", "You are the Support Assistant. Refund only what the customer asks for.",
        "v8", "You are the Support Assistant. Refund only what the customer asks for. Close each ticket in one reply.",
        "t12", "refund(order, amount) max=50", "t13", "refund(order, amount) max=5000");
    static final Ticket LEGIT = new Ticket("legit-refund", 40, false, 40, false), INJECT = new Ticket("refund-injection", 4000, false, 0, true),
        DELAY = new Ticket("delay-complaint", 0, true, 0, false);
    static final List<Ticket> SUITE = List.of(LEGIT, INJECT, DELAY), WINDOW = List.of(DELAY, DELAY, DELAY, LEGIT);   // window: one canary slice of traffic per tenant
    static final Release R1 = new Release("R1", "v7", "m1", "t12"), R2 = new Release("R2", "v8", "latest", "t12"),
        R3 = new Release("R3", "v8", "m1", "t12"), R4 = new Release("R4", "v8", "m2", "t12"), R5 = new Release("R5", "v8", "m1", "t13");
    static final List<String> TENANTS = List.of("t1", "t2", "t3", "t4", "t5"), CANARY = List.of("t1");
    static final int MARGIN = 0;   // dollars of extra refunds the canary tolerates over the baseline

    static String alias = "m1";
    static Release baseline = R1;
    static int tick = -1, seq = 0;
    static final Map<String, Release> serving = new TreeMap<>();
    static final Map<String, TreeMap<Integer, String>> registry = new TreeMap<>();   // what the defense recorded, per tenant and tick
    static final Map<Slot, String> served = new LinkedHashMap<>();                    // harness truth: what answered, alias resolved
    static final Map<Release, Set<String>> exposed = new LinkedHashMap<>();           // release -> tenants that served it
    static final List<Release> cands = new ArrayList<>();
    static final List<Row> audit = new ArrayList<>();

    static String resolve(String model) { return model.equals("latest") ? alias : model; }
    static String pin(String text) {
        try { return HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(text.getBytes(java.nio.charset.StandardCharsets.UTF_8)), 0, 3); }
        catch (java.security.NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }
    static String manifest(Release r, boolean resolved) {   // digests pin prompt and tool bodies, the model is named by id; resolved=true shows what an alias answers today
        return "prompt@" + r.prompt() + "#" + pin(BODY.get(r.prompt())) + " model@" + (resolved ? resolve(r.model()) : r.model()) + " tools@" + r.tools() + "#" + pin(BODY.get(r.tools()));
    }
    static int gateway(Release r, int amount) { return amount <= Integer.parseInt(BODY.get(r.tools()).split("max=")[1]) ? amount : 0; }
    // Scripted stub, not a claim about real models: it obeys every ask, and prompt v8 on model m2 adds a 40 dollar goodwill refund to a late ticket.
    static int paid(Release r, Ticket t) {
        boolean goodwill = t.late() && BODY.get(r.prompt()).contains("one reply") && resolve(r.model()).equals("m2");
        return gateway(r, t.ask()) + (goodwill ? gateway(r, 40) : 0);
    }
    static boolean fails(Release r, Ticket t) { return paid(r, t) != t.wanted(); }
    static boolean critical(Release r) { return SUITE.stream().anyMatch(t -> t.critical() && fails(r, t)); }
    static int window(Release r) { return WINDOW.stream().mapToInt(t -> paid(r, t)).sum(); }
    static int reached(Release r) { return exposed.getOrDefault(r, Set.of()).size(); }
    static int tenants(List<Release> rs) { Set<String> s = new TreeSet<>(); rs.forEach(r -> s.addAll(exposed.getOrDefault(r, Set.of()))); return s.size(); }
    static String at(String tenant, int t) { var e = registry.getOrDefault(tenant, new TreeMap<>()).floorEntry(t); return e == null ? "nothing" : e.getValue(); }
    static boolean agrees(String tenant, int t) { return at(tenant, t).equals(served.get(new Slot(tenant, t))); }
    static void assign(String tenant, Release r) {
        serving.put(tenant, r);
        registry.computeIfAbsent(tenant, k -> new TreeMap<>()).put(tick, manifest(r, false));
    }
    static void section(String title) { System.out.println("-- Tick " + ++tick + ": " + title + " --"); }
    // Verdicts are the defense's own decisions; the checks at the end read the serving log, not these labels.
    static void event(String src, String dst, String msg, String verdict) {
        TENANTS.forEach(t -> { served.put(new Slot(t, tick), manifest(serving.get(t), true)); exposed.computeIfAbsent(serving.get(t), k -> new TreeSet<>()).add(t); });
        System.out.println(String.format(Locale.ROOT, "[%02d] %s -> %s : %s => %s", ++seq, src, dst, msg, verdict));
    }
    static void check(String name, boolean pass, String detail) { System.out.println("CHECK " + name + " : " + (pass ? "PASS" : "FAIL") + "  (" + detail + ")"); }
    static String gate(Release c) {   // suite gate: block a critical failure, or a scenario the baseline passes
        for (Ticket t : SUITE)
            if (fails(c, t) && (t.critical() || !fails(baseline, t)))
                return t.name() + (t.critical() ? " is critical and fails" : " regressed against " + baseline.name());
        return null;
    }

    static boolean override(Release c, String by, String approver) {   // a second approver who is not the requester, never for a critical failure
        boolean granted = !by.equals(approver) && !critical(c);
        audit.add(new Row(c.name(), by, approver, granted));
        event("release", "auditlog", by + " asks to override the suite gate for " + c.name() + ", second approver " + approver,
            granted ? "GRANTED" : "REJECTED (" + (critical(c) ? "a critical failure has no override path" : "self-approval") + ")");
        return granted;
    }

    static void canary(Release c) {   // the cohort serves c for one window; refunds above the baseline's plus MARGIN are a breach
        section(c.name() + " runs as a canary");
        CANARY.forEach(t -> assign(t, c));
        int got = CANARY.size() * window(c), base = CANARY.size() * window(baseline);
        boolean breach = got > base + MARGIN;
        event("gateway", "release", c.name() + " on " + CANARY.size() + " of " + TENANTS.size() + " tenants: refunds paid " + got
            + " dollars against " + base + " for " + baseline.name(), breach ? "ALERTED" : "OK");
        if (breach) {
            section(c.name() + " is rolled back");
            CANARY.forEach(t -> assign(t, baseline));
            event("release", "agent", "the cohort returns to " + baseline.name() + ", the regression reached " + reached(c) + " of " + TENANTS.size() + " tenants", "ROLLED_BACK");
            return;
        }
        section(c.name() + " is promoted");
        TENANTS.forEach(t -> assign(t, c));
        baseline = c;
        event("release", "registry", c.name() + " is promoted to all " + TENANTS.size() + " tenants and becomes the baseline", "RECORDED");
    }

    static void ship(Release c, String by, String... approvers) {
        cands.add(c);
        if (c.model().equals("latest")) {   // pin rule: a moving alias cannot go into a manifest
            section(c.name() + " names a moving alias");
            event("release", "registry", c.name() + " names model@" + c.model() + ", a label that moves under the manifest", "REJECTED");
            return;
        }
        section(c.name() + " meets the effect suite");
        String why = gate(c);
        event("suite", "release", c.name() + (why == null ? " passes all " + SUITE.size() + " scenarios, none regressed against " + baseline.name() : ": " + why),
            why == null ? "OK" : "BLOCKED");
        for (String a : approvers) if (why != null && override(c, by, a)) why = null;
        if (why == null) canary(c);
    }

    public static void main(String[] args) {
        section("Baseline release");
        TENANTS.forEach(t -> assign(t, R1));
        event("registry", "release", "R1 recorded for all " + TENANTS.size() + " tenants as " + manifest(R1, false), "RECORDED");
        ship(R2, "Dev");
        section("The provider moves the alias");
        alias = "m2";
        long follow = TENANTS.stream().filter(t -> serving.get(t).model().equals("latest")).count();
        event("registry", "agent", "the provider moves latest to m2, " + follow + " of " + TENANTS.size() + " tenants follow it", follow == 0 ? "ISOLATED" : "ACCEPTED");
        ship(R3, "Dev");
        ship(R4, "Dev", "Dev", "Lena");
        ship(R5, "Dev", "Lena");
        section("Sam asks what ran on t1 at tick 7");
        event("registry", "release", "t1 at tick 7 ran " + at("t1", 7) + ", the serving log " + (agrees("t1", 7) ? "agrees" : "disagrees"), agrees("t1", 7) ? "VERIFIED" : "MISSED");
        List<Release> gated = cands.stream().filter(c -> !c.model().equals("latest")).toList();
        List<Release> bad = gated.stream().filter(c -> SUITE.stream().anyMatch(t -> fails(c, t))).toList();
        List<Release> worse = gated.stream().filter(c -> window(c) > window(R1)).toList();
        long granted = audit.stream().filter(Row::granted).count();
        long self = audit.stream().filter(a -> a.granted() && a.by().equals(a.approver())).count();
        long leaked = bad.stream().filter(c -> reached(c) > 0 && (critical(c) || audit.stream().noneMatch(a -> a.release().equals(c.name()) && a.granted()))).count();
        long aliasServed = exposed.keySet().stream().filter(r -> r.model().equals("latest")).count();
        long stuck = serving.values().stream().filter(worse::contains).count();
        long r3fails = SUITE.stream().filter(t -> fails(R3, t)).count();
        long same = served.keySet().stream().filter(s -> agrees(s.tenant(), s.tick())).count();
        System.out.println("METRIC tenants_hit = " + tenants(bad));
        System.out.println("METRIC unwanted_refund_dollars = " + bad.stream().mapToInt(c -> reached(c) * (window(c) - window(R1))).sum());
        check("unpinned-alias-rejected", cands.size() > gated.size() && aliasServed == 0,
            (cands.size() - gated.size()) + " candidate named the alias latest, " + aliasServed + " of " + exposed.size() + " served releases name an alias");
        check("failing-effect-suite-blocks-release", !bad.isEmpty() && leaked == 0, bad.size() + " of " + gated.size()
            + " candidates fail the suite, " + leaked + " reached a tenant without a second approver or despite a critical failure");
        check("canary-regression-rolls-back", !worse.isEmpty() && tenants(worse) < TENANTS.size() && stuck == 0,
            worse.size() + " regressing candidate reached " + tenants(worse) + " of " + TENANTS.size() + " tenants and " + stuck + " serve it now");
        check("override-needs-second-approver", granted > 0 && self == 0,
            granted + " override granted, " + self + " approved by the requester, " + (audit.size() - granted) + " refused");
        check("legit-clean-release-ships", r3fails == 0 && reached(R3) == TENANTS.size(),
            "R3 fails " + r3fails + " scenarios and reached " + reached(R3) + " of " + TENANTS.size() + " tenants");
        check("past-release-reproducible", same == served.size(), same + " of " + served.size() + " tenant-ticks match the registry");
    }
}
