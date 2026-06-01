/**
 * Strategy interface — isolates encryption behavior variation.
 * See: volume-2/.../paper-04-strategy-pattern-through-real-refactoring
 */
public interface EncryptionStrategy {

    String encrypt(String plainText);

    String algorithmName();
}
