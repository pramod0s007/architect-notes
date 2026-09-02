// humanRelevanceLabel is ground truth, used only to check the ranking, never fed
// into it. crossEncoderScore is a fixed number representing what a real
// cross-encoder would plausibly produce for this query-document pair -- standing
// in for actual joint-attention inference, the same simplification used
// elsewhere in this volume (see Papers 68-69's Document.java).
class Candidate {
    final String id;
    final String text;
    final double humanRelevanceLabel;
    final double crossEncoderScore;

    Candidate(String id, String text, double humanRelevanceLabel, double crossEncoderScore) {
        this.id = id;
        this.text = text;
        this.humanRelevanceLabel = humanRelevanceLabel;
        this.crossEncoderScore = crossEncoderScore;
    }
}
