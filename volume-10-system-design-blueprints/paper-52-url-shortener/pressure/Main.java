public class Main {
    public static void main(String[] args) {
        double[] fillLevels = {0.0, 0.5, 0.8, 0.95};
        int samplesPerLevel = 500;

        System.out.println("Random 3-char code + collision retry, keyspace=" + RandomCodeShortener.KEYSPACE_SIZE);
        for (double fill : fillLevels) {
            RandomCodeShortener shortener = new RandomCodeShortener(42);
            shortener.prefillTo(fill);

            long totalAttempts = 0;
            for (int i = 0; i < samplesPerLevel; i++) {
                totalAttempts += shortener.shorten();
            }
            double avgAttempts = (double) totalAttempts / samplesPerLevel;
            System.out.printf("  keyspace %.0f%% full: avg attempts per new code = %.2f%n", fill * 100, avgAttempts);
        }
        System.out.println("Attempts climb toward 1/(1-fill) -- the math every naive shortener eventually hits.");
    }
}
