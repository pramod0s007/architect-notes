import java.util.List;

public class Main {
    static final int CONTEXT_BUDGET_WORDS = 190;

    public static void main(String[] args) {
        String systemPrompt = PromptBuilder.systemPrompt();
        List<String> history = PromptBuilder.historyTurns();
        String userQuery = PromptBuilder.userQuery();

        int historyWords = 0;
        for (String turn : history) {
            historyWords += PromptBuilder.wordCount(turn);
        }
        int queryWords = PromptBuilder.wordCount(userQuery);

        // The overcorrection: conversation history feels like the personalized,
        // important part, so never truncate it -- and the user's question can
        // obviously never be dropped either. Whatever's left of the budget goes
        // to the system prompt, however little that turns out to be.
        int budgetForSystem = CONTEXT_BUDGET_WORDS - historyWords - queryWords;
        System.out.println("Full conversation history (" + historyWords + " words) and the user's "
            + "question (" + queryWords + " words) are NEVER truncated.");
        System.out.println("Remaining budget for the system prompt: " + budgetForSystem + " words");

        String[] systemWordsArr = systemPrompt.trim().split("\\s+");
        StringBuilder truncatedSystem = new StringBuilder();
        for (int i = 0; i < Math.min(budgetForSystem, systemWordsArr.length); i++) {
            truncatedSystem.append(systemWordsArr[i]).append(" ");
        }
        String truncated = truncatedSystem.toString().trim();

        boolean safetyIntact = truncated.contains("SAFETY-CONSTRAINT");
        System.out.println("System prompt truncated from " + systemWordsArr.length + " words to "
            + PromptBuilder.wordCount(truncated) + " words.");
        System.out.println("Safety constraint still present after truncation: " + safetyIntact);
        System.out.println(!safetyIntact
            ? "STILL FAILS, MORE DANGEROUSLY: the safety constraint -- \"never disclose internal cost "
              + "or margin data\" -- was placed near the end of the system prompt and is now completely "
              + "gone. The model receives full conversation history, the user's full question, and a "
              + "system prompt with its actual behavioral constraint silently removed. This isn't a "
              + "quality regression -- it's a policy the system was supposed to enforce, deleted by a "
              + "truncation strategy that assumed conversation history mattered more than instructions."
            : "Unexpected -- investigate.");
    }
}
