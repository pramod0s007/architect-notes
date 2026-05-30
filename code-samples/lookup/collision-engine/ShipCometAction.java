public final class ShipCometAction implements Action {

    @Override
    public void apply(Object a, Object b) {
        System.out.println("destroy(" + label(a) + ") — collided with " + label(b));
    }

    @Override
    public String description() {
        return "Ship + Comet -> destroy(ship)";
    }

    private static String label(Object object) {
        return object.getClass().getSimpleName();
    }
}
