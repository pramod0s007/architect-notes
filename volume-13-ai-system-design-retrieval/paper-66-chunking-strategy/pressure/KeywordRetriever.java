import java.util.List;

// Stands in for a real vector/embedding retriever: scores each chunk by how many
// query words it contains and returns the single best-scoring chunk. The failure
// demonstrated in this paper is a property of the CHUNK BOUNDARY, not of the
// retrieval algorithm -- it reproduces identically with a real embedding-based
// retriever, because no retriever can return text a chunk simply doesn't contain.
class KeywordRetriever {
    String bestChunk(List<String> chunks, String query) {
        String[] queryWords = query.toLowerCase().split("\\s+");
        String best = chunks.get(0);
        int bestScore = -1;
        for (String c : chunks) {
            int score = 0;
            String lower = c.toLowerCase();
            for (String w : queryWords) {
                if (lower.contains(w)) {
                    score++;
                }
            }
            if (score > bestScore) {
                bestScore = score;
                best = c;
            }
        }
        return best;
    }
}
