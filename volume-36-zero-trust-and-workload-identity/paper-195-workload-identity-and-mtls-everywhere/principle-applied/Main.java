import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

// Each workload gets its own identity only after the issuer matches the platform's facts about it. Both ends of
// every connection prove a key, and Meridian maps the verified identity to a per-endpoint policy that denies by default.
// Toy cryptography: a real verifier needs only the public key, not the keyring lookup used here.
public class Main {
    // facts come from the platform that started the workload, never from its request (made-up format).
    record Workload(String name, String id, String facts, String priv) {}
    record Cred(String subject, String pub, String seal) {}
    // origin and stolen are harness bookkeeping for the checks; Meridian never reads them.
    record Req(String origin, String subject, String endpoint, int order, int amount, boolean stolen, String verdict) {}

    static final Map<String, String> EXPECTED = new LinkedHashMap<>();
    static final Map<String, String> KEYRING = new LinkedHashMap<>();
    static final Map<String, Set<String>> POLICY = Map.of("POST /refunds", Set.of("orbit.gateway"),
        "GET /status", Set.of("reports.batch"));
    static final List<String[]> ISSUED = new ArrayList<>();
    static final List<Req> SEEN = new ArrayList<>();
    static final List<String> DELIVERED = new ArrayList<>();
    static int seq = 0;

    static String h(String... parts) {
        try {
            byte[] d = MessageDigest.getInstance("SHA-256").digest(String.join("|", parts).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(d).substring(0, 8);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    static Workload deploy(String name, String id) {
        String facts = "ns=" + id.substring(0, id.indexOf('.')) + " img=" + h("image", name);
        Workload w = new Workload(name, id, facts, h("priv", name));
        EXPECTED.put(id, facts);
        KEYRING.put(h("pub", w.priv()), w.priv());
        return w;
    }

    // The issuer compares the facts recorded for the requester with the record for the identity it asks for.
    static Cred request(Workload w, String wanted) {
        String ask = "asks for " + wanted + " with " + w.facts();
        if (!w.facts().equals(EXPECTED.get(wanted))) {
            event(w.name(), "issuer", ask, "REJECTED (facts do not match the " + wanted + " record)");
            return null;
        }
        String pub = h("pub", w.priv());
        ISSUED.add(new String[] {wanted, w.id()});
        event(w.name(), "issuer", ask, "ISSUED");
        return new Cred(wanted, pub, h("seal", wanted, pub));
    }

    // One side of the handshake: the credential must carry the issuer's seal, the prover must show the key it is
    // bound to, and a caller that dials a named peer also checks the subject.
    static String auth(Workload prover, Cred c, String nonce, String expect) {
        if (!c.seal().equals(h("seal", c.subject(), c.pub()))) {
            return "credential was not issued by the company authority";
        }
        if (!h("sig", prover.priv(), nonce).equals(h("sig", KEYRING.getOrDefault(c.pub(), ""), nonce))) {
            return "no proof of the key bound to " + c.subject();
        }
        return expect == null || c.subject().equals(expect) ? null : "peer presents " + c.subject() + ", expected " + expect;
    }

    // The caller verifies the server first. The server then verifies the caller and applies the policy.
    static void call(Workload server, Cred serverCred, Workload w, Cred shown, String endpoint, int order, int amount) {
        String nonce = "n" + (seq + 1);
        String what = endpoint + (order > 0 ? " order " + order + " $" + amount : "");
        String bad = auth(server, serverCred, nonce, "meridian.api");
        if (bad == null) {
            DELIVERED.add(serverCred.subject());
            what += " presenting " + shown.subject();
            bad = auth(w, shown, nonce, null);
        }
        String verdict = bad != null ? "REJECTED (" + bad + ")"
            : POLICY.getOrDefault(endpoint, Set.of()).contains(shown.subject()) ? "ACCEPTED"
            : "DENIED (" + shown.subject() + " is not allowed on " + endpoint + ")";
        boolean copied = !shown.pub().equals(h("pub", w.priv()));
        SEEN.add(new Req(w.name(), shown.subject(), endpoint, order, amount, copied, verdict.split(" ")[0]));
        event(w.name(), server.name(), what, verdict);
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
        Workload gateway = deploy("gateway", "orbit.gateway");
        Workload batch = deploy("report-batch", "reports.batch");
        Workload meridian = deploy("meridian", "meridian.api");

        section("Issuance, attest first");
        Cred gatewayCred = request(gateway, "orbit.gateway");
        Cred batchCred = request(batch, "reports.batch");
        Cred meridianCred = request(meridian, "meridian.api");
        Cred impostor = request(batch, "orbit.gateway");

        section("Calls, both sides prove a key");
        call(meridian, meridianCred, gateway, gatewayCred, "POST /refunds", 5521, 35);
        call(meridian, meridianCred, batch, batchCred, "GET /status", 0, 0);

        section("The intruder tries everything it holds");
        Cred selfMade = new Cred("orbit.gateway", h("pub", batch.priv()), h("sealed by the intruder"));
        Arrays.asList(batchCred, impostor, selfMade, gatewayCred).stream().filter(Objects::nonNull)
            .forEach(c -> call(meridian, meridianCred, batch, c, "POST /refunds", 5530, 2400));
        call(batch, batchCred, gateway, gatewayCred, "POST /refunds", 5522, 20);

        List<Req> ok = SEEN.stream().filter(r -> r.verdict().equals("ACCEPTED")).toList();
        Map<String, Set<String>> behind = new LinkedHashMap<>();
        ok.forEach(r -> behind.computeIfAbsent(r.subject(), k -> new LinkedHashSet<>()).add(r.origin()));
        int widest = behind.values().stream().mapToInt(Set::size).max().orElse(0);
        long tried = SEEN.stream().filter(Req::stolen).count();
        long replays = ok.stream().filter(Req::stolen).count();
        long denied = SEEN.stream().filter(r -> r.verdict().equals("DENIED")).count();
        long offside = ok.stream().filter(r -> r.order() > 0 && !r.subject().equals("orbit.gateway")).count();
        long unattested = ISSUED.stream().filter(i -> !i[0].equals(i[1])).count();
        long misdialled = DELIVERED.stream().filter(s -> !s.equals("meridian.api")).count();
        int lostUsd = ok.stream().filter(r -> r.order() > 0 && !r.origin().equals("gateway")).mapToInt(Req::amount).sum();
        boolean legit = ok.stream().anyMatch(r -> r.origin().equals("gateway") && r.order() == 5521);
        boolean batchOk = ok.stream().anyMatch(r -> r.origin().equals("report-batch") && r.endpoint().equals("GET /status"));

        System.out.println("METRIC credentials_issued = " + ISSUED.size());
        System.out.println("METRIC identities_in_log = " + behind.size());
        System.out.println("METRIC requests_accepted = " + ok.size());
        System.out.println("METRIC requests_refused = " + (SEEN.size() - ok.size()));
        System.out.println("METRIC unauthorized_refund_usd = " + lostUsd);
        check("distinct-identity-per-workload", widest == 1, behind.size() + " identities logged, one workload each",
            "one logged identity stands for " + widest + " workloads");
        check("valid-cert-without-policy-denied", offside == 0 && denied > 0, denied + " refund with a valid certificate was denied",
            offside + " refunds accepted from a certificate other than orbit.gateway");
        check("stolen-credential-unusable-without-key", replays == 0 && tried > 0,
            tried + " call with a copied credential was refused", replays + " calls with a copied credential were accepted");
        check("identity-issued-only-after-attestation", unattested == 0, ISSUED.size() + " credentials issued, each to the workload it names",
            unattested + " credentials issued to a workload that is not that identity");
        check("legit-gateway-call-works", legit, "order 5521, $35", "order 5521 not refunded");
        check("callee-identity-verified", misdialled == 0, DELIVERED.size() + " requests reached meridian.api, none reached a stand-in",
            misdialled + " requests reached a peer other than meridian.api");
        check("legit-batch-status-works", batchOk, "reports.batch read GET /status", "reports.batch was shut out of GET /status");
    }
}
