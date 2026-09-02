import java.util.List;

public class Main {
    static final int CONTEXT_BUDGET_WORDS = 190;

    public static void main(String[] args) {
        String systemPrompt = PromptBuilder.systemPrompt();
        List<String> history = PromptBuilder.historyTurns();
        String userQuery = PromptBuilder.userQuery();

        StringBuilder full = new StringBuilder(systemPrompt);
        for (String turn : history) {
            full.append(" ").append(turn);
        }
        full.append(" ").append(userQuery);

        String[] words = full.toString().trim().split("\\s+");
        System.out.println("Full assembled prompt: " + words.length + " words "
            + "(system=" + PromptBuilder.wordCount(systemPrompt) + ", history="
            + (words.length - PromptBuilder.wordCount(systemPrompt) - PromptBuilder.wordCount(userQuery))
            + ", query=" + PromptBuilder.wordCount(userQuery) + ")");
        System.out.println("Context budget: " + CONTEXT_BUDGET_WORDS + " words");

        StringBuilder truncated = new StringBuilder();
        for (int i = 0; i < Math.min(CONTEXT_BUDGET_WORDS, words.length); i++) {
            truncated.append(words[i]).append(" ");
        }
        String result = truncated.toString().trim();

        boolean hasUserQuestion = result.contains("USER-QUESTION");
        System.out.println("Assembled prompt truncated from the end to fit the budget.");
        System.out.println("Truncated prompt contains the user's actual question: " + hasUserQuestion);
        System.out.println(hasUserQuestion
            ? "Unexpected -- investigate."
            : "CONFIRMED FAILURE: the model never sees the user's question at all. It was placed last "
              + "in the assembled prompt, and naive tail truncation cut it completely to make room for "
              + "content that was already there before the user ever asked anything.");
    }
}
