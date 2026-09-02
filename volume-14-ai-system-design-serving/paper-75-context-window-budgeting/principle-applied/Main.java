import java.util.ArrayList;
import java.util.List;

public class Main {
    static final int CONTEXT_BUDGET_WORDS = 190;

    public static void main(String[] args) {
        String systemPrompt = PromptBuilder.systemPrompt();
        List<String> history = PromptBuilder.historyTurns();
        String userQuery = PromptBuilder.userQuery();

        int systemWords = PromptBuilder.wordCount(systemPrompt);
        int queryWords = PromptBuilder.wordCount(userQuery);
        int reservedWords = systemWords + queryWords;
        int budgetForHistory = CONTEXT_BUDGET_WORDS - reservedWords;

        System.out.println("System prompt (" + systemWords + " words) and user query (" + queryWords
            + " words) are ALWAYS reserved, never truncated.");
        System.out.println("Remaining budget for history: " + budgetForHistory + " words");

        // Drop OLDEST turns first until what remains fits the remaining budget.
        List<String> keptHistory = new ArrayList<>(history);
        int historyWords = 0;
        for (String turn : keptHistory) {
            historyWords += PromptBuilder.wordCount(turn);
        }
        List<String> dropped = new ArrayList<>();
        while (historyWords > budgetForHistory && !keptHistory.isEmpty()) {
            String oldest = keptHistory.remove(0);
            dropped.add(oldest);
            historyWords -= PromptBuilder.wordCount(oldest);
        }

        System.out.println("Turns dropped (oldest first): " + dropped.size() + " of " + history.size());
        System.out.println("Turns kept: " + keptHistory.size() + ", " + historyWords + " words");

        int totalWords = systemWords + historyWords + queryWords;
        System.out.println("Total assembled prompt: " + totalWords + " words (budget: " + CONTEXT_BUDGET_WORDS + ")");

        boolean systemIntact = systemPrompt.contains("SAFETY-CONSTRAINT");
        boolean queryIntact = userQuery.contains("USER-QUESTION");
        System.out.println("System prompt's safety constraint intact: " + systemIntact);
        System.out.println("User's actual question intact: " + queryIntact);
        System.out.println((systemIntact && queryIntact && totalWords <= CONTEXT_BUDGET_WORDS)
            ? "FIXED: both the safety constraint and the user's actual question survive completely. "
              + "Only the oldest, least-relevant conversation history was dropped to fit the budget."
            : "Unexpected -- investigate.");
    }
}
