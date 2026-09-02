import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

// A fast, general-purpose cryptographic hash -- SHA-256 -- applied directly
// to the password with no salt. SHA-256 is a perfectly good hash function;
// it was never designed to be slow, and password storage specifically needs
// slow, because "fast" is exactly what makes guessing cheap at scale.
class PasswordHasher {
    static String hash(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(password.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException | java.io.UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
