// Production implementation. Can be swapped for SMTP, SendGrid, etc. without touching OrderService.
public class SesEmailService implements EmailService {

    @Override
    public void sendConfirmation(String email, Order order) {
        System.out.println("SES: confirmation email sent to " + email
                + " for order " + order.getId());
    }
}
