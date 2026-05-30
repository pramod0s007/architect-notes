/**
 * Run: javac *.java && java Main
 */
public final class Main {

    public static void main(String[] args) {
        CollisionEngine engine = new CollisionEngine();
        engine.register(CollisionEngine.CollisionKey.of(Ship.class, Station.class), new ShipStationAction());
        engine.register(CollisionEngine.CollisionKey.of(Ship.class, Comet.class), new ShipCometAction());
        engine.register(CollisionEngine.CollisionKey.of(Station.class, Asteroid.class), new StationAsteroidAction());

        System.out.println("Lookup table: Map<CollisionKey, Action>");
        System.out.println();

        engine.resolve(new Ship(), new Station());
        engine.resolve(new Ship(), new Comet());
        engine.resolve(new Station(), new Asteroid());
    }

    static final class Ship {
    }

    static final class Station {
    }

    static final class Comet {
    }
    static final class Asteroid {
    }
}
