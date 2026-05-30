public final class DesEncryption implements EncryptionStrategy {

    @Override
    public String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            throw new IllegalArgumentException("plainText must not be null or empty");
        }
        // Teaching sample: stand-in for real DES — not cryptographic code
        return "DES:" + plainText;
    }

    @Override
    public String algorithmName() {
        return "DES";
    }
}
