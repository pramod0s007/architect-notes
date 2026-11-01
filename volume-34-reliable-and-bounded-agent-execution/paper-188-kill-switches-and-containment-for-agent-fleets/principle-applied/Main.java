import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Principle: one gateway sits in front of every effect, reads the switch on each call and fails closed.
// DENIED (...) is the gateway's own decision text; VERIFIED, MISSED, CONTAINED and OVERRUN are stamped by the harness from its own data.
public class Main {
    record Fx(int tick, String agent, String tool, String tenant, int order, int amount) { boolean ops() { return agent.equals("ops-agent"); } }
    record Done(int tick, Fx fx) {}
    record Verdict(Fx fx, String why, boolean readable) {
        boolean allowed() { return why == null; }
        String word() { return allowed() ? "ALLOWED" : "DENIED (" + why + ")"; }
    }
    // A null field matches anything, so one record covers per-tool, per-agent, per-tenant and global scopes.
    record Stop(String agent, String tool, String tenant) {
        static boolean match(String want, String have) { return want == null || want.equals(have); }
        boolean covers(Fx f) { return match(agent, f.agent()) && match(tool, f.tool()) && match(tenant, f.tenant()); }
        String label() { return (agent == null ? "*" : agent) + "/" + (tool == null ? "*" : tool) + "/" + (tenant == null ? "*" : tenant); }
    }

    static final Stop GLOBAL = new Stop(null, null, null);
    static final int REFUND = 75, WORKER_RATE = 2, STOP_AT = 5, DARK_AT = 9;
    static final String[] TENANTS = {"m-31", "m-44", "m-58", "m-72"};
    static final List<Fx> LEGIT = List.of(
        new Fx(0, "support-agent", "refund", "m-31", 5201, 40), new Fx(6, "support-agent", "refund", "m-44", 5202, 40),
        new Fx(7, "insights-agent", "email", "m-58", 0, 0), new Fx(8, "support-agent", "email", "m-72", 0, 0),
        new Fx(9, "support-agent", "email", "m-31", 0, 0), new Fx(10, "support-agent", "email", "m-31", 0, 0)); // t=10 retries t=9
    static int order = 5101, seq = 0;

    // Every effect passes through here. The switch is read on each call; if it cannot be read, the call is denied.
    static class Gateway {
        final List<Stop> stops = new ArrayList<>();
        final List<Fx> queue = new ArrayList<>(), held = new ArrayList<>();
        final List<Done> ledger = new ArrayList<>();
        final List<Verdict> log = new ArrayList<>();
        boolean storeUp = true;

        String block(Fx f) {
            if (!storeUp) return "switch state unreadable, failing closed";
            return stops.stream().filter(s -> s.covers(f)).findFirst().map(s -> "stopped: scope " + s.label()).orElse(null);
        }

        Verdict submit(Fx f, int t) {
            Verdict v = new Verdict(f, block(f), storeUp);
            log.add(v);
            if (v.allowed() && f.tool().equals("refund")) queue.add(f);
            else if (v.allowed()) ledger.add(new Done(t, f));
            return v;
        }

        void stop(Stop s) {
            stops.add(s);
            held.addAll(queue.stream().filter(s::covers).toList());
            queue.removeIf(s::covers);
        }
    }

    static void drain(Gateway g, int t) { for (int i = 0; i < WORKER_RATE && !g.queue.isEmpty(); i++) g.ledger.add(new Done(t, g.queue.remove(0))); }
    static List<Done> opsDone(Gateway g, int from) { return g.ledger.stream().filter(d -> d.fx().ops() && d.tick() >= from).toList(); }
    static int ranAt(Gateway g, Fx f) { return g.ledger.stream().filter(d -> d.fx().equals(f)).mapToInt(Done::tick).findFirst().orElse(-1); }
    static String describe(Fx f) { return f.tool() + (f.order() > 0 ? " order " + f.order() + " $" + f.amount() : " digest") + " tenant " + f.tenant(); }
    static void section(String title) { System.out.println("-- " + title + " --"); }

    static void event(String src, String dst, String msg, String verdict) {
        String tail = verdict == null ? "" : " => " + verdict;
        System.out.println(String.format(Locale.ROOT, "[%02d] %s -> %s : %s%s", ++seq, src, dst, msg, tail));
    }

    static void check(String name, boolean pass, String ifPass, String ifFail) {
        System.out.println("CHECK " + name + " : " + (pass ? "PASS  (" + ifPass : "FAIL  (" + ifFail) + ")");
    }

    static void check(String name, boolean pass, String detail) { check(name, pass, detail, detail); }

    static void call(Gateway g, Fx f, int t) {
        Verdict v = g.submit(f, t);
        event("gateway", f.tool().equals("refund") ? "meridian" : "mailer", f.agent() + " " + describe(f) + " at t=" + t, v.word());
    }

    static void burst(Gateway g, int t) {
        List<Verdict> vs = new ArrayList<>();
        for (String m : TENANTS) vs.add(g.submit(new Fx(t, "ops-agent", "refund", m, order++, REFUND), t));
        event("ops-agent", "gateway", "t=" + t + ": proposed " + vs.size() + ", accepted " + vs.stream().filter(Verdict::allowed).count()
            + ", orders " + (order - vs.size()) + ".." + (order - 1), vs.get(0).word());
    }

    public static void main(String[] args) {
        section("Drill: rehearse every scope on a throwaway gateway");
        Fx canary = new Fx(0, "canary-agent", "email", "m-drill", 0, 0), spare = new Fx(0, "other-agent", "refund", "m-31", 0, 0);
        List<Stop> scopes = List.of(new Stop(null, "email", null), new Stop("canary-agent", null, null), new Stop(null, null, "m-drill"), GLOBAL);
        int drilled = 0;
        for (Stop s : scopes) {
            Gateway dg = new Gateway();
            dg.stop(s);
            boolean denied = !dg.submit(canary, 0).allowed(), spared = s == GLOBAL || dg.submit(spare, 0).allowed();
            dg.stops.clear();
            boolean released = dg.submit(canary, 0).allowed(), ok = denied && spared && released;
            drilled += ok ? 1 : 0;
            event("orchestrator", "gateway", "drill " + s.label() + ": canary denied=" + denied + ", other agent allowed="
                + (s == GLOBAL ? "n/a" : spared) + ", released=" + released, ok ? "VERIFIED" : "MISSED");
        }

        Gateway g = new Gateway();
        section("Setup: the same four tenants and the same bad prompt, now behind the gateway");
        event("orchestrator", "ops-agent", "start run-0417 to run-0420, one per tenant, prompt v18", "STARTED");
        call(g, LEGIT.get(0), 0); drain(g, 0);
        section("The same bad night");
        for (int t = 1; t < STOP_AT; t++) { burst(g, t); drain(g, t); }
        int pending = g.queue.size(), done = opsDone(g, 0).size();
        event("gateway", "meridian", "worker drains " + WORKER_RATE + " a tick: " + done + " done ($" + done * REFUND + "), " + pending + " still queued", "EXECUTED");
        section("The stop: scope ops-agent, enforced at the gateway");
        Stop incident = new Stop("ops-agent", null, null);
        g.stop(incident);
        int heldAtStop = g.held.size();
        event("orchestrator", "gateway", "stop " + incident.label() + " at t=" + STOP_AT + " with " + pending + " refunds queued", "STOPPED");
        event("gateway", "meridian", "sweep moves " + heldAtStop + " queued refunds to held, " + g.queue.size() + " left in the queue", "HELD");
        for (int t = STOP_AT; t <= DARK_AT + 1; t++) {
            if (t == DARK_AT) section("The honest cost: the switch store goes dark for one tick");
            g.storeUp = t != DARK_AT;
            burst(g, t);
            for (Fx f : LEGIT) if (f.tick() == t) call(g, f, t);
            drain(g, t);
        }
        int executedAfter = opsDone(g, STOP_AT).size();
        section("Outcome");
        event("gateway", "meridian", "refunds executed after the stop: " + executedAfter + ", held for review: " + heldAtStop
            + " ($" + heldAtStop * REFUND + ")", executedAfter == 0 ? "CONTAINED" : "OVERRUN");

        List<Verdict> afterStop = g.log.stream().filter(v -> v.fx().ops() && v.fx().tick() >= STOP_AT).toList();
        List<Verdict> dark = g.log.stream().filter(v -> !v.readable()).toList();
        List<Verdict> others = g.log.stream().filter(v -> v.readable() && !v.fx().ops() && v.fx().tick() >= STOP_AT).toList();
        long hit = afterStop.stream().filter(v -> v.readable() && !v.allowed()).count(), accepted = afterStop.stream().filter(Verdict::allowed).count();
        long spill = others.stream().filter(v -> !v.allowed()).count(), ran = others.stream().filter(v -> ranAt(g, v.fx()) >= 0).count();
        long blind = dark.stream().filter(Verdict::allowed).count(), opsDenied = g.log.stream().filter(v -> v.fx().ops() && !v.allowed()).count();
        System.out.println("METRIC refunds_executed_after_stop = " + executedAfter);
        System.out.println("METRIC ops_calls_denied = " + opsDenied);
        System.out.println("METRIC legit_calls_denied_during_store_outage = " + dark.stream().filter(v -> !v.fx().ops() && !v.allowed()).count());
        check("legit-refund-works", ranAt(g, LEGIT.get(0)) == 0, "order 5201, $40, done at t=0", "order 5201 not refunded");
        check("scoped-stop-halts-only-target", hit > 0 && spill == 0, hit + " ops-agent calls denied, " + spill + " other-agent calls denied");
        check("stop-takes-effect-on-next-call", !afterStop.isEmpty() && accepted == 0, accepted + " of " + afterStop.size() + " ops-agent calls after the stop were accepted");
        check("unknown-switch-state-fails-closed", !dark.isEmpty() && blind == 0, blind + " of " + dark.size() + " calls accepted while the switch was unreadable");
        check("queued-effects-drained-after-stop", pending > 0 && heldAtStop == pending && executedAfter == 0 && g.queue.stream().noneMatch(Fx::ops),
            heldAtStop + " of " + pending + " queued refunds held at the stop, " + executedAfter + " executed after it");
        check("legit-other-agents-keep-working", !others.isEmpty() && ran == others.size(), "all " + ran + " other-agent calls ran with a readable switch, the t="
            + LEGIT.get(1).tick() + " refund waited " + (ranAt(g, LEGIT.get(1)) - LEGIT.get(1).tick()) + " ticks", ran + " of " + others.size() + " other-agent calls ran");
        check("drill-covers-every-scope", drilled == scopes.size(), drilled + " of " + scopes.size() + " scopes drilled: tool, agent, tenant, global");
    }
}
