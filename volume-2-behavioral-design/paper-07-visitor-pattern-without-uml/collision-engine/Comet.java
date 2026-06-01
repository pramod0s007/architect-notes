public final class Comet implements GameObject {

    private final String designation;

    public Comet(String designation) {
        this.designation = designation;
    }

    public String designation() {
        return designation;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String label() {
        return "Comet(" + designation + ")";
    }
}
