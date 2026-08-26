import java.util.List;

// Not GpsTrackedRefrigeratedTruck extends RefrigeratedTruck extends Truck — that path
// needs a new subclass for every combination of capabilities a dispatcher might order.
class FleetVehicle {
    private final String baseType;
    private final List<VehicleCapability> capabilities;

    FleetVehicle(String baseType, List<VehicleCapability> capabilities) {
        this.baseType = baseType;
        this.capabilities = capabilities;
    }

    String describe() {
        StringBuilder sb = new StringBuilder(baseType);
        for (VehicleCapability c : capabilities) {
            sb.append(", ").append(c.describe());
        }
        return sb.toString();
    }
}
