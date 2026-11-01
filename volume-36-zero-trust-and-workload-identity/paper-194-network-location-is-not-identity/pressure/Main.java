import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Meridian admits any caller whose address starts with 10. and never asks for a credential. ABUSED and
// MISSED are harness labels stamped by this script. No check reads them: the checks read the ledger,
// the log and the lease table. The red team acts exactly as scripted and no model is involved.
public class Main {
    record Lease(int tick, String address, String workload) {}
    record Entry(int tick, String endpoint, String address, String identity, boolean allowed) {}
    record Refund(int tick, int order, int amount, String address) {}

    static final String REFUNDS = "POST /refunds", STATUS = "GET /status", PAYOUT = "PUT /payout-destination";
    static final List<String> ENDPOINTS = List.of(REFUNDS, STATUS, PAYOUT);
    static final List<Integer> LEGIT = List.of(7702, 7703);
    static final List<Lease> leases = new ArrayList<>();
    static final List<Entry> log = new ArrayList<>();
    static final List<Refund> ledger = new ArrayList<>();
    static int seq = 0;

    static List<String> holders(String address) {
        return leases.stream().filter(l -> l.address().equals(address)).map(Lease::workload).distinct().toList();
    }

    static boolean executed(int order) { return ledger.stream().anyMatch(r -> r.order() == order); }
    static String t(int tick) { return String.format(Locale.ROOT, "t=%02d", tick); }

    static void lease(String actor, int tick, String address, String workload, String note) {
        String why = holders(address).isEmpty() ? "address never leased before"
            : "address held earlier by " + String.join(", ", holders(address));
        leases.add(new Lease(tick, address, workload));
        event(actor, actor, t(tick) + " " + note + ", orchestrator leases " + address, "STARTED (" + why + ")");
    }

    // Meridian's whole access rule is the address prefix; the caller's identity is never asked for.
    static void call(String src, int tick, String endpoint, String address, int order, int amount) {
        boolean ok = address.startsWith("10.");
        log.add(new Entry(tick, endpoint, address, null, ok));
        if (ok && endpoint.equals(REFUNDS)) ledger.add(new Refund(tick, order, amount, address));
        String label = !ok ? "DENIED" : src.equals("report-batch") && endpoint.equals(REFUNDS) ? "ABUSED" : "ALLOWED";
        String body = order > 0 ? " order " + order + " amount " + amount : "";
        event(src, "meridian", t(tick) + " " + endpoint + body + " from " + address + ", no credential presented",
            label + (ok ? " (address starts with 10.)" : " (address does not start with 10.)"));
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
        event("meridian", "meridian", "access rule loaded, admit any address starting with 10.",
            "OK (no credential is asked for)");
        lease("gateway", 1, "10.0.4.17", "orbit.gateway", "starts");
        section("Legitimate traffic");
        call("gateway", 2, REFUNDS, "10.0.4.17", 7702, 40);
        section("The address changes hands");
        lease("gateway", 4, "10.0.4.52", "orbit.gateway", "rescheduled");
        lease("report-batch", 5, "10.0.4.17", "reports.batch", "restarted");
        section("The red team calls Meridian");
        call("report-batch", 6, REFUNDS, "10.0.4.17", 7731, 2500);
        call("report-batch", 6, STATUS, "10.0.4.17", 0, 0);
        call("report-batch", 6, PAYOUT, "10.0.4.17", 0, 0);
        section("The gateway keeps working");
        call("gateway", 7, REFUNDS, "10.0.4.52", 7703, 40);

        Entry hit = log.stream().filter(e -> e.tick() == 6 && e.endpoint().equals(REFUNDS)).findFirst().orElseThrow();
        int sharing = holders(hit.address()).size();
        section("What the log can say");
        event("meridian", "meridian", "t=06 log line is " + hit.endpoint() + " from " + hit.address()
            + ", no workload recorded", "RECORDED (address only)");
        event("meridian", "meridian", hit.address() + " was held by " + sharing + " workloads, the log cannot say which",
            "MISSED (attribution lost; harness label)");

        long open = ENDPOINTS.stream().filter(ep -> log.stream()
            .anyMatch(e -> e.endpoint().equals(ep) && e.allowed() && e.identity() == null)).count();
        System.out.println("METRIC refunds_executed = " + ledger.size());
        System.out.println("METRIC unauthorized_refund_amount = "
            + ledger.stream().filter(r -> !LEGIT.contains(r.order())).mapToInt(Refund::amount).sum());
        System.out.println("METRIC endpoints_answering_without_credential = " + open);
        System.out.println("METRIC workloads_behind_logged_address = " + sharing);
        check("legit-gateway-works-from-any-address", LEGIT.stream().allMatch(Main::executed),
            "orders 7702, 7703 executed, but only because every 10. address is admitted",
            "a legitimate refund did not execute");
        check("unknown-workload-on-trusted-subnet-denied", !executed(7731),
            "order 7731 not executed", "order 7731 executed for 2500 from 10.0.4.17, a caller with no credential");
        check("identity-required-on-every-endpoint", open == 0,
            "every endpoint refused a caller with no credential",
            open + " of " + ENDPOINTS.size() + " endpoints answered a caller with no credential");
    }
}
