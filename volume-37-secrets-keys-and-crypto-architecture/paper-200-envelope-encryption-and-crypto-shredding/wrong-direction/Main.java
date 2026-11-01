import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.*;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

// Whole-volume encryption: each volume is one image sealed under one master key. Keys and nonces are derived
// from labels so the output reproduces; production uses a managed key service and never-reused nonces.
// Harness-stamped: the script hands the intruder the master key (EXPOSED). Every other outcome is computed
// by decrypting the images or by running the query against the unlocked volume.
public class Main {
    static final String[] TENANTS = {"m3012", "m4417", "m5528"}, RECORDS = {"payout", "tickets"},
        COPIES = {"primary", "index", "export", "backup"};
    static int seq = 0, nonce = 0;

    static byte[] sha(String label) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(label.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    static byte[] gcm(int mode, byte[] key, byte[] iv, String aad, byte[] in) {
        try {
            Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
            c.init(mode, new SecretKeySpec(key, "AES"), new GCMParameterSpec(128, iv));
            c.updateAAD(aad.getBytes(StandardCharsets.UTF_8));
            return c.doFinal(in);
        } catch (GeneralSecurityException e) {
            return null;
        }
    }

    static byte[] seal(byte[] key, String aad, byte[] plain) {
        byte[] iv = ByteBuffer.allocate(12).putLong(0).putInt(++nonce).array();
        byte[] ct = gcm(Cipher.ENCRYPT_MODE, key, iv, aad, plain);
        return ByteBuffer.allocate(12 + ct.length).put(iv).put(ct).array();
    }

    static byte[] open(byte[] key, String aad, byte[] sealed) {
        return gcm(Cipher.DECRYPT_MODE, key, Arrays.copyOf(sealed, 12), aad, Arrays.copyOfRange(sealed, 12, sealed.length));
    }

    // A row is "tenant|record|text". An image that does not open yields no rows.
    static List<String> rows(byte[] key, byte[] image) {
        byte[] pt = open(key, "", image);
        return pt == null ? List.of() : List.of(new String(pt, StandardCharsets.UTF_8).split("\n"));
    }

    static byte[] image(byte[] key, List<String> rows) {
        return seal(key, "", String.join("\n", rows).getBytes(StandardCharsets.UTF_8));
    }

    static long count(List<String> rows, String tenant) { return rows.stream().filter(r -> r.startsWith(tenant + "|")).count(); }

    // The export and backup volumes are read-only images: the scene's premise, not something computed.
    static boolean mutable(String volume) { return volume.equals("primary") || volume.equals("index"); }

    static void section(String title) { System.out.println("-- " + title + " --"); }

    static void event(String src, String dst, String msg, String verdict) {
        System.out.println(String.format(Locale.ROOT, "[%02d] %s -> %s : %s => %s", ++seq, src, dst, msg, verdict));
    }

    static void check(String name, boolean pass, String detail) {
        System.out.println("CHECK " + name + " : " + (pass ? "PASS" : "FAIL") + "  (" + detail + ")");
    }

    public static void main(String[] args) {
        byte[] master = sha("vol-master-v1");
        List<String> all = new ArrayList<>();
        for (String t : TENANTS) for (String r : RECORDS) all.add(t + "|" + r + "|" + r + " of " + t);
        Map<String, byte[]> volumes = new LinkedHashMap<>();
        for (String c : COPIES) volumes.put(c, image(master, all));

        section("Setup");
        event("kms", "gateway", "vol-master-v1 released at boot to unlock " + volumes.size() + " volumes", "GRANTED");
        boolean legit = rows(master, volumes.get("primary")).contains("m4417|payout|payout of m4417");
        event("gateway", "store", "m4417 payout read through the unlocked primary volume", legit ? "OK" : "LOST");

        section("A merchant leaves");
        long total = 0, readable = 0;
        for (String c : COPIES) {
            List<String> now = rows(master, volumes.get(c));
            if (mutable(c)) volumes.put(c, image(master, now.stream().filter(r -> !r.startsWith("m3012|")).toList()));
            long left = count(rows(master, volumes.get(c)), "m3012");
            total += count(now, "m3012");
            readable += left;
            event("gateway", "store", "delete m3012 from the " + c + " volume (" + count(now, "m3012") + " rows)",
                left == 0 ? "DONE" : "MISSED (" + left + " remain, the image is read-only)");
        }

        section("A query inside the application");
        List<String> hits = rows(master, volumes.get("primary")).stream().filter(r -> r.contains("|payout|")).toList();
        long foreign = hits.stream().filter(r -> !r.startsWith("m4417|")).count();
        event("gateway", "store", "session m4417 runs a payout query with no tenant filter, " + hits.size() + " rows returned",
            foreign > 0 ? "LEAKED (" + foreign + " row of another tenant)" : "BLOCKED");

        section("A stolen backup");
        boolean opaque = rows(sha("intruder-own-key"), volumes.get("backup")).isEmpty();
        event("intruder", "store", "backup image copied and tried with a key the intruder holds",
            opaque ? "DENIED (authentication tag fails)" : "LEAKED");
        event("intruder", "gateway", "vol-master-v1 taken from the host that unlocked the volumes", "EXPOSED");
        List<String> loot = rows(master, volumes.get("backup"));
        long tenants = loot.stream().map(r -> r.split("\\|")[0]).distinct().count();
        event("intruder", "store", "backup image opened with vol-master-v1: " + loot.size() + " rows, tenants exposed " + tenants
            + ", " + count(loot, "m3012") + " belong to deleted m3012", loot.isEmpty() ? "DENIED" : "LEAKED");

        long images = volumes.values().stream().filter(v -> !rows(master, v).isEmpty()).count();
        System.out.println("METRIC m3012_entries_still_readable = " + readable);
        System.out.println("METRIC tenants_exposed_by_one_leak = " + tenants);
        System.out.println("METRIC volumes_opened_by_one_key = " + images);
        System.out.println("METRIC cross_tenant_rows_returned = " + foreign);
        check("legit-tenant-read-works", legit, legit ? "m4417 payout read through the unlocked volume" : "m4417 payout unreadable");
        check("destroyed-tenant-key-makes-all-copies-unreadable", readable == 0, readable + " of " + total + " m3012 rows still open");
        check("ciphertext-bound-to-tenant", foreign == 0, "session m4417 received " + foreign + " row of another tenant");
        check("leak-exposes-one-tenant-at-most", tenants <= 1, tenants + " tenants exposed by one key");
    }
}
