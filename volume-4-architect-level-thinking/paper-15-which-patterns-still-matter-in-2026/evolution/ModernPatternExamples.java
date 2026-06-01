package evolution;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * MODERN PATTERN EXAMPLES — How Classic GoF Patterns Look in 2026
 *
 * Three sections:
 *   1. Lambda as Strategy   — when a Function<T,R> replaces an interface
 *   2. Record + Builder     — when a record is enough vs when Builder is needed
 *   3. Switch Expression    — replacing Map lookup for small, stable sets
 *
 * The patterns are not dead. The syntax is leaner.
 * AI tools can write these in seconds. Your job: know WHEN to apply them.
 */
public class ModernPatternExamples {

    // ===========================================================
    // SECTION 1 — Lambda as Strategy
    //
    // Classic GoF: PricingStrategy interface + class per algorithm
    // Modern Java: Function<Product, Double> — when lambda is enough
    //
    // Lambda IS enough when:
    //   - The behavior is one expression or a few lines
    //   - No state is needed between calls
    //   - No need to name/document the strategy independently
    //   - No need to test the strategy class in isolation
    //
    // Interface IS still better when:
    //   - The algorithm is complex enough to deserve its own class and tests
    //   - The strategy needs injected dependencies (e.g., a database client)
    //   - You need to store the strategy by name (registry)
    //   - The strategy has multiple related methods
    // ===========================================================

    record Product(String name, double basePrice, String category, boolean isPremium) {}

    // --- Classic: interface hierarchy ---
    interface PricingStrategy_Classic {
        double calculatePrice(Product product);
    }

    static class StandardPricingStrategy implements PricingStrategy_Classic {
        @Override public double calculatePrice(Product p) { return p.basePrice(); }
    }
    static class PremiumPricingStrategy implements PricingStrategy_Classic {
        @Override public double calculatePrice(Product p) { return p.basePrice() * 1.20; }
    }
    static class DiscountPricingStrategy implements PricingStrategy_Classic {
        @Override public double calculatePrice(Product p) { return p.basePrice() * 0.85; }
    }

    // --- Modern: Function<Product, Double> ---
    static class PricingService_Modern {
        // Lambda strategies — defined once, reused everywhere
        static final Function<Product, Double> STANDARD_PRICING =
                product -> product.basePrice();

        static final Function<Product, Double> PREMIUM_PRICING =
                product -> product.basePrice() * 1.20;

        static final Function<Product, Double> DISCOUNT_PRICING =
                product -> product.basePrice() * 0.85;

        // Complex strategy: still deserves a named function or class
        static final Function<Product, Double> TIERED_PRICING = product -> {
            double base = product.basePrice();
            if (base > 500)  return base * 0.75;
            if (base > 100)  return base * 0.90;
            return base;
            // 3 lines of real logic -> still readable as lambda, no class needed
        };

        double applyPricing(Product product, Function<Product, Double> strategy) {
            return strategy.apply(product);
        }

        // When to keep the interface: strategy needs a DB call
        // static class DatabaseBackedPricingStrategy implements PricingStrategy_Classic {
        //     private final PriceOverrideRepository repo;  // injected dependency
        //     @Override public double calculatePrice(Product p) {
        //         return repo.findOverride(p.name()).orElse(p.basePrice());
        //     }
        // }
        // A lambda can't hold an injected dependency cleanly. Use the interface then.
    }

    // ===========================================================
    // SECTION 2 — Record + Builder
    //
    // Java 16+ record: immutable value object in 3 lines.
    // Builder: still appropriate for 5+ fields, optional fields, validation.
    //
    // Use RECORD when:
    //   - All fields required
    //   - No complex validation at construction time
    //   - 2-4 fields
    //   - No inheritance needed
    //
    // Use BUILDER when:
    //   - 5+ fields, especially with optionals
    //   - Multi-step validation across fields
    //   - Named steps prevent positional errors (multiple String fields)
    //   - Immutable result still required
    // ===========================================================

    // --- Record: adequate for simple immutable objects ---
    record GeoPoint(double latitude, double longitude) {
        // Compact constructor for validation
        GeoPoint {
            if (latitude  < -90  || latitude  > 90)  throw new IllegalArgumentException("latitude out of range");
            if (longitude < -180 || longitude > 180) throw new IllegalArgumentException("longitude out of range");
        }

        double distanceTo(GeoPoint other) {
            // Haversine simplified for illustration
            double dlat = Math.toRadians(other.latitude() - this.latitude());
            double dlon = Math.toRadians(other.longitude() - this.longitude());
            return Math.sqrt(dlat * dlat + dlon * dlon) * 6371; // rough km
        }
    }

    // --- Builder: still appropriate for complex objects ---
    static final class MonitoringAlert {
        private final String       metricName;       // required
        private final double       threshold;        // required
        private final String       channel;          // required
        private final List<String> recipients;       // required, non-empty
        private final int          priority;         // optional, default 3
        private final int          windowSeconds;    // optional, default 300
        private final boolean      active;           // optional, default true

        private MonitoringAlert(Builder b) {
            this.metricName   = b.metricName;
            this.threshold    = b.threshold;
            this.channel      = b.channel;
            this.recipients   = List.copyOf(b.recipients);
            this.priority     = b.priority;
            this.windowSeconds = b.windowSeconds;
            this.active       = b.active;
        }

        @Override public String toString() {
            return "MonitoringAlert{metric='" + metricName + "', threshold=" + threshold
                    + ", channel='" + channel + "', priority=" + priority
                    + ", window=" + windowSeconds + "s, active=" + active + "}";
        }

        static final class Builder {
            private String       metricName;
            private double       threshold  = -1;
            private String       channel;
            private List<String> recipients = List.of();
            private int          priority   = 3;
            private int          windowSeconds = 300;
            private boolean      active     = true;

            public Builder metric(String m)            { this.metricName = m;   return this; }
            public Builder threshold(double t)         { this.threshold  = t;   return this; }
            public Builder channel(String c)           { this.channel    = c;   return this; }
            public Builder recipients(String... r)     { this.recipients = List.of(r); return this; }
            public Builder priority(int p)             { this.priority   = p;   return this; }
            public Builder windowSeconds(int s)        { this.windowSeconds = s; return this; }
            public Builder inactive()                  { this.active     = false; return this; }

            public MonitoringAlert build() {
                if (metricName == null || metricName.isBlank()) throw new IllegalStateException("metric required");
                if (threshold <= 0)                             throw new IllegalStateException("threshold > 0 required");
                if (channel == null || channel.isBlank())       throw new IllegalStateException("channel required");
                if (recipients.isEmpty())                       throw new IllegalStateException("recipients required");
                return new MonitoringAlert(this);
            }
        }
    }

    // ===========================================================
    // SECTION 3 — Switch Expression as Lookup Table
    //
    // Java 14+ switch expression: replaces Map for small, STABLE enums.
    //
    // Use SWITCH EXPRESSION when:
    //   - The set of values is known at compile time (enum)
    //   - Exhaustiveness check is desired (compiler warns on missing cases)
    //   - Readability is more important than runtime extensibility
    //
    // Use MAP when:
    //   - Values are loaded from config or database
    //   - New entries are added without recompiling
    //   - The set grows frequently (every sprint)
    // ===========================================================

    enum HttpStatus { OK, CREATED, BAD_REQUEST, UNAUTHORIZED, FORBIDDEN, NOT_FOUND,
                      INTERNAL_SERVER_ERROR, SERVICE_UNAVAILABLE }

    // --- Classic: Map ---
    static final Map<HttpStatus, String> STATUS_DESCRIPTIONS_MAP = Map.of(
            HttpStatus.OK,                   "Request succeeded",
            HttpStatus.CREATED,              "Resource created",
            HttpStatus.BAD_REQUEST,          "Client error in request",
            HttpStatus.UNAUTHORIZED,         "Authentication required",
            HttpStatus.FORBIDDEN,            "Access denied",
            HttpStatus.NOT_FOUND,            "Resource not found",
            HttpStatus.INTERNAL_SERVER_ERROR,"Server error",
            HttpStatus.SERVICE_UNAVAILABLE,  "Service temporarily unavailable"
    );

    // --- Modern: switch expression (exhaustive, compiler-checked) ---
    static String describeStatus(HttpStatus status) {
        return switch (status) {
            case OK                    -> "Request succeeded";
            case CREATED               -> "Resource created";
            case BAD_REQUEST           -> "Client error in request";
            case UNAUTHORIZED          -> "Authentication required";
            case FORBIDDEN             -> "Access denied";
            case NOT_FOUND             -> "Resource not found";
            case INTERNAL_SERVER_ERROR -> "Server error";
            case SERVICE_UNAVAILABLE   -> "Service temporarily unavailable";
            // Compiler enforces exhaustiveness — missing a case is a compile error.
            // The Map version silently returns null for a missing key.
        };
    }

    // When Map is still better: HTTP status codes loaded from a config file
    // static Map<String, String> loadStatusDescriptionsFromConfig(Path configFile) {...}
    // New status codes from a future HTTP spec don't require recompilation.

    // ---------------------------------------------------------------
    // Main
    // ---------------------------------------------------------------
    public static void main(String[] args) {
        System.out.println("=== Modern Pattern Examples (2026) ===\n");

        // Section 1: Lambda as Strategy
        System.out.println("-- Section 1: Lambda as Strategy --");
        Product widget  = new Product("Widget",  50.00, "tools",    false);
        Product gadget  = new Product("Gadget", 250.00, "electronics", true);
        Product luxury  = new Product("Luxury", 800.00, "premium",  true);

        var svc = new PricingService_Modern();
        System.out.printf("Widget  standard: $%.2f%n", svc.applyPricing(widget,  PricingService_Modern.STANDARD_PRICING));
        System.out.printf("Gadget  premium:  $%.2f%n", svc.applyPricing(gadget,  PricingService_Modern.PREMIUM_PRICING));
        System.out.printf("Luxury  tiered:   $%.2f%n", svc.applyPricing(luxury,  PricingService_Modern.TIERED_PRICING));
        System.out.printf("Widget  discount: $%.2f%n", svc.applyPricing(widget,  PricingService_Modern.DISCOUNT_PRICING));

        // Section 2: Record vs Builder
        System.out.println("\n-- Section 2: Record vs Builder --");
        GeoPoint sf = new GeoPoint(37.7749, -122.4194);
        GeoPoint ny = new GeoPoint(40.7128, -74.0060);
        System.out.printf("Distance SF->NY: %.1f km (rough)%n", sf.distanceTo(ny));

        MonitoringAlert alert = new MonitoringAlert.Builder()
                .metric("cpu_usage")
                .threshold(80.0)
                .channel("pagerduty")
                .recipients("oncall@corp.com", "sre@corp.com")
                .priority(5)
                .build();
        System.out.println("Alert: " + alert);

        // Section 3: Switch Expression
        System.out.println("\n-- Section 3: Switch Expression vs Map --");
        for (HttpStatus s : new HttpStatus[]{HttpStatus.OK, HttpStatus.NOT_FOUND, HttpStatus.FORBIDDEN}) {
            System.out.println("Switch: " + s + " -> " + describeStatus(s));
            System.out.println("Map:    " + s + " -> " + STATUS_DESCRIPTIONS_MAP.get(s));
        }

        System.out.println();
        System.out.println("Key decisions:");
        System.out.println("  Lambda vs interface: Does the strategy need injection or multiple methods?");
        System.out.println("  Record vs Builder:   Are there optionals or 5+ fields?");
        System.out.println("  Switch vs Map:       Is the set compile-time fixed or runtime-extensible?");
    }
}
