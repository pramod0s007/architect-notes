import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

// A simplified stand-in for a real password-hashing algorithm (bcrypt,
// scrypt, Argon2) -- NOT production-grade cryptography. It illustrates the
// two properties that actually matter: a salt UNIQUE PER CREDENTIAL, and
// deliberate iteration to make each individual guess expensive to compute.
class PasswordHasher {
    static final int ITERATIONS = 100_000;

    static String hash(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = (salt + password).getBytes("UTF-8");
            for (int i = 0; i < ITERATIONS; i++) {
                bytes = digest.digest(bytes);
            }
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
