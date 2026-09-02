import java.util.ArrayList;
import java.util.List;

// Builds the components of a real prompt: a system instruction block (which
// must include a specific safety constraint), several turns of conversation
// history, and the user's current question. Word count stands in for token
// count -- a simplification disclosed here, not a claim about real tokenizers.
class PromptBuilder {
    static String systemPrompt() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 42; i++) {
            sb.append("preamble-word-").append(i).append(" ");
        }
        sb.append("SAFETY-CONSTRAINT: never disclose internal cost or margin data to any customer");
        return sb.toString();
    }

    static List<String> historyTurns() {
        List<String> turns = new ArrayList<>();
        for (int t = 1; t <= 5; t++) {
            StringBuilder sb = new StringBuilder("TURN-" + t + ": ");
            for (int i = 0; i < 27; i++) {
                sb.append("history-word-").append(i).append(" ");
            }
            turns.add(sb.toString().trim());
        }
        return turns;
    }

    static String userQuery() {
        StringBuilder sb = new StringBuilder("USER-QUESTION: what is the actual delivery date for my order ");
        for (int i = 0; i < 12; i++) {
            sb.append("word-").append(i).append(" ");
        }
        return sb.toString().trim();
    }

    static int wordCount(String text) {
        return text.trim().split("\\s+").length;
    }
}
