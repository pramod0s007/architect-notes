# Request Pipeline — Chain of Responsibility

Runnable sample for **Chain of Responsibility** (Paper 10).

## Run

```bash
cd code-samples/chain-of-responsibility/request-pipeline
javac *.java
java Main
```

## Flow

```text
Request
   ↓
Authentication
   ↓
Authorization
   ↓
Validation
   ↓
Rate Limiting
   ↓
Success
```

Each handler calls the next link only when its check passes.

## Build chain

```java
Handler head = new PipelineBuilder().build();
// Authentication → Authorization → Validation → Rate Limiting
```
