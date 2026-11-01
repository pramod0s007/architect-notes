import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

// A separate attestor signs what the isolated builder holds; the registry runs five rules. COMPROMISED is a harness label, not a detection.
public class Main {
    interface Step { void run(TreeMap<String, String> work); }
    record Statement(String commit, String builder, String artifact, String sig) {}
    record Upload(String bytes, Statement stmt) {}

    static final Step COMPILE = w -> w.replaceAll((path, text) -> "obj:" + text);
    static final Step STRIP_DEBUG = w -> w.keySet().removeIf(path -> path.startsWith("debug/"));
    static final List<Step> REFERENCE = List.of(COMPILE, STRIP_DEBUG);
    static final List<Step> PIPELINE = new ArrayList<>(REFERENCE);
    static final List<String> ALLOWED = List.of("builder-a");
    static final String JOB_KEY = "job-key";
    static final Map<String, TreeMap<String, String>> REPO = new LinkedHashMap<>();
    static final Set<String> REVIEWED = new LinkedHashSet<>();
    static final List<Upload> REGISTRY = new ArrayList<>();
    static final List<Statement> BY_JOB = new ArrayList<>(); // harness bookkeeping: only this list records who wrote a statement
    static int seq = 0, refused = 0, rebuilds = 0;

    static String sha(String text) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8)), 0, 6); }
        catch (GeneralSecurityException e) { throw new IllegalStateException(e); }
    }

    // HMAC under a key derived from a label stands in for an identity-bound signature.
    static String sign(String keyLabel, String text) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(keyLabel.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(text.getBytes(StandardCharsets.UTF_8)), 0, 6);
        } catch (GeneralSecurityException e) { throw new IllegalStateException(e); }
    }

    static String land(String limits) {
        TreeMap<String, String> tree = new TreeMap<>(Map.of("Auth.src", "verify", "Limits.src", limits, "debug/Trace.src", "dump"));
        REPO.put(sha(tree.toString()), tree);
        return sha(tree.toString());
    }

    static String pack(String commit, List<Step> steps) {
        TreeMap<String, String> work = new TreeMap<>(REPO.get(commit));
        steps.forEach(s -> s.run(work));
        return work.toString();
    }

    static Statement write(String key, String commit, String builder, String artifact) {
        return new Statement(commit, builder, artifact, sign(key, commit + "|" + builder + "|" + artifact));
    }

    static boolean signed(Statement s) { return write("attestor/" + s.builder(), s.commit(), s.builder(), s.artifact()).equals(s); }

    // The builder fetches the commit itself and runs the steps it is handed; the attestor signs what the builder holds.
    static Upload build(String commit, String builder, List<Step> steps) {
        String bytes = pack(commit, steps);
        return new Upload(bytes, write("attestor/" + builder, commit, builder, sha(bytes)));
    }

    // Five independent rules; the rebuild runs last because it costs the most. The first rule an upload fails names the reason.
    static String gate(Upload u) {
        Statement s = u.stmt();
        if (!signed(s)) return "signature does not verify under the key of " + s.builder();
        if (!ALLOWED.contains(s.builder())) return s.builder() + " is not on the release allow-list";
        if (!s.artifact().equals(sha(u.bytes()))) return "statement says " + s.artifact() + ", the bytes hash to " + sha(u.bytes());
        if (!REVIEWED.contains(s.commit())) return "commit " + s.commit() + " was never reviewed";
        rebuilds++;
        String again = sha(pack(s.commit(), REFERENCE));
        return again.equals(s.artifact()) ? null : "rebuild of " + s.commit() + " gives " + again + ", the statement says " + s.artifact();
    }

    static void submit(String src, String what, Upload u) {
        String why = gate(u);
        if (why == null) REGISTRY.add(u); else refused++;
        event(src, "registry", what, why == null ? "ACCEPTED (all five rules pass)" : "REFUSED (" + why + ")");
    }

    // The audit judges a stored artifact from the reviewed set and the true recipe, without asking the gate.
    static boolean bound(Upload e) {
        String commit = e.stmt().commit();
        return REVIEWED.contains(commit) && sha(pack(commit, REFERENCE)).equals(sha(e.bytes()));
    }

    static void section(String title) { System.out.println("-- " + title + " --"); }

    static void event(String src, String dst, String msg, String verdict) {
        System.out.println(String.format(Locale.ROOT, "[%02d] %s -> %s : %s => %s", ++seq, src, dst, msg, verdict));
    }

    static void check(String name, boolean pass, String ifPass, String ifFail) {
        System.out.println("CHECK " + name + " : " + (pass ? "PASS  (" + ifPass : "FAIL  (" + ifFail) + ")");
    }

    public static void main(String[] args) {
        String c141 = land("limit=100"), c142 = land("limit=120"), cx = land("limit=900");
        REVIEWED.addAll(List.of(c141, c142));

        section("Release 1.4.1 through the isolated builder");
        Upload u141 = build(c141, "builder-a", PIPELINE);
        event("source", "builder", "builder fetches tag 1.4.1 at commit " + c141, "OK (reviewed and merged)");
        event("builder", "attestor", "built artifact " + u141.stmt().artifact() + ", identity builder-a", "SIGNED (a key the steps never see)");
        submit("build", "upload gateway-1.4.1 with its statement", u141);

        section("An altered step builds release 1.4.2");
        PIPELINE.set(1, w -> { STRIP_DEBUG.run(w); w.put("lib/updater.obj", "obj:beacon"); });
        event("intruder", "build", "strip-debug step replaced, it also adds lib/updater.obj", "COMPROMISED (harness label, no pipeline alarm)");
        Upload u142 = build(c142, "builder-a", PIPELINE);
        event("builder", "attestor", "built artifact " + u142.stmt().artifact() + " from commit " + c142, "SIGNED (true: it says what the builder ran)");
        submit("build", "upload gateway-1.4.2 with its statement", u142);

        section("Four more uploads, each wrong in one way");
        Statement fake = write(JOB_KEY, c142, "builder-a", sha(pack(c142, REFERENCE)));
        BY_JOB.add(fake);
        submit("build", "upload gateway-1.4.2 with a statement the job's own script wrote", new Upload(pack(c142, REFERENCE), fake));
        submit("build", "upload gateway-1.4.2 built on builder-lab, an enrolled lab builder", build(c142, "builder-lab", REFERENCE));
        submit("intruder", "upload the tampered 1.4.2 bytes under 1.4.1's genuine statement", new Upload(u142.bytes(), u141.stmt()));
        submit("build", "upload gateway-1.4.3 built from commit " + cx + ", which nobody reviewed", build(cx, "builder-a", REFERENCE));

        section("The reviewed step is restored");
        PIPELINE.set(1, STRIP_DEBUG);
        Upload ok = build(c142, "builder-a", PIPELINE);
        submit("build", "upload gateway-1.4.2 rebuilt after the step was restored", ok);

        section("Lena asks which commit, built where, by what");
        boolean holds = REGISTRY.contains(ok) && signed(ok.stmt()) && bound(ok);
        event("registry", "source", "stored answer: commit " + c142 + ", builder builder-a, artifact " + ok.stmt().artifact(),
            holds ? "VERIFIED (signed by that builder, and a rebuild of that commit agrees)" : "MISSED (the answer does not hold)");

        long unbound = REGISTRY.stream().filter(e -> !bound(e)).count();
        long written = REGISTRY.stream().filter(e -> BY_JOB.contains(e.stmt())).count();
        System.out.println("METRIC uploads_accepted = " + REGISTRY.size());
        System.out.println("METRIC uploads_refused = " + refused);
        System.out.println("METRIC rebuilds_run = " + rebuilds);
        System.out.println("METRIC accepted_without_source_match = " + unbound);
        check("legit-reviewed-build-accepted", REGISTRY.contains(u141) && REGISTRY.contains(ok) && bound(u141) && bound(ok),
            "gateway-1.4.1 and the restored 1.4.2 are stored and rebuild from their commits", "a reviewed release was refused or does not rebuild");
        check("provenance-binds-artifact-to-source-commit", unbound == 0,
            "every stored artifact rebuilds from its reviewed commit", "stored artifacts that do not rebuild from a reviewed commit: " + unbound);
        check("provenance-written-outside-build-steps", written == 0,
            "no stored statement was written by the job that built the file", written + " of " + REGISTRY.size() + " stored statements were written by the job");
        check("tampered-build-detected-by-rebuild", REGISTRY.stream().noneMatch(e -> e.bytes().equals(u142.bytes())),
            "tampered 1.4.2 not stored, a rebuild of " + c142 + " gives " + sha(pack(c142, REFERENCE)) + " not " + u142.stmt().artifact(),
            "the tampered 1.4.2 bytes are stored");
        check("builder-identity-on-allowlist", REGISTRY.stream().allMatch(e -> ALLOWED.contains(e.stmt().builder())),
            "every stored statement names an allow-listed builder", "a stored statement names a builder off the allow-list");
    }
}
