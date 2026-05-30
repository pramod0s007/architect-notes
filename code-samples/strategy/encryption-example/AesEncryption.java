public final class AesEncryption implements EncryptionStrategy {

    @Override
    public String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            throw new IllegalArgumentException("plainText must not be null or empty");
        }
        // Teaching sample: stand-in for real AES — not cryptographic code
        return "AES:" + plainText;
    }

    @Override
    public String algorithmName() {
        return "AES";
    }
}
