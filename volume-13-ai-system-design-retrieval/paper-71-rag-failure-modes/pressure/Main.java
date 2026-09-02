import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Main {
    static final int CONTEXT_BUDGET_CHARS = 150;

    public static void main(String[] args) {
        // Retrieval + reranking (Papers 66-70) already did their job correctly: the
        // most relevant chunk really is C1, and it really was ranked #1. The bug in
        // this section is entirely in what happens AFTER that -- how the chunks get
        // assembled into one block of context under a fixed size budget.
        List<RetrievedChunk> chunks = new ArrayList<>();
        chunks.add(new RetrievedChunk("C1",
            "The maximum file upload size is 25MB. Files exceeding this limit are rejected " +
            "with an error message before upload begins.", 1, 3));
        chunks.add(new RetrievedChunk("C2",
            "Uploads are processed asynchronously and you'll receive an email confirmation " +
            "once complete.", 2, 2));
        chunks.add(new RetrievedChunk("C3",
            "For enterprise accounts, contact your account manager for custom limits.", 3, 1));

        // Naive assembly: concatenate in the order chunks appear in the source
        // corpus (a reasonable-sounding choice -- "present it in natural reading
        // order") rather than by relevance, then truncate to the budget.
        chunks.sort(Comparator.comparingInt(c -> c.documentOrder));
        StringBuilder context = new StringBuilder();
        for (RetrievedChunk c : chunks) {
            if (context.length() > 0) context.append(" ");
            context.append(c.text);
        }
        String assembled = context.substring(0, Math.min(CONTEXT_BUDGET_CHARS, context.length()));

        System.out.println("Query: \"what is the maximum file upload size and what happens if I exceed it\"");
        System.out.println("Assembly order: by document order (C3, C2, C1), budget=" + CONTEXT_BUDGET_CHARS + " chars");
        System.out.println("Full concatenated length before truncation: " + context.length() + " chars");
        System.out.println();
        System.out.println("Assembled context sent to generation:");
        System.out.println("  \"" + assembled + "\"");

        boolean hasAnswer = assembled.contains("25MB");
        System.out.println();
        System.out.println("Assembled context contains the actual answer (\"25MB\"): " + hasAnswer);
        System.out.println(hasAnswer
            ? "Unexpected -- investigate."
            : "CONFIRMED FAILURE: C1 -- the single most relevant chunk, correctly ranked #1 by the " +
              "reranker -- was cut entirely. It happened to be last in the source document, and " +
              "concatenate-then-truncate has no idea which chunk actually matters; it just runs out " +
              "of room. Adding more retrieved chunks to the context doesn't just risk irrelevant " +
              "noise -- it can directly push the most relevant one out of the budget entirely.");
    }
}
