public class Main {

    public static void main(String[] args) {

        System.out.println("══════════════════════════════════════════");
        System.out.println(" BEFORE SRP — UserAccountManager");
        System.out.println("══════════════════════════════════════════");
        UserAccountManager manager = new UserAccountManager();

        System.out.println("\n--- authenticate ---");
        manager.authenticate("U001", "password");   // success
        manager.authenticate("U001", "wrongpass");  // failure

        System.out.println("\n--- updateProfile ---");
        manager.updateProfile("U001", "Alice B. Sharma", "+1-555-9999");
        manager.updateProfile("U001", "Alice B. Sharma", "not-a-phone"); // invalid

        System.out.println("\n--- sendWelcomeEmail ---");
        manager.sendWelcomeEmail("U001");

        System.out.println("\n══════════════════════════════════════════");
        System.out.println(" AFTER SRP — Three focused services");
        System.out.println("══════════════════════════════════════════");

        AuthenticationService authService      = new AuthenticationService();
        ProfileService        profileService   = new ProfileService();
        EmailNotificationService emailService  = new EmailNotificationService();

        System.out.println("\n--- AuthenticationService ---");
        authService.authenticate("U001", "password");  // success
        authService.authenticate("U002", "wrong");     // failure
        authService.authenticate("U002", "wrong");     // failure 2

        System.out.println("\n--- ProfileService ---");
        profileService.updateProfile("U001", "Alice B. Sharma", "+1-555-9999");
        profileService.updateProfile("U002", "Bob Chen", "bad#phone");   // rejected
        ProfileService.UserProfile profile = profileService.getProfile("U001");
        System.out.println("[Main] Fetched: " + profile);

        System.out.println("\n--- EmailNotificationService ---");
        emailService.sendWelcomeEmail("U001", "Alice B. Sharma");
        System.out.println();
        emailService.sendPasswordResetEmail("U002", "Bob Chen", "tok_abc123XYZ");
    }
}
