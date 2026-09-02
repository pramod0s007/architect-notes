// A chunk that already came out of retrieval + reranking (Papers 66-70), correctly
// ranked by relevance. Its position in the SOURCE document is a separate, unrelated
// property -- documentOrder -- that has nothing to do with how relevant it is.
class RetrievedChunk {
    final String id;
    final String text;
    final int relevanceRank;   // 1 = most relevant, per the reranker
    final int documentOrder;   // position in the original source corpus

    RetrievedChunk(String id, String text, int relevanceRank, int documentOrder) {
        this.id = id;
        this.text = text;
        this.relevanceRank = relevanceRank;
        this.documentOrder = documentOrder;
    }
}
