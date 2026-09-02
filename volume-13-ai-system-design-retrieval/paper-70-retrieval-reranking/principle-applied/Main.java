import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // The fix: rerank the SAME top-3 candidates with a cross-encoder-style scorer
        // that reads the query and each document TOGETHER, rather than comparing two
        // separately-computed embeddings. This is more expensive per pair -- which is
        // exactly why it's only run over the small top-k the bi-encoder already
        // narrowed down, not the whole corpus.
        List<Candidate> candidates = new ArrayList<>();
        candidates.add(new Candidate("D1",
            "Password reset instructions: general guide for resetting your password from the login page.",
            0.50, 0.55));
        candidates.add(new Candidate("D2",
            "If your account is locked due to too many failed login attempts, wait 30 minutes or " +
            "contact support to unlock it, then reset your password from the account settings page.",
            0.95, 0.93));
        candidates.add(new Candidate("D3",
            "Two-factor authentication setup guide.",
            0.10, 0.10));

        System.out.println("Query: \"how do I reset my password after my account was locked " +
            "due to too many failed attempts\"");
        System.out.println("Reranked by cross-encoder joint relevance score:");

        candidates.sort((a, b) -> Double.compare(b.crossEncoderScore, a.crossEncoderScore));
        for (Candidate c : candidates) {
            System.out.printf("  %s: cross-encoder score=%.2f, human relevance label=%.2f%n",
                c.id, c.crossEncoderScore, c.humanRelevanceLabel);
        }

        Candidate top = candidates.get(0);
        Candidate mostRelevant = candidates.stream()
            .max((a, b) -> Double.compare(a.humanRelevanceLabel, b.humanRelevanceLabel)).get();

        System.out.println();
        System.out.println("Sent onward as top-1: " + top.id + " (cross-encoder score " + top.crossEncoderScore + ")");
        System.out.println("Actually most relevant, by human label: " + mostRelevant.id
            + " (human relevance " + mostRelevant.humanRelevanceLabel + ")");
        System.out.println(top.id.equals(mostRelevant.id)
            ? "FIXED: reranking with a joint query-document score correctly promotes the truly best " +
              "answer to position 1 -- the same document that was already in the top-3, just misranked."
            : "Unexpected -- investigate.");
    }
}
