// Test double: silently discards email. Tests run without an SMTP server.
public class NoOpEmailService implements EmailService {

    @Override
    public void sendConfirmation(String email, Order order) {
        System.out.println("NoOp: email suppressed for order " + order.getId());
    }
}
