package evolution;

/**
 * SMELL 2 — Factory Hell (Over-Abstracted Creation)
 *
 * Anti-Pattern: A 5-layer abstract factory hierarchy to create
 * a single EmailNotification. Every layer delegates to the next.
 * Adding a new notification type requires changes in at least 4 files.
 *
 * Diagnosis: This was built anticipating extensibility that never came.
 * The system has one notification type in production after 18 months.
 *
 * Fix: Direct instantiation, or inject the concrete object via DI.
 * If multiple types are genuinely needed, a single LoggerFactory.create()
 * is sufficient (see Paper 12 v2).
 */
public class smell2_FactoryHell {

    // ---------------------------------------------------------------
    // BEFORE: The 5-layer factory hierarchy
    // ---------------------------------------------------------------

    /** Layer 5 — the actual notification */
    static class EmailNotification {
        private final String to;
        private final String subject;
        private final String body;

        EmailNotification(String to, String subject, String body) {
            this.to      = to;
            this.subject = subject;
            this.body    = body;
        }

        public void send() {
            System.out.println("  Sending email to=" + to
                    + " subject='" + subject + "' body='" + body + "'");
        }
    }

    /** Layer 4 — concrete factory creates EmailNotification */
    static class EmailNotificationFactory {
        public EmailNotification create(String to, String subject, String body) {
            return new EmailNotification(to, subject, body);
        }
    }

    /** Layer 3 — abstract notification factory (for "future flexibility") */
    static abstract class AbstractNotificationFactory {
        public abstract EmailNotificationFactory getEmailFactory();
        // getSmsFactory(), getPushFactory() — planned but never built
    }

    /** Layer 2 — concrete implementation of the abstract factory */
    static class ConcreteNotificationFactory extends AbstractNotificationFactory {
        @Override
        public EmailNotificationFactory getEmailFactory() {
            return new EmailNotificationFactory();
        }
    }

    /** Layer 1 — factory factory (creates the factory that creates factories) */
    static class AbstractNotificationFactoryFactory {
        public AbstractNotificationFactory getFactory(String type) {
            if ("default".equals(type)) {
                return new ConcreteNotificationFactory();
            }
            throw new IllegalArgumentException("Unknown factory type: " + type);
        }
    }

    /** Caller — 5 hops to send one email */
    static class NotificationService_Before {
        void sendWelcomeEmail(String userEmail) {
            AbstractNotificationFactoryFactory factoryFactory =
                    new AbstractNotificationFactoryFactory();                 // Layer 1

            AbstractNotificationFactory notifFactory =
                    factoryFactory.getFactory("default");                     // Layer 2

            EmailNotificationFactory emailFactory =
                    notifFactory.getEmailFactory();                           // Layer 3 → 4

            EmailNotification notification =
                    emailFactory.create(userEmail, "Welcome!", "Hello!");     // Layer 5

            notification.send();                                              // Finally.
        }
    }

    // ---------------------------------------------------------------
    // AFTER: Direct instantiation — or inject via DI container
    // ---------------------------------------------------------------

    /** After: same result, no factory layers */
    static class NotificationService_After {
        void sendWelcomeEmail(String userEmail) {
            // Option A: direct instantiation (fine for stable, simple objects)
            EmailNotification notification =
                    new EmailNotification(userEmail, "Welcome!", "Hello!");
            notification.send();
        }
    }

    /**
     * After with DI: receive the pre-built notification via constructor.
     * The DI container (Spring, Guice, etc.) handles creation.
     * Testable: inject a mock in tests without any factory.
     */
    static class NotificationService_WithDI {
        // In real DI: @Autowired or constructor injection
        interface NotificationSender {
            void send(String to, String subject, String body);
        }

        static class EmailSender implements NotificationSender {
            @Override
            public void send(String to, String subject, String body) {
                new EmailNotification(to, subject, body).send();
            }
        }

        private final NotificationSender sender;

        NotificationService_WithDI(NotificationSender sender) {
            this.sender = sender;
        }

        void sendWelcomeEmail(String userEmail) {
            sender.send(userEmail, "Welcome!", "Hello!");
        }
    }

    // ---------------------------------------------------------------
    // Main
    // ---------------------------------------------------------------
    public static void main(String[] args) {
        System.out.println("=== Smell 2: Factory Hell ===\n");

        System.out.println("--- BEFORE: 5 layers ---");
        new NotificationService_Before().sendWelcomeEmail("user@example.com");

        System.out.println();
        System.out.println("--- AFTER: direct instantiation ---");
        new NotificationService_After().sendWelcomeEmail("user@example.com");

        System.out.println();
        System.out.println("--- AFTER with DI: injected sender ---");
        var service = new NotificationService_WithDI(
                new NotificationService_WithDI.EmailSender());
        service.sendWelcomeEmail("user@example.com");

        System.out.println();
        System.out.println("Diagnosis: Factories are for varying creation logic.");
        System.out.println("One concrete class with no variants = no factory needed.");
        System.out.println("'Future flexibility' that never arrives = present complexity.");
    }
}
