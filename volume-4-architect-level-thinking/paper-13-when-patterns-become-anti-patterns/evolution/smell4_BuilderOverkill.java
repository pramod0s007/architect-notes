package evolution;

/**
 * SMELL 4 — Builder Overkill (Builder for 2-Field Value Object)
 *
 * Anti-Pattern: CoordinateBuilder for a (lat, lon) pair.
 * Both fields are required. Neither is optional. There are no invariants
 * beyond basic range checks. The Builder is 40 lines of boilerplate
 * for a 2-field record.
 *
 * When Builder is appropriate:
 * - 5+ parameters, especially with many optionals
 * - Invariants must be validated at construction time
 * - Named steps prevent positional errors between same-type fields
 *
 * When Builder is overkill:
 * - 2-3 fields
 * - All fields required
 * - No complex validation
 * - Fields have different types (no positional confusion)
 *
 * Fix: Constructor with validation, or Java record.
 */
public class smell4_BuilderOverkill {

    // ---------------------------------------------------------------
    // BEFORE: CoordinateBuilder — 40 lines for 2 fields
    // ---------------------------------------------------------------

    static final class Coordinate_Before {
        private final double latitude;
        private final double longitude;

        private Coordinate_Before(Builder b) {
            this.latitude  = b.latitude;
            this.longitude = b.longitude;
        }

        public double getLatitude()  { return latitude; }
        public double getLongitude() { return longitude; }

        @Override
        public String toString() {
            return "Coordinate(" + latitude + ", " + longitude + ")";
        }

        // 40-line Builder for 2 required fields — no optionals, no complex invariants
        static final class Builder {
            private double latitude;
            private double longitude;
            private boolean latSet = false;
            private boolean lonSet = false;

            public Builder latitude(double latitude) {
                this.latitude = latitude;
                this.latSet   = true;
                return this;
            }

            public Builder longitude(double longitude) {
                this.longitude = longitude;
                this.lonSet    = true;
                return this;
            }

            public Coordinate_Before build() {
                if (!latSet)  throw new IllegalStateException("latitude not set");
                if (!lonSet)  throw new IllegalStateException("longitude not set");
                if (latitude  < -90  || latitude  > 90)  throw new IllegalArgumentException("lat out of range");
                if (longitude < -180 || longitude > 180) throw new IllegalArgumentException("lon out of range");
                return new Coordinate_Before(this);
            }
        }
    }

    // ---------------------------------------------------------------
    // AFTER A: Constructor with validation
    // ---------------------------------------------------------------

    static final class Coordinate_Constructor {
        private final double latitude;
        private final double longitude;

        public Coordinate_Constructor(double latitude, double longitude) {
            if (latitude  < -90  || latitude  > 90)  throw new IllegalArgumentException("lat out of range: " + latitude);
            if (longitude < -180 || longitude > 180) throw new IllegalArgumentException("lon out of range: " + longitude);
            this.latitude  = latitude;
            this.longitude = longitude;
        }

        public double getLatitude()  { return latitude; }
        public double getLongitude() { return longitude; }

        @Override
        public String toString() {
            return "Coordinate(" + latitude + ", " + longitude + ")";
        }
    }

    // ---------------------------------------------------------------
    // AFTER B: Java record (Java 16+) — most concise
    // ---------------------------------------------------------------

    record Coordinate(double latitude, double longitude) {
        // Compact canonical constructor for validation
        Coordinate {
            if (latitude  < -90  || latitude  > 90)  throw new IllegalArgumentException("lat out of range");
            if (longitude < -180 || longitude > 180) throw new IllegalArgumentException("lon out of range");
        }
    }

    // ---------------------------------------------------------------
    // Usage comparison
    // ---------------------------------------------------------------

    public static void main(String[] args) {
        System.out.println("=== Smell 4: Builder Overkill ===\n");

        System.out.println("--- BEFORE: CoordinateBuilder (40 lines of boilerplate) ---");
        Coordinate_Before before = new Coordinate_Before.Builder()
                .latitude(37.7749)
                .longitude(-122.4194)
                .build();
        System.out.println("Built: " + before);

        System.out.println();
        System.out.println("--- AFTER A: Constructor (5 lines) ---");
        Coordinate_Constructor withConstructor = new Coordinate_Constructor(37.7749, -122.4194);
        System.out.println("Built: " + withConstructor);

        System.out.println();
        System.out.println("--- AFTER B: Java record (3 lines) ---");
        Coordinate withRecord = new Coordinate(37.7749, -122.4194);
        System.out.println("Built: " + withRecord);

        System.out.println();
        System.out.println("--- Invalid coordinate (same validation in all three) ---");
        try {
            new Coordinate(200.0, 0.0); // invalid lat
        } catch (IllegalArgumentException e) {
            System.out.println("Caught: " + e.getMessage());
        }

        System.out.println();
        System.out.println("Rule: Builder when fields >= 5, or optional fields exist,");
        System.out.println("      or same-type params create positional ambiguity.");
        System.out.println("      Constructor or record for 2-3 required, differently-typed fields.");
    }
}
