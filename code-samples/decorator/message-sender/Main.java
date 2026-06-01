package decorator.messagesender;

public class Main {

    public static void main(String[] args) {
        System.out.println("=== Plain sender ===");
        MessageSender plain = new PlainMessageSender();
        plain.send("Hello World", "alice@example.com");

        System.out.println("\n=== Logged + Encrypted + Compressed ===");
        MessageSender full = new LoggedMessageSender(
            new EncryptedMessageSender(
                new CompressedMessageSender(
                    new PlainMessageSender())));
        full.send("Hello World this is a longer message", "bob@example.com");

        System.out.println("\n=== Logged only (no encryption for internal messages) ===");
        MessageSender internal = new LoggedMessageSender(new PlainMessageSender());
        internal.send("Internal ping", "monitor@internal.com");
    }
}
