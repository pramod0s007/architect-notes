import java.util.Arrays;

// =============================================================================
// v3 — STRATEGY PATTERN APPLIED
// =============================================================================
// Domain: Sorting Algorithms
//
// WHAT CHANGED from v2:
//   - Introduced SortStrategy interface with a single sort(int[]) method
//   - 7 algorithm implementations: BubbleSort, MergeSort, QuickSort,
//     HeapSort, RadixSort, CountingSort, TimSort
//   - SortService accepts SortStrategy via constructor (and a setStrategy()
//     for runtime swapping)
//   - SortService has ZERO if-else — it delegates entirely to the strategy
//
// WHY Strategy Pattern here (confirmed behavior variation):
//   - The trigger: "data scientists want to swap algorithms at runtime by
//     dataset size" — this is the canonical behavior variation signal
//   - Each algorithm is a genuinely different computation:
//       BubbleSort:   O(n^2)   — simple, good for nearly-sorted small arrays
//       MergeSort:    O(n log n) stable — predictable, extra space
//       QuickSort:    O(n log n) avg — fastest in practice, in-place
//       HeapSort:     O(n log n) — guaranteed, no extra space, not stable
//       RadixSort:    O(nk) — beats comparison sorts for integers
//       CountingSort: O(n+k) — fastest when value range is small
//       TimSort:      adaptive — best for real-world partially-sorted data
//   - Adding a new algorithm (e.g. ShellSort) = new class only, zero
//     changes to SortService, zero changes to existing strategies
//   - Data scientists can compose a SmartSortStrategy (auto-selector) that
//     delegates to different strategies based on dataset characteristics —
//     impossible cleanly without the Strategy interface
// =============================================================================

// ---------------------------------------------------------------------------
// The Strategy interface — a single contract for all sorting algorithms
// ---------------------------------------------------------------------------
interface SortStrategy {
    // Implementations MUST return a sorted copy (do not sort in-place on input)
    int[] sort(int[] data);
    String name();
}

// ---------------------------------------------------------------------------
// Concrete strategies — each is independently testable and deployable
// ---------------------------------------------------------------------------

class BubbleSort implements SortStrategy {
    @Override public String name() { return "BubbleSort O(n^2)"; }
    @Override
    public int[] sort(int[] data) {
        int[] arr = Arrays.copyOf(data, data.length);
        int n = arr.length;
        for (int i = 0; i < n - 1; i++)
            for (int j = 0; j < n - i - 1; j++)
                if (arr[j] > arr[j + 1]) {
                    int tmp = arr[j]; arr[j] = arr[j + 1]; arr[j + 1] = tmp;
                }
        return arr;
    }
}

class MergeSort implements SortStrategy {
    @Override public String name() { return "MergeSort O(n log n) stable"; }
    @Override
    public int[] sort(int[] data) {
        return mergeSort(Arrays.copyOf(data, data.length));
    }
    private int[] mergeSort(int[] arr) {
        if (arr.length <= 1) return arr;
        int mid    = arr.length / 2;
        int[] left  = mergeSort(Arrays.copyOfRange(arr, 0, mid));
        int[] right = mergeSort(Arrays.copyOfRange(arr, mid, arr.length));
        return merge(left, right);
    }
    private int[] merge(int[] left, int[] right) {
        int[] r = new int[left.length + right.length];
        int i = 0, j = 0, k = 0;
        while (i < left.length && j < right.length)
            r[k++] = (left[i] <= right[j]) ? left[i++] : right[j++];
        while (i < left.length)  r[k++] = left[i++];
        while (j < right.length) r[k++] = right[j++];
        return r;
    }
}

class QuickSort implements SortStrategy {
    @Override public String name() { return "QuickSort O(n log n) avg"; }
    @Override
    public int[] sort(int[] data) {
        int[] arr = Arrays.copyOf(data, data.length);
        quickSort(arr, 0, arr.length - 1);
        return arr;
    }
    private void quickSort(int[] arr, int low, int high) {
        if (low < high) {
            int pivot = partition(arr, low, high);
            quickSort(arr, low, pivot - 1);
            quickSort(arr, pivot + 1, high);
        }
    }
    private int partition(int[] arr, int low, int high) {
        int pivot = arr[high], i = low - 1;
        for (int j = low; j < high; j++) {
            if (arr[j] <= pivot) {
                i++;
                int tmp = arr[i]; arr[i] = arr[j]; arr[j] = tmp;
            }
        }
        int tmp = arr[i + 1]; arr[i + 1] = arr[high]; arr[high] = tmp;
        return i + 1;
    }
}

class HeapSort implements SortStrategy {
    @Override public String name() { return "HeapSort O(n log n) in-place"; }
    @Override
    public int[] sort(int[] data) {
        int[] arr = Arrays.copyOf(data, data.length);
        int n = arr.length;
        for (int i = n / 2 - 1; i >= 0; i--) heapify(arr, n, i);
        for (int i = n - 1; i > 0; i--) {
            int tmp = arr[0]; arr[0] = arr[i]; arr[i] = tmp;
            heapify(arr, i, 0);
        }
        return arr;
    }
    private void heapify(int[] arr, int n, int i) {
        int largest = i, left = 2 * i + 1, right = 2 * i + 2;
        if (left < n  && arr[left]  > arr[largest]) largest = left;
        if (right < n && arr[right] > arr[largest]) largest = right;
        if (largest != i) {
            int tmp = arr[i]; arr[i] = arr[largest]; arr[largest] = tmp;
            heapify(arr, n, largest);
        }
    }
}

class RadixSort implements SortStrategy {
    @Override public String name() { return "RadixSort O(nk) non-negative ints"; }
    @Override
    public int[] sort(int[] data) {
        int[] arr = Arrays.copyOf(data, data.length);
        int max = Arrays.stream(arr).max().orElse(0);
        for (int exp = 1; max / exp > 0; exp *= 10)
            countByDigit(arr, exp);
        return arr;
    }
    private void countByDigit(int[] arr, int exp) {
        int n = arr.length;
        int[] output = new int[n], count = new int[10];
        for (int v : arr) count[(v / exp) % 10]++;
        for (int i = 1; i < 10; i++) count[i] += count[i - 1];
        for (int i = n - 1; i >= 0; i--) output[--count[(arr[i] / exp) % 10]] = arr[i];
        System.arraycopy(output, 0, arr, 0, n);
    }
}

class CountingSort implements SortStrategy {
    @Override public String name() { return "CountingSort O(n+k) small range"; }
    @Override
    public int[] sort(int[] data) {
        int[] arr = Arrays.copyOf(data, data.length);
        int max = Arrays.stream(arr).max().orElse(0);
        int[] count = new int[max + 1];
        for (int v : arr) count[v]++;
        int[] result = new int[arr.length];
        int idx = 0;
        for (int v = 0; v <= max; v++)
            for (int c = 0; c < count[v]; c++) result[idx++] = v;
        return result;
    }
}

class TimSort implements SortStrategy {
    private static final int RUN = 32;
    @Override public String name() { return "TimSort adaptive (merge+insertion)"; }
    @Override
    public int[] sort(int[] data) {
        int[] arr = Arrays.copyOf(data, data.length);
        int n = arr.length;
        for (int i = 0; i < n; i += RUN)
            insertionSort(arr, i, Math.min(i + RUN - 1, n - 1));
        for (int size = RUN; size < n; size *= 2) {
            for (int left = 0; left < n; left += 2 * size) {
                int mid   = Math.min(left + size - 1, n - 1);
                int right = Math.min(left + 2 * size - 1, n - 1);
                if (mid < right) {
                    int[] merged = mergeParts(
                        Arrays.copyOfRange(arr, left, mid + 1),
                        Arrays.copyOfRange(arr, mid + 1, right + 1));
                    System.arraycopy(merged, 0, arr, left, merged.length);
                }
            }
        }
        return arr;
    }
    private void insertionSort(int[] arr, int left, int right) {
        for (int i = left + 1; i <= right; i++) {
            int key = arr[i], j = i - 1;
            while (j >= left && arr[j] > key) arr[j + 1] = arr[j--];
            arr[j + 1] = key;
        }
    }
    private int[] mergeParts(int[] left, int[] right) {
        int[] r = new int[left.length + right.length];
        int i = 0, j = 0, k = 0;
        while (i < left.length && j < right.length)
            r[k++] = (left[i] <= right[j]) ? left[i++] : right[j++];
        while (i < left.length)  r[k++] = left[i++];
        while (j < right.length) r[k++] = right[j++];
        return r;
    }
}

// ---------------------------------------------------------------------------
// SmartSort: A meta-strategy that selects the best algorithm at runtime
// based on dataset characteristics. This is the DATA SCIENTISTS' requirement.
// It's clean only because Strategy gives us a composable abstraction.
// ---------------------------------------------------------------------------
class SmartSort implements SortStrategy {
    private final SortStrategy smallDataStrategy  = new TimSort();
    private final SortStrategy generalStrategy    = new QuickSort();
    private final SortStrategy largeRangeStrategy = new MergeSort();
    private final SortStrategy integerStrategy    = new RadixSort();

    @Override public String name() { return "SmartSort (auto-selector)"; }

    @Override
    public int[] sort(int[] data) {
        int n = data.length;
        int max = Arrays.stream(data).max().orElse(0);

        if (n <= 32) {
            System.out.println("  [SmartSort] small n=" + n + " → " + smallDataStrategy.name());
            return smallDataStrategy.sort(data);
        }
        if (max <= 10_000) {
            System.out.println("  [SmartSort] small range max=" + max + " → " + integerStrategy.name());
            return integerStrategy.sort(data);
        }
        if (n > 100_000) {
            System.out.println("  [SmartSort] large n=" + n + " → " + largeRangeStrategy.name());
            return largeRangeStrategy.sort(data);
        }
        System.out.println("  [SmartSort] general n=" + n + " → " + generalStrategy.name());
        return generalStrategy.sort(data);
    }
}

// ---------------------------------------------------------------------------
// SortService — zero if-else, delegates entirely to the strategy
// ---------------------------------------------------------------------------
class SortService {

    private SortStrategy strategy;

    public SortService(SortStrategy strategy) {
        this.strategy = strategy;
    }

    // Swap strategy at runtime — the data scientists' requirement fulfilled
    public void setStrategy(SortStrategy strategy) {
        this.strategy = strategy;
    }

    // No if-else — pure delegation
    public int[] sort(int[] data) {
        return strategy.sort(data);
    }

    public String currentAlgorithm() {
        return strategy.name();
    }
}

// ---------------------------------------------------------------------------
// Demo
// ---------------------------------------------------------------------------
public class v3_StrategyApplied {
    public static void main(String[] args) {
        int[] data = {64, 25, 12, 22, 11, 90, 34, 55, 1, 78};
        System.out.println("Input: " + Arrays.toString(data));

        // Explicit algorithm selection
        SortStrategy[] strategies = {
            new BubbleSort(), new MergeSort(), new QuickSort(),
            new HeapSort(), new RadixSort(), new CountingSort(), new TimSort()
        };

        for (SortStrategy s : strategies) {
            int[] result = s.sort(data);
            System.out.printf("%-42s → %s%n", s.name(), Arrays.toString(result));
        }

        // Runtime strategy swap — the key requirement
        System.out.println("\n=== Runtime swap ===");
        SortService service = new SortService(new BubbleSort());
        System.out.println("Initially: " + service.currentAlgorithm());
        service.setStrategy(new QuickSort());
        System.out.println("After swap: " + service.currentAlgorithm());
        System.out.println("Result: " + Arrays.toString(service.sort(data)));

        // SmartSort: auto-selects based on dataset characteristics
        System.out.println("\n=== SmartSort (auto-selector) ===");
        service.setStrategy(new SmartSort());

        int[] smallData = {5, 2, 8};
        System.out.println("Small array: " + Arrays.toString(service.sort(smallData)));

        int[] mediumData = new int[100];
        for (int i = 0; i < 100; i++) mediumData[i] = (int)(Math.random() * 10_000);
        int[] sorted = service.sort(mediumData);
        System.out.println("Medium array (first 10): " + Arrays.toString(Arrays.copyOf(sorted, 10)));

        // Adding a new algorithm — zero changes to SortService or any existing strategy
        System.out.println("\n=== New algorithm (ShellSort) added — zero changes to SortService ===");
        SortStrategy shellSort = new SortStrategy() {
            @Override public String name() { return "ShellSort (new, no SortService changes)"; }
            @Override public int[] sort(int[] data) {
                int[] arr = Arrays.copyOf(data, data.length);
                int gap = arr.length / 2;
                while (gap > 0) {
                    for (int i = gap; i < arr.length; i++) {
                        int temp = arr[i], j = i;
                        while (j >= gap && arr[j - gap] > temp) { arr[j] = arr[j - gap]; j -= gap; }
                        arr[j] = temp;
                    }
                    gap /= 2;
                }
                return arr;
            }
        };
        service.setStrategy(shellSort);
        System.out.println(service.currentAlgorithm());
        System.out.println(Arrays.toString(service.sort(data)));
    }
}
