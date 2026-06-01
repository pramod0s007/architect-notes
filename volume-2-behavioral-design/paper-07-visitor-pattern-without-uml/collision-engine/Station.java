public final class Station implements GameObject {

    private final String name;

    public Station(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String label() {
        return "Station(" + name + ")";
    }
}
