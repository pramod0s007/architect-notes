import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;

/**
 * Principle applied: every service accepts only a token issued for it and authorizes each call against
 * what that token carries. The gateway never uses its own reach; authz exchanges the caller's token for
 * a narrower one per backend. Signatures are assumed valid. The agent is the same scripted stub.
 */
public class Main {
    record Token(String sub, String act, String aud, Set<String> scopes, Set<Integer> merchants, int exp,
                 String chain) {}
    record Call(String to, Token token, int merchant, boolean ok) {}

    static final int NOW = 10;
    static final Map<String, String> ROWS = Map.of("atlas/3012", "212 orders", "atlas/4417", "1380 orders",
        "meridian/3012", "3 payouts, $18240", "meridian/4417", "5 payouts, $92600");
    static final Map<String, String> NEEDS = Map.of("atlas", "orders:read", "meridian", "payouts:read");
    static final Token CALLER = new Token("admin-3012", "agent:ops", "gateway",
        Set.of("orders:read", "payouts:read"), Set.of(3012), 60, "agent:ops");
    static final List<Token> ISSUED = new ArrayList<>(List.of(CALLER));
    static final List<Call> AT_GATEWAY = new ArrayList<>();
    static final List<Call> AT_BACKEND = new ArrayList<>();
    static int seq = 0;

    // True when a token carries the caller's user and nothing wider than the caller's merchants and scopes.
    static boolean within(Token t) {
        return t.sub().equals(CALLER.sub()) && CALLER.merchants().containsAll(t.merchants())
            && CALLER.scopes().containsAll(t.scopes());
    }

    // Every service, the gateway included, takes only a token issued for it, then checks that it covers the call.
    static String refuse(Token t, String me, String need, int merchant) {
        return !ISSUED.contains(t) ? "REJECTED (token was not issued by authz)"
            : !t.aud().equals(me) ? "REJECTED (aud=" + t.aud() + ", this service expects aud=" + me + ")"
            : t.exp() <= NOW ? "REJECTED (token expired)"
            : !t.scopes().contains(need) || !t.merchants().contains(merchant)
                ? "DENIED (token does not cover " + need + " for merchant " + merchant + ")" : null;
    }

    // Token exchange: authz narrows the caller's token to one backend, one scope and one merchant.
    static Token exchange(Token in, String backend, int merchant) {
        String need = NEEDS.get(backend);
        String ask = "exchange for " + backend + ": " + need + ", merchant " + merchant;
        String refusal = refuse(in, "gateway", need, merchant);
        if (refusal != null) {
            event("gateway", "authz", ask, refusal);
            return null;
        }
        Token out = new Token(in.sub(), "gateway", backend, Set.of(need), Set.of(merchant),
            Math.min(in.exp(), NOW + 15), in.chain() + ">gateway");
        ISSUED.add(out);
        event("gateway", "authz", ask, "ISSUED (aud=" + backend + ", exp=" + out.exp() + ")");
        return out;
    }

    // A backend serves only a token issued for it that covers the scope and the merchant.
    static boolean serve(String backend, Token t, int merchant, String note) {
        String need = NEEDS.get(backend);
        String refusal = refuse(t, backend, need, merchant);
        AT_BACKEND.add(new Call(backend, t, merchant, refusal == null));
        String what = need + ", merchant " + merchant + note;
        if (refusal != null) {
            event("gateway", backend, what, refusal);
            return false;
        }
        event("gateway", backend, what + ", as " + t.sub() + " via " + t.chain(),
            "ALLOWED (" + ROWS.get(backend + "/" + merchant) + ")");
        return true;
    }

    // The gateway authorizes against the caller's token, then exchanges it for one token per backend.
    static void gateway(Token t, int merchant) {
        String ask = "payout summary, merchant " + merchant + ", token aud=" + t.aud();
        String refusal = refuse(t, "gateway", "payouts:read", merchant);
        AT_GATEWAY.add(new Call("gateway", t, merchant, refusal == null));
        if (refusal != null) {
            event("agent:ops", "gateway", ask, refusal);
            return;
        }
        event("agent:ops", "gateway", ask, "ACCEPTED (merchant " + merchant + " is in the token)");
        boolean whole = true;
        for (String backend : List.of("atlas", "meridian")) {
            Token down = exchange(t, backend, merchant);
            whole = down != null && serve(backend, down, merchant, "") && whole;
        }
        if (whole) {
            event("gateway", "agent:ops", "report for merchant " + merchant, "DELIVERED");
        }
    }

    static void event(String src, String dst, String msg, String verdict) {
        System.out.println(String.format(Locale.ROOT, "[%02d] %s -> %s : %s => %s", ++seq, src, dst, msg, verdict));
    }

    static void check(String name, boolean pass, String detail) {
        System.out.println("CHECK " + name + " : " + (pass ? "PASS" : "FAIL") + "  (" + detail + ")");
    }

    // A defense passes only if the attack was attempted and never got through.
    static void guard(String name, String tried, long attempts, String got, long breaches) {
        check(name, attempts > 0 && breaches == 0, tried + ": " + attempts + ", " + got + ": " + breaches);
    }

    static long count(List<Call> calls, Predicate<Call> p) { return calls.stream().filter(p).count(); }

    public static void main(String[] args) {
        System.out.println("-- Setup --");
        event("authz", "agent:ops", "token sub=" + CALLER.sub() + " (act=" + CALLER.act() + ", aud=" + CALLER.aud()
            + ", merchants=" + new TreeSet<>(CALLER.merchants()) + ", scopes=" + new TreeSet<>(CALLER.scopes()) + ")",
            "ISSUED");
        System.out.println("-- A legitimate report --");
        gateway(CALLER, 3012);
        System.out.println("-- The model asks for another merchant --");
        gateway(CALLER, 4417);
        System.out.println("-- A gateway that forgot its own merchant check --");
        Token wide = exchange(CALLER, "meridian", 4417);
        if (wide != null) {
            serve("meridian", wide, 4417, ", no gateway check");
        }
        System.out.println("-- A downstream token is replayed at the gateway --");
        gateway(ISSUED.stream().filter(t -> t.aud().equals("meridian")).findFirst().orElseThrow(), 3012);
        System.out.println("-- The caller's token is forwarded to a backend unchanged --");
        serve("meridian", CALLER, 3012, ", forwarded unchanged");

        Predicate<Call> foreign = c -> !CALLER.merchants().contains(c.merchant());
        Predicate<Call> offAudience = c -> !c.token().aud().equals(c.to());
        long served = count(AT_BACKEND, Call::ok), leaked = count(AT_BACKEND, foreign.and(Call::ok));
        long own = AT_BACKEND.stream().filter(c -> c.ok() && c.merchant() == 3012).map(Call::to).distinct().count();
        System.out.println("METRIC foreign_reads_served = " + leaked);
        System.out.println("METRIC authz_exchanges = "
            + ISSUED.stream().filter(t -> t.act().equals("gateway")).count());
        System.out.println("METRIC backend_calls_served = " + served);
        guard("cross-merchant-read-denied", "foreign-merchant requests at the gateway", count(AT_GATEWAY, foreign),
            "backend reads served", leaked);
        guard("token-audience-enforced", "tokens for another audience sent to the gateway",
            count(AT_GATEWAY, offAudience), "accepted", count(AT_GATEWAY, offAudience.and(Call::ok)));
        guard("downstream-call-uses-caller-authority", "backend calls served", served,
            "wider than the caller", count(AT_BACKEND, c -> c.ok() && !within(c.token())));
        check("legit-own-merchant-report-works", own == 2,
            "backends that served the merchant 3012 report: " + own + " of 2");
        guard("token-passthrough-rejected", "tokens for another audience forwarded to a backend",
            count(AT_BACKEND, offAudience), "served", count(AT_BACKEND, offAudience.and(Call::ok)));
    }
}
