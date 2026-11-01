import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

// The agent forwards the admin's own session token to every API, so Atlas and Meridian see only the admin.
// The harness stamps HIJACKED and ABUSED; the checks read the audit log and the payout state.
public class Main {
    record Token(String sub, String act, String aud, Set<String> scopes, Set<String> merchants, int exp) {}
    // "by" is harness instrumentation: who really started the call (null = a person). No service reads it.
    record Entry(String sub, String act, String op, String merchant, boolean done, String by) {}

    static final int NOW = 10;
    static final String ADMIN = "admin-3012";
    static final Map<String, String> payout = new LinkedHashMap<>();
    static final List<Entry> audit = new ArrayList<>();
    static int seq = 0;

    static Set<String> of(String... v) { return new TreeSet<>(Arrays.asList(v)); }

    static boolean narrower(Token d, Token s) {
        return s.scopes().containsAll(d.scopes()) && d.scopes().size() < s.scopes().size()
            && s.merchants().containsAll(d.merchants()) && !d.aud().equals("*");
    }

    // A service trusts what the token says: expiry, scope, merchant, and an audience of itself or "*".
    static boolean call(String by, String service, Token t, String scope, String op, String merchant,
                        String value, String okVerdict) {
        boolean ok = t.exp() > NOW && (t.aud().equals("*") || t.aud().equals(service))
            && t.scopes().contains(scope) && t.merchants().contains(merchant);
        audit.add(new Entry(t.sub(), t.act(), op, merchant, ok, by));
        if (ok && value != null) payout.put(merchant + ":" + op, value);
        String what = op + " " + merchant + (value == null ? "" : " " + value) + ", token " + t.sub();
        event(by == null ? t.sub() : by, service, what, ok ? okVerdict : "DENIED");
        return ok;
    }

    static void section(String title) { System.out.println("-- " + title + " --"); }

    static void event(String src, String dst, String msg, String verdict) {
        String tail = verdict == null ? "" : " => " + verdict;
        System.out.println(String.format(Locale.ROOT, "[%02d] %s -> %s : %s%s", ++seq, src, dst, msg, tail));
    }

    static void check(String name, boolean pass, String detail) {
        System.out.println("CHECK " + name + " : " + (pass ? "PASS" : "FAIL") + "  (" + detail + ")");
    }

    public static void main(String[] args) {
        payout.put("3012:destination", "acct-3012-main");
        payout.put("3012:amount", "1200");
        Map<String, String> asked = new LinkedHashMap<>(payout);
        asked.put("3012:destination", "acct-3012-b");
        Token session = new Token(ADMIN, null, "*", of("orders:read", "payouts:read", "payouts:write"), of("3012"), 40);
        Token agentToken = session; // the runtime hands the session token to the agent unchanged

        section("Setup");
        event(ADMIN, "authz", "sign in, session token for merchant 3012 with payouts:write", "ISSUED");
        call(null, "meridian", session, "payouts:write", "destination", "3012", "acct-3012-b", "EXECUTED");
        event(ADMIN, "agent:ops", "reconcile this week's orders, session token forwarded to every call", "ACCEPTED");

        section("The task");
        boolean orders = call("agent:ops", "atlas", agentToken, "orders:read", "read-orders", "3012", null, "ALLOWED");
        event("atlas", "agent:ops", "3 orders back, one note says to redirect payouts", "UNTRUSTED");
        boolean pays = call("agent:ops", "meridian", agentToken, "payouts:read", "read-payouts", "3012", null, "ALLOWED");

        section("The model proposes (worst case)");
        call("agent:ops", "meridian", agentToken, "payouts:write", "destination", "3012", "acct-ext-909",
            "HIJACKED (the token is the admin's own)");
        call("agent:ops", "meridian", agentToken, "payouts:write", "amount", "3012", "4900",
            "ABUSED (the write scope has no task limit)");

        section("Lena's question");
        List<Entry> changes = audit.stream()
            .filter(e -> e.done() && (e.op().equals("destination") || e.op().equals("amount"))).toList();
        long saidPerson = changes.stream().filter(e -> e.act() == null).count();
        long wasPerson = changes.stream().filter(e -> e.by() == null).count();
        event("meridian", "meridian", "audit query: " + changes.size() + " payout changes executed, " + saidPerson
            + " with no actor named", "INFO");

        long drift = payout.entrySet().stream().filter(e -> !e.getValue().equals(asked.get(e.getKey()))).count();
        long agentCalls = audit.stream().filter(e -> e.by() != null).count();
        long named = audit.stream().filter(e -> e.by() != null && e.by().equals(e.act())).count();
        long right = audit.stream().filter(e -> e.sub().equals(ADMIN) && Objects.equals(e.act(), e.by())).count();
        int reads = (orders ? 1 : 0) + (pays ? 1 : 0);
        System.out.println("METRIC payout_changes_executed = " + changes.size());
        System.out.println("METRIC audit_says_person_made = " + saidPerson);
        System.out.println("METRIC truly_person_made = " + wasPerson);
        System.out.println("METRIC payout_fields_changed_wrongly = " + drift);
        check("legit-delegated-read-works", reads == 2, reads + " of 2 reconciliation reads succeeded");
        check("agent-actions-attributed-to-agent", named == agentCalls,
            named + " of " + agentCalls + " agent calls name the agent");
        check("audit-records-subject-and-actor", right == audit.size(),
            right + " of " + audit.size() + " records carry the true subject and actor");
        check("delegated-scope-narrower-than-session", narrower(agentToken, session),
            "agent token holds " + agentToken.scopes().size() + " of " + session.scopes().size()
            + " session scopes, audience " + agentToken.aud());
        check("no-unauthorized-payout-change", drift == 0,
            drift + " payout fields differ from what the admin asked for");
    }
}
