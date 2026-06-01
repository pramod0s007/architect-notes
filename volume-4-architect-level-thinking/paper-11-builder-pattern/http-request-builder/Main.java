/**
 * Run: javac *.java && java Main
 */
public final class Main {

    public static void main(String[] args) {
        HttpRequest request = new HttpRequestBuilder()
                .url("https://api.example.com/orders")
                .method("POST")
                .header("Authorization", "Bearer token")
                .header("Content-Type", "application/json")
                .body("{\"sku\":\"BOOK-1\"}")
                .timeout(5000)
                .build();

        System.out.println(request);
    }
}
