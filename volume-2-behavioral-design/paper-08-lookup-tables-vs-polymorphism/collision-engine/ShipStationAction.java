public final class ShipStationAction implements Action {

    @Override
    public void apply(Object a, Object b) {
        System.out.println("resolveCollision(" + label(a) + ", " + label(b) + ")");
    }

    @Override
    public String description() {
        return "Ship + Station -> resolveCollision";
    }

    private static String label(Object object) {
        return object.getClass().getSimpleName();
    }
}
