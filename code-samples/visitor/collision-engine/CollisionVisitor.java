/**
 * Double dispatch: the first object accepts this visitor; {@code visit} uses the partner's type.
 */
public final class CollisionVisitor implements Visitor {

    private final GameObject partner;
    private String outcome = "no interaction";

    public CollisionVisitor(GameObject partner) {
        if (partner == null) {
            throw new IllegalArgumentException("partner must not be null");
        }
        this.partner = partner;
    }

    public String outcome() {
        return outcome;
    }

    public void collide(GameObject self, GameObject other) {
        CollisionVisitor visitor = new CollisionVisitor(other);
        self.accept(visitor);
    }

    @Override
    public void visit(Ship ship) {
        if (partner instanceof Station station) {
            outcome = "resolveCollision(" + ship.label() + ", " + station.label() + ")";
        } else if (partner instanceof Comet comet) {
            outcome = "destroy(" + ship.label() + ") — hit " + comet.label();
        }
    }

    @Override
    public void visit(Station station) {
        if (partner instanceof Ship ship) {
            outcome = "resolveCollision(" + ship.label() + ", " + station.label() + ")";
        } else if (partner instanceof Asteroid asteroid) {
            outcome = "damage(" + station.label() + ") — struck " + asteroid.label();
        }
    }

    @Override
    public void visit(Comet comet) {
        if (partner instanceof Ship ship) {
            outcome = "destroy(" + ship.label() + ") — hit " + comet.label();
        }
    }

    @Override
    public void visit(Asteroid asteroid) {
        if (partner instanceof Station station) {
            outcome = "damage(" + station.label() + ") — struck " + asteroid.label();
        }
    }
}
