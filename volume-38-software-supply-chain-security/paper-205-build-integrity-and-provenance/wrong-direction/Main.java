import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

// The job signs its own statement as its last step and the registry verifies that signature. COMPROMISED is a harness label, not a detection.
public class Main {
    interface Step { void run(TreeMap<String, String> work); }
    record Statement(String commit, String builder, String artifact, String sig) {}
    record Upload(String bytes, Statement stmt) {}

    static final Step COMPILE = w -> w.replaceAll((path, text) -> "obj:" + text);
    static final Step STRIP_DEBUG = w -> w.keySet().removeIf(path -> path.startsWith("debug/"));
    static final Step STRIP_AND_BEACON = w -> { STRIP_DEBUG.run(w); w.put("lib/updater.obj", "obj:beacon"); };
    static final List<Step> REFERENCE = List.of(COMPILE, STRIP_DEBUG);
    static final List<Step> PIPELINE = new ArrayList<>(REFERENCE);
    static final String JOB_KEY = "job-key";
    static final Map<String, TreeMap<String, String>> REPO = new LinkedHashMap<>();
    static final Set<String> REVIEWED = new LinkedHashSet<>();
    static final List<Upload> REGISTRY = new ArrayList<>();
    static final List<Statement> BY_JOB = new ArrayList<>(); // harness bookkeeping: only this list records who wrote a statement
    static int seq = 0, refused = 0;

    static String sha(String text) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8)), 0, 6); }
        catch (GeneralSecurityException e) { throw new IllegalStateException(e); }
    }

    // HMAC under a key derived from a label stands in for a signature.
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

    // The job packs the workspace, writes the statement about it and signs it as its last step.
    static Upload job(String commit, List<Step> steps) {
        String bytes = pack(commit, steps);
        Statement s = write(JOB_KEY, commit, "builder-a", sha(bytes));
        BY_JOB.add(s);
        return new Upload(bytes, s);
    }

    // The registry asks two questions: did the job key sign this statement, and does it describe these bytes?
    static String gate(Upload u) {
        Statement s = u.stmt();
        if (!write(JOB_KEY, s.commit(), s.builder(), s.artifact()).equals(s)) return "no valid signature from the job key";
        return s.artifact().equals(sha(u.bytes())) ? null : "statement says " + s.artifact() + ", the bytes hash to " + sha(u.bytes());
    }

    static void submit(String src, String what, Upload u) {
        String why = gate(u);
        if (why == null) REGISTRY.add(u); else refused++;
        event(src, "registry", what, why == null ? "ACCEPTED (signature verifies, digest equals the bytes)" : "REJECTED (" + why + ")");
    }

    // The audit judges a stored artifact from the reviewed set and the true recipe, without asking the registry.
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
        String c141 = land("limit=100"), c142 = land("limit=120");
        REVIEWED.addAll(List.of(c141, c142));

        section("Release 1.4.1, signed by the job");
        event("source", "build", "tag 1.4.1 checked out at commit " + c141, "OK (reviewed and merged)");
        Upload u141 = job(c141, PIPELINE);
        submit("build", "upload gateway-1.4.1, artifact " + u141.stmt().artifact() + ", signed with the job key", u141);

        section("The intruder tries the front door, then the job");
        String altered = pack(c142, List.of(COMPILE, STRIP_AND_BEACON));
        submit("intruder", "altered bytes built by hand, signed with the intruder's own key",
            new Upload(altered, write("intruder-key", c142, "builder-a", sha(altered))));
        PIPELINE.set(1, STRIP_AND_BEACON);
        event("intruder", "build", "strip-debug step replaced, it also adds lib/updater.obj", "COMPROMISED (harness label, no pipeline alarm)");

        section("Release 1.4.2, signed by the same job");
        event("source", "build", "tag 1.4.2 checked out at commit " + c142, "OK (reviewed and merged)");
        Upload u142 = job(c142, PIPELINE);
        submit("build", "upload gateway-1.4.2, artifact " + u142.stmt().artifact() + ", signed with the job key", u142);

        section("Dev rebuilds 1.4.2 on a clean machine");
        String clean = sha(pack(c142, REFERENCE));
        event("source", "build", "clean-machine rebuild of tag 1.4.2 gives artifact " + clean, "OK (reference steps, nothing modified)");
        event("build", "registry", "clean " + clean + " against stored " + u142.stmt().artifact(),
            clean.equals(u142.stmt().artifact()) ? "OK (digests agree)" : "DETECTED (differs, found by hand outside the release path)");

        section("Lena asks which commit, built where, by what");
        event("registry", "source", "stored answer for gateway-1.4.2 is commit " + c142 + ", builder builder-a, signed by the job key",
            bound(u142) ? "VERIFIED (a rebuild of that commit agrees)" : "MISSED (a rebuild of that commit gives " + clean + ", not the stored artifact)");

        long unbound = REGISTRY.stream().filter(e -> !bound(e)).count();
        long written = REGISTRY.stream().filter(e -> BY_JOB.contains(e.stmt())).count();
        System.out.println("METRIC uploads_accepted = " + REGISTRY.size());
        System.out.println("METRIC uploads_refused = " + refused);
        System.out.println("METRIC accepted_without_source_match = " + unbound);
        check("legit-reviewed-build-accepted", REGISTRY.contains(u141) && bound(u141),
            "gateway-1.4.1 is stored and rebuilds from its commit", "gateway-1.4.1 is missing or does not rebuild");
        check("provenance-binds-artifact-to-source-commit", unbound == 0,
            "every stored artifact rebuilds from its reviewed commit", "stored artifacts that do not rebuild from a reviewed commit: " + unbound);
        check("provenance-written-outside-build-steps", written == 0,
            "no stored statement was written by the job that built the file", written + " of " + REGISTRY.size() + " stored statements were written by the job");
        check("tampered-build-detected-by-rebuild", REGISTRY.stream().noneMatch(e -> e.bytes().equals(u142.bytes())),
            "tampered 1.4.2 is not stored", "the tampered 1.4.2 bytes are stored, nothing rebuilt them");
    }
}
