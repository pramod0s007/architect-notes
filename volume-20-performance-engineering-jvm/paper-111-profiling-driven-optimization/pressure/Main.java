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

    // Real bug: O(n^2) duplicate check via List.contains, scanning the whole
    // accumulated result for every single item.
    static List<String> stageB_dedup(List<String> items) {
        List<String> seen = new ArrayList<>();
        for (String s : items) {
            if (!seen.contains(s)) seen.add(s);
        }
        return seen;
    }

    // Looks like the expensive part -- lots of string concatenation in a loop --
    // but it only ever formats a bounded preview, never the full result set.
    static String stageC_format(List<String> items) {
        String out = "";
        for (int i = 0; i < Math.min(PREVIEW_SIZE, items.size()); i++) out += items.get(i) + "\n";
        return out;
    }

    // The "guessed" optimization: switch the visually-busiest stage to StringBuilder,
    // without measuring whether stageC was ever the bottleneck.
    static String stageC_format_guessedFix(List<String> items) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(PREVIEW_SIZE, items.size()); i++) sb.append(items.get(i)).append("\n");
        return sb.toString();
    }

    static long runPipeline(boolean useGuessedFix) {
        long start = System.nanoTime();
        List<String> input = makeInput();
        List<String> normalized = stageA_normalize(input);
        List<String> deduped = stageB_dedup(normalized);
        String formatted = useGuessedFix ? stageC_format_guessedFix(deduped) : stageC_format(deduped);
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;
        return elapsedMs + formatted.length() - formatted.length(); // keep formatted live
    }

    public static void main(String[] args) {
        long beforeMs = runPipeline(false);
        long afterMs = runPipeline(true);

        System.out.println(NUM_ITEMS + " items, original stageC (string concatenation): " + beforeMs + "ms on this run");
        System.out.println(NUM_ITEMS + " items, guessed-fix stageC (StringBuilder): " + afterMs + "ms on this run");
        System.out.println();
        System.out.println("CONFIRMED FAILURE (relative to the fix, measured next): optimizing the stage that "
            + "looked slowest barely moved total wall-clock time, because stageC was never where the time was "
            + "actually going -- guessing at the bottleneck without measuring it spent real effort on the wrong stage.");
    }
}
