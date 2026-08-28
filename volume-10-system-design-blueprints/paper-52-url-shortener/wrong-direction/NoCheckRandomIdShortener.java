import java.util.Random;

// "The ID space is huge, so collisions are basically impossible" -- generates
// a random ID and hands it out with NO collision check at all. Looks fine in
// every manual test, because manual testing never generates enough IDs to
// notice the birthday paradox catching up.
class NoCheckRandomIdShortener {
    private static final int ID_SPACE = 1_048_576; // 2^20 -- "a million, basically infinite"

    private final Random random;

    NoCheckRandomIdShortener(long seed) {
        this.random = new Random(seed);
    }

    String shorten() {
        int id = random.nextInt(ID_SPACE);
        return Base62.encode(id);
    }
}
