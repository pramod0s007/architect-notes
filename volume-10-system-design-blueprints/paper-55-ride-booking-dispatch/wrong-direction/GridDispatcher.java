import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Buckets drivers into fixed-size grid cells. A ride request only scans the
// rider's own cell and its 8 neighbors -- never the whole city -- so the
// cost per request stops growing with the total number of drivers and
// starts depending only on driver DENSITY per cell.
class GridDispatcher {
    private final double cellSize;
    private final Map<Long, List<Driver>> grid = new HashMap<>();

    GridDispatcher(List<Driver> drivers, double cellSize) {
        this.cellSize = cellSize;
        for (Driver d : drivers) {
            grid.computeIfAbsent(cellKey(d.x, d.y), k -> new ArrayList<>()).add(d);
        }
    }

    Driver findNearest(double riderX, double riderY) {
        int riderCellX = (int) (riderX / cellSize);
        int riderCellY = (int) (riderY / cellSize);

        Driver nearest = null;
        double bestDistance = Double.MAX_VALUE;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                List<Driver> cellDrivers = grid.get(key(riderCellX + dx, riderCellY + dy));
                if (cellDrivers == null) continue;
                for (Driver d : cellDrivers) {
                    double dist = d.distanceTo(riderX, riderY);
                    if (dist < bestDistance) {
                        bestDistance = dist;
                        nearest = d;
                    }
                }
            }
        }
        return nearest;
    }

    private long cellKey(double x, double y) {
        return key((int) (x / cellSize), (int) (y / cellSize));
    }

    private long key(int cellX, int cellY) {
        return ((long) cellX << 32) ^ (cellY & 0xFFFFFFFFL);
    }
}
