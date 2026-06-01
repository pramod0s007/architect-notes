public interface Visitor {

    void visit(Ship ship);

    void visit(Station station);

    void visit(Comet comet);

    void visit(Asteroid asteroid);
}
