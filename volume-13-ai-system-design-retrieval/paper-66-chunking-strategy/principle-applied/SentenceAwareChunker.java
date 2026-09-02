import java.util.ArrayList;
import java.util.List;

// Packs whole sentences into chunks up to a target size. Never splits a sentence,
// even if that means a chunk comes in under (or, for one long sentence, over) target.
class SentenceAwareChunker {
    private final int targetSize;

    SentenceAwareChunker(int targetSize) {
        this.targetSize = targetSize;
    }

    List<String> chunk(String text) {
        String[] sentences = text.split("(?<=\\.)\\s+");
        List<String> chunks = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        for (String sentence : sentences) {
            if (current.length() > 0 && current.length() + 1 + sentence.length() > targetSize) {
                chunks.add(current.toString());
                current = new StringBuilder();
            }
            if (current.length() > 0) {
                current.append(" ");
            }
            current.append(sentence);
        }
        if (current.length() > 0) {
            chunks.add(current.toString());
        }
        return chunks;
    }
}
