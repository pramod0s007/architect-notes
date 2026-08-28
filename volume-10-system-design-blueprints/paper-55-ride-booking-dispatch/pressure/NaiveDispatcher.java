import java.util.List;

// Scans every driver in the city for every single ride request. Correct,
// and its cost grows linearly with the number of drivers on the platform --
// exactly the thing that grows as the business succeeds.
class NaiveDispatcher {
    private final List<Driver> drivers;

    NaiveDispatcher(List<Driver> drivers) {
        this.drivers = drivers;
    }

    Driver findNearest(double riderX, double riderY) {
        Driver nearest = null;
        double bestDistance = Double.MAX_VALUE;
        for (Driver d : drivers) {
            double dist = d.distanceTo(riderX, riderY);
            if (dist < bestDistance) {
                bestDistance = dist;
                nearest = d;
            }
        }
        return nearest;
    }
}
