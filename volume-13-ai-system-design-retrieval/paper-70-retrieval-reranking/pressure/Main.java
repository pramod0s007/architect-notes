import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // A bi-encoder retrieval stage embeds the query and each document SEPARATELY,
        // with no interaction between them, then ranks by similarity of those two
        // independent embeddings. Fast, and good enough to find the right ballpark of
        // candidates -- which is exactly what happened here: the truly best answer IS
        // in this top-3. It just isn't ranked first.
        List<Candidate> candidates = new ArrayList<>();
        candidates.add(new Candidate("D1",
            "Password reset instructions: general guide for resetting your password from the login page.",
            0.50, 0.88));
        candidates.add(new Candidate("D2",
            "If your account is locked due to too many failed login attempts, wait 30 minutes or " +
            "contact support to unlock it, then reset your password from the account settings page.",
            0.95, 0.85));
        candidates.add(new Candidate("D3",
            "Two-factor authentication setup guide.",
            0.10, 0.60));

        System.out.println("Query: \"how do I reset my password after my account was locked " +
            "due to too many failed attempts\"");
        System.out.println("Ranked by initial bi-encoder retrieval score:");

        candidates.sort((a, b) -> Double.compare(b.initialBiEncoderScore, a.initialBiEncoderScore));
        for (Candidate c : candidates) {
            System.out.printf("  %s: bi-encoder score=%.2f, human relevance label=%.2f%n",
                c.id, c.initialBiEncoderScore, c.humanRelevanceLabel);
        }

        Candidate top = candidates.get(0);
        Candidate mostRelevant = candidates.stream()
            .max((a, b) -> Double.compare(a.humanRelevanceLabel, b.humanRelevanceLabel)).get();

        System.out.println();
        System.out.println("Sent onward as top-1: " + top.id + " (bi-encoder score " + top.initialBiEncoderScore + ")");
        System.out.println("Actually most relevant, by human label: " + mostRelevant.id
            + " (human relevance " + mostRelevant.humanRelevanceLabel + ")");
        System.out.println(top.id.equals(mostRelevant.id)
            ? "Unexpected -- investigate."
            : "CONFIRMED FAILURE: the top-1 sent onward is NOT the most relevant candidate. The truly " +
              "best answer (" + mostRelevant.id + ") was retrieved -- it's right here in the top-3 -- " +
              "but it's ranked #2, because a bi-encoder can't compare the query and a document jointly; " +
              "it only compares two independently-computed summaries of each.");
    }
}
