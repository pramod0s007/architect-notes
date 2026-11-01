import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;

// The registry stores whatever the job uploads, and the job's own script writes the metadata that describes it.
// COMPROMISED is a label the harness stamps, not a detection.
public class Main {
    interface Step { void run(TreeMap<String, String> work); }
    record Statement(String commit, String builder, String artifact) {}
    record Upload(String bytes, Statement stmt) {}

    static final Step COMPILE = w -> w.replaceAll((path, text) -> "obj:" + text);
    static final Step STRIP_DEBUG = w -> w.keySet().removeIf(path -> path.startsWith("debug/"));
    static final List<Step> REFERENCE = List.of(COMPILE, STRIP_DEBUG);
    static final List<Step> PIPELINE = new ArrayList<>(REFERENCE);
    static final Map<String, TreeMap<String, String>> REPO = new LinkedHashMap<>();
    static final Set<String> REVIEWED = new LinkedHashSet<>();
    static final List<Upload> REGISTRY = new ArrayList<>();
    static final List<Statement> BY_JOB = new ArrayList<>(); // harness bookkeeping: only this list records who wrote a statement
    static int seq = 0, refused = 0;

    static String sha(String text) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8)), 0, 6); }
        catch (GeneralSecurityException e) { throw new IllegalStateException(e); }
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

    // The job's own script packs the workspace and writes the metadata about it.
    static Upload job(String commit, List<Step> steps) {
        String bytes = pack(commit, steps);
        Statement s = new Statement(commit, "builder-a", sha(bytes));
        BY_JOB.add(s);
        return new Upload(bytes, s);
    }

    // The registry's single question: does the metadata describe these bytes?
    static boolean accept(Upload u) {
        boolean ok = u.stmt().artifact().equals(sha(u.bytes()));
        if (ok) REGISTRY.add(u); else refused++;
        return ok;
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

        section("Release 1.4.1 through the reviewed pipeline");
        event("source", "build", "tag 1.4.1 checked out at commit " + c141, "OK (reviewed and merged)");
        Upload u141 = job(c141, PIPELINE);
        event("build", "registry", "upload gateway-1.4.1, artifact " + u141.stmt().artifact() + ", metadata written by the job",
            accept(u141) ? "ACCEPTED (metadata digest equals the bytes)" : "REJECTED (metadata digest differs)");

        section("An intruder alters one build step");
        PIPELINE.set(1, w -> { STRIP_DEBUG.run(w); w.put("lib/updater.obj", "obj:beacon"); });
        event("intruder", "build", "strip-debug step replaced, it also adds lib/updater.obj", "COMPROMISED (harness label, no pipeline alarm)");

        section("Release 1.4.2 through the same pipeline");
        event("source", "build", "tag 1.4.2 checked out at commit " + c142, "OK (reviewed and merged)");
        Upload u142 = job(c142, PIPELINE);
        event("build", "registry", "upload gateway-1.4.2, artifact " + u142.stmt().artifact() + ", metadata names commit " + c142,
            accept(u142) ? "ACCEPTED (metadata digest equals the bytes)" : "REJECTED (metadata digest differs)");

        section("Dev rebuilds 1.4.2 on a clean machine");
        String clean = sha(pack(c142, REFERENCE));
        event("source", "build", "clean-machine rebuild of tag 1.4.2 gives artifact " + clean, "OK (reference steps, nothing modified)");
        event("build", "registry", "clean " + clean + " against stored " + u142.stmt().artifact(),
            clean.equals(u142.stmt().artifact()) ? "OK (digests agree)" : "DETECTED (differs, found by hand outside the release path)");

        section("Lena asks which commit, built where, by what");
        event("registry", "source", "stored answer for gateway-1.4.2 is commit " + c142 + ", builder builder-a",
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
