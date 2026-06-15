// OCP abstraction — defines the contract every channel formatter must fulfil.
// NotificationDispatcher depends only on this interface.
// Adding a new channel (WhatsApp, Slack, etc.) = new class, zero changes to dispatcher.

public interface NotificationFormatter {

    /**
     * Formats the order confirmation message for this channel.
     *
     * @param order the confirmed order
     * @return the formatted message string ready to be sent
     */
    String format(Order order);

    /**
     * Returns the channel identifier (e.g. "EMAIL", "SMS", "PUSH").
     * Used for logging in the dispatcher.
     */
    String getChannel();
}
