package evolution;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * CLASSIFICATION EXERCISES — Pattern Selection Decision Tree in Action
 *
 * Each scenario shows:
 *   1. The symptom (growing if-else or coupling)
 *   2. The WRONG diagnosis (jumping to a pattern without classifying)
 *   3. The CORRECT diagnosis (classify the variation bucket first)
 *   4. The right pattern and why
 *
 * Decision Tree:
 *   Q1: What is varying?
 *     -> Data/values only              => Config map / Template
 *     -> Which object to create        => Interface + Factory
 *     -> How an algorithm runs         => Strategy
 *     -> Rules combined in many ways   => Specification
 *     -> Behavior changes with state   => State Pattern
 */
public class ClassificationExercises {

    // ===========================================================
    // SCENARIO 1 — ReportService: growing format if-else
    // Bucket: DATA VARIATION  =>  Config/Template, NOT Strategy
    // ===========================================================

    enum ReportFormat { PDF, CSV, JSON, HTML, EXCEL }

    /**
     * WRONG: jumped straight to Strategy.
     * Created PdfReportStrategy, CsvReportStrategy, etc.
     * Each class: one method, returns a MIME type string + file extension.
     * Zero algorithm variation — pure data lookup.
     */
    static class ReportService_WrongDiagnosis {
        interface ReportStrategy {
            String getMimeType();
            String getFileExtension();
        }
        static class PdfStrategy  implements ReportStrategy {
            public String getMimeType()      { return "application/pdf"; }
            public String getFileExtension() { return ".pdf"; }
        }
        static class CsvStrategy  implements ReportStrategy {
            public String getMimeType()      { return "text/csv"; }
            public String getFileExtension() { return ".csv"; }
        }
        // ...JsonStrategy, HtmlStrategy, ExcelStrategy...
        // 5 classes for 10 string literals. Adding Excel = new class file.

        String buildFileName(String base, ReportFormat format) {
            ReportStrategy strategy = switch (format) {
                case PDF   -> new PdfStrategy();
                case CSV   -> new CsvStrategy();
                default    -> throw new UnsupportedOperationException("add class first");
            };
            return base + strategy.getFileExtension();
        }
    }

    /**
     * CORRECT diagnosis: What varies? MIME type and extension — pure values.
     * Fix: Map<ReportFormat, ReportMeta> — no interface, no classes.
     * Adding Excel = one Map.of() entry.
     */
    static class ReportService_CorrectDiagnosis {
        record ReportMeta(String mimeType, String extension) {}

        private static final Map<ReportFormat, ReportMeta> FORMATS = Map.of(
                ReportFormat.PDF,   new ReportMeta("application/pdf",                    ".pdf"),
                ReportFormat.CSV,   new ReportMeta("text/csv",                           ".csv"),
                ReportFormat.JSON,  new ReportMeta("application/json",                   ".json"),
                ReportFormat.HTML,  new ReportMeta("text/html",                          ".html"),
                ReportFormat.EXCEL, new ReportMeta("application/vnd.ms-excel",           ".xlsx")
        );

        String buildFileName(String base, ReportFormat format) {
            ReportMeta meta = FORMATS.getOrDefault(format,
                    new ReportMeta("application/octet-stream", ".bin"));
            return base + meta.extension();
        }

        String getMimeType(ReportFormat format) {
            return FORMATS.getOrDefault(format,
                    new ReportMeta("application/octet-stream", ".bin")).mimeType();
        }
    }

    // ===========================================================
    // SCENARIO 2 — StorageService: provider-specific if-else
    // Bucket: OBJECT VARIATION  =>  Interface + Factory
    // ===========================================================

    /**
     * WRONG: jumped to Decorator.
     * Added a S3StorageDecorator wrapping a LocalStorage — but there is no
     * shared behavior to wrap; the providers are completely different.
     * Decorator is for adding layers around the SAME behavior, not replacing it.
     */
    static class StorageService_WrongDiagnosis {
        void upload(String provider, String key, byte[] data) {
            if ("s3".equals(provider)) {
                System.out.println("  S3: PUT s3://bucket/" + key);
            } else if ("gcs".equals(provider)) {
                System.out.println("  GCS: PUT gs://bucket/" + key);
            } else if ("azure".equals(provider)) {
                System.out.println("  Azure: PUT https://storage.azure.com/" + key);
            }
            // Adding GCS required editing this method.
            // The if-else is the symptom of OBJECT variation.
        }
    }

    /**
     * CORRECT diagnosis: What varies? WHICH OBJECT handles the upload.
     * Fix: Interface + Factory — one implementation per provider.
     * Adding a new provider = new class + one factory entry. Zero edits to existing code.
     */
    static class StorageService_CorrectDiagnosis {
        interface StorageProvider {
            void upload(String key, byte[] data);
            byte[] download(String key);
        }

        static class S3StorageProvider implements StorageProvider {
            @Override public void   upload(String key, byte[] data) { System.out.println("  S3: PUT s3://bucket/" + key); }
            @Override public byte[] download(String key)            { System.out.println("  S3: GET s3://bucket/" + key); return new byte[0]; }
        }

        static class GcsStorageProvider implements StorageProvider {
            @Override public void   upload(String key, byte[] data) { System.out.println("  GCS: PUT gs://bucket/" + key); }
            @Override public byte[] download(String key)            { System.out.println("  GCS: GET gs://bucket/" + key); return new byte[0]; }
        }

        static class StorageFactory {
            static StorageProvider create(String provider) {
                return switch (provider.toLowerCase()) {
                    case "s3"    -> new S3StorageProvider();
                    case "gcs"   -> new GcsStorageProvider();
                    default      -> throw new IllegalArgumentException("Unknown provider: " + provider);
                };
            }
        }

        private final StorageProvider provider;
        StorageService_CorrectDiagnosis(String providerName) {
            this.provider = StorageFactory.create(providerName);
        }

        void upload(String key, byte[] data) { provider.upload(key, data); }
    }

    // ===========================================================
    // SCENARIO 3 — PricingService: customer-tier if-else
    // Bucket: BEHAVIOR VARIATION  =>  Strategy Pattern
    // ===========================================================

    enum CustomerTier { STANDARD, SILVER, GOLD, ENTERPRISE }

    /**
     * WRONG: jumped to Template Method.
     * Created BasePricingService with protected hooks. But the discount
     * calculation is a PURE algorithm — no shared orchestration needed.
     * Template Method is for "same steps, different implementations."
     * Here, there are no shared steps — just different discount math.
     */
    static abstract class PricingService_WrongDiagnosis {
        // Template method — but there's no shared step worth keeping
        public final double calculatePrice(double base) {
            return base - calculateDiscount(base);
        }
        protected abstract double calculateDiscount(double base); // the only varying thing
    }

    /**
     * CORRECT diagnosis: What varies? HOW the discount is calculated.
     * That is algorithm variation = Strategy.
     * Each tier has a different discount algorithm, and they may grow complex
     * (volume discounts, loyalty multipliers, contract rates).
     */
    static class PricingService_CorrectDiagnosis {
        interface PricingStrategy {
            double calculatePrice(double basePrice);
        }

        // Each strategy encapsulates one algorithm; independently testable
        static class StandardPricing   implements PricingStrategy {
            @Override public double calculatePrice(double base) { return base; }
        }
        static class SilverPricing     implements PricingStrategy {
            @Override public double calculatePrice(double base) { return base * 0.90; } // 10% off
        }
        static class GoldPricing       implements PricingStrategy {
            @Override public double calculatePrice(double base) {
                double discount = base > 1000 ? 0.20 : 0.15; // volume discount
                return base * (1 - discount);
            }
        }
        static class EnterprisePricing implements PricingStrategy {
            private final double contractRate;
            EnterprisePricing(double contractRate) { this.contractRate = contractRate; }
            @Override public double calculatePrice(double base) { return base * contractRate; }
        }

        private final Map<CustomerTier, PricingStrategy> strategies = new HashMap<>();

        PricingService_CorrectDiagnosis() {
            strategies.put(CustomerTier.STANDARD,   new StandardPricing());
            strategies.put(CustomerTier.SILVER,     new SilverPricing());
            strategies.put(CustomerTier.GOLD,       new GoldPricing());
            strategies.put(CustomerTier.ENTERPRISE, new EnterprisePricing(0.70));
        }

        double calculatePrice(double base, CustomerTier tier) {
            PricingStrategy strategy = strategies.getOrDefault(tier, new StandardPricing());
            return strategy.calculatePrice(base);
        }
    }

    // ===========================================================
    // SCENARIO 4 — EligibilityService: rule combinations
    // Bucket: RULES VARIATION  =>  Specification Pattern
    // ===========================================================

    record Customer(int age, boolean isVerified, String region, double annualIncome) {}

    /**
     * WRONG: jumped to Strategy.
     * Created EligibilityStrategy per product type — but eligibility rules
     * are combined in different ways for different products.
     * "Age > 18 AND verified AND income > 50k" vs "Age > 21 AND (US OR CA)"
     * Strategy can't compose rules. Specification can.
     */
    static class EligibilityService_WrongDiagnosis {
        interface EligibilityStrategy {
            boolean isEligible(Customer customer);
        }
        // Each class hard-codes a specific combination — not reusable
        static class CreditCardEligibility implements EligibilityStrategy {
            @Override public boolean isEligible(Customer c) {
                return c.age() >= 18 && c.isVerified() && c.annualIncome() >= 50000;
            }
        }
        static class PremiumCardEligibility implements EligibilityStrategy {
            @Override public boolean isEligible(Customer c) {
                // Copy-pasted age/verified check, plus income check — not reusable pieces
                return c.age() >= 21 && c.isVerified() && c.annualIncome() >= 100000;
            }
        }
    }

    /**
     * CORRECT diagnosis: What varies? RULES and how they are COMBINED.
     * Fix: Specification Pattern — small, composable rule objects with and/or/not.
     */
    static class EligibilityService_CorrectDiagnosis {
        interface Specification<T> {
            boolean isSatisfiedBy(T candidate);

            default Specification<T> and(Specification<T> other) {
                return candidate -> this.isSatisfiedBy(candidate) && other.isSatisfiedBy(candidate);
            }

            default Specification<T> or(Specification<T> other) {
                return candidate -> this.isSatisfiedBy(candidate) || other.isSatisfiedBy(candidate);
            }

            default Specification<T> not() {
                return candidate -> !this.isSatisfiedBy(candidate);
            }
        }

        // Atomic rules — each tested alone
        static final Specification<Customer> IS_ADULT         = c -> c.age() >= 18;
        static final Specification<Customer> IS_VERIFIED      = c -> c.isVerified();
        static final Specification<Customer> IS_HIGH_EARNER   = c -> c.annualIncome() >= 50000;
        static final Specification<Customer> IS_PREMIUM_EARNER= c -> c.annualIncome() >= 100000;
        static final Specification<Customer> IS_US_OR_CA      = c -> List.of("US", "CA").contains(c.region());

        // Composite rules — built from atomic pieces, no copy-paste
        static final Specification<Customer> CREDIT_CARD_ELIGIBLE =
                IS_ADULT.and(IS_VERIFIED).and(IS_HIGH_EARNER);

        static final Specification<Customer> PREMIUM_CARD_ELIGIBLE =
                IS_ADULT.and(IS_VERIFIED).and(IS_PREMIUM_EARNER).and(IS_US_OR_CA);

        boolean isEligibleForCreditCard(Customer c) { return CREDIT_CARD_ELIGIBLE.isSatisfiedBy(c); }
        boolean isEligibleForPremiumCard(Customer c) { return PREMIUM_CARD_ELIGIBLE.isSatisfiedBy(c); }
    }

    // ===========================================================
    // SCENARIO 5 — SubscriptionService: state-dependent behavior
    // Bucket: BEHAVIOR VARIATION (state)  =>  State Pattern
    // ===========================================================

    /**
     * WRONG: jumped to Strategy.
     * Created a SubscriptionStatusStrategy that is swapped out.
     * But each operation (renew, cancel, suspend, reactivate) depends on
     * the CURRENT state AND transitions TO a new state.
     * Strategy has no concept of state transitions — State Pattern does.
     */
    enum SubscriptionStatus { TRIAL, ACTIVE, SUSPENDED, CANCELLED }

    static class SubscriptionService_WrongDiagnosis {
        private SubscriptionStatus status;

        SubscriptionService_WrongDiagnosis() { this.status = SubscriptionStatus.TRIAL; }

        void renew() {
            // if-else grows every time a new status is added
            if (status == SubscriptionStatus.TRIAL || status == SubscriptionStatus.ACTIVE) {
                status = SubscriptionStatus.ACTIVE;
                System.out.println("  Renewed -> ACTIVE");
            } else if (status == SubscriptionStatus.SUSPENDED) {
                System.out.println("  Cannot renew while SUSPENDED");
            } else {
                System.out.println("  Cannot renew CANCELLED subscription");
            }
        }

        void cancel() {
            if (status == SubscriptionStatus.CANCELLED) {
                System.out.println("  Already cancelled");
            } else {
                status = SubscriptionStatus.CANCELLED;
                System.out.println("  Cancelled");
            }
        }
    }

    /**
     * CORRECT diagnosis: What varies? WHICH BEHAVIORS are valid depends on STATE.
     * Fix: State Pattern — each state encapsulates what it can do and how it transitions.
     */
    static class SubscriptionService_CorrectDiagnosis {
        interface SubscriptionState {
            SubscriptionState renew(SubscriptionContext ctx);
            SubscriptionState cancel(SubscriptionContext ctx);
            SubscriptionState suspend(SubscriptionContext ctx);
            String name();
        }

        static class SubscriptionContext {
            private SubscriptionState state;

            SubscriptionContext() { this.state = new TrialState(); }

            void renew()   { this.state = state.renew(this); }
            void cancel()  { this.state = state.cancel(this); }
            void suspend() { this.state = state.suspend(this); }
            String status() { return state.name(); }
        }

        static class TrialState implements SubscriptionState {
            @Override public SubscriptionState renew(SubscriptionContext ctx) {
                System.out.println("  Trial -> ACTIVE (upgraded)");
                return new ActiveState();
            }
            @Override public SubscriptionState cancel(SubscriptionContext ctx) {
                System.out.println("  Trial -> CANCELLED");
                return new CancelledState();
            }
            @Override public SubscriptionState suspend(SubscriptionContext ctx) {
                System.out.println("  Trial -> SUSPENDED");
                return new SuspendedState();
            }
            @Override public String name() { return "TRIAL"; }
        }

        static class ActiveState implements SubscriptionState {
            @Override public SubscriptionState renew(SubscriptionContext ctx) {
                System.out.println("  Active -> ACTIVE (renewed)");
                return this;
            }
            @Override public SubscriptionState cancel(SubscriptionContext ctx) {
                System.out.println("  Active -> CANCELLED");
                return new CancelledState();
            }
            @Override public SubscriptionState suspend(SubscriptionContext ctx) {
                System.out.println("  Active -> SUSPENDED");
                return new SuspendedState();
            }
            @Override public String name() { return "ACTIVE"; }
        }

        static class SuspendedState implements SubscriptionState {
            @Override public SubscriptionState renew(SubscriptionContext ctx) {
                System.out.println("  Suspended -> ACTIVE (reactivated)");
                return new ActiveState();
            }
            @Override public SubscriptionState cancel(SubscriptionContext ctx) {
                System.out.println("  Suspended -> CANCELLED");
                return new CancelledState();
            }
            @Override public SubscriptionState suspend(SubscriptionContext ctx) {
                System.out.println("  Already SUSPENDED — no transition");
                return this;
            }
            @Override public String name() { return "SUSPENDED"; }
        }

        static class CancelledState implements SubscriptionState {
            @Override public SubscriptionState renew(SubscriptionContext ctx) {
                System.out.println("  Cannot renew CANCELLED subscription");
                return this;
            }
            @Override public SubscriptionState cancel(SubscriptionContext ctx) {
                System.out.println("  Already CANCELLED");
                return this;
            }
            @Override public SubscriptionState suspend(SubscriptionContext ctx) {
                System.out.println("  Cannot suspend CANCELLED subscription");
                return this;
            }
            @Override public String name() { return "CANCELLED"; }
        }
    }

    // ---------------------------------------------------------------
    // Main — run all 5 scenarios
    // ---------------------------------------------------------------
    public static void main(String[] args) {
        System.out.println("=== Classification Exercises: Decision Tree in Action ===\n");

        // Scenario 1
        System.out.println("-- Scenario 1: ReportService (Data Variation -> Map, not Strategy) --");
        var reportBefore = new ReportService_WrongDiagnosis();
        System.out.println("Wrong:  " + reportBefore.buildFileName("report", ReportFormat.PDF));
        var reportAfter = new ReportService_CorrectDiagnosis();
        System.out.println("Right:  " + reportAfter.buildFileName("report", ReportFormat.EXCEL));
        System.out.println("        MIME: " + reportAfter.getMimeType(ReportFormat.JSON));

        // Scenario 2
        System.out.println("\n-- Scenario 2: StorageService (Object Variation -> Interface + Factory) --");
        var storageBefore = new StorageService_WrongDiagnosis();
        storageBefore.upload("s3", "photo.jpg", new byte[0]);
        var storageAfter = new StorageService_CorrectDiagnosis("gcs");
        storageAfter.upload("photo.jpg", new byte[0]);

        // Scenario 3
        System.out.println("\n-- Scenario 3: PricingService (Behavior Variation -> Strategy) --");
        var pricing = new PricingService_CorrectDiagnosis();
        System.out.printf("Standard $1000: $%.2f%n", pricing.calculatePrice(1000, CustomerTier.STANDARD));
        System.out.printf("Gold     $1000: $%.2f%n", pricing.calculatePrice(1000, CustomerTier.GOLD));
        System.out.printf("Enterprise $1000: $%.2f%n", pricing.calculatePrice(1000, CustomerTier.ENTERPRISE));

        // Scenario 4
        System.out.println("\n-- Scenario 4: EligibilityService (Rules Variation -> Specification) --");
        var eligibility = new EligibilityService_CorrectDiagnosis();
        Customer young   = new Customer(17, true,  "US", 60000);
        Customer adult   = new Customer(25, true,  "US", 80000);
        Customer premium = new Customer(25, true,  "US", 120000);
        System.out.println("Young  credit card eligible: " + eligibility.isEligibleForCreditCard(young));
        System.out.println("Adult  credit card eligible: " + eligibility.isEligibleForCreditCard(adult));
        System.out.println("Premium card eligible:       " + eligibility.isEligibleForPremiumCard(premium));

        // Scenario 5
        System.out.println("\n-- Scenario 5: SubscriptionService (State-Dependent -> State Pattern) --");
        var ctx = new SubscriptionService_CorrectDiagnosis.SubscriptionContext();
        System.out.println("Status: " + ctx.status());
        ctx.renew();    System.out.println("Status: " + ctx.status());
        ctx.suspend();  System.out.println("Status: " + ctx.status());
        ctx.renew();    System.out.println("Status: " + ctx.status());
        ctx.cancel();   System.out.println("Status: " + ctx.status());
        ctx.renew();    // attempt to renew cancelled

        System.out.println();
        System.out.println("The tree takes 60 seconds with practice.");
        System.out.println("Classify the variation bucket FIRST, then pick the pattern.");
    }
}
