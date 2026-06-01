# Decorator Pattern — Adding Behavior Without Subclassing

*Java's InputStream, Spring's security filters, HTTP middleware — you've used Decorator Pattern thousands of times. Here's the pressure that created it.*

---

Here is a design question that sounds simple:

You have a `TextMessage` class that sends plain text. Product wants to add compression. Security wants to add encryption. Analytics wants to add logging. Compliance wants to add audit trails.

How do you add these capabilities without rewriting `TextMessage`?

The inheritance answer:

```
TextMessage
├── CompressedTextMessage
├── EncryptedTextMessage
├── LoggedTextMessage
├── AuditedTextMessage
├── CompressedEncryptedTextMessage
├── CompressedLoggedTextMessage
├── EncryptedLoggedTextMessage
├── CompressedEncryptedLoggedTextMessage
├── CompressedEncryptedLoggedAuditedTextMessage
└── ... (2^n classes for n capabilities)
```

Four capabilities. Sixteen subclasses to cover every combination. Add a fifth capability: thirty-two subclasses.

**Inheritance explodes combinatorially.** This is the pressure Decorator Pattern solves.

---

## The Alternative — Wrapping

Instead of subclassing, wrap.

Each capability is a wrapper that adds one behavior and delegates the core operation to whatever it wraps.

```java
interface MessageSender {
    void send(String message);
}

// Base implementation
class TextMessageSender implements MessageSender {
    public void send(String message) {
        // actual send logic
        network.transmit(message);
    }
}

// Compression wrapper
class CompressedMessageSender implements MessageSender {
    private final MessageSender wrapped;

    CompressedMessageSender(MessageSender wrapped) {
        this.wrapped = wrapped;
    }

    public void send(String message) {
        String compressed = compress(message);
        wrapped.send(compressed);  // delegate to inner
    }
}

// Encryption wrapper
class EncryptedMessageSender implements MessageSender {
    private final MessageSender wrapped;

    EncryptedMessageSender(MessageSender wrapped) {
        this.wrapped = wrapped;
    }

    public void send(String message) {
        String encrypted = encrypt(message);
        wrapped.send(encrypted);
    }
}

// Logging wrapper
class LoggedMessageSender implements MessageSender {
    private final MessageSender wrapped;

    LoggedMessageSender(MessageSender wrapped) {
        this.wrapped = wrapped;
    }

    public void send(String message) {
        log.info("Sending message, length={}", message.length());
        wrapped.send(message);
        log.info("Message sent");
    }
}
```

Compose them:

```java
// Compressed + Encrypted + Logged — any combination
MessageSender sender =
    new LoggedMessageSender(
        new EncryptedMessageSender(
            new CompressedMessageSender(
                new TextMessageSender())));

sender.send("Hello World");
```

Order matters — outermost wraps run first. Log → Encrypt → Compress → Send.

Need a different combination? Change the wrapping order. No new classes. No inheritance hierarchy.

---

## You Already Use This Every Day

**Java I/O streams — the canonical example:**

```java
// Read a file, with buffering and decompression
InputStream input =
    new GZIPInputStream(
        new BufferedInputStream(
            new FileInputStream("data.gz")));
```

`FileInputStream` is the base. `BufferedInputStream` wraps it to add buffering. `GZIPInputStream` wraps that to add decompression. Three independent capabilities, composed freely.

**Spring Security filter chain:**
```java
http
    .addFilterBefore(new JwtAuthFilter(), UsernamePasswordAuthenticationFilter.class)
    .addFilterBefore(new RateLimitFilter(), JwtAuthFilter.class)
    .addFilterBefore(new RequestLoggingFilter(), RateLimitFilter.class);
```

Each filter is a decorator on the request/response pair. Capabilities stack independently.

**OkHttp interceptors:**
```java
OkHttpClient client = new OkHttpClient.Builder()
    .addInterceptor(new LoggingInterceptor())
    .addInterceptor(new RetryInterceptor(3))
    .addInterceptor(new AuthInterceptor(token))
    .build();
```

**Node.js Express middleware:**
```javascript
app.use(helmet());         // Security headers decorator
app.use(compression());    // Compression decorator
app.use(morgan('combined')); // Logging decorator
app.use(rateLimit(...));   // Rate limiting decorator
```

The pattern name varies. The structure is identical: wrap, delegate, add one behavior.

---

## The Four-Component Structure

| Component | Role |
|-----------|------|
| **Component interface** | Defines the operation — `MessageSender.send()` |
| **Concrete Component** | Base implementation — `TextMessageSender` |
| **Decorator (abstract)** | Holds a reference to wrapped component, implements interface |
| **ConcreteDecorator** | Adds one specific behavior before/after delegation |

The abstract decorator is often optional in Java — each concrete decorator can hold the wrapped reference directly.

---

## The Cross-Cutting Concerns Problem

Software systems have two categories of concerns:

**Core concerns** — the business logic. Process a payment. Generate a report. Send a notification.

**Cross-cutting concerns** — infrastructure behavior that applies across multiple core concerns. Logging. Caching. Retry. Metrics. Rate limiting. Circuit breaking. Tracing.

The problem: cross-cutting concerns don't belong inside business logic, but they need to wrap it.

Inheritance solves this by creating subclasses. But with five cross-cutting concerns and fifteen services, that's seventy-five subclasses — or an unmaintainable base class with seventy-five methods.

Decorator solves this by wrapping. Each cross-cutting concern is one decorator. Any service can be wrapped with any combination.

```java
// Before: cross-cutting concerns inside service
class OrderService {
    Order createOrder(CreateOrderRequest request) {
        long start = System.currentTimeMillis();
        log.info("Creating order for {}", request.getCustomerId());

        if (cache.contains(request.getIdempotencyKey())) {
            return cache.get(request.getIdempotencyKey());
        }

        try {
            Order order = doCreateOrder(request);
            metrics.increment("orders.created");
            cache.put(request.getIdempotencyKey(), order);
            log.info("Order created: {}", order.getId());
            return order;
        } catch (Exception e) {
            metrics.increment("orders.failed");
            log.error("Order creation failed", e);
            throw e;
        } finally {
            metrics.record("orders.latency", System.currentTimeMillis() - start);
        }
    }
}
```

Eighty lines of cross-cutting noise around ten lines of business logic.

```java
// After: cross-cutting concerns as decorators
class OrderService {
    Order createOrder(CreateOrderRequest request) {
        // 10 lines of actual business logic
        return orderRepository.save(new Order(request));
    }
}

// Assembled at wiring time:
OrderService service = new MetricsDecorator(
    new LoggingDecorator(
        new CachingDecorator(
            new OrderService(),
            cache
        ),
        log
    ),
    registry
);
```

Each decorator is independently testable, independently removable, and independently reusable across any service.

This is exactly what Spring AOP does — the annotations (`@Cacheable`, `@Transactional`, `@PreAuthorize`) tell Spring which decorators to apply. You write the business logic; the framework wires the cross-cutting concerns around it.

## The Ordering Rule

Decorator order matters. The outermost runs first, so think about:

- **Logging should be outermost** — log the full request before any transformation
- **Authentication/authorization before business logic** — fail fast if unauthorized
- **Caching before expensive operations** — return cached result without touching DB
- **Retry should wrap the core operation** — not the logging or auth

```java
// Correct order for a repository:
new LoggingDecorator(         // 1st: log the incoming request
    new AuthDecorator(        // 2nd: check permissions
        new CachingDecorator( // 3rd: return cached if available
            new RetryDecorator(   // 4th: retry on failure
                new RealRepository()  // 5th: actual DB call
            )
        )
    )
)
```

Wrong order — caching before auth — would serve cached results to unauthorized callers. Wrong order — retry outside caching — would retry even when the cache has the result.

**When you assemble a decorator chain, you're defining a policy. Document the policy in a builder.**

---

## Three Real Production Examples

### 1. Caching Decorator

```java
class CachingProductRepository implements ProductRepository {

    private final ProductRepository wrapped;
    private final Cache<Long, Product> cache;

    CachingProductRepository(ProductRepository wrapped, Cache<Long, Product> cache) {
        this.wrapped = wrapped;
        this.cache = cache;
    }

    public Product findById(Long id) {
        return cache.get(id, () -> wrapped.findById(id));
    }

    public void save(Product product) {
        cache.invalidate(product.getId());
        wrapped.save(product);
    }
}
```

Usage: `new CachingProductRepository(new JpaProductRepository(em), caffeineCache)`

Add caching to any repository without modifying it. Remove caching: unwrap. Change cache provider: swap the cache, not the repository.

### 2. Retry Decorator

```java
class RetryingPaymentGateway implements PaymentGateway {

    private final PaymentGateway wrapped;
    private final int maxRetries;
    private final Duration backoff;

    public PaymentResult charge(PaymentRequest request) {
        int attempt = 0;
        while (true) {
            try {
                return wrapped.charge(request);
            } catch (TransientPaymentException e) {
                if (++attempt >= maxRetries) throw e;
                sleep(backoff.multipliedBy(attempt));
            }
        }
    }
}
```

Wrap any `PaymentGateway` with retry behavior. The underlying gateway doesn't change.

### 3. Metrics Decorator

```java
class MetricsCollectingRepository<T, ID> implements Repository<T, ID> {

    private final Repository<T, ID> wrapped;
    private final MeterRegistry registry;
    private final String repoName;

    public T findById(ID id) {
        Timer.Sample sample = Timer.start(registry);
        try {
            T result = wrapped.findById(id);
            sample.stop(registry.timer(repoName + ".findById", "status", "success"));
            return result;
        } catch (Exception e) {
            sample.stop(registry.timer(repoName + ".findById", "status", "error"));
            throw e;
        }
    }
}
```

Add Prometheus metrics to any repository without modifying it.

---

## Decorator vs Inheritance vs Strategy

| Approach | When | Trade-off |
|----------|------|-----------|
| Inheritance | Capability is fundamental to identity | Combinatorial explosion with multiple capabilities |
| Decorator | Add capabilities dynamically, combinations vary | Nesting can become deep; debug stack traces |
| Strategy | Swap algorithm, one variation at a time | Single variation, not capability stacking |

Use Decorator when: capabilities are orthogonal (independent of each other), the combination varies by context, and you need runtime composability.

---

## The Warning — Decorator Depth

Deep decorator chains are hard to debug:

```java
// This is fine
sender = new LoggedMessageSender(new EncryptedMessageSender(base));

// This becomes painful
sender = new CachedSender(
    new CircuitBreakerSender(
        new RetryingSender(
            new LoggedSender(
                new TracedSender(
                    new MetricsSender(
                        new RateLimitedSender(base)))))));
```

At this depth, stack traces become unreadable. Use a builder pattern to assemble decorator chains:

```java
MessageSender sender = MessageSenderBuilder.wrap(base)
    .withMetrics(registry)
    .withRateLimit(100, Duration.ofMinutes(1))
    .withRetry(3, Duration.ofSeconds(1))
    .withCircuitBreaker(failureThreshold)
    .withLogging()
    .build();
```

The builder constructs the chain; the reader doesn't need to count nesting levels.

---

## The Interview Answer

**Question:** When would you use Decorator Pattern?

**Weak answer:** *"When you want to add behavior to a class."*

**Strong answer:**

*"Decorator Pattern solves combinatorial inheritance explosion — when multiple independent capabilities can be added to a component in any combination, inheritance creates 2^n subclasses. Decorator wraps the component: each decorator adds one capability and delegates to the inner component. The result is runtime composability — you choose which capabilities to apply by choosing which wrappers to stack. Java's I/O streams are the canonical example. In production I use it for cross-cutting concerns: caching, retry, metrics, circuit breaking, logging — each as an independent decorator on a repository or service interface. The failure mode is deep nesting that's hard to debug; a builder helps."*

---

## Key Takeaways

- Decorator solves **combinatorial inheritance explosion** from multiple orthogonal capabilities.
- Wrap instead of extend. Delegate to the inner component.
- Order matters — outermost decorator runs first.
- Java I/O streams, Spring Security filters, HTTP interceptors — all Decorator Pattern.
- Cross-cutting concerns (caching, retry, metrics, logging) are natural decorators.
- Deep chains need a builder for readability.

---

*All papers and runnable samples: [github.com/pramod0s007/architect-notes](https://github.com/pramod0s007/architect-notes)*

*Previous → Paper 16: Observer Pattern | Next → Paper 18: Proxy Pattern*
