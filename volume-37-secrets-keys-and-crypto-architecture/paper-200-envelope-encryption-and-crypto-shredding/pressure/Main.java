import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.*;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

// One key for every tenant, held by the application. Keys and nonces are derived from labels so the output
// reproduces; production uses a managed key service and nonces that are never reused under one key.
// Harness-stamped: the script hands the intruder a backup copy and the key (EXFILTRATED, EXPOSED).
// Every outcome after that is computed by trying to decrypt the stored bytes.
public class Main {
    record Entry(String copy, String tenant, String rec, byte[] sealed) {}
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

    static String plain(String tenant, String rec) { return rec + " of " + tenant; }

    // The export is append-only and the backup is immutable: the scene's premise, not something computed.
    static boolean mutable(String copy) { return copy.equals("primary") || copy.equals("index"); }

    static List<Entry> openable(List<Entry> es, byte[] key) {
        return es.stream().filter(e -> open(key, "", e.sealed()) != null).toList();
    }

    static String read(List<Entry> es, byte[] key, String tenant, String rec) {
        return es.stream().filter(e -> e.copy().equals("primary") && e.tenant().equals(tenant) && e.rec().equals(rec))
            .findFirst().map(e -> open(key, "", e.sealed())).map(b -> new String(b, StandardCharsets.UTF_8)).orElse(null);
    }

    static void section(String title) { System.out.println("-- " + title + " --"); }

    static void event(String src, String dst, String msg, String verdict) {
        System.out.println(String.format(Locale.ROOT, "[%02d] %s -> %s : %s => %s", ++seq, src, dst, msg, verdict));
    }

    static void check(String name, boolean pass, String detail) {
        System.out.println("CHECK " + name + " : " + (pass ? "PASS" : "FAIL") + "  (" + detail + ")");
    }

    public static void main(String[] args) {
        byte[] key = sha("dk-all-v1");
        List<Entry> entries = new ArrayList<>();
        for (String t : TENANTS) {
            for (String r : RECORDS) {
                byte[] sealed = seal(key, "", plain(t, r).getBytes(StandardCharsets.UTF_8));
                for (String c : COPIES) entries.add(new Entry(c, t, r, sealed));
            }
        }
        long total = entries.stream().filter(e -> e.tenant().equals("m3012")).count();

        section("Setup");
        event("gateway", "store", entries.size() + " entries sealed under dk-all-v1 in " + COPIES.length + " copies", "OK");
        event("gateway", "store", "m4417 payout read from primary", plain("m4417", "payout").equals(read(entries, key, "m4417", "payout")) ? "OK" : "LOST");

        section("A merchant leaves");
        for (String c : COPIES) {
            long had = entries.stream().filter(e -> e.tenant().equals("m3012") && e.copy().equals(c)).count();
            if (mutable(c)) entries.removeIf(e -> e.tenant().equals("m3012") && e.copy().equals(c));
            long left = entries.stream().filter(e -> e.tenant().equals("m3012") && e.copy().equals(c)).count();
            event("gateway", "store", "delete m3012 from " + c + " (" + had + " entries)",
                left == 0 ? "DONE" : "MISSED (" + left + " remain, the store takes no deletes)");
        }
        long readable = openable(entries.stream().filter(e -> e.tenant().equals("m3012")).toList(), key).size();

        section("A backup leaks");
        List<Entry> stolen = entries.stream().filter(e -> e.copy().equals("backup")).toList();
        event("intruder", "store", "copy of the backup taken (" + stolen.size() + " entries)", "EXFILTRATED");
        event("intruder", "gateway", "dk-all-v1 read from a crash dump of the application", "EXPOSED");
        List<Entry> opened = openable(stolen, key);
        long tenants = opened.stream().map(Entry::tenant).distinct().count();
        long deleted = opened.stream().filter(e -> e.tenant().equals("m3012")).count();
        event("intruder", "store", "backup opened with dk-all-v1: " + opened.size() + " of " + stolen.size()
            + " entries, tenants exposed " + tenants + ", " + deleted + " belong to deleted m3012", opened.isEmpty() ? "DENIED" : "LEAKED");

        section("The key rotates");
        byte[] next = sha("dk-all-v2");
        long rewritten = entries.stream().filter(e -> mutable(e.copy())).count();
        entries.replaceAll(e -> mutable(e.copy())
            ? new Entry(e.copy(), e.tenant(), e.rec(), seal(next, "", open(key, "", e.sealed()))) : e);
        event("gateway", "store", "primary and index re-encrypted under dk-all-v2 (" + rewritten + " entries)", "DONE");
        long old = openable(entries, key).size();
        event("gateway", "store", "export and backup still hold " + old + " entries that open under dk-all-v1", old > 0 ? "EXPOSED" : "DONE");

        System.out.println("METRIC m3012_entries_still_readable = " + readable);
        System.out.println("METRIC tenants_exposed_by_one_leak = " + tenants);
        System.out.println("METRIC data_entries_rewritten_by_rotation = " + rewritten);
        System.out.println("METRIC entries_left_under_old_key = " + old);
        boolean legit = plain("m4417", "payout").equals(read(entries, next, "m4417", "payout"));
        check("legit-tenant-read-works", legit, legit ? "m4417 payout read under dk-all-v2 after the rotation" : "m4417 payout unreadable");
        check("destroyed-tenant-key-makes-all-copies-unreadable", readable == 0, readable + " of " + total + " m3012 entries still open");
        check("leak-exposes-one-tenant-at-most", tenants <= 1, tenants + " tenants exposed by one key");
        check("kek-rotation-rewraps-without-reencrypting-data", rewritten == 0 && old == 0,
            rewritten + " entries re-encrypted, " + old + " still open under the old key");
    }
}
