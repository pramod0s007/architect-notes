import java.util.*;

public class Main {
    static final int NUM_ITEMS = 12_000;
    static final int PREVIEW_SIZE = 200;
    static final int TRIALS = 7;

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

    // Same fixed pipeline, timed only at the 3 stage boundaries -- the same
    // granularity that actually found the bottleneck in Paper 111's fix.
    static long runStageLevelTimed(List<String> input) {
        long start = System.nanoTime();
        List<String> normalized = stageA_normalize(input);
        List<String> deduped = stageB_dedup_fixed(normalized);
        String formatted = stageC_format(deduped);
        return (System.nanoTime() - start) / 1_000 + formatted.length() - formatted.length();
    }

    // Overcorrection: "measuring found the bug once, so measure everything, always" --
    // every single item's dedup step gets its own before/after timestamp, permanently,
    // even though the pipeline itself is now fast enough that this adds real overhead.
    static long runPerItemInstrumented(List<String> input) {
        long start = System.nanoTime();
        List<String> normalized = stageA_normalize(input);

        Set<String> seen = new HashSet<>();
        List<String> deduped = new ArrayList<>();
        long[] perItemNanos = new long[normalized.size()];
        for (int i = 0; i < normalized.size(); i++) {
            long itemStart = System.nanoTime();
            String s = normalized.get(i);
            if (seen.add(s)) deduped.add(s);
            perItemNanos[i] = System.nanoTime() - itemStart;
        }

        String formatted = stageC_format(deduped);
        long total = 0;
        for (long n : perItemNanos) total += n; // "utilize" the per-item log, as a real dashboard would
        return (System.nanoTime() - start) / 1_000 + formatted.length() - formatted.length() + (total - total);
    }

    static long median(java.util.function.Function<List<String>, Long> fn, List<String> input) {
        long[] times = new long[TRIALS];
        for (int t = 0; t < TRIALS; t++) times[t] = fn.apply(input);
        Arrays.sort(times);
        return times[TRIALS / 2];
    }

    public static void main(String[] args) {
        List<String> input = makeInput();

        // Warm up both paths before timing either.
        for (int i = 0; i < 20; i++) { runStageLevelTimed(input); runPerItemInstrumented(input); }

        long stageLevelUs = median(Main::runStageLevelTimed, input);
        long perItemUs = median(Main::runPerItemInstrumented, input);

        System.out.println(NUM_ITEMS + " items, fixed pipeline, stage-level timing only, median of " + TRIALS + " trials: " + stageLevelUs + "us");
        System.out.println(NUM_ITEMS + " items, fixed pipeline, permanent per-item timing, median of " + TRIALS + " trials: " + perItemUs + "us");
        System.out.println();
        System.out.println(perItemUs > stageLevelUs
            ? "CONFIRMED FAILURE: instrumenting every single item, permanently, on a pipeline that measuring "
              + "already fixed, makes the now-fast pipeline measurably slower again. The dedup step itself is "
              + "microseconds of work per item; a nanoTime() call and an array write on every item is no longer "
              + "negligible next to that, and it never stopped running once the real bottleneck was gone."
            : "Unexpected -- investigate.");
    }
}
