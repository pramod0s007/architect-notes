public final class Asteroid implements GameObject {

    private final int massKg;

    public Asteroid(int massKg) {
        this.massKg = massKg;
    }

    public int massKg() {
        return massKg;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String label() {
        return "Asteroid(" + massKg + "kg)";
    }
}
