/**
 * Run: javac *.java && java Main
 */
public final class Main {

    public static void main(String[] args) {
        PipelineBuilder pipeline = new PipelineBuilder();

        Request ok = new Request(
                "POST",
                "/orders",
                "customer:42",
                "token-abc",
                "{\"sku\":\"BOOK-1\"}");

        printPipeline("Valid request", pipeline.run(ok));

        System.out.println();
        Request noToken = new Request("GET", "/orders", "customer:42", "", "");
        printPipeline("Missing token", pipeline.run(noToken));
    }

    private static void printPipeline(String title, PipelineBuilder.PipelineResult result) {
        System.out.println("=== " + title + " ===");
        System.out.println("Request");
        System.out.println("   ↓");
        for (String stage : result.trace().split("\n")) {
            System.out.println(stage);
            System.out.println("   ↓");
        }
        if (result.success()) {
            System.out.println("Success");
        } else {
            System.out.println("Stopped — " + result.failureReason());
        }
    }
}
