// A retrieved candidate document. humanRelevanceLabel is ground truth -- what a
// human evaluator determined this document's actual relevance to the query is.
// It is used here only to CHECK each ranking method's answer, never fed into
// any ranking method itself, exactly like a real IR evaluation would use it.
// initialBiEncoderScore is a fixed number representing what a real bi-encoder
// would plausibly produce for this query-document pair -- standing in for
// actual independent-embedding inference, the same simplification used
// elsewhere in this volume (see Papers 68-69's Document.java).
class Candidate {
    final String id;
    final String text;
    final double humanRelevanceLabel;
    final double initialBiEncoderScore;

    Candidate(String id, String text, double humanRelevanceLabel, double initialBiEncoderScore) {
        this.id = id;
        this.text = text;
        this.humanRelevanceLabel = humanRelevanceLabel;
        this.initialBiEncoderScore = initialBiEncoderScore;
    }
}
