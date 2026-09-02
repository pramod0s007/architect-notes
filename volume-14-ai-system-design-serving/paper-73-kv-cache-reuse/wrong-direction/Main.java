public class Main {
    public static void main(String[] args) {
        LengthKeyedCacheServer server = new LengthKeyedCacheServer();

        // Conversation A: a customer asking about a refund. Its prefix reaches
        // 80 tokens by turn 3.
        String conversationAContent = "System: You are a support agent. User: I want a refund " +
            "for order #4471. Assistant: I can help with that refund request.";
        server.recordPrefix(80, conversationAContent);

        // Conversation B: a COMPLETELY different customer, a different session,
        // asking about a password reset. Its prefix ALSO happens to reach 80
        // tokens, by coincidence -- different content, same length.
        String conversationBContent = "System: You are a support agent. User: I forgot my " +
            "password and need to reset it urgently. Assistant: Let's get that reset started.";

        System.out.println("Conversation A cached its 80-token prefix (refund request).");
        System.out.println("Conversation B's prefix also reaches 80 tokens (password reset) -- different content.");

        String servedContent = server.servedCacheContentFor(80);
        boolean servedWrongContext = !servedContent.equals(conversationBContent);

        System.out.println();
        System.out.println("Conversation B requests its cached prefix by length (80 tokens).");
        System.out.println("Content actually served from cache: \"" + servedContent + "\"");
        System.out.println("This matches Conversation B's real prefix: " + !servedWrongContext);
        System.out.println(servedWrongContext
            ? "STILL FAILS, WORSE THAN BEFORE: Conversation B was served Conversation A's cached KV "
              + "state -- a refund conversation's context -- for what should have been its own password-"
              + "reset conversation. This isn't a performance regression, it's a correctness bug: the "
              + "wrong customer's conversation history would be reused, silently, because two unrelated "
              + "prefixes happened to be the same length. Caching by length instead of content trades a "
              + "cheaper cache key for the one property a cache key actually needs -- uniquely identifying "
              + "what it's a cache OF."
            : "Unexpected -- investigate.");
    }
}
