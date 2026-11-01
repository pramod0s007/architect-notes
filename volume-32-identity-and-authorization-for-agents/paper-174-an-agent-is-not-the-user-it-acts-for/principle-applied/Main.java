import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

// The agent has its own identity. authz exchanges the admin's session for a short-lived token naming the admin (sub)
// and the agent (act), narrowed to the task's scopes and one audience. APIs authorize the token and record the actor.
public class Main {
    record Token(String sub, String act, String aud, Set<String> scopes, Set<String> merchants, int exp) {}
    // "claimed" is the header as received; "by" is harness instrumentation (null = a person). No service reads it.
    record Entry(String sub, String act, String claimed, String op, String merchant, boolean done, String by) {}

    static int now = 10, seq = 0; // now: logical tick, moved by the harness; seq: event counter
    static final String ADMIN = "admin-3012";
    static final Map<String, Set<String>> TASK = new LinkedHashMap<>(); // reconcile: scopes allowed per audience
    static final Map<String, String> payout = new LinkedHashMap<>();
    static final List<Entry> audit = new ArrayList<>();

    static Set<String> of(String... v) { return new TreeSet<>(Arrays.asList(v)); }

    static boolean narrower(Token d, Token s) {
        return s.scopes().containsAll(d.scopes()) && d.scopes().size() < s.scopes().size()
            && s.merchants().containsAll(d.merchants()) && !d.aud().equals("*");
    }

    // authz: the subject comes from the verified session; the harness supplies the actor (a real authz takes it from the
    // authenticated workload). The grant is what the agent asked for, cut to the task's scopes and to the session's own.
    static Token exchange(Token session, String agent, Set<String> asked, String aud) {
        Set<String> granted = new TreeSet<>(asked);
        granted.retainAll(TASK.get(aud));
        granted.retainAll(session.scopes());
        return new Token(session.sub(), agent, aud, granted, session.merchants(), now + 5);
    }

    static String describe(Token t) {
        return "sub " + t.sub() + ", act " + t.act() + ", aud " + t.aud() + ", scopes " + String.join(" ", t.scopes())
            + ", exp " + t.exp();
    }

    // Every API decides on the token alone. The actor is recorded, and the header is logged as a claim.
    static boolean call(String by, String service, Token t, String header, String scope, String op, String merchant,
                        String value, String okVerdict) {
        String why = !(t.aud().equals(service) || t.aud().equals("*")) ? "audience " + t.aud() + " is not " + service
            : t.exp() <= now ? "token expired"
            : !t.merchants().contains(merchant) ? "merchant " + merchant + " not in token"
            : !t.scopes().contains(scope) ? "scope " + scope + " not in token" : null;
        boolean ok = why == null;
        audit.add(new Entry(t.sub(), t.act(), header, op, merchant, ok, by));
        if (ok && value != null) payout.put(merchant + ":" + op, value);
        String what = op + " " + merchant + (value == null ? "" : " " + value) + ", token " + t.sub()
            + (t.act() == null ? "" : " via " + t.act()) + (header == null ? "" : ", header " + header);
        event(by == null ? t.sub() : by, service, what,
            ok ? okVerdict : "DENIED (" + why + (header == null ? "" : "; header ignored") + ")");
        return ok;
    }

    static void section(String title) { System.out.println("-- " + title + " --"); }

    static void event(String src, String dst, String msg, String verdict) {
        System.out.println(String.format(Locale.ROOT, "[%02d] %s -> %s : %s => %s", ++seq, src, dst, msg, verdict));
    }

    static void check(String name, boolean pass, String detail) {
        System.out.println("CHECK " + name + " : " + (pass ? "PASS" : "FAIL") + "  (" + detail + ")");
    }

    public static void main(String[] args) {
        TASK.put("atlas", of("orders:read", "refunds:read"));
        TASK.put("meridian", of("payouts:read"));
        payout.put("3012:destination", "acct-3012-main");
        payout.put("3012:amount", "1200");
        payout.put("4417:destination", "acct-4417-main");
        Map<String, String> asked = new LinkedHashMap<>(payout);
        asked.put("3012:destination", "acct-3012-b");
        Token session = new Token(ADMIN, null, "*", of("orders:read", "payouts:read", "payouts:write"), of("3012"), 40);

        section("Setup");
        event(ADMIN, "authz", "sign in, session token for merchant 3012 with payouts:write", "ISSUED");
        call(null, "meridian", session, null, "payouts:write", "destination", "3012", "acct-3012-b", "EXECUTED");
        section("The agent exchanges the session for narrower tokens");
        Set<String> everything = of("orders:read", "payouts:read", "payouts:write", "refunds:read"); // all it can ask
        Token forAtlas = exchange(session, "agent:ops", everything, "atlas");
        event("agent:ops", "authz", "exchange the session for atlas, asking for " + everything.size() + " scopes",
            "ISSUED (" + describe(forAtlas) + ")");
        Token forMeridian = exchange(session, "agent:ops", everything, "meridian");
        event("agent:ops", "authz", "exchange the session for meridian, asking for " + everything.size() + " scopes",
            "ISSUED (" + describe(forMeridian) + ")");
        section("The task");
        boolean orders = call("agent:ops", "atlas", forAtlas, null, "orders:read", "read-orders", "3012", null, "ALLOWED");
        event("atlas", "agent:ops", "3 orders back, one note says to redirect payouts", "UNTRUSTED");
        boolean pays = call("agent:ops", "meridian", forMeridian, null, "payouts:read", "read-payouts", "3012", null, "ALLOWED");
        section("The model proposes (worst case)");
        call("agent:ops", "meridian", forMeridian, "admin-3012", "payouts:write", "destination", "3012", "acct-ext-909",
            "HIJACKED");
        call("agent:ops", "meridian", forMeridian, "admin-3012", "payouts:write", "amount", "3012", "4900", "ABUSED");
        call("agent:ops", "meridian", forMeridian, "admin-4417", "payouts:write", "destination", "4417", "acct-ext-909",
            "FORGED");
        call("agent:ops", "meridian", forMeridian, "admin-4417", "payouts:read", "read-payouts", "4417", null, "EXPOSED");
        section("A token meant for Atlas reaches Meridian");
        boolean wrongAudience = call("agent:ops", "meridian", forAtlas, null, "orders:read", "read-orders", "3012", null,
            "EXPOSED");
        section("Tick 15: the tokens have expired");
        now = 15;
        boolean afterExpiry = call("agent:ops", "meridian", forMeridian, null, "payouts:read", "read-payouts", "3012",
            null, "EXPOSED");
        section("Lena's question");
        List<Entry> changes = audit.stream()
            .filter(e -> e.done() && (e.op().equals("destination") || e.op().equals("amount"))).toList();
        long saidPerson = changes.stream().filter(e -> e.act() == null).count();
        long wasPerson = changes.stream().filter(e -> e.by() == null).count();
        long refused = audit.stream().filter(e -> !e.done() && "agent:ops".equals(e.act())).count();
        event("meridian", "meridian", "audit query: " + changes.size() + " payout change executed, " + saidPerson
            + " with no actor named; " + refused + " attempts logged as admin-3012 via agent:ops", "INFO");

        long drift = payout.entrySet().stream().filter(e -> !e.getValue().equals(asked.get(e.getKey()))).count();
        long agentCalls = audit.stream().filter(e -> e.by() != null).count();
        long named = audit.stream().filter(e -> e.by() != null && e.by().equals(e.act())).count();
        long right = audit.stream().filter(e -> e.sub().equals(ADMIN) && Objects.equals(e.act(), e.by())).count();
        long spoofs = audit.stream().filter(e -> e.claimed() != null && !e.claimed().equals(ADMIN)).count();
        long tookEffect = audit.stream().filter(e -> e.claimed() != null && !e.claimed().equals(ADMIN)
            && (e.done() || !e.sub().equals(ADMIN))).count();
        int reads = (orders ? 1 : 0) + (pays ? 1 : 0);
        int misuseRefused = (wrongAudience ? 0 : 1) + (afterExpiry ? 0 : 1);
        System.out.println("METRIC payout_changes_executed = " + changes.size());
        System.out.println("METRIC audit_says_person_made = " + saidPerson);
        System.out.println("METRIC truly_person_made = " + wasPerson);
        System.out.println("METRIC payout_fields_changed_wrongly = " + drift);
        System.out.println("METRIC agent_attempts_refused = " + refused);
        check("legit-delegated-read-works", reads == 2, reads + " of 2 reconciliation reads succeeded");
        check("agent-actions-attributed-to-agent", named == agentCalls,
            named + " of " + agentCalls + " agent calls name agent:ops");
        check("audit-records-subject-and-actor", right == audit.size(),
            right + " of " + audit.size() + " records carry the true subject and actor");
        check("spoofed-on-behalf-header-ignored", spoofs > 0 && tookEffect == 0,
            tookEffect + " of " + spoofs + " spoofed headers took effect");
        check("delegated-scope-narrower-than-session", narrower(forAtlas, session) && narrower(forMeridian, session),
            "tokens hold " + forAtlas.scopes().size() + " and " + forMeridian.scopes().size() + " of "
            + session.scopes().size() + " session scopes, audiences " + forAtlas.aud() + " and " + forMeridian.aud());
        check("wrong-audience-or-expired-token-refused", misuseRefused == 2,
            misuseRefused + " of 2 uses at the wrong audience or after expiry refused");
        check("no-unauthorized-payout-change", drift == 0,
            drift + " payout fields differ from what the admin asked for");
    }
}
