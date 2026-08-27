import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class Main {
    public static void main(String[] args) {
        DataParallelDemo.run();
        runTaskPipeline();
    }

    private static void runTaskPipeline() {
        TaskPipeline pipeline = new TaskPipeline();
        int imageCount = 20;

        List<CompletableFuture<String>> futures = new ArrayList<>();
        for (int i = 0; i < imageCount; i++) {
            futures.add(pipeline.process("image" + i + ".raw"));
        }

        int mismatches = 0;
        for (int i = 0; i < imageCount; i++) {
            String result = futures.get(i).join();
            String expected = ImagePipeline.correctSequential("image" + i + ".raw");
            if (!result.equals(expected)) mismatches++;
        }

        System.out.println("Task pipeline: " + imageCount + " images, " + mismatches + " mismatches");
        System.out.println("Decode ran on " + pipeline.decodeThreads.size() + " thread(s), "
                + "resize on " + pipeline.resizeThreads.size() + ", "
                + "watermark on " + pipeline.watermarkThreads.size() + ", "
                + "encode on " + pipeline.encodeThreads.size()
                + " -- 4 DIFFERENT stages running on 4 DIFFERENT dedicated pools, concurrently.");

        pipeline.shutdown();
    }
}
