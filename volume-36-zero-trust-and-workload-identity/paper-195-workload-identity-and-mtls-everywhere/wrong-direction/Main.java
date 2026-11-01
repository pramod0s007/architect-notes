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

// Mutual TLS done properly, then one shortcut: Meridian accepts any certificate the company authority issued.
// Every workload has its own certificate and key, so attribution is right. What is missing is a rule for what a
// valid certificate may call. ABUSED is the harness's label for a refund that only orbit.gateway should make.
// Toy cryptography: a real verifier needs only the public key, not the keyring lookup used here.
public class Main {
    record Cert(String subject, String pub, String seal) {}
    record Workload(String name, String priv, Cert cert) {}
    // origin and stolen are harness bookkeeping for the checks; Meridian never reads them.
    record Call(String origin, String subject, String endpoint, int order, int amount, boolean stolen) {}

    static final Map<String, String> KEYRING = new LinkedHashMap<>();
    static final List<Call> ACCEPTED = new ArrayList<>();
    static int seq = 0;
    static int issued = 0;
    static int refused = 0;
    static int copied = 0;

    static String h(String... parts) {
        try {
            byte[] d = MessageDigest.getInstance("SHA-256")
                .digest(String.join("|", parts).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(d).substring(0, 8);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    // The issuer is correct by assumption here, so the only gap left is policy.
    static Workload enroll(String name, String subject) {
        String priv = h("priv", name);
        String pub = h("pub", priv);
        KEYRING.put(pub, priv);
        issued++;
        event("issuer", name, "certificate for " + subject, "ISSUED");
        return new Workload(name, priv, new Cert(subject, pub, h("seal", subject, pub)));
    }

    static String rejection(Cert c, String proof, String nonce) {
        if (!c.seal().equals(h("seal", c.subject(), c.pub()))) {
            return "certificate was not issued by the company authority";
        }
        return proof.equals(h("sig", KEYRING.getOrDefault(c.pub(), ""), nonce)) ? null
            : "no proof of the key bound to " + c.subject();
    }

    // Both ends are verified, then Meridian accepts the call. It has no policy.
    static void call(Workload server, Workload w, Cert shown, String endpoint, int order, int amount) {
        String nonce = "n" + (seq + 1);
        String what = endpoint + (order > 0 ? " order " + order + " $" + amount : "") + " presenting " + shown.subject();
        copied += shown.equals(w.cert()) ? 0 : 1;
        String reason = rejection(server.cert(), h("sig", server.priv(), nonce), nonce);
        if (reason == null) {
            reason = rejection(shown, h("sig", w.priv(), nonce), nonce);
        }
        if (reason != null) {
            refused++;
            event(w.name(), server.name(), what, "REJECTED (" + reason + ")");
            return;
        }
        ACCEPTED.add(new Call(w.name(), shown.subject(), endpoint, order, amount, !shown.equals(w.cert())));
        boolean abuse = order > 0 && !shown.subject().equals("orbit.gateway");
        event(w.name(), server.name(), what,
            abuse ? "ABUSED (valid certificate, and nothing says what it may call)" : "ACCEPTED");
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
        section("Each workload gets its own certificate");
        Workload gateway = enroll("gateway", "orbit.gateway");
        Workload batch = enroll("report-batch", "reports.batch");
        Workload meridian = enroll("meridian", "meridian.api");

        section("Mutual TLS, accept any company certificate");
        call(meridian, gateway, gateway.cert(), "POST /refunds", 5521, 35);

        section("A compromised batch job");
        call(meridian, batch, gateway.cert(), "POST /refunds", 5530, 2400);
        call(meridian, batch, batch.cert(), "POST /refunds", 5530, 2400);
        call(meridian, batch, batch.cert(), "GET /status", 0, 0);

        Map<String, Set<String>> behind = new LinkedHashMap<>();
        for (Call c : ACCEPTED) {
            behind.computeIfAbsent(c.subject(), k -> new LinkedHashSet<>()).add(c.origin());
        }
        int widest = behind.values().stream().mapToInt(Set::size).max().orElse(0);
        long replays = ACCEPTED.stream().filter(Call::stolen).count();
        List<Call> offside = ACCEPTED.stream()
            .filter(c -> c.order() > 0 && !c.subject().equals("orbit.gateway")).toList();
        String who = String.join(",", new LinkedHashSet<>(offside.stream().map(Call::subject).toList()));
        int offsideUsd = offside.stream().mapToInt(Call::amount).sum();
        int lostUsd = ACCEPTED.stream().filter(c -> c.order() > 0 && !c.origin().equals("gateway"))
            .mapToInt(Call::amount).sum();
        boolean legit = ACCEPTED.stream().anyMatch(c -> c.origin().equals("gateway") && c.order() == 5521);

        System.out.println("METRIC credentials_issued = " + issued);
        System.out.println("METRIC identities_in_log = " + behind.size());
        System.out.println("METRIC requests_accepted = " + ACCEPTED.size());
        System.out.println("METRIC requests_refused = " + refused);
        System.out.println("METRIC unauthorized_refund_usd = " + lostUsd);
        check("legit-gateway-call-works", legit, "order 5521, $35", "order 5521 not refunded");
        check("distinct-identity-per-workload", widest == 1, behind.size() + " identities logged, one workload each",
            "one logged identity stands for " + widest + " workloads");
        check("stolen-credential-unusable-without-key", replays == 0 && copied > 0,
            copied + " call with a copied certificate was refused", replays + " calls with a copied certificate were accepted");
        check("valid-cert-without-policy-denied", offside.isEmpty(), "no valid certificate outside orbit.gateway refunded",
            who + " refunded $" + offsideUsd + " with a valid certificate");
    }
}
