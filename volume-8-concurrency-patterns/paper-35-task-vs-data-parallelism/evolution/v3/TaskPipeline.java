import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Different, heterogeneous operations, each with its own dedicated pool.
// Stage N of one image can run while stage N+1 of another image also runs.
class TaskPipeline {
    private final ExecutorService decodeStage = Executors.newFixedThreadPool(2);
    private final ExecutorService resizeStage = Executors.newFixedThreadPool(2);
    private final ExecutorService watermarkStage = Executors.newFixedThreadPool(2);
    private final ExecutorService encodeStage = Executors.newFixedThreadPool(2);

    final Set<String> decodeThreads = ConcurrentHashMap.newKeySet();
    final Set<String> resizeThreads = ConcurrentHashMap.newKeySet();
    final Set<String> watermarkThreads = ConcurrentHashMap.newKeySet();
    final Set<String> encodeThreads = ConcurrentHashMap.newKeySet();

    CompletableFuture<String> process(String raw) {
        return CompletableFuture
                .supplyAsync(() -> {
                    decodeThreads.add(Thread.currentThread().getName());
                    return ImagePipeline.decode(raw);
                }, decodeStage)
                .thenApplyAsync(decoded -> {
                    resizeThreads.add(Thread.currentThread().getName());
                    return ImagePipeline.resize(decoded);
                }, resizeStage)
                .thenApplyAsync(resized -> {
                    watermarkThreads.add(Thread.currentThread().getName());
                    return ImagePipeline.watermark(resized);
                }, watermarkStage)
                .thenApplyAsync(watermarked -> {
                    encodeThreads.add(Thread.currentThread().getName());
                    return ImagePipeline.encode(watermarked);
                }, encodeStage);
    }

    void shutdown() {
        decodeStage.shutdown();
        resizeStage.shutdown();
        watermarkStage.shutdown();
        encodeStage.shutdown();
    }
}
