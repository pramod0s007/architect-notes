public final class StationAsteroidAction implements Action {

    @Override
    public void apply(Object a, Object b) {
        System.out.println("damage(" + label(a) + ") — struck by " + label(b));
    }

    @Override
    public String description() {
        return "Station + Asteroid -> damage(station)";
    }

    private static String label(Object object) {
        return object.getClass().getSimpleName();
    }
}
