import java.util.Arrays;

// =============================================================================
// v1 — NO PATTERN (Month 1)
// =============================================================================
// Domain: Sorting Algorithms
//
// SortService with 2 algorithms (bubble sort and merge sort).
// This code is CORRECT AS-IS. No pressure yet. No pattern needed.
//
// Why this is fine:
//   - Two stable algorithms, each appropriate for different data sizes
//   - The service is only called from one place in production
//   - No request has come in to add more algorithms
//   - No runtime swapping is needed yet
//
// Comments: "correct as-is, no pressure yet"
// =============================================================================

public class v1_NoPattern {

    static class SortService {

        // Two algorithms. Simple. Stable. No pressure.
        public int[] sort(int[] data, String algorithm) {
            int[] copy = Arrays.copyOf(data, data.length);

            if ("bubble".equals(algorithm)) {
                return bubbleSort(copy);
            }
            if ("merge".equals(algorithm)) {
                return mergeSort(copy);
            }
            throw new IllegalArgumentException("Unknown algorithm: " + algorithm);
        }

        // Bubble sort: O(n^2) — fine for small datasets (n < 1000)
        private int[] bubbleSort(int[] arr) {
            int n = arr.length;
            for (int i = 0; i < n - 1; i++) {
                for (int j = 0; j < n - i - 1; j++) {
                    if (arr[j] > arr[j + 1]) {
                        int tmp = arr[j];
                        arr[j]     = arr[j + 1];
                        arr[j + 1] = tmp;
                    }
                }
            }
            return arr;
        }

        // Merge sort: O(n log n) — reliable for general purpose
        private int[] mergeSort(int[] arr) {
            if (arr.length <= 1) return arr;
            int mid    = arr.length / 2;
            int[] left  = mergeSort(Arrays.copyOfRange(arr, 0, mid));
            int[] right = mergeSort(Arrays.copyOfRange(arr, mid, arr.length));
            return merge(left, right);
        }

        private int[] merge(int[] left, int[] right) {
            int[] result = new int[left.length + right.length];
            int i = 0, j = 0, k = 0;
            while (i < left.length && j < right.length) {
                result[k++] = (left[i] <= right[j]) ? left[i++] : right[j++];
            }
            while (i < left.length)  result[k++] = left[i++];
            while (j < right.length) result[k++] = right[j++];
            return result;
        }
    }

    public static void main(String[] args) {
        SortService service = new SortService();
        int[] data = {5, 2, 8, 1, 9, 3, 7, 4, 6};

        System.out.println("Input:      " + Arrays.toString(data));
        System.out.println("Bubble:     " + Arrays.toString(service.sort(data, "bubble")));
        System.out.println("Merge:      " + Arrays.toString(service.sort(data, "merge")));

        // Verify correctness
        int[] sorted = service.sort(data, "merge");
        for (int i = 0; i < sorted.length - 1; i++) {
            assert sorted[i] <= sorted[i + 1] : "Sort failed at index " + i;
        }
        System.out.println("Assertion passed: array is sorted");
    }
}
