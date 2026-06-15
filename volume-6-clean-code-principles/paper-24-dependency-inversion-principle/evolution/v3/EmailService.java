public interface EmailService {
    void sendConfirmation(String email, Order order);
}
