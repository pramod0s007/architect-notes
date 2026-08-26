// Fixed: data coupling. Only the two primitives actually used cross the boundary.
class RateCalculator {
    double quote(double weightKg, String destinationZip) {
        double base = weightKg * 2.5;
        return destinationZip.startsWith("0") ? base * 1.2 : base;
    }
}
