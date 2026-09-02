// A document, its real text, and a fixed similarity score representing what a
// real embedding model would produce for one specific query -- standing in for
// an actual embedding model + cosine similarity, the same simplification used
// in Paper 68, disclosed here for the same reason: the mechanism under test is
// how the fusion is done, not the embedding model itself.
class Document {
    final String id;
    final String text;
    final double vectorSimilarityToQuery;

    Document(String id, String text, double vectorSimilarityToQuery) {
        this.id = id;
        this.text = text;
        this.vectorSimilarityToQuery = vectorSimilarityToQuery;
    }
}
