import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Main {
    private static final int FILE_SIZE = 100_000;

    public static void main(String[] args) {
        byte[] original = randomBytes(FILE_SIZE, new Random(1));
        byte[] edited = withInsertionNearStart(original, 5); // insert 5 bytes near the beginning

        ContentDefinedChunker chunker = new ContentDefinedChunker();
        ChunkStore store = new ChunkStore();

        List<byte[]> originalChunks = chunker.chunk(original);
        int newFromOriginal = store.storeChunks(originalChunks);

        List<byte[]> editedChunks = chunker.chunk(edited);
        int newFromEdited = store.storeChunks(editedChunks);

        System.out.println("Original file: " + FILE_SIZE + " bytes, chunked into " + originalChunks.size() + " chunks, all new.");
        System.out.println("Edited file (5 bytes inserted near the start): " + edited.length + " bytes, chunked into " + editedChunks.size() + " chunks.");
        System.out.println("New chunks needed for the edited version: " + newFromEdited + " out of " + editedChunks.size()
                + " (" + (editedChunks.size() - newFromEdited) + " reused unchanged from the original)");
        System.out.println("Total bytes actually stored for both versions: " + store.totalBytesStored()
                + " (vs. " + (original.length + edited.length) + " if both were stored in full)");
    }

    static byte[] randomBytes(int size, Random random) {
        byte[] bytes = new byte[size];
        random.nextBytes(bytes);
        return bytes;
    }

    static byte[] withInsertionNearStart(byte[] original, int insertLength) {
        byte[] inserted = new byte[insertLength];
        new Random(99).nextBytes(inserted);
        byte[] result = new byte[original.length + insertLength];
        int insertAt = 10;
        System.arraycopy(original, 0, result, 0, insertAt);
        System.arraycopy(inserted, 0, result, insertAt, insertLength);
        System.arraycopy(original, insertAt, result, insertAt + insertLength, original.length - insertAt);
        return result;
    }
}
