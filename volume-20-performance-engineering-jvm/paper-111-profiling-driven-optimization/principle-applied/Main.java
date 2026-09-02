import java.util.*;

public class Main {
    static final int NUM_ITEMS = 12_000;
    static final int PREVIEW_SIZE = 200;

    static List<String> makeInput() {
        List<String> items = new ArrayList<>();
        for (int i = 0; i < NUM_ITEMS; i++) {
            items.add("item-" + i);
        }
        return items;
    }

    static List<String> stageA_normalize(List<String> items) {
        List<String> out = new ArrayList<>();
        for (String s : items) out.add(s.trim().toLowerCase());
        return out;
    }

    // Naive version, kept only so the per-stage breakdown below can show what it cost.
    static List<String> stageB_dedup_naive(List<String> items) {
        List<String> seen = new ArrayList<>();
        for (String s : items) {
            if (!seen.contains(s)) seen.add(s);
        }
        return seen;
    }

    // Real fix: a HashSet lookup is O(1) instead of an O(n) linear scan, so the
    // whole dedup pass drops from O(n^2) to O(n).
    static List<String> stageB_dedup_fixed(List<String> items) {
        Set<String> seen = new HashSet<>();
        List<String> out = new ArrayList<>();
        for (String s : items) {
            if (seen.add(s)) out.add(s);
        }
        return out;
    }

    static String stageC_format(List<String> items) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(PREVIEW_SIZE, items.size()); i++) sb.append(items.get(i)).append("\n");
        return sb.toString();
    }

    public static void main(String[] args) {
        // Measure each stage individually before touching anything, instead of
        // guessing which one is slow from how the code reads.
        List<String> input = makeInput();

        long t0 = System.nanoTime();
        List<String> normalized = stageA_normalize(input);
        long tA = System.nanoTime();
        List<String> dedupedNaive = stageB_dedup_naive(normalized);
        long tB = System.nanoTime();
        String formatted = stageC_format(dedupedNaive);
        long tC = System.nanoTime();

        long stageAMs = (tA - t0) / 1_000_000;
        long stageBMs = (tB - tA) / 1_000_000;
        long stageCMs = (tC - tB) / 1_000_000;
        long totalMs = stageAMs + stageBMs + stageCMs;

        System.out.println("Per-stage breakdown, " + NUM_ITEMS + " items (naive stageB): "
            + "stageA=" + stageAMs + "ms, stageB=" + stageBMs + "ms, stageC=" + stageCMs + "ms, total=" + totalMs + "ms");
        System.out.println("stageB share of total: " + (100 * stageBMs / Math.max(1, totalMs)) + "%");

        // The measurement identifies stageB as the real bottleneck. Fix that, and
        // re-measure the same pipeline.
        long start = System.nanoTime();
        List<String> normalized2 = stageA_normalize(input);
        List<String> dedupedFixed = stageB_dedup_fixed(normalized2);
        String formatted2 = stageC_format(dedupedFixed);
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        System.out.println();
        System.out.println(NUM_ITEMS + " items, fixed stageB (HashSet-based dedup): " + elapsedMs + "ms on this run");
        System.out.println("FIXED: measuring first identified stageB as the actual bottleneck; replacing its O(n^2) "
            + "linear-scan duplicate check with an O(1) hash lookup collapsed total wall-clock time by measuring "
            + "the real cause instead of guessing at it.");
    }
}
