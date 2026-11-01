import java.util.*;

// The approval is bound to the order version its approver saw. At execution the gateway re-reads the order,
// and Meridian applies the refund only if that version still holds, spending the approval in the same step.
public class Main {
    record Approval(String id, int order, int amount, String approver, int grantedAt, int boundVersion) {}
    record Call(String step, Approval approval) {}
    record Seen(int tick, int version) {}

    static final int NOW = 60, TTL = 48, MAX_AGE = 48;
    static final String UNCHANGED = "ap-3";
    // The only part of an order an approval can refer to: a version, bumped by every change and every refund.
    static final Map<Integer, Integer> VERSION = new LinkedHashMap<>();
    static final Map<String, Approval> APPROVALS = new LinkedHashMap<>();
    static final Map<Integer, Integer> FRESH = new LinkedHashMap<>();
    static final Set<String> CONSUMED = new LinkedHashSet<>(), DETECTED = new LinkedHashSet<>();
    static final List<String> LEDGER = new ArrayList<>();
    // Harness bookkeeping, never read by the gateway: the tick and the order version each approver saw.
    static final Map<String, Seen> SEEN = new LinkedHashMap<>();
    static int seq = 0;

    static String t(int tick) { return String.format(Locale.ROOT, "t=%02d", tick); }

    static void grant(String id, int order, int amount, int tick) {
        int version = VERSION.get(order);
        Approval a = new Approval(id, order, amount, "support-lead", tick, version);
        APPROVALS.put(id, a);
        SEEN.put(id, new Seen(tick, version));
        event("gateway", "runlog", id + " approved by " + a.approver() + " for refund order " + order + " $" + amount
            + " at " + t(tick) + ", bound to v" + version, "APPROVED");
    }

    static void change(int tick, int order, String what) {
        int v = VERSION.merge(order, 1, Integer::sum);
        event("meridian", "ledger", what + " on order " + order + " at " + t(tick) + ", v" + (v - 1) + " to v" + v, "RECORDED");
    }

    // Scripted worst-case stub: one call per approval, and after a timeout it repeats one of them.
    static List<Call> model() {
        List<Call> calls = new ArrayList<>();
        for (Approval a : APPROVALS.values()) {
            calls.add(new Call("s" + (calls.size() + 1), a));
            if (a.id().equals(UNCHANGED)) calls.add(new Call("s" + (calls.size() + 1), a));
        }
        return calls;
    }

    // Gate 1 is the time limit, kept as a second bound. Gate 2 re-reads the order and compares versions.
    static void gate(Call c, List<Call> cleared) {
        Approval a = c.approval();
        int current = VERSION.get(a.order()), age = NOW - a.grantedAt();
        String what = c.step() + " " + a.id() + " refund order " + a.order() + " $" + a.amount();
        if (age > TTL) {
            event("gateway", "runlog", what, "EXPIRED (age " + age + " over the " + TTL + "-tick limit)");
        } else if (current != a.boundVersion()) {
            DETECTED.add(a.id());
            event("gateway", "meridian", what + ", order re-read",
                "DETECTED (approved at v" + a.boundVersion() + ", order now v" + current + ")");
            FRESH.put(a.order(), current);
            event("gateway", "runlog", "fresh approval requested for order " + a.order() + " $" + a.amount()
                + ", bound to v" + current, "PENDING");
        } else {
            cleared.add(c);
            event("gateway", "meridian", what + ", order re-read", "VERIFIED (still at v" + current + ")");
        }
    }

    // Meridian's conditional write. Nothing runs between the check and the update; a real target needs a transaction.
    static void apply(Call c) {
        Approval a = c.approval();
        int current = VERSION.get(a.order());
        String what = c.step() + " " + a.id() + " refund order " + a.order() + " $" + a.amount() + " only if still at v"
            + a.boundVersion();
        if (CONSUMED.contains(a.id()) || current != a.boundVersion()) {
            String why = CONSUMED.contains(a.id()) ? "approval already spent" : "version moved";
            event("gateway", "meridian", what, "REJECTED (" + why + ", order at v" + current + ")");
            return;
        }
        CONSUMED.add(a.id());
        LEDGER.add(a.id());
        int version = VERSION.merge(a.order(), 1, Integer::sum);
        event("gateway", "meridian", what, "EXECUTED (order now v" + version + ")");
    }

    static void section(String title) { System.out.println("-- " + title + " --"); }

    static void event(String src, String dst, String msg, String verdict) {
        String tail = verdict == null ? "" : " => " + verdict;
        System.out.println(String.format(Locale.ROOT, "[%02d] %s -> %s : %s%s", ++seq, src, dst, msg, tail));
    }

    static void check(String name, boolean pass, String ifPass, String ifFail) {
        System.out.println("CHECK " + name + " : " + (pass ? "PASS  (" + ifPass : "FAIL  (" + ifFail) + ")");
    }

    public static void main(String[] args) {
        for (int order = 5101; order <= 5104; order++) VERSION.put(order, 1);

        section("Approvals are granted, each bound to the order version");
        grant("ap-1", 5101, 20, 3);
        grant("ap-2", 5102, 40, 20);
        grant("ap-3", 5103, 30, 21);
        section("Overnight: the world moves");
        change(30, 5102, "support rep refunded $50");
        change(41, 5102, "card dispute opened");
        section("Morning: one more approval, one more change");
        grant("ap-4", 5104, 25, 58);
        change(59, 5104, "support rep refunded $20");

        List<String> drifted = APPROVALS.values().stream()
            .filter(a -> VERSION.get(a.order()) != SEEN.get(a.id()).version()).map(Approval::id).toList();
        section("The run resumes at " + t(NOW) + ": every approval is re-read first");
        List<Call> calls = model();
        event("ops-agent", "gateway", "run-0417 resumes, the model proposes " + calls.size()
            + " refunds, one of them a repeat after a timeout", "QUEUED");
        List<Call> cleared = new ArrayList<>();
        for (Call c : calls) gate(c, cleared);
        section("Meridian applies a refund only if the version still holds");
        for (Call c : cleared) apply(c);

        Set<String> spent = new LinkedHashSet<>(LEDGER);
        int staleDollars = LEDGER.stream().filter(drifted::contains).mapToInt(id -> APPROVALS.get(id).amount()).sum();
        int repeatedDollars = LEDGER.stream().mapToInt(id -> APPROVALS.get(id).amount()).sum()
            - spent.stream().mapToInt(id -> APPROVALS.get(id).amount()).sum();
        long staleRan = drifted.stream().filter(spent::contains).count();
        long detected = drifted.stream().filter(DETECTED::contains).count();
        long refiled = drifted.stream().map(APPROVALS::get)
            .filter(a -> FRESH.getOrDefault(a.order(), -1).equals(VERSION.get(a.order()))).count();
        long tooOld = spent.stream().filter(id -> NOW - SEEN.get(id).tick() > MAX_AGE).count();

        System.out.println("METRIC refunds_applied = " + LEDGER.size());
        System.out.println("METRIC stale_refund_dollars = " + staleDollars);
        System.out.println("METRIC repeated_refund_dollars = " + repeatedDollars);
        System.out.println("METRIC fresh_approvals_requested = " + FRESH.size());
        check("stale-approval-rejected", staleRan == 0, "no refund ran against a changed order",
            staleRan + " of " + drifted.size() + " approvals on changed orders still refunded, $" + staleDollars);
        check("state-drift-detected-at-execution", detected == drifted.size(),
            "all " + drifted.size() + " changed orders detected", detected + " of " + drifted.size() + " changed orders detected");
        check("approval-consumed-atomically", LEDGER.size() == spent.size() && CONSUMED.containsAll(spent),
            "each approval produced one refund and was spent with it", "an approval paid out twice or ran unspent");
        check("mismatch-requests-fresh-approval", refiled == drifted.size(),
            "all " + drifted.size() + " changed orders have a fresh request bound to their current version",
            refiled + " of " + drifted.size() + " changed orders have a fresh request");
        check("expired-approval-rejected", tooOld == 0, "no refund ran on an approval older than " + MAX_AGE + " ticks",
            tooOld + " approvals older than " + MAX_AGE + " ticks still produced a refund");
        check("legit-unchanged-state-executes", spent.contains(UNCHANGED), "unchanged order 5103 was refunded under ap-3",
            "unchanged order 5103 was never refunded");
    }
}
