import java.util.HashMap;
import java.util.Map;

/**
 * A frequency-counting cache: eviction is based on how many times a key
 * has been accessed while resident, not on recency. Uses simple admission
 * control -- a candidate key with a lower frequency than every resident
 * key is not admitted at all, rather than evicting a genuinely hotter
 * resident to make room for a key that has never proven itself. This
 * admission rule is exactly what makes LFU resistant to one-time scans:
 * a key touched only once can be admitted only by evicting a resident
 * whose own frequency is no higher than 1 -- it can never displace a
 * resident with a real access history above that.
 */
public class LfuCache {
    private final int capacity;
    private final Map<String, Integer> frequencyByKey = new HashMap<>();

    public LfuCache(int capacity) {
        this.capacity = capacity;
    }

    /** Returns true if the key was already cached (a hit), false if it was a miss. */
    public boolean access(String key) {
        if (frequencyByKey.containsKey(key)) {
            frequencyByKey.merge(key, 1, Integer::sum);
            return true;
        }
        admit(key);
        return false;
    }

    private void admit(String key) {
        if (frequencyByKey.size() < capacity) {
            frequencyByKey.put(key, 1);
            return;
        }
        String victim = null;
        int minFrequency = Integer.MAX_VALUE;
        for (Map.Entry<String, Integer> entry : frequencyByKey.entrySet()) {
            if (entry.getValue() < minFrequency) {
                minFrequency = entry.getValue();
                victim = entry.getKey();
            }
        }
        if (1 < minFrequency) {
            return; // candidate's starting frequency (1) doesn't beat any resident -- reject, don't cache it
        }
        frequencyByKey.remove(victim);
        frequencyByKey.put(key, 1);
    }

    public boolean contains(String key) {
        return frequencyByKey.containsKey(key);
    }

    public int frequencyOf(String key) {
        return frequencyByKey.getOrDefault(key, 0);
    }

    /** Halves every resident key's frequency count, rounding down but never below 1. */
    public void decayAll() {
        for (String key : frequencyByKey.keySet()) {
            frequencyByKey.put(key, Math.max(1, frequencyByKey.get(key) / 2));
        }
    }
}
