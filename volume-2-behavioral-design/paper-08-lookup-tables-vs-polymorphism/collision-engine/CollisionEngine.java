import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class CollisionEngine {

    private final Map<CollisionKey, Action> table = new HashMap<>();

    public void register(CollisionKey key, Action action) {
        table.put(Objects.requireNonNull(key), Objects.requireNonNull(action));
    }

    public void resolve(Object a, Object b) {
        CollisionKey key = CollisionKey.of(a, b);
        Action action = table.get(key);
        if (action == null) {
            throw new IllegalStateException("No action for " + key);
        }
        action.apply(a, b);
    }

    public static final class CollisionKey {
        private final Class<?> typeA;
        private final Class<?> typeB;

        private CollisionKey(Class<?> typeA, Class<?> typeB) {
            this.typeA = typeA;
            this.typeB = typeB;
        }

        public static CollisionKey of(Object a, Object b) {
            return new CollisionKey(a.getClass(), b.getClass());
        }

        public static CollisionKey of(Class<?> typeA, Class<?> typeB) {
            return new CollisionKey(typeA, typeB);
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof CollisionKey that)) {
                return false;
            }
            return typeA.equals(that.typeA) && typeB.equals(that.typeB);
        }

        @Override
        public int hashCode() {
            return Objects.hash(typeA, typeB);
        }

        @Override
        public String toString() {
            return typeA.getSimpleName() + "+" + typeB.getSimpleName();
        }
    }
}
