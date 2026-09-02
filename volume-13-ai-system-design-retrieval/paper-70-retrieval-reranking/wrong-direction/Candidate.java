// humanRelevanceLabel is ground truth, used only to check the ranking, never fed
// into it. biggerBiEncoderScore is a fixed number representing what a larger,
// higher-capacity bi-encoder would plausibly produce for this pair -- still an
// independent-embedding score, standing in for real inference the same way as
// elsewhere in this volume (see Papers 68-69's Document.java).
class Candidate {
    final String id;
    final String text;
    final double humanRelevanceLabel;
    final double biggerBiEncoderScore;

    Candidate(String id, String text, double humanRelevanceLabel, double biggerBiEncoderScore) {
        this.id = id;
        this.text = text;
        this.humanRelevanceLabel = humanRelevanceLabel;
        this.biggerBiEncoderScore = biggerBiEncoderScore;
    }
}
