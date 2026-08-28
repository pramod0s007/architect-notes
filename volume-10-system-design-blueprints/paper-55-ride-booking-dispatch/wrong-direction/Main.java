import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// The overcorrection: "grid bucketing worked, so ANY grid must work." A cell
// size picked without regard to driver density -- here, one cell roughly
// the size of the whole city -- means every driver still falls into the
// same handful of cells, and the search still scans nearly everyone.
public class Main {
    private static final int WARMUP_REQUESTS = 2000;
    private static final int REQUESTS = 5000;
    private static final double CITY_SIZE = 1000.0;
    private static final double TOO_LARGE_CELL_SIZE = 1000.0; // one cell covers the whole city

    public static void main(String[] args) {
        for (int driverCount : new int[]{1000, 10000, 50000}) {
            List<Driver> drivers = randomDrivers(driverCount, new Random(1));
            GridDispatcher dispatcher = new GridDispatcher(drivers, TOO_LARGE_CELL_SIZE);
            Random riderRandom = new Random(2);

            for (int i = 0; i < WARMUP_REQUESTS; i++) {
                dispatcher.findNearest(riderRandom.nextDouble() * CITY_SIZE, riderRandom.nextDouble() * CITY_SIZE);
            }

            long start = System.nanoTime();
            for (int i = 0; i < REQUESTS; i++) {
                double rx = riderRandom.nextDouble() * CITY_SIZE;
                double ry = riderRandom.nextDouble() * CITY_SIZE;
                dispatcher.findNearest(rx, ry);
            }
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;

            System.out.printf("drivers=%,7d : %d ride requests matched in %dms (cell size == city size)%n", driverCount, REQUESTS, elapsedMs);
        }
    }

    static List<Driver> randomDrivers(int count, Random random) {
        List<Driver> drivers = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            drivers.add(new Driver("D" + i, random.nextDouble() * CITY_SIZE, random.nextDouble() * CITY_SIZE));
        }
        return drivers;
    }
}
