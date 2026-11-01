import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

// One static API key is copied into five deployments. Meridian can only ask "is this the key?", so every
// holder is the same caller. FORGED is the harness's label for a call that carried a copied key; Meridian
// cannot see it and none of the checks read it.
public class Main {
    // origin and stolen are harness bookkeeping for the checks; Meridian never reads them.
    record Call(String origin, String key, String endpoint, int order, int amount, boolean stolen) {}
    record Entry(String caller, Call call) {}

    static final List<String> HOLDERS =
        List.of("gateway", "gateway-canary", "gateway-staging", "ops-agent", "report-batch");
    static final String SHARED_KEY = digest("meridian-shared-key");
    static final List<Entry> LOG = new ArrayList<>();
    static int seq = 0;

    static String digest(String text) {
        try {
            byte[] d = MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(d).substring(0, 8);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    // Meridian's whole authentication: the key matches, so the caller is logged as orbit.gateway.
    static boolean meridian(Call c) {
        if (!c.key().equals(SHARED_KEY)) {
            return false;
        }
        LOG.add(new Entry("orbit.gateway", c));
        return true;
    }

    static void section(String title) { System.out.println("-- " + title + " --"); }

    static void event(String src, String dst, String msg, String verdict) {
        String tail = verdict == null ? "" : " => " + verdict;
        System.out.println(String.format(Locale.ROOT, "[%02d] %s -> %s : %s%s", ++seq, src, dst, msg, tail));
    }

    static void check(String name, boolean pass, String ifPass, String ifFail) {
        System.out.println("CHECK " + name + " : " + (pass ? "PASS  (" + ifPass : "FAIL  (" + ifFail) + ")");
    }

    static void send(Call c) {
        boolean ok = meridian(c);
        String what = c.endpoint() + (c.order() > 0 ? " order " + c.order() + " $" + c.amount() : "")
            + " with key " + c.key();
        event(c.origin(), "meridian", what, !ok ? "REJECTED (key does not match)"
            : c.stolen() ? "FORGED (logged as orbit.gateway, sent by " + c.origin() + ")"
            : "ACCEPTED (logged as orbit.gateway)");
    }

    public static void main(String[] args) {
        section("One shared key, five deployments");
        send(new Call("gateway", SHARED_KEY, "POST /refunds", 5521, 35, false));
        send(new Call("ops-agent", SHARED_KEY, "GET /status", 0, 0, false));
        send(new Call("report-batch", SHARED_KEY, "GET /status", 0, 0, false));

        section("The least important copy leaks");
        send(new Call("report-batch", SHARED_KEY, "GET /status", 0, 0, true));
        send(new Call("report-batch", SHARED_KEY, "POST /refunds", 5530, 2400, true));

        Map<String, Set<String>> behind = new LinkedHashMap<>();
        for (Entry e : LOG) {
            behind.computeIfAbsent(e.caller(), k -> new LinkedHashSet<>()).add(e.call().origin());
        }
        int widest = behind.values().stream().mapToInt(Set::size).max().orElse(0);
        long copies = LOG.stream().filter(e -> e.call().stolen()).count();
        int lostUsd = LOG.stream().filter(e -> e.call().order() > 0 && !e.call().origin().equals("gateway"))
            .mapToInt(e -> e.call().amount()).sum();
        boolean legit = LOG.stream().anyMatch(e -> e.call().origin().equals("gateway") && e.call().order() == 5521);

        System.out.println("METRIC deployments_holding_key = " + HOLDERS.size());
        System.out.println("METRIC requests_accepted = " + LOG.size());
        System.out.println("METRIC identities_in_log = " + behind.size());
        System.out.println("METRIC unauthorized_refund_usd = " + lostUsd);
        check("legit-gateway-call-works", legit, "order 5521, $35", "order 5521 not refunded");
        check("distinct-identity-per-workload", widest == 1, behind.size() + " identities logged, one workload each",
            "one logged identity stands for " + widest + " workloads");
        check("stolen-credential-unusable-without-key", copies == 0, "no request with a copied key was accepted",
            copies + " requests with a copied key were accepted");
    }
}
