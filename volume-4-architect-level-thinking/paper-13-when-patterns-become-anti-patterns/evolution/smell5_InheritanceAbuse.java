package evolution;

/**
 * SMELL 5 — Inheritance Abuse (BaseService with 12 Protected Hooks)
 *
 * Anti-Pattern: BaseService defines 12 protected methods as hooks.
 * EmailService overrides 7 of them. SmsService overrides 5 of them.
 * Neither subclass uses the other 7 hooks — they inherit dead weight.
 * Adding a new service requires understanding all 12 hooks
 * and deciding which 5-7 to override.
 *
 * Problems:
 * 1. Subclasses are coupled to the base class internals.
 * 2. Unused inherited methods appear in every subclass's public/protected API.
 * 3. Template method contracts are implicit — no compiler enforcement.
 * 4. Testing requires instantiating the subclass, not the varying behavior.
 *
 * Fix: Composition. Extract the varying behavior into small Strategy interfaces.
 * NotificationService composes a Formatter and a Sender — each tested independently.
 */
public class smell5_InheritanceAbuse {

    // ---------------------------------------------------------------
    // BEFORE: BaseService with 12 hooks — inheritance abuse
    // ---------------------------------------------------------------

    static abstract class BaseService {
        // Template method — the fixed orchestration
        public final void execute(String payload) {
            String validated  = validate(payload);
            String formatted  = format(validated);
            String enriched   = enrich(formatted);
            String compressed = compress(enriched);
            audit(compressed);
            String encrypted  = encrypt(compressed);
            String routed     = route(encrypted);
            deliver(routed);
            log(routed);
            String receipt    = acknowledge(routed);
            cleanup(receipt);
            notifyMonitor(receipt);
        }

        // 12 hooks — subclasses choose which to override
        protected String validate(String payload)      { return payload; }         // hook 1
        protected String format(String payload)        { return payload; }         // hook 2
        protected String enrich(String payload)        { return payload; }         // hook 3
        protected String compress(String payload)      { return payload; }         // hook 4
        protected void   audit(String payload)         { /* default: noop */ }     // hook 5
        protected String encrypt(String payload)       { return payload; }         // hook 6
        protected String route(String payload)         { return payload; }         // hook 7
        protected void   deliver(String payload)       { /* default: noop */ }     // hook 8
        protected void   log(String payload)           { /* default: noop */ }     // hook 9
        protected String acknowledge(String payload)   { return "ACK"; }           // hook 10
        protected void   cleanup(String receipt)       { /* default: noop */ }     // hook 11
        protected void   notifyMonitor(String receipt) { /* default: noop */ }     // hook 12
    }

    /** Overrides 7 of 12. Silently inherits the other 5 as noops. */
    static class EmailService extends BaseService {
        @Override protected String validate(String p)  { return p.trim(); }                                    // 1
        @Override protected String format(String p)    { return "<html>" + p + "</html>"; }                    // 2
        @Override protected String enrich(String p)    { return p + " [from: noreply@corp.com]"; }             // 3
        @Override protected String encrypt(String p)   { return "ENC[" + p + "]"; }                           // 6
        @Override protected void   deliver(String p)   { System.out.println("  EMAIL SMTP: " + p); }          // 8
        @Override protected void   log(String p)       { System.out.println("  Email logged"); }               // 9
        @Override protected String acknowledge(String p) { return "EMAIL-ACK"; }                               // 10
        // compress(4), audit(5), route(7), cleanup(11), notifyMonitor(12) — inherited as noops, unused
    }

    /** Overrides 5 of 12. Silently inherits the other 7. */
    static class SmsService extends BaseService {
        @Override protected String validate(String p)    { return p.length() > 160 ? p.substring(0, 160) : p; } // 1
        @Override protected String format(String p)      { return "[SMS] " + p; }                                // 2
        @Override protected void   deliver(String p)     { System.out.println("  SMS GATEWAY: " + p); }         // 8
        @Override protected void   log(String p)         { System.out.println("  SMS logged"); }                 // 9
        @Override protected void   notifyMonitor(String r) { System.out.println("  SMS monitor notified"); }    // 12
        // enrich(3), compress(4), audit(5), encrypt(6), route(7), acknowledge(10), cleanup(11) — inherited, unused
    }

    // ---------------------------------------------------------------
    // AFTER: Composition + Strategy — each step is injectable
    // ---------------------------------------------------------------

    // Small, focused strategy interfaces
    interface MessageFormatter { String format(String payload); }
    interface MessageSender    { void send(String payload); }
    interface MessageLogger    { void log(String payload); }

    static class NotificationService {
        private final MessageFormatter formatter;
        private final MessageSender    sender;
        private final MessageLogger    logger;

        NotificationService(MessageFormatter formatter,
                            MessageSender sender,
                            MessageLogger logger) {
            this.formatter = formatter;
            this.sender    = sender;
            this.logger    = logger;
        }

        public void execute(String payload) {
            if (payload == null || payload.isBlank()) {
                throw new IllegalArgumentException("Payload must not be blank");
            }
            String formatted = formatter.format(payload);
            sender.send(formatted);
            logger.log(formatted);
        }
    }

    // Email strategies
    static class HtmlFormatter      implements MessageFormatter {
        @Override public String format(String p) { return "<html>" + p.trim() + "</html>"; }
    }
    static class SmtpSender         implements MessageSender {
        @Override public void send(String p) { System.out.println("  EMAIL SMTP: " + p); }
    }
    static class EmailLogger        implements MessageLogger {
        @Override public void log(String p)  { System.out.println("  Email logged"); }
    }

    // SMS strategies
    static class SmsFormatter       implements MessageFormatter {
        @Override public String format(String p) {
            String trimmed = p.length() > 160 ? p.substring(0, 160) : p;
            return "[SMS] " + trimmed;
        }
    }
    static class SmsGatewaySender   implements MessageSender {
        @Override public void send(String p) { System.out.println("  SMS GATEWAY: " + p); }
    }
    static class SmsLogger          implements MessageLogger {
        @Override public void log(String p)  { System.out.println("  SMS logged"); }
    }

    // ---------------------------------------------------------------
    // Main
    // ---------------------------------------------------------------
    public static void main(String[] args) {
        System.out.println("=== Smell 5: Inheritance Abuse ===\n");

        System.out.println("--- BEFORE: 12 hooks, 2 subclasses ---");
        new EmailService().execute("Account created");
        new SmsService().execute("Verification code: 4821");

        System.out.println();
        System.out.println("--- AFTER: Composition + Strategy ---");
        NotificationService emailService = new NotificationService(
                new HtmlFormatter(), new SmtpSender(), new EmailLogger());
        emailService.execute("Account created");

        NotificationService smsService = new NotificationService(
                new SmsFormatter(), new SmsGatewaySender(), new SmsLogger());
        smsService.execute("Verification code: 4821");

        System.out.println();
        System.out.println("After benefits:");
        System.out.println("  - Each strategy is independently testable");
        System.out.println("  - Mix and match: HtmlFormatter + SmsGatewaySender if needed");
        System.out.println("  - New channel = new Formatter + Sender, zero base class changes");
        System.out.println("  - No inherited dead weight — no unused hooks");
    }
}
