# HTTP Request Builder

Runnable sample for **Builder Pattern** (Paper 11).

## Run

```bash
cd code-samples/builder/http-request-builder
javac *.java
java Main
```

## Usage

```java
HttpRequest request = new HttpRequestBuilder()
    .url("https://api.example.com/orders")
    .method("POST")
    .timeout(5000)
    .build();
```

Validation runs at `build()` — not at every setter.
