import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;
import javax.crypto.*;
import javax.crypto.spec.*;

// One data key per tenant, wrapped by a key only the key service can read. Keys and nonces derive from labels so the
// output reproduces. Harness-stamped: the intruder is handed a backup copy and one worker's data key (EXFILTRATED, EXPOSED).
public class Main {
    record Entry(String copy, String tenant, String rec, byte[] sealed) {}
    static final String[] TENANTS = {"m3012", "m4417", "m5528"}, RECORDS = {"payout", "tickets"},
        COPIES = {"primary", "index", "export", "backup"};
    static final List<Entry> ENTRIES = new ArrayList<>();
    static int seq = 0, nonce = 0, attempts = 0;
    static final Kms KMS = new Kms(Arrays.stream(TENANTS).map(Main::keyId).toArray(String[]::new));

    static byte[] sha(String label) {
        try { return MessageDigest.getInstance("SHA-256").digest(label.getBytes(StandardCharsets.UTF_8)); }
        catch (GeneralSecurityException e) { throw new IllegalStateException(e); }
    }
    // One AES-GCM routine for both directions: a sealed blob is a 12-byte counter nonce, then ciphertext and tag.
    static byte[] crypt(int mode, byte[] key, String aad, byte[] in) {
        boolean enc = mode == Cipher.ENCRYPT_MODE;
        byte[] iv = enc ? ByteBuffer.allocate(12).putLong(0).putInt(++nonce).array() : Arrays.copyOf(in, 12);
        try {
            Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
            c.init(mode, new SecretKeySpec(key, "AES"), new GCMParameterSpec(128, iv));
            c.updateAAD(aad.getBytes(StandardCharsets.UTF_8));
            byte[] out = enc ? c.doFinal(in) : c.doFinal(in, 12, in.length - 12);
            return enc ? ByteBuffer.allocate(12 + out.length).put(iv).put(out).array() : out;
        } catch (GeneralSecurityException e) { return null; }
    }
    static byte[] seal(byte[] key, String aad, byte[] plain) { return crypt(Cipher.ENCRYPT_MODE, key, aad, plain); }
    static byte[] open(byte[] key, String aad, byte[] sealed) { return crypt(Cipher.DECRYPT_MODE, key, aad, sealed); }
    static String keyId(String tenant) { return "dk-" + tenant + "-v1"; }
    static String plain(String tenant, String rec) { return rec + " of " + tenant; }
    static byte[] unwrap(String caller, String id) { attempts++; return KMS.unwrap(caller, id); }
    // The AAD is the tenant of the session the request arrived in, never a field of the row being opened.
    static String read(String session, String tenant, String rec) {
        Entry e = ENTRIES.stream().filter(x -> x.copy().equals("primary") && x.tenant().equals(tenant) && x.rec().equals(rec)).findFirst().orElseThrow();
        byte[] dek = unwrap("gateway", keyId(tenant)), pt = dek == null ? null : open(dek, session, e.sealed());
        return pt == null ? null : new String(pt, StandardCharsets.UTF_8);
    }
    // The attacker's best case: it knows each entry's tenant, so it supplies the right AAD.
    static List<Entry> openable(List<Entry> es, List<byte[]> keys) {
        return es.stream().filter(e -> keys.stream().anyMatch(k -> open(k, e.tenant(), e.sealed()) != null)).toList();
    }
    static void section(String title) { System.out.println("-- " + title + " --"); }
    static void event(String src, String dst, String msg, String verdict) {
        System.out.println(String.format(Locale.ROOT, "[%02d] %s -> %s : %s => %s", ++seq, src, dst, msg, verdict));
    }
    static void check(String name, boolean pass, String detail) { System.out.println("CHECK " + name + " : " + (pass ? "PASS" : "FAIL") + "  (" + detail + ")"); }

    public static void main(String[] args) {
        section("Setup");
        for (String t : TENANTS) {
            byte[] dek = unwrap("gateway", keyId(t));
            for (String r : RECORDS) {
                byte[] sealed = seal(dek, t, plain(t, r).getBytes(StandardCharsets.UTF_8));
                for (String c : COPIES) ENTRIES.add(new Entry(c, t, r, sealed));
            }
        }
        event("gateway", "kms", TENANTS.length + " tenant data keys created and wrapped under kek-v1", "ISSUED");
        event("gateway", "store", ENTRIES.size() + " entries written in " + COPIES.length + " copies, each bound to its tenant", "ACCEPTED");
        section("A query inside the application");
        List<String> got = Arrays.stream(TENANTS).map(t -> read("m4417", t, "payout")).filter(Objects::nonNull).toList();
        long foreign = got.stream().filter(r -> !r.equals(plain("m4417", "payout"))).count();
        event("gateway", "store", "session m4417 runs a payout query with no tenant filter, " + TENANTS.length + " rows fetched, " + got.size() + " returned",
            foreign == 0 ? "REJECTED (" + (TENANTS.length - got.size()) + " rows fail the tenant binding)" : "LEAKED (" + foreign + " rows of another tenant)");
        section("A merchant leaves");
        int removed = KMS.destroy(keyId("m3012"));
        event("gateway", "kms", keyId("m3012") + " destroyed, " + removed + " wrapped copies removed", "DONE");
        String after = read("m3012", "m3012", "payout");
        event("gateway", "kms", "session m3012 asks for its payout row, unwrap " + keyId("m3012") + " requested",
            after == null ? "DENIED (no wrapped key left)" : "MISSED (key still served)");
        List<Entry> mine = ENTRIES.stream().filter(e -> e.tenant().equals("m3012")).toList();
        int readable = openable(mine, KMS.recover(sha("kek-v1"))).size();
        event("intruder", "store", mine.size() + " m3012 entries tried with kek-v1 and every wrapped key left",
            readable == 0 ? "DENIED (0 open)" : "LEAKED (" + readable + " open)");
        section("A backup leaks");
        List<Entry> stolen = ENTRIES.stream().filter(e -> e.copy().equals("backup")).toList();
        event("intruder", "store", "copy of the backup taken (" + stolen.size() + " entries)", "EXFILTRATED");
        byte[] worker = unwrap("gateway", keyId("m4417"));
        event("intruder", "gateway", "data key of the m4417 worker taken from a memory dump", "EXPOSED");
        byte[] asked = unwrap("intruder", keyId("m5528"));
        event("intruder", "kms", "unwrap " + keyId("m5528") + " requested", asked == null ? "DENIED (caller not authorized)" : "LEAKED (key served)");
        List<Entry> opened = openable(stolen, Arrays.asList(worker, asked).stream().filter(Objects::nonNull).toList());
        long tenants = opened.stream().map(Entry::tenant).distinct().count();
        event("intruder", "store", "backup opened with the keys it holds: " + opened.size() + " of " + stolen.size()
            + " entries, tenants exposed " + tenants, opened.isEmpty() ? "DENIED" : "LEAKED");
        section("The wrapping key rotates");
        List<byte[]> before = ENTRIES.stream().map(e -> e.sealed().clone()).toList();
        int rewrapped = KMS.rotate("kek-v2");
        long rewritten = ENTRIES.stream().filter(e -> before.stream().noneMatch(b -> Arrays.equals(b, e.sealed()))).count();
        int stillOld = KMS.recover(sha("kek-v1")).size();
        event("gateway", "kms", "kek-v1 rotated to kek-v2: " + rewrapped + " wrapped keys re-wrapped, " + rewritten + " data entries rewritten", "DONE");
        String[] live = {"m4417", "m5528"}; int tried = live.length * RECORDS.length, served = 0;
        for (String t : live) for (String r : RECORDS) if (plain(t, r).equals(read(t, t, r))) served++;
        event("gateway", "store", served + " of " + tried + " rows of m4417 and m5528 read in their own sessions after the shred and the rotation",
            served == tried ? "OK" : "LOST");
        long denied = KMS.audit.stream().filter(a -> a.endsWith("denied")).count();
        boolean audited = KMS.audit.size() == attempts && KMS.audit.stream().anyMatch(a -> a.contains(keyId("m5528") + " by intruder") && a.endsWith("denied"));
        event("kms", "auditlog", attempts + " unwrap attempts, " + KMS.audit.size() + " audited, " + denied + " denied", audited ? "RECORDED" : "MISSED");
        System.out.println("METRIC m3012_entries_still_readable = " + readable);
        System.out.println("METRIC tenants_exposed_by_one_leak = " + tenants);
        System.out.println("METRIC data_entries_rewritten_by_rotation = " + rewritten);
        System.out.println("METRIC wrapped_keys_rewrapped = " + rewrapped);
        boolean own = got.contains(plain("m4417", "payout"));
        check("legit-tenant-read-works", served == tried, served + " of " + tried + " rows of m4417 and m5528 read after the shred and the rotation");
        check("destroyed-tenant-key-makes-all-copies-unreadable", readable == 0 && after == null,
            readable + " of " + mine.size() + " m3012 entries open with kek-v1 and every wrapped key; the gateway " + (after == null ? "gets no key" : "still gets a key"));
        check("ciphertext-bound-to-tenant", foreign == 0 && own,
            foreign + " rows of another tenant opened in an m4417 session; its own row " + (own ? "opened" : "did not open"));
        check("leak-exposes-one-tenant-at-most", tenants <= 1, tenants + " tenant exposed, " + opened.size() + " of " + stolen.size() + " backup entries open");
        check("kek-rotation-rewraps-without-reencrypting-data", rewrapped > 0 && rewritten == 0 && stillOld == 0,
            rewrapped + " wrapped keys re-wrapped, " + rewritten + " data entries rewritten, " + stillOld + " wrapped copies open under kek-v1");
        check("unwrap-requires-authorized-caller-and-is-audited", asked == null && audited,
            "intruder " + (asked == null ? "denied" : "served") + ", " + KMS.audit.size() + " of " + attempts + " attempts in the audit");
    }
}

// The key service. The wrapping key is private to this class, so no other code can read it; a real service is a
// separate process or hardware module. The key table holds only wrapped keys, in a live table and a replica.
class Kms {
    private byte[] kek = Main.sha("kek-v1");
    private final List<Map<String, byte[]>> tables = List.of(new LinkedHashMap<>(), new LinkedHashMap<>());
    final List<String> audit = new ArrayList<>();
    Kms(String... ids) { for (String id : ids) tables.forEach(t -> t.put(id, Main.seal(kek, "", Main.sha(id)))); }
    byte[] unwrap(String caller, String id) {
        byte[] wrapped = tables.get(0).get(id);
        boolean ok = caller.equals("gateway") && wrapped != null;
        audit.add("unwrap " + id + " by " + caller + (ok ? " granted" : " denied"));
        return ok ? Main.open(kek, "", wrapped) : null;
    }
    int destroy(String id) { int n = 0; for (var t : tables) if (t.remove(id) != null) n++; return n; }
    int rotate(String next) {
        byte[] fresh = Main.sha(next);
        tables.forEach(t -> t.replaceAll((id, w) -> Main.seal(fresh, "", Main.open(kek, "", w))));
        kek = fresh;
        return tables.stream().mapToInt(Map::size).sum();
    }

    // Test hook, not service API: the data keys an attacker holding a wrapping key recovers from every wrapped copy.
    List<byte[]> recover(byte[] with) {
        return tables.stream().flatMap(t -> t.values().stream()).map(w -> Main.open(with, "", w)).filter(Objects::nonNull).toList();
    }
}
