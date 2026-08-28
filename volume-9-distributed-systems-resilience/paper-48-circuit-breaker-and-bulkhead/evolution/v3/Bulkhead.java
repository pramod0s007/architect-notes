import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Supplier;

// A dedicated, bounded thread pool for one downstream dependency. Whatever
// happens to calls submitted here -- slow, retrying, hung -- it can never
// consume threads that belong to a different dependency's bulkhead.
class Bulkhead {
    private final ExecutorService pool;

    Bulkhead(int poolSize) {
        this.pool = Executors.newFixedThreadPool(poolSize);
    }

    <T> Future<T> submit(Supplier<T> task) {
        return pool.submit(task::get);
    }

    void shutdown() {
        pool.shutdown();
    }
}
