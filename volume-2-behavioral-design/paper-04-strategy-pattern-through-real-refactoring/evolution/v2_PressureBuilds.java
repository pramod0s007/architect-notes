import java.util.Arrays;

// =============================================================================
// v2 — PRESSURE BUILDS (Month 1 → Month 9)
// =============================================================================
// Domain: Sorting Algorithms
//
// The data science team has been adding algorithms for 9 months.
// Every addition modifies SortService.
//
// PRESSURE: "Data scientists want to swap algorithms at runtime by dataset
// size — use the fastest algorithm for the input characteristics without
// redeploying."
//
// This IS behavior variation:
//   - Bubble sort: O(n^2) — good for small, nearly-sorted arrays
//   - Merge sort: O(n log n) — stable, good general purpose
//   - Quick sort: O(n log n) average — fast in practice, good cache locality
//   - Heap sort: O(n log n) — guaranteed, no extra space
//   - Radix sort: O(nk) — beats comparison-based sorts for integers
//   - Counting sort: O(n+k) — fastest when value range is small
//   - Tim sort: adaptive merge+insertion — best for real-world data
//
// Each algorithm is a genuinely different COMPUTATION. This is not data
// variation (same algorithm, different vocabulary) or object variation
// (same workflow, different resource). The algorithm itself varies.
//
// Pain points labeled with [!] below.
// =============================================================================

public class v2_PressureBuilds {

    static class SortService {

        public int[] sort(int[] data, String algorithm) {
            int[] copy = Arrays.copyOf(data, data.length);

            if ("bubble".equals(algorithm)) {
                return bubbleSort(copy);
            }
            if ("merge".equals(algorithm)) {
                return mergeSort(copy);
            }
            // [!] Month 3: data science added quick sort — had to open this class
            if ("quick".equals(algorithm)) {
                quickSort(copy, 0, copy.length - 1);
                return copy;
            }
            // [!] Month 5: heap sort added — another modification to this class
            if ("heap".equals(algorithm)) {
                heapSort(copy);
                return copy;
            }
            // [!] Month 7: radix sort added — "just add another else-if, easy"
            if ("radix".equals(algorithm)) {
                return radixSort(copy);
            }
            // [!] Month 8: counting sort added for small-range integer arrays
            if ("counting".equals(algorithm)) {
                return countingSort(copy);
            }
            // [!] Month 9: tim sort added — "we want the same thing Java uses internally"
            if ("tim".equals(algorithm)) {
                return timSort(copy);
            }
            throw new IllegalArgumentException("Unknown algorithm: " + algorithm);
        }

        // [!] This class now has 7 algorithms. Any new algorithm:
        //     1. Opens this class (risk of breaking existing algorithms)
        //     2. Adds another else-if branch
        //     3. Forces the full sort test suite to re-run for all 7 algorithms
        //     4. Cannot be unit-tested in isolation without instantiating SortService

        // [!] The data scientists' requirement: "select algorithm at runtime
        //     based on dataset characteristics" cannot be satisfied without
        //     either: (a) passing algorithm names as strings everywhere, or
        //     (b) adding a selectAlgorithm(int[] data) method that duplicates
        //     the if-else in a different form. Neither is clean.

        // Bubble sort: O(n^2)
        private int[] bubbleSort(int[] arr) {
            int n = arr.length;
            for (int i = 0; i < n - 1; i++) {
                for (int j = 0; j < n - i - 1; j++) {
                    if (arr[j] > arr[j + 1]) {
                        int tmp = arr[j]; arr[j] = arr[j + 1]; arr[j + 1] = tmp;
                    }
                }
            }
            return arr;
        }

        // Merge sort: O(n log n) stable
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

        // Quick sort: O(n log n) average — added month 3
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

        // Heap sort: O(n log n) in-place — added month 5
        private void heapSort(int[] arr) {
            int n = arr.length;
            for (int i = n / 2 - 1; i >= 0; i--) heapify(arr, n, i);
            for (int i = n - 1; i > 0; i--) {
                int tmp = arr[0]; arr[0] = arr[i]; arr[i] = tmp;
                heapify(arr, i, 0);
            }
        }
        private void heapify(int[] arr, int n, int i) {
            int largest = i, left = 2 * i + 1, right = 2 * i + 2;
            if (left < n && arr[left] > arr[largest])   largest = left;
            if (right < n && arr[right] > arr[largest]) largest = right;
            if (largest != i) {
                int tmp = arr[i]; arr[i] = arr[largest]; arr[largest] = tmp;
                heapify(arr, n, largest);
            }
        }

        // Radix sort: O(nk) for non-negative integers — added month 7
        private int[] radixSort(int[] arr) {
            int max = Arrays.stream(arr).max().orElse(0);
            int[] result = arr.clone();
            for (int exp = 1; max / exp > 0; exp *= 10)
                countByDigit(result, exp);
            return result;
        }
        private void countByDigit(int[] arr, int exp) {
            int n = arr.length;
            int[] output = new int[n], count = new int[10];
            for (int v : arr) count[(v / exp) % 10]++;
            for (int i = 1; i < 10; i++) count[i] += count[i - 1];
            for (int i = n - 1; i >= 0; i--) output[--count[(arr[i] / exp) % 10]] = arr[i];
            System.arraycopy(output, 0, arr, 0, n);
        }

        // Counting sort: O(n+k) — added month 8
        private int[] countingSort(int[] arr) {
            int max = Arrays.stream(arr).max().orElse(0);
            int[] count = new int[max + 1];
            for (int v : arr) count[v]++;
            int[] result = new int[arr.length];
            int idx = 0;
            for (int v = 0; v <= max; v++)
                for (int c = 0; c < count[v]; c++) result[idx++] = v;
            return result;
        }

        // Tim sort: adaptive merge + insertion — added month 9
        private int[] timSort(int[] arr) {
            // Simplified tim sort: insertion sort for small runs, merge for large
            int n = arr.length, runSize = 32;
            for (int i = 0; i < n; i += runSize)
                insertionSort(arr, i, Math.min(i + runSize - 1, n - 1));
            for (int size = runSize; size < n; size *= 2) {
                for (int left = 0; left < n; left += 2 * size) {
                    int mid   = Math.min(left + size - 1, n - 1);
                    int right = Math.min(left + 2 * size - 1, n - 1);
                    if (mid < right) {
                        int[] merged = merge(
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
                while (j >= left && arr[j] > key) arr[j-- + 1] = arr[j + 1];
                arr[j + 1] = key;
            }
        }
    }

    public static void main(String[] args) {
        SortService service = new SortService();
        int[] data = {64, 25, 12, 22, 11, 90, 34, 55, 1, 78};

        System.out.println("Input:    " + Arrays.toString(data));
        System.out.println("Bubble:   " + Arrays.toString(service.sort(data, "bubble")));
        System.out.println("Merge:    " + Arrays.toString(service.sort(data, "merge")));
        System.out.println("Quick:    " + Arrays.toString(service.sort(data, "quick")));
        System.out.println("Heap:     " + Arrays.toString(service.sort(data, "heap")));
        System.out.println("Radix:    " + Arrays.toString(service.sort(data, "radix")));
        System.out.println("Counting: " + Arrays.toString(service.sort(data, "counting")));
        System.out.println("Tim:      " + Arrays.toString(service.sort(data, "tim")));

        // [!] Data scientists want this — impossible without string passing:
        // service.sort(data, dataSize < 32 ? "insertion" : dataSize < 10000 ? "quick" : "radix");
        // This is clunky. Strategy Pattern solves it cleanly.
    }
}
