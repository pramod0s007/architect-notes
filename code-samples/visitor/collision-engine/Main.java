/**
 * Run: javac *.java && java Main
 */
public final class Main {

    public static void main(String[] args) {
        Ship ship = new Ship("Odyssey");
        Station station = new Station("Relay-7");
        Comet comet = new Comet("C/2024-A1");
        Asteroid asteroid = new Asteroid(1200);

        demonstrateShipStation(ship, station);
        demonstrateShipComet(ship, comet);
        demonstrateStationAsteroid(station, asteroid);
    }

    private static void demonstrateShipStation(Ship ship, Station station) {
        CollisionVisitor visitor = new CollisionVisitor(station);
        ship.accept(visitor);
        station.accept(new CollisionVisitor(ship));
        System.out.println("Ship + Station -> " + visitor.outcome());
    }

    private static void demonstrateShipComet(Ship ship, Comet comet) {
        CollisionVisitor visitor = new CollisionVisitor(comet);
        ship.accept(visitor);
        System.out.println("Ship + Comet    -> " + visitor.outcome());
    }

    private static void demonstrateStationAsteroid(Station station, Asteroid asteroid) {
        CollisionVisitor visitor = new CollisionVisitor(asteroid);
        station.accept(visitor);
        System.out.println("Station + Asteroid -> " + visitor.outcome());
    }
}
