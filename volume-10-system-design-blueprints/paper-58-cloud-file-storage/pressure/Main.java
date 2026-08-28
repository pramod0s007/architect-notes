import java.util.Random;

public class Main {
    private static final int FILE_SIZE = 100_000;
    private static final int VERSIONS = 5;

    public static void main(String[] args) {
        NaiveFileStore store = new NaiveFileStore();

        byte[] file = randomBytes(FILE_SIZE, new Random(1));
        store.store(file);

        for (int v = 1; v < VERSIONS; v++) {
            file = withSmallEdit(file, v); // each version: one small change from the last
            store.store(file);
        }

        System.out.println("File size: " + FILE_SIZE + " bytes. Versions saved: " + VERSIONS + ", each with one small edit.");
        System.out.println("Total bytes stored: " + store.totalBytesStored());
        System.out.println("That's " + VERSIONS + "x the file size, even though each edit changed only a few bytes.");
    }

    static byte[] randomBytes(int size, Random random) {
        byte[] bytes = new byte[size];
        random.nextBytes(bytes);
        return bytes;
    }

    static byte[] withSmallEdit(byte[] original, int seed) {
        byte[] edited = original.clone();
        Random random = new Random(seed);
        int position = random.nextInt(original.length);
        edited[position] = (byte) (edited[position] + 1); // flip a handful of bytes
        return edited;
    }
}
