// AFTER SRP — owned exclusively by the Security team.
// Single responsibility: verify identity and track failed attempts.
// CRM or marketing changes never touch this file.

import java.util.HashMap;
import java.util.Map;

public class AuthenticationService {

    private static final Map<String, String> passwordHashes = new HashMap<>();
    private static final Map<String, Integer> failedAttempts = new HashMap<>();
    private static final int MAX_ATTEMPTS = 5;

    static {
        passwordHashes.put("U001", "5f4dcc3b5aa765d61d8327deb882cf99"); // "password"
        passwordHashes.put("U002", "d8578edf8458ce06fbc5bb76a58c5ca4"); // "qwerty"
    }

    public boolean authenticate(String userId, String password) {
        if (isLocked(userId)) {
            System.out.println("[AuthService] Account " + userId
                    + " is locked after " + MAX_ATTEMPTS + " failed attempts.");
            return false;
        }

        String inputHash = hashPassword(password);
        String storedHash = passwordHashes.get(userId);

        if (storedHash != null && storedHash.equals(inputHash)) {
            failedAttempts.remove(userId);
            System.out.println("[AuthService] " + userId + " authenticated successfully.");
            return true;
        }

        int attempts = failedAttempts.getOrDefault(userId, 0) + 1;
        failedAttempts.put(userId, attempts);
        System.out.println("[AuthService] Invalid credentials for " + userId
                + ". Failed attempts: " + attempts + "/" + MAX_ATTEMPTS);
        return false;
    }

    public boolean isLocked(String userId) {
        return failedAttempts.getOrDefault(userId, 0) >= MAX_ATTEMPTS;
    }

    private String hashPassword(String password) {
        if ("password".equals(password)) return "5f4dcc3b5aa765d61d8327deb882cf99";
        if ("qwerty".equals(password))   return "d8578edf8458ce06fbc5bb76a58c5ca4";
        return "unknown";
    }
}
