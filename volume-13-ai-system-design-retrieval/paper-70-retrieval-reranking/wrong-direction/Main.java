import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // The overcorrection: "rerank" using a bigger, better bi-encoder embedding
        // model -- still encoding the query and each document SEPARATELY, with no
        // interaction between them, just with more capacity. More capacity makes it
        // better at general topical similarity. It does not give it the one thing
        // it structurally lacks: the ability to check whether a specific document
        // actually addresses a specific detail in the query.
        List<Candidate> candidates = new ArrayList<>();
        candidates.add(new Candidate("D1",
            "Password reset instructions: general guide for resetting your password from the login page.",
            0.50, 0.90));
        candidates.add(new Candidate("D2",
            "If your account is locked due to too many failed login attempts, wait 30 minutes or " +
            "contact support to unlock it, then reset your password from the account settings page.",
            0.95, 0.87));
        candidates.add(new Candidate("D3",
            "Two-factor authentication setup guide.",
            0.10, 0.50));

        System.out.println("Query: \"how do I reset my password after my account was locked " +
            "due to too many failed attempts\"");
        System.out.println("Reranked by a BIGGER bi-encoder (still independent encoding, more capacity):");

        candidates.sort((a, b) -> Double.compare(b.biggerBiEncoderScore, a.biggerBiEncoderScore));
        for (Candidate c : candidates) {
            System.out.printf("  %s: bigger bi-encoder score=%.2f, human relevance label=%.2f%n",
                c.id, c.biggerBiEncoderScore, c.humanRelevanceLabel);
        }

        Candidate top = candidates.get(0);
        Candidate mostRelevant = candidates.stream()
            .max((a, b) -> Double.compare(a.humanRelevanceLabel, b.humanRelevanceLabel)).get();

        System.out.println();
        System.out.println("Sent onward as top-1: " + top.id + " (bigger bi-encoder score " + top.biggerBiEncoderScore + ")");
        System.out.println("Actually most relevant, by human label: " + mostRelevant.id
            + " (human relevance " + mostRelevant.humanRelevanceLabel + ")");
        System.out.println(top.id.equals(mostRelevant.id)
            ? "Unexpected -- investigate."
            : "STILL FAILS: the top-1 changed scores but not its identity -- " + top.id + " wins again, " +
              "for the same structural reason as before. A bigger bi-encoder is better at recognizing " +
              "'this document is generally about password resets,' which " + top.id + " clearly is. It " +
              "still can't check whether the document addresses the SPECIFIC detail in the query -- " +
              "being locked out from failed attempts -- because it never reads the query and the " +
              "document together. More capacity on the same architecture doesn't fix a limitation " +
              "that comes from the architecture itself.");
    }
}
