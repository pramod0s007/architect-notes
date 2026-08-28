import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main {
    private static final int WARMUP_REQUESTS = 2000;
    private static final int REQUESTS = 5000;
    private static final double CITY_SIZE = 1000.0;

    public static void main(String[] args) {
        for (int driverCount : new int[]{1000, 10000, 50000}) {
            List<Driver> drivers = randomDrivers(driverCount, new Random(1));
            NaiveDispatcher dispatcher = new NaiveDispatcher(drivers);
            Random riderRandom = new Random(2);

            for (int i = 0; i < WARMUP_REQUESTS; i++) { // let the JIT warm up before measuring
                dispatcher.findNearest(riderRandom.nextDouble() * CITY_SIZE, riderRandom.nextDouble() * CITY_SIZE);
            }

            long start = System.nanoTime();
            for (int i = 0; i < REQUESTS; i++) {
                double rx = riderRandom.nextDouble() * CITY_SIZE;
                double ry = riderRandom.nextDouble() * CITY_SIZE;
                dispatcher.findNearest(rx, ry);
            }
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;

            System.out.printf("drivers=%,7d : %d ride requests matched in %dms%n", driverCount, REQUESTS, elapsedMs);
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
