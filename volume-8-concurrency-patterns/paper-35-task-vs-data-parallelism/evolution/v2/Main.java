import java.util.concurrent.CompletableFuture;

public class Main {
    public static void main(String[] args) {
        String raw = "image1.raw";
        String correct = ImagePipeline.correctSequential(raw);

        // WRONG: treating 3 dependent steps as 3 independent "parallel" tasks.
        // Each stage reads `raw` directly instead of the PREVIOUS stage's output.
        CompletableFuture<String> decoded = CompletableFuture.supplyAsync(() -> ImagePipeline.decode(raw));
        CompletableFuture<String> resized = CompletableFuture.supplyAsync(() -> ImagePipeline.resize(raw));       // should be resize(decoded)
        CompletableFuture<String> watermarked = CompletableFuture.supplyAsync(() -> ImagePipeline.watermark(raw)); // should be watermark(resized)

        String wrongResult = ImagePipeline.encode(watermarked.join());

        System.out.println("Correct (sequential): " + correct);
        System.out.println("Wrong (data-parallelized dependent steps): " + wrongResult);
        System.out.println("Silently different, no exception: " + !correct.equals(wrongResult));
        System.out.println("v2: applying data parallelism to a dependency chain produces wrong output, not just a slowdown.");
    }
}
