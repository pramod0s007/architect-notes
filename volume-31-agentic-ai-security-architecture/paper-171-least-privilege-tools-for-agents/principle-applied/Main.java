import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Principle applied: no admin tool. A broker builds each session's toolbox from the task and binds the merchant from the
 * login; a gateway checks every call against that grant. The model is the same worst-case stub, retrying after a refusal.
 */
public class Main {
    record Tool(String name, String verb, int maxAmount, int maxCalls) {}
    record Grant(String merchant, String task, List<Tool> tools, Map<String, Integer> used) {} // used: calls run, per tool
    record Proposal(String tool, String order, int amount) {
        public String toString() { return tool + "(" + (order == null ? "" : order + (amount > 0 ? ", " + amount : "")) + ")"; }
    }
    record Decision(String verdict, String detail) {}
    record Effect(String task, String verb, String owner, int amount) {}

    static final String ME = "m-204"; // the merchant in the login session
    static final int LISTINGS_AT_START = 6, ASKED = 40;
    static final int RULE_CAP = 100, RULE_CALLS = 2; // Halcyon's refund rule for one support session
    static final List<String> DESTRUCTIVE = List.of("delete", "prune", "reset");
    static final List<Tool> REGISTRY = List.of(
        new Tool("order_status", "status", 0, 3),
        new Tool("issue_refund", "refund", RULE_CAP, RULE_CALLS),
        new Tool("catalog_delete", "delete", 0, 1)); // exists for other callers; no support task is ever granted it
    static final Map<String, List<String>> TASK_VERBS = Map.of( // looked up, never iterated
        "order-status", List.of("status"), "refund", List.of("status", "refund"), "catalog-cleanup", List.of());
    static final Map<String, String> OWNER = new LinkedHashMap<>();     // order id -> merchant
    static final Map<String, String> STATUS = new LinkedHashMap<>();    // order id -> status
    static final Map<String, Integer> LISTINGS = new LinkedHashMap<>(); // merchant -> live listings
    static final List<Grant> GRANTS = new ArrayList<>();                // one per session, written by the broker
    static final List<Effect> EFFECTS = new ArrayList<>();              // written by the console, one per command that ran
    static int seq = 0, proposed = 0, refused = 0;

    // The toolbox comes from the task and the merchant from the login. The model supplies neither.
    static Grant grant(String task) {
        Grant g = new Grant(ME, task, REGISTRY.stream().filter(t -> TASK_VERBS.get(task).contains(t.verb())).toList(), new LinkedHashMap<>());
        GRANTS.add(g);
        String names = String.join(", ", g.tools().stream().map(Tool::name).toList());
        event("assistant", "broker", "toolbox for task " + task + ", merchant from the login",
            names.isEmpty() ? "DENIED (no agent tool exists for this task)" : "GRANTED (" + names + " for " + ME + ")");
        return g;
    }

    static Decision dispatch(Grant g, Proposal p) {
        Tool tool = g.tools().stream().filter(t -> t.name().equals(p.tool())).findFirst().orElse(null);
        if (tool == null) return new Decision("DENIED", p.tool() + " is not in this session's toolbox");
        if (p.order() != null && !g.merchant().equals(OWNER.get(p.order())))
            return new Decision("DENIED", "no order " + p.order() + " for " + g.merchant());
        if (p.amount() > tool.maxAmount()) return new Decision("CAPPED", "$" + p.amount() + " is above the $" + tool.maxAmount() + " cap");
        if (g.used().getOrDefault(tool.name(), 0) >= tool.maxCalls())
            return new Decision("THROTTLED", tool.name() + " budget of " + tool.maxCalls() + " calls is spent");
        g.used().merge(tool.name(), 1, Integer::sum);
        return new Decision("EXECUTED", console(g, tool, p));
    }

    // The console is unchanged and still powerful; the difference is that only the gateway calls it.
    static String console(Grant g, Tool tool, Proposal p) {
        String owner = p.order() == null ? g.merchant() : OWNER.get(p.order());
        EFFECTS.add(new Effect(g.task(), tool.verb(), owner, p.amount()));
        if (tool.verb().equals("status")) return STATUS.get(p.order());
        if (tool.verb().equals("refund")) return "refunded $" + p.amount();
        LISTINGS.put(owner, 0);
        return "catalog emptied";
    }

    // A goal lists phrasings in order; like an agent loop reading an error, the stub tries the next after a refusal.
    static String pursue(Grant g, List<Proposal> goal) {
        for (Proposal p : goal) {
            Decision d = dispatch(g, p);
            event("model", "gateway", p.toString(), d.verdict() + " (" + d.detail() + ")");
            proposed++;
            if (d.verdict().equals("EXECUTED")) return d.detail();
            refused++;
        }
        return null;
    }

    static void section(String title) {
        System.out.println("-- " + title + " --");
    }

    static void event(String src, String dst, String msg, String verdict) {
        String tail = verdict == null ? "" : " => " + verdict;
        System.out.println(String.format(Locale.ROOT, "[%02d] %s -> %s : %s%s", ++seq, src, dst, msg, tail));
    }

    static void check(String name, boolean pass, String ifPass, String ifFail) {
        System.out.println("CHECK " + name + " : " + (pass ? "PASS  (" + ifPass : "FAIL  (" + ifFail) + ")");
    }

    public static void main(String[] args) {
        OWNER.put("5521", ME);
        STATUS.put("5521", "shipped");
        OWNER.put("5512", "m-377");
        STATUS.put("5512", "delayed");
        LISTINGS.put(ME, LISTINGS_AT_START);

        section("Session 1: order-status");
        event("merchant", "assistant", "m-204 asks \"where is order 5521?\" (task: order-status)", null);
        Grant s1 = grant("order-status");
        String shipped = pursue(s1, List.of(new Proposal("order_status", "5521", 0)));  // right: own order
        pursue(s1, List.of(new Proposal("order_status", "5512", 0)));  // confused: another merchant's order
        pursue(s1, List.of(new Proposal("catalog_delete", null, 0), new Proposal("catalog_prune", null, 0)));  // manipulated: wipe
        pursue(s1, List.of(new Proposal("issue_refund", "5521", 4000)));  // manipulated: settle for $4000
        event("assistant", "merchant", "\"Order 5521 has " + shipped + ".\"", "DELIVERED");

        section("Session 2: refund");
        event("merchant", "assistant", "m-204 asks for a $" + ASKED + " refund on order 5521 (task: refund)", null);
        Grant s2 = grant("refund");
        pursue(s2, List.of(new Proposal("issue_refund", "5521", ASKED)));  // right: what was asked
        pursue(s2, List.of(new Proposal("issue_refund", "5521", 4000), new Proposal("issue_refund", "5521", RULE_CAP)));  // manipulated, then fits the cap
        pursue(s2, List.of(new Proposal("issue_refund", "5521", RULE_CAP)));  // keeps pushing
        List<Effect> refunds = EFFECTS.stream().filter(e -> e.verb().equals("refund")).toList();
        int largest = refunds.stream().mapToInt(Effect::amount).max().orElse(0);
        int refunded = refunds.stream().mapToInt(Effect::amount).sum();
        event("assistant", "merchant", "receipt: " + refunds.size() + " refunds, $" + refunded + " in all", "DELIVERED");

        section("Session 3: catalog-cleanup");
        event("merchant", "assistant", "m-204 asks \"clear my stale listings\" (task: catalog-cleanup)", null);
        if (grant("catalog-cleanup").tools().isEmpty())
            event("assistant", "support-team", "catalog cleanup for " + ME + " handed to a person", "ESCALATED");

        long foreign = EFFECTS.stream().filter(e -> !e.owner().equals(ME)).count();
        List<String> exposed = GRANTS.stream().flatMap(g -> g.tools().stream()).map(Tool::verb).distinct()
            .filter(DESTRUCTIVE::contains).toList();
        boolean ran = EFFECTS.stream().anyMatch(e -> DESTRUCTIVE.contains(e.verb()));
        long offScope = EFFECTS.stream().filter(e -> !TASK_VERBS.get(e.task()).contains(e.verb())).count();

        System.out.println("METRIC calls_proposed = " + proposed);
        System.out.println("METRIC calls_executed = " + EFFECTS.size());
        System.out.println("METRIC calls_refused = " + refused);
        System.out.println("METRIC destructive_verbs_reachable = " + exposed.size());
        System.out.println("METRIC foreign_records_touched = " + foreign);
        System.out.println("METRIC listings_lost = " + (LISTINGS_AT_START - LISTINGS.get(ME)));
        System.out.println("METRIC refunded_total = " + refunded);
        System.out.println("METRIC refund_requested = " + ASKED);
        System.out.println("METRIC refund_bound_per_session = " + RULE_CAP * RULE_CALLS);
        check("legit-order-status-works", "shipped".equals(shipped), "order 5521 answered: shipped", "order 5521 not answered");
        check("legit-refund-works", refunds.stream().anyMatch(e -> e.amount() == ASKED), "the $" + ASKED + " refund ran", "no refund ran");
        check("no-cross-merchant-access", foreign == 0, "no record of another merchant touched", "other merchants' records touched: " + foreign);
        check("no-destructive-tool-exposed", exposed.isEmpty() && !ran, "no granted tool carries a destructive verb, and none ran",
            "destructive verbs granted: " + exposed.size() + ", one ran: " + ran);
        check("tool-scope-matches-task", offScope == 0, "every executed call is a verb its task needs", "executed calls outside their task: " + offScope);
        check("per-tool-limits-enforced", largest <= RULE_CAP && refunds.size() <= RULE_CALLS,
            "refunds within " + RULE_CALLS + " calls of $" + RULE_CAP, "largest refund $" + largest + " over " + refunds.size() + " refunds");
    }
}
