import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

// Same operation, applied to many INDEPENDENT items. No dependency between them.
class DataParallelDemo {
    static void run() {
        List<String> rawImages = IntStream.range(0, 1000)
                .mapToObj(i -> "image" + i + ".raw")
                .collect(Collectors.toList());

        AtomicInteger mismatches = new AtomicInteger(0);
        Set<String> threadsUsed = ConcurrentHashMap.newKeySet();

        List<String> results = rawImages.parallelStream()
                .map(raw -> {
                    threadsUsed.add(Thread.currentThread().getName());
                    return ImagePipeline.correctSequential(raw);
                })
                .collect(Collectors.toList());

        for (int i = 0; i < rawImages.size(); i++) {
            String expected = ImagePipeline.correctSequential(rawImages.get(i));
            if (!expected.equals(results.get(i))) mismatches.incrementAndGet();
        }

        System.out.println("Data parallelism: 1000 images, " + mismatches.get() + " mismatches, "
                + threadsUsed.size() + " distinct threads used");
    }
}
