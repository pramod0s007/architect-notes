import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Meridian keeps an allow-list built once from the gateway's lease at t=01 and never refreshes it. ABUSED
// and MISSED are harness labels stamped by this script. No check reads them: the checks read the ledger,
// the log and the lease table. The red team acts exactly as scripted and no model is involved.
public class Main {
    record Lease(int tick, String address, String workload) {}
    record Entry(int tick, String endpoint, String address, String identity, boolean allowed) {}
    record Refund(int tick, int order, int amount, String address) {}

    static final String REFUNDS = "POST /refunds", GATEWAY = "orbit.gateway", BATCH = "reports.batch";
    static final List<Integer> LEGIT = List.of(7702, 7703);
    static final List<String> ALLOW = new ArrayList<>();
    static final List<Lease> leases = new ArrayList<>();
    static final List<Entry> log = new ArrayList<>();
    static final List<Refund> ledger = new ArrayList<>();
    static int seq = 0;

    static List<String> holders(String address) {
        return leases.stream().filter(l -> l.address().equals(address)).map(Lease::workload).distinct().toList();
    }

    // Harness ground truth for the checks and the last event: who held the address at that tick. Meridian never reads it.
    static String holderAt(String address, int tick) {
        return leases.stream().filter(l -> l.address().equals(address) && l.tick() <= tick)
            .reduce((a, b) -> b).map(Lease::workload).orElse("nobody");
    }

    static boolean executed(int order) { return ledger.stream().anyMatch(r -> r.order() == order); }
    static String t(int tick) { return String.format(Locale.ROOT, "t=%02d", tick); }

    static void lease(String actor, int tick, String address, String workload, String note) {
        String why = holders(address).isEmpty() ? "address never leased before"
            : "address held earlier by " + String.join(", ", holders(address));
        leases.add(new Lease(tick, address, workload));
        event(actor, actor, t(tick) + " " + note + ", orchestrator leases " + address, "STARTED (" + why + ")");
    }

    // Meridian's whole access rule is membership of the allow-list; the caller's identity is never asked for.
    static void call(String src, int tick, String address, int order, int amount) {
        boolean ok = ALLOW.contains(address);
        log.add(new Entry(tick, REFUNDS, address, null, ok));
        if (ok) ledger.add(new Refund(tick, order, amount, address));
        String label = !ok ? "DENIED" : src.equals("report-batch") ? "ABUSED" : "ALLOWED";
        event(src, "meridian", t(tick) + " " + REFUNDS + " order " + order + " amount " + amount + " from " + address,
            label + (ok ? " (address is on the allow-list)" : " (address is not on the allow-list)"));
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
        section("Setup");
        lease("gateway", 1, "10.0.4.17", GATEWAY, "starts");
        lease("report-batch", 1, "10.0.9.31", BATCH, "starts");
        ALLOW.add("10.0.4.17");
        event("meridian", "meridian", "allow-list built once from the gateway lease, holds " + String.join(", ", ALLOW),
            "OK (snapshot, never refreshed)");
        section("The allow-list at work");
        call("gateway", 2, "10.0.4.17", 7702, 40);
        call("report-batch", 3, "10.0.9.31", 7731, 2500);
        section("The address changes hands");
        lease("gateway", 4, "10.0.4.52", GATEWAY, "rescheduled");
        lease("report-batch", 5, "10.0.4.17", BATCH, "restarted");
        section("The same attack, and the real gateway");
        call("report-batch", 6, "10.0.4.17", 7731, 2500);
        call("gateway", 7, "10.0.4.52", 7703, 40);

        List<Entry> denials = log.stream().filter(e -> !e.allowed()).toList();
        List<String> truth = denials.stream().map(e -> holderAt(e.address(), e.tick())).toList();
        section("What the log can say");
        event("meridian", "meridian", denials.size() + " denials logged for "
            + String.join(" and ", denials.stream().map(Entry::address).toList()) + ", no workload named",
            "RECORDED (addresses only)");
        event("meridian", "meridian", "the denied callers were " + String.join(" and ", truth),
            "MISSED (the log names neither; harness label)");

        List<String> late = ledger.stream().filter(r -> holderAt(r.address(), r.tick()).equals(BATCH))
            .map(r -> String.valueOf(r.order())).toList();
        List<String> blocked = LEGIT.stream().filter(o -> !executed(o)).map(String::valueOf).toList();
        long unnamed = denials.stream().filter(e -> e.identity() == null).count();
        System.out.println("METRIC refunds_executed = " + ledger.size());
        System.out.println("METRIC unauthorized_refund_amount = "
            + ledger.stream().filter(r -> !LEGIT.contains(r.order())).mapToInt(Refund::amount).sum());
        System.out.println("METRIC legit_refunds_blocked = " + blocked.size());
        System.out.println("METRIC denials_naming_a_workload = " + (denials.size() - unnamed));
        check("unlisted-address-denied", log.stream().anyMatch(e -> e.tick() == 3 && !e.allowed()),
            "t=03 probe from 10.0.9.31 denied", "t=03 probe from 10.0.9.31 admitted");
        check("unknown-workload-on-trusted-subnet-denied", !executed(7731),
            "order 7731 not executed", "order 7731 executed for 2500 from 10.0.4.17, which is on the allow-list");
        check("reused-address-gives-no-authority", late.isEmpty(),
            "no refund came from a later holder of an allow-listed address",
            "10.0.4.17 held by " + holders("10.0.4.17").size() + " workloads, order " + String.join(", ", late)
                + " executed for the later one");
        check("legit-gateway-works-from-any-address", blocked.isEmpty(),
            "orders 7702, 7703 executed", "order " + String.join(", ", blocked) + " denied, the gateway moved to 10.0.4.52");
        check("denials-log-identity-not-just-address", unnamed == 0,
            "every denial names a workload", unnamed + " of " + denials.size() + " denials name only an address");
    }
}
