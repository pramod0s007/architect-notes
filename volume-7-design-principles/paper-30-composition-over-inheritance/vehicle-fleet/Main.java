import java.util.List;

public class Main {
    public static void main(String[] args) {
        FleetVehicle plainTruck = new FleetVehicle("Truck", List.of());
        FleetVehicle gpsTruck = new FleetVehicle("Truck", List.of(new GpsTracking()));
        FleetVehicle gpsRefrigeratedTruck = new FleetVehicle(
                "Truck", List.of(new GpsTracking(), new Refrigeration()));

        System.out.println(plainTruck.describe());
        System.out.println(gpsTruck.describe());
        System.out.println(gpsRefrigeratedTruck.describe());
        System.out.println("A 3rd capability (hazmat-certified) adds one class, not a new subclass per combination.");
    }
}
