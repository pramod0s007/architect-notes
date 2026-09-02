import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Main {
    // The overcorrection to truncation: make the budget generous enough that
    // nothing ever gets cut. That does fix truncation -- and removes the one
    // thing that was accidentally limiting how much retrieved (but stale or
    // duplicate) material could make it into the context at all.
    static final int CONTEXT_BUDGET_CHARS = 1000;

    public static void main(String[] args) {
        List<RetrievedChunk> chunks = new ArrayList<>();
        chunks.add(new RetrievedChunk("C1",
            "The maximum file upload size is 25MB. Files exceeding this limit are rejected " +
            "with an error message before upload begins.", 1, 3));
        // A near-duplicate chunk from an old FAQ page that was never removed from the
        // index when the limit changed. It's topically almost identical to C1, so it
        // scores just as high on relevance -- the retrieval and reranking stages have
        // no way to know it's now factually wrong; nothing marks it as superseded.
        chunks.add(new RetrievedChunk("C4",
            "The maximum file upload size is 10MB.", 2, 0));
        chunks.add(new RetrievedChunk("C2",
            "Uploads are processed asynchronously and you'll receive an email confirmation " +
            "once complete.", 3, 2));
        chunks.add(new RetrievedChunk("C3",
            "For enterprise accounts, contact your account manager for custom limits.", 4, 1));

        chunks.sort(Comparator.comparingInt(c -> c.relevanceRank));
        StringBuilder context = new StringBuilder();
        for (RetrievedChunk c : chunks) {
            if (context.length() > 0) context.append(" ");
            context.append(c.text);
        }
        String assembled = context.substring(0, Math.min(CONTEXT_BUDGET_CHARS, context.length()));

        System.out.println("Query: \"what is the maximum file upload size and what happens if I exceed it\"");
        System.out.println("Assembly: all 4 retrieved chunks, generous budget=" + CONTEXT_BUDGET_CHARS
            + " chars (nothing truncated)");
        System.out.println();
        System.out.println("Assembled context sent to generation:");
        System.out.println("  \"" + assembled + "\"");

        boolean has25 = assembled.contains("25MB");
        boolean has10 = assembled.contains("10MB");
        System.out.println();
        System.out.println("Contains \"25MB\": " + has25 + ", contains \"10MB\": " + has10);
        System.out.println((has25 && has10)
            ? "STILL FAILS, DIFFERENTLY: nothing was truncated, but the assembled context now contains " +
              "TWO DIFFERENT ANSWERS to the same question, with no field anywhere marking which one is " +
              "current. A generous budget didn't lose information -- it included stale, contradicting " +
              "information alongside the correct answer, and nothing in the context tells a reader " +
              "(or a generation step) which of the two numbers to trust."
            : "Unexpected -- investigate.");
    }
}
