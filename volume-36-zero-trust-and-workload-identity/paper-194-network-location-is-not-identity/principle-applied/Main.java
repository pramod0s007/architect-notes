import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

// Authority comes from a verified workload identity checked against a per-endpoint policy. The address
// is only logged. A keyed digest stands in for the issuer's signature. Every verdict below comes from
// this code; nothing is stamped by the harness, and no model is involved.
public class Main {
    record Credential(String workload, String sig) {}
    record Lease(int tick, String address, String workload) {}
    record Entry(int tick, String endpoint, String address, String identity, boolean allowed) {}
    record Refund(int tick, int order, int amount, String address) {}

    static final String REFUNDS = "POST /refunds", STATUS = "GET /status", PAYOUT = "PUT /payout-destination";
    static final List<String> ENDPOINTS = List.of(REFUNDS, STATUS, PAYOUT);
    static final String GATEWAY = "orbit.gateway", BATCH = "reports.batch";
    static final String ISSUER_KEY = digest("halcyon-issuer-demo");
    static final List<Integer> LEGIT = List.of(7702, 7703, 7704);
    static final Map<String, List<String>> POLICY = new LinkedHashMap<>();
    static final List<Lease> leases = new ArrayList<>();
    static final List<Entry> log = new ArrayList<>();
    static final List<Refund> ledger = new ArrayList<>();
    static int seq = 0;

    static String digest(String text) {
        try {
            byte[] d = MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(d, 0, 8);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    static Credential issue(String workload) { return new Credential(workload, digest(ISSUER_KEY + "|" + workload)); }
    static boolean verifies(Credential c) { return c.sig().equals(digest(ISSUER_KEY + "|" + c.workload())); }

    static List<String> holders(String address) {
        return leases.stream().filter(l -> l.address().equals(address)).map(Lease::workload).distinct().toList();
    }

    // Harness ground truth for the checks: who held the address at that tick. Meridian never reads it.
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

    // The only source of authority is the verified credential; the address goes to the log and nowhere else.
    static void call(String src, int tick, String endpoint, String address, Credential cred, int order, int amount) {
        String who = cred == null ? "none" : verifies(cred) ? cred.workload() : "unverified";
        boolean ok = POLICY.getOrDefault(endpoint, List.of()).contains(who);
        String verdict = cred == null ? "DENIED (no credential)"
            : who.equals("unverified") ? "REJECTED (signature does not verify)"
            : ok ? "ALLOWED (" + who + " is in the policy)" : "DENIED (" + who + " is not in the policy)";
        log.add(new Entry(tick, endpoint, address, who, ok));
        if (ok && endpoint.equals(REFUNDS)) ledger.add(new Refund(tick, order, amount, address));
        String body = order > 0 ? " order " + order + " amount " + amount : "";
        String shown = cred == null ? "no credential" : "credential naming " + cred.workload();
        event(src, "meridian", t(tick) + " " + endpoint + body + " from " + address + ", " + shown, verdict);
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
        // The strictest policy the scripted legitimate traffic allows. PAYOUT has no line, so nobody is admitted.
        POLICY.put(REFUNDS, List.of(GATEWAY));
        POLICY.put(STATUS, List.of(BATCH));
        Credential gw = issue(GATEWAY), batch = issue(BATCH), forged = new Credential(GATEWAY, digest("guess|" + GATEWAY));
        section("Setup");
        event("issuer", "gateway", "credential naming " + GATEWAY, "ISSUED (signed by the issuer)");
        event("issuer", "report-batch", "credential naming " + BATCH, "ISSUED (signed by the issuer)");
        ENDPOINTS.forEach(ep -> event("meridian", "meridian", "policy " + ep + " admits "
            + String.join(", ", POLICY.getOrDefault(ep, List.of("no workload"))), "OK (anything not listed is denied)"));
        lease("gateway", 1, "10.0.4.17", GATEWAY, "starts");
        section("Legitimate traffic");
        call("gateway", 2, REFUNDS, "10.0.4.17", gw, 7702, 40);
        section("The address changes hands");
        lease("gateway", 4, "10.0.4.52", GATEWAY, "rescheduled");
        lease("report-batch", 5, "10.0.4.17", BATCH, "restarted");
        section("The red team calls Meridian from the recycled address");
        call("report-batch", 6, REFUNDS, "10.0.4.17", null, 7731, 2500);
        call("report-batch", 6, REFUNDS, "10.0.4.17", forged, 7733, 2500);
        call("report-batch", 6, STATUS, "10.0.4.17", batch, 0, 0);
        call("report-batch", 6, REFUNDS, "10.0.4.17", batch, 7732, 900);
        call("report-batch", 6, STATUS, "10.0.4.17", null, 0, 0);
        call("report-batch", 6, PAYOUT, "10.0.4.17", null, 0, 0);
        section("The gateway works from any address");
        call("gateway", 7, REFUNDS, "10.0.4.52", gw, 7703, 40);
        lease("gateway", 8, "10.20.1.5", GATEWAY, "rescheduled");
        call("gateway", 9, REFUNDS, "10.20.1.5", gw, 7704, 40);

        List<Entry> denials = log.stream().filter(e -> !e.allowed()).toList();
        List<String> seen = denials.stream().map(Entry::identity).distinct().toList();
        List<String> named = seen.stream().filter(i -> !i.equals("none") && !i.equals("unverified")).toList();
        List<String> sharing = holders("10.0.4.17");
        section("What the log can say");
        event("meridian", "meridian", denials.size() + " denials logged for 10.0.4.17, identities "
            + String.join(", ", seen), "RECORDED (address kept as a signal only)");
        long probed = ENDPOINTS.stream().filter(ep -> log.stream()
            .anyMatch(e -> e.endpoint().equals(ep) && e.identity().equals("none"))).count();
        long open = log.stream().filter(e -> e.identity().equals("none") && e.allowed()).count();
        List<String> late = ledger.stream().filter(r -> holderAt(r.address(), r.tick()).equals(BATCH))
            .map(r -> String.valueOf(r.order())).toList();
        boolean batchRead = log.stream()
            .anyMatch(e -> e.endpoint().equals(STATUS) && e.identity().equals(BATCH) && e.allowed());
        System.out.println("METRIC refunds_executed = " + ledger.size());
        System.out.println("METRIC unauthorized_refund_amount = "
            + ledger.stream().filter(r -> !LEGIT.contains(r.order())).mapToInt(Refund::amount).sum());
        System.out.println("METRIC denials_logged = " + denials.size());
        System.out.println("METRIC denials_naming_a_workload = "
            + denials.stream().filter(e -> named.contains(e.identity())).count());
        check("unknown-workload-on-trusted-subnet-denied", !executed(7731),
            "order 7731 from 10.0.4.17 not executed", "order 7731 executed from 10.0.4.17");
        check("reused-address-gives-no-authority", sharing.size() == 2 && late.isEmpty(),
            "10.0.4.17 held by " + sharing.size() + " workloads, no refund from the later holder",
            "order " + String.join(", ", late) + " executed for the later holder of 10.0.4.17");
        check("identity-required-on-every-endpoint", open == 0 && probed == ENDPOINTS.size(),
            probed + " of " + ENDPOINTS.size() + " endpoints refused a caller with no credential",
            open + " requests with no credential were admitted");
        check("legit-gateway-works-from-any-address", LEGIT.stream().allMatch(Main::executed),
            "orders 7702, 7703, 7704 executed from " + ledger.stream().filter(r -> LEGIT.contains(r.order()))
                .map(Refund::address).distinct().count() + " addresses", "a legitimate refund did not execute");
        check("denials-log-identity-not-just-address", !named.isEmpty() && sharing.size() > 1,
            "the policy denial names " + String.join(", ", named) + "; the address alone fits " + sharing.size()
                + " workloads", "no denial names a workload; the address alone fits " + sharing.size() + " workloads");
        check("forged-credential-rejected", !executed(7733),
            "order 7733 with a forged signature not executed", "order 7733 executed");
        check("legit-batch-status-read-allowed", batchRead, "reports.batch read " + STATUS + " with its own credential",
            "the status policy no longer admits reports.batch");
    }
}
