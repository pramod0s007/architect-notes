public class Main {
    static final int SYSTEM_PROMPT_TOKENS = 20;

    public static void main(String[] args) {
        int numTurns = 5;
        int tokensPerTurn = 30;

        KvCacheServer server = new KvCacheServer();
        String priorKey = "conversation-A:empty";
        int priorLength = 0;
        int total = 0;

        System.out.println("Same conversation, " + numTurns + " turns, "
            + tokensPerTurn + " new tokens per turn, KV-cache reuse keyed by prefix CONTENT:");

        for (int turn = 1; turn <= numTurns; turn++) {
            int newTokens = (turn == 1) ? SYSTEM_PROMPT_TOKENS + tokensPerTurn : tokensPerTurn;
            String newKey = "conversation-A:turn" + turn;
            int tokensProcessed = server.tokensProcessedForTurn(priorKey, priorLength, newTokens, newKey);
            total += tokensProcessed;
            System.out.println("  Turn " + turn + ": processes " + tokensProcessed + " tokens ("
                + (turn == 1 ? "cache miss, full prefix" : "cache hit, only new tokens") + ")");
            priorLength += newTokens;
            priorKey = newKey;
        }

        System.out.println();
        System.out.println("Total tokens processed across " + numTurns + " turns: " + total);
        int noCacheBaseline = 550;
        System.out.println("vs. no caching: " + noCacheBaseline + " tokens -- "
            + String.format("%.2f", noCacheBaseline / (double) total) + "x fewer tokens processed, "
            + "identical conversation, identical final context.");
    }
}
