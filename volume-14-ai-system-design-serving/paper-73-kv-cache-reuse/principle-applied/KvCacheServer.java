import java.util.HashMap;
import java.util.Map;

// Caches the total token length already computed for a prefix, keyed by the
// prefix's actual CONTENT (a hash/fingerprint of everything in it), not just how
// long it happens to be. A turn whose prior prefix is already cached only pays
// the cost of its own new tokens; everything before it is reused, not recomputed.
class KvCacheServer {
    private final Map<String, Integer> cachedPrefixLength = new HashMap<>();

    int tokensProcessedForTurn(String priorPrefixKey, int priorPrefixLength, int newTokens, String newPrefixKey) {
        boolean cacheHit = cachedPrefixLength.containsKey(priorPrefixKey);
        int tokensToProcess = cacheHit ? newTokens : priorPrefixLength + newTokens;
        cachedPrefixLength.put(newPrefixKey, priorPrefixLength + newTokens);
        return tokensToProcess;
    }
}
