/**
 * Caller stays stable; behavior varies via composed {@link EncryptionStrategy}.
 */
public final class Encryptor {

    private final EncryptionStrategy strategy;

    public Encryptor(EncryptionStrategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("strategy must not be null");
        }
        this.strategy = strategy;
    }

    public String encrypt(String plainText) {
        return strategy.encrypt(plainText);
    }

    public String algorithm() {
        return strategy.algorithmName();
    }
}
