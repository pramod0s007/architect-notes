class Driver {
    final String id;
    final double x;
    final double y;

    Driver(String id, double x, double y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }

    double distanceTo(double rx, double ry) {
        double dx = x - rx;
        double dy = y - ry;
        return Math.sqrt(dx * dx + dy * dy);
    }
}
