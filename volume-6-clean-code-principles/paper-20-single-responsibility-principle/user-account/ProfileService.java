// AFTER SRP — owned exclusively by the CRM team.
// Single responsibility: validate and persist user profile data.
// Security or marketing changes never touch this file.

import java.util.HashMap;
import java.util.Map;

public class ProfileService {

    // In production this would be a DB repository
    private final Map<String, UserProfile> profiles = new HashMap<>();

    public ProfileService() {
        profiles.put("U001", new UserProfile("Alice Sharma", "+1-555-0101"));
        profiles.put("U002", new UserProfile("Bob Chen",    "+44-20-7946-0202"));
    }

    public void updateProfile(String userId, String name, String phone) {
        if (name == null || name.trim().isEmpty()) {
            System.out.println("[ProfileService] Name must not be blank.");
            return;
        }
        if (!isValidPhone(phone)) {
            System.out.println("[ProfileService] Invalid phone format: " + phone
                    + ". Expected E.164 or local format.");
            return;
        }
        profiles.put(userId, new UserProfile(name.trim(), phone.trim()));
        System.out.println("[ProfileService] Profile saved — userId=" + userId
                + ", name=" + name.trim() + ", phone=" + phone.trim());
    }

    public UserProfile getProfile(String userId) {
        return profiles.getOrDefault(userId, null);
    }

    private boolean isValidPhone(String phone) {
        return phone != null && phone.matches("\\+?[0-9][0-9\\-\\s]{6,14}");
    }

    // Lightweight value object — avoids raw String arrays
    public static class UserProfile {
        public final String name;
        public final String phone;

        public UserProfile(String name, String phone) {
            this.name  = name;
            this.phone = phone;
        }

        @Override
        public String toString() {
            return "UserProfile{name='" + name + "', phone='" + phone + "'}";
        }
    }
}
