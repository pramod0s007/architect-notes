public class Main {
    public static void main(String[] args) {
        int numTurns = 5;
        int tokensPerTurn = 30;

        NoCacheServer server = new NoCacheServer();
        System.out.println("Multi-turn conversation, " + numTurns + " turns, "
            + tokensPerTurn + " new tokens per turn, no caching:");
        for (int turn = 1; turn <= numTurns; turn++) {
            int tokens = server.tokensProcessedForTurn(turn, tokensPerTurn);
            System.out.println("  Turn " + turn + ": reprocesses " + tokens
                + " tokens (system prompt + all " + (turn - 1) + " prior turns + this turn)");
        }

        int total = server.totalTokensProcessed(numTurns, tokensPerTurn);
        System.out.println();
        System.out.println("Total tokens processed across " + numTurns + " turns: " + total);
        System.out.println("CONFIRMED: every prior turn's tokens get reprocessed, from scratch, "
            + "on every single subsequent turn -- cost grows with the SQUARE of conversation length, "
            + "not linearly with how much new content the user actually added.");
    }
}
