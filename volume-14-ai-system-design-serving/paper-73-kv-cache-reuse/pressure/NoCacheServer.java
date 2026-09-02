// Reprocesses the ENTIRE conversation -- system prompt plus every prior turn --
// from scratch on every single turn. Cost is measured in tokens processed,
// standing in for real attention/feed-forward compute, disclosed as illustrative
// rather than a claim about any specific model's actual per-token cost.
class NoCacheServer {
    static final int SYSTEM_PROMPT_TOKENS = 20;

    int tokensProcessedForTurn(int turnNumber, int tokensPerTurn) {
        return SYSTEM_PROMPT_TOKENS + tokensPerTurn * turnNumber;
    }

    int totalTokensProcessed(int numTurns, int tokensPerTurn) {
        int total = 0;
        for (int turn = 1; turn <= numTurns; turn++) {
            total += tokensProcessedForTurn(turn, tokensPerTurn);
        }
        return total;
    }
}
