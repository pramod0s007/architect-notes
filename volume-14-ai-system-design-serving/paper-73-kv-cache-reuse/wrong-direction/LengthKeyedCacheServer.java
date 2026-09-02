import java.util.HashMap;
import java.util.Map;

// The overcorrection: cache by prefix LENGTH instead of content, on the theory
// that length is cheaper to compute and compare than hashing real content. Two
// different conversations that happen to reach the same token count get treated
// as the same cached prefix -- because, by this cache's own key, they are.
class LengthKeyedCacheServer {
    private final Map<Integer, String> cachedContentForLength = new HashMap<>();

    void recordPrefix(int lengthTokens, String actualContent) {
        cachedContentForLength.put(lengthTokens, actualContent);
    }

    // Returns whatever content is cached for this length -- which may or may not
    // actually be the requested conversation's real prefix.
    String servedCacheContentFor(int lengthTokens) {
        return cachedContentForLength.get(lengthTokens);
    }
}
