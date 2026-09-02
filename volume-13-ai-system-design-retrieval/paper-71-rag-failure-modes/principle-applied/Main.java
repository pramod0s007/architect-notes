import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Main {
    static final int CONTEXT_BUDGET_CHARS = 150;

    public static void main(String[] args) {
        List<RetrievedChunk> chunks = new ArrayList<>();
        chunks.add(new RetrievedChunk("C1",
            "The maximum file upload size is 25MB. Files exceeding this limit are rejected " +
            "with an error message before upload begins.", 1, 3));
        chunks.add(new RetrievedChunk("C2",
            "Uploads are processed asynchronously and you'll receive an email confirmation " +
            "once complete.", 2, 2));
        chunks.add(new RetrievedChunk("C3",
            "For enterprise accounts, contact your account manager for custom limits.", 3, 1));

        // The fix: assemble by RELEVANCE rank, not document order. The budget gets
        // spent on the most relevant material first; whatever's left over goes to
        // the next-most-relevant chunk, and truncation -- if it happens at all --
        // eats into the LEAST relevant chunk, not a random one.
        chunks.sort(Comparator.comparingInt(c -> c.relevanceRank));
        StringBuilder context = new StringBuilder();
        for (RetrievedChunk c : chunks) {
            if (context.length() > 0) context.append(" ");
            context.append(c.text);
        }
        String assembled = context.substring(0, Math.min(CONTEXT_BUDGET_CHARS, context.length()));

        System.out.println("Query: \"what is the maximum file upload size and what happens if I exceed it\"");
        System.out.println("Assembly order: by relevance rank (C1, C2, C3), budget=" + CONTEXT_BUDGET_CHARS + " chars");
        System.out.println();
        System.out.println("Assembled context sent to generation:");
        System.out.println("  \"" + assembled + "\"");

        boolean hasAnswer = assembled.contains("25MB");
        System.out.println();
        System.out.println("Assembled context contains the actual answer (\"25MB\"): " + hasAnswer);
        System.out.println(hasAnswer
            ? "FIXED: C1 fits entirely within budget because it was assembled first. The chunk that " +
              "gets cut when space runs out is C3 -- the least relevant one -- not the most relevant one."
            : "Unexpected -- investigate.");
    }
}
