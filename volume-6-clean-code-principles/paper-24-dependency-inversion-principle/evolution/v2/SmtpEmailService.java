public class SmtpEmailService {

    public void sendConfirmation(String email, Order order) {
        System.out.println("SMTP: confirmation email sent to " + email
                + " for order " + order.getId());
    }
}
