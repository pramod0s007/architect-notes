/**
 * Run: javac *.java && java Main
 */
public final class Main {

    public static void main(String[] args) {
        String message = "architect-notes";

        run(new Encryptor(new AesEncryption()), message);
        run(new Encryptor(new DesEncryption()), message);
    }

    private static void run(Encryptor encryptor, String message) {
        String cipher = encryptor.encrypt(message);
        System.out.printf("[%s] %s -> %s%n", encryptor.algorithm(), message, cipher);
    }
}
