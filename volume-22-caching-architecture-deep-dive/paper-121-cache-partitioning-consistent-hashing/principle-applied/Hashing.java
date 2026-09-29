/**
 * A small, deterministic string hash (FNV-1a, 32-bit) used instead of
 * String.hashCode() so the demo's numbers are stable across JDK versions
 * and don't depend on an unspecified/undocumented hash implementation.
 */
public final class Hashing {
    private static final int FNV_OFFSET_BASIS = 0x811c9dc5;
    private static final int FNV_PRIME = 0x01000193;

    private Hashing() {
    }

    public static long hash(String input) {
        int hash = FNV_OFFSET_BASIS;
        for (int i = 0; i < input.length(); i++) {
            hash ^= input.charAt(i);
            hash *= FNV_PRIME;
        }
        // Mask to a non-negative long so callers can treat it as an
        // unsigned 32-bit position on a hash ring / modulo space.
        return hash & 0xFFFFFFFFL;
    }
}
