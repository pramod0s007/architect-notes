// BEFORE SRP — one class touched by 3 teams
// Problem: security team, CRM team, and marketing team
// all modify this single file — conflicts, risky deploys.
// 3 teams can independently change this class — security, CRM, marketing

import java.util.HashMap;
import java.util.Map;

public class UserAccountManager {

    private static final Map<String, String> passwordHashes = new HashMap<>();
    private static final Map<String, Integer> failedAttempts = new HashMap<>();
    private static final Map<String, String[]> profiles = new HashMap<>();

    static {
        passwordHashes.put("U001", "5f4dcc3b5aa765d61d8327deb882cf99"); // "password"
        profiles.put("U001", new String[]{"Alice Sharma", "+1-555-0101"});
    }

    // Security team modifies this — checks password hash, tracks failures
    public boolean authenticate(String userId, String password) {
        String inputHash = md5Simulate(password);
        String storedHash = passwordHashes.get(userId);
        if (storedHash == null || !storedHash.equals(inputHash)) {
            int attempts = failedAttempts.getOrDefault(userId, 0) + 1;
            failedAttempts.put(userId, attempts);
            System.out.println("[Auth] Failed attempt #" + attempts + " for user " + userId);
            return false;
        }
        failedAttempts.remove(userId);
        System.out.println("[Auth] User " + userId + " authenticated successfully.");
        return true;
    }

    // CRM team modifies this — validates and persists profile data
    public void updateProfile(String userId, String name, String phone) {
        if (!phone.matches("\\+?[0-9\\-]{7,15}")) {
            System.out.println("[Profile] Invalid phone format: " + phone);
            return;
        }
        profiles.put(userId, new String[]{name, phone});
        System.out.println("[Profile] Updated profile for " + userId
                + ": name=" + name + ", phone=" + phone);
    }

    // Marketing team modifies this — email copy, links, branding
    public void sendWelcomeEmail(String userId) {
        String[] profile = profiles.getOrDefault(userId, new String[]{"Customer", ""});
        String name = profile[0];
        System.out.println("[Email] Sending welcome email to " + userId);
        System.out.println("[Email] Subject: Welcome to SecureBank, " + name + "!");
        System.out.println("[Email] Body: Your account is ready. Log in at securebank.com");
    }

    private String md5Simulate(String input) {
        // Simplified hash simulation for demo purposes
        return input.equals("password") ? "5f4dcc3b5aa765d61d8327deb882cf99" : "invalid";
    }
}
