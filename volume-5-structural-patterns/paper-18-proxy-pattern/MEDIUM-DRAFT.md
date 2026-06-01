# Proxy Pattern — Controlling Access to Objects

*Every gRPC stub, every Spring @Transactional method, every lazy-loaded JPA entity is a proxy. Here's the four problems it solves.*

---

Proxy Pattern is one of the most commonly used patterns in production systems — and one of the least recognized by the engineers using it.

Every time you call a Spring `@Transactional` service method, a proxy intercepts the call and manages the transaction.

Every time JPA returns a `@OneToMany` collection and doesn't load it until you access it — that's a lazy-loading proxy.

Every time a gRPC stub routes your method call to a remote server — that's a remote proxy.

Every time an ACL service intercepts a repository call to check permissions — that's a protection proxy.

Four different problems. One pattern. The pressure in each case: **add behavior around object access without the caller knowing about it.**

---

## The Proxy Structure

A proxy implements the same interface as the real object. The caller can't tell the difference. The proxy intercepts the call, does something, and delegates to the real object.

```java
interface PaymentGateway {
    PaymentResult charge(PaymentRequest request);
    void refund(String transactionId, double amount);
}

// Real object
class StripePaymentGateway implements PaymentGateway {
    public PaymentResult charge(PaymentRequest request) {
        return stripe.createCharge(request);
    }
    public void refund(String transactionId, double amount) {
        stripe.createRefund(transactionId, amount);
    }
}

// Proxy — caller can't tell this apart from the real object
class PaymentGatewayProxy implements PaymentGateway {

    private PaymentGateway real;

    public PaymentResult charge(PaymentRequest request) {
        // Before: check access, log, validate
        securityService.assertCanCharge(currentUser());
        log.info("Charging {} for amount {}", request.getCustomerId(), request.getAmount());

        PaymentResult result = real.charge(request);  // delegate

        // After: metrics, audit
        metrics.recordCharge(result);
        auditLog.record("CHARGE", request, result);
        return result;
    }
}
```

The caller holds a `PaymentGateway` reference. It never knows whether it's the real object or the proxy. This is the fundamental invariant.

---

## Four Types of Proxy — Four Pressures

### 1. Virtual Proxy (Lazy Loading)

**Pressure:** Creating the real object is expensive. You want to defer that cost until the object is actually needed.

```java
class HeavyReportProxy implements Report {

    private Report realReport; // null until first access

    public String getTitle() {
        return "Q4 Financial Summary";  // cheap — no need to load
    }

    public byte[] getPdfContent() {
        if (realReport == null) {
            realReport = reportGenerator.generate();  // expensive — load now
        }
        return realReport.getPdfContent();
    }
}
```

**Real examples:** JPA lazy-loaded collections (Hibernate creates a proxy that loads from DB on first access), Spring's `@Lazy` beans, virtual proxies in asset loading for games.

### 2. Remote Proxy (RPC Stubs)

**Pressure:** The real object lives on a different machine. The caller should interact with it as if it were local.

```java
// gRPC generated stub — this IS a remote proxy
PaymentServiceGrpc.PaymentServiceBlockingStub stub =
    PaymentServiceGrpc.newBlockingStub(channel);

// Caller treats this like a local object
PaymentResult result = stub.charge(request);
// Under the hood: serializes request → network → remote service → deserialize response
```

The caller doesn't write socket code. The proxy handles serialization, network transport, and deserialization. **Every RPC stub is a remote proxy.**

### 3. Protection Proxy (Access Control)

**Pressure:** Different callers should have different access rights to the same object. Access control should not live inside the real object.

```java
class SecureUserRepository implements UserRepository {

    private final UserRepository wrapped;
    private final SecurityContext security;

    public User findById(Long id) {
        if (!security.hasRole("ADMIN") && !security.isCurrentUser(id)) {
            throw new AccessDeniedException("Cannot access other users");
        }
        return wrapped.findById(id);
    }

    public void delete(Long id) {
        security.assertRole("SUPER_ADMIN");
        wrapped.delete(id);
    }
}
```

**Real examples:** Spring Security method security (`@PreAuthorize`), row-level security proxies, API gateway authorization.

### 4. Caching Proxy

**Pressure:** The real object's operation is expensive (DB call, external API, heavy computation). Results should be cached transparently.

```java
class CachingProductService implements ProductService {

    private final ProductService wrapped;
    private final Cache<String, Product> cache;

    public Product getProduct(String sku) {
        return cache.get(sku, () -> wrapped.getProduct(sku));
    }

    public void updateProduct(Product product) {
        cache.invalidate(product.getSku());
        wrapped.updateProduct(product);
    }
}
```

**Real examples:** Caffeine cache wrappers, Redis caching proxies, CDN edge proxies for HTTP responses.

---

## Spring AOP — Proxy Under the Hood

Spring's most powerful features are all proxy-based:

```java
@Service
class OrderService {

    @Transactional          // Spring creates a proxy that manages transactions
    @PreAuthorize("hasRole('ADMIN')")  // Spring creates a proxy that checks auth
    @Cacheable("orders")   // Spring creates a proxy that caches results
    public Order getOrder(Long id) {
        return orderRepository.findById(id);
    }
}
```

When you autowire `OrderService`, Spring injects a proxy, not the real class. The proxy intercepts the call, handles the transaction, checks authorization, checks the cache — then delegates to your real code.

**You write the business logic. The proxy handles cross-cutting concerns.**

This is why `@Transactional` doesn't work when you call a method from within the same class — internal calls bypass the proxy because they go directly to `this`, not through the injected proxy reference.

---

## The @Transactional Self-Call Problem — Deep Dive

The most common production bug related to Proxy Pattern is `@Transactional` not working on self-calls. It's worth understanding exactly why.

When you autowire a Spring service, Spring gives you a proxy:

```java
@Service
class OrderService {

    @Transactional
    public void createOrder(CreateOrderRequest request) {
        Order order = orderRepository.save(new Order(request));
        notifyWarehouse(order);  // another @Transactional call
    }

    @Transactional
    public void notifyWarehouse(Order order) {
        warehouseRepository.createPickRequest(order);
    }
}
```

When a caller invokes `orderService.createOrder(...)`, the call goes through the proxy. The proxy starts a transaction, calls the real `createOrder`, then commits.

Inside `createOrder`, when `this.notifyWarehouse(order)` is called, **it bypasses the proxy**. It calls `notifyWarehouse` directly on `this` — the real object, not the proxy. No new transaction is started; the transactional behavior of `notifyWarehouse` is ignored.

Fix: inject the service into itself, or restructure the class to avoid self-calls entirely by extracting the inner method to a separate service bean. Both approaches ensure the call routes through the Spring proxy rather than directly to `this`.

```java
@Service
class OrderService {

    @Autowired
    private OrderService self;  // injecting proxy, not raw this

    @Transactional
    public void createOrder(CreateOrderRequest request) {
        Order order = orderRepository.save(new Order(request));
        self.notifyWarehouse(order);  // goes through proxy — transaction respected
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notifyWarehouse(Order order) {
        warehouseRepository.createPickRequest(order);
    }
}
```

**Understanding that Spring injects a proxy — not the real object — makes this behavior obvious rather than mysterious.**

---

## Proxy vs Decorator — The Distinction

Both patterns wrap an object and delegate. The distinction is intent:

| | Proxy | Decorator |
|-|-------|-----------|
| **Purpose** | Control access to the real object | Add behavior to an object |
| **Who creates it** | Often framework-created, not caller | Caller explicitly wraps |
| **Caller awareness** | Caller usually doesn't know it's a proxy | Caller composes decorators explicitly |
| **Common use** | Lazy loading, auth, caching, remote | Compression, logging, retry, metrics |
| **Transparency** | High — proxy is invisible | Lower — caller stacks decorators |

In practice the boundary blurs. A caching proxy and a caching decorator do the same thing. The label matters less than the intent and transparency.

---

## Dynamic Proxies (Java)

Java's `java.lang.reflect.Proxy` creates proxies at runtime without writing a class for each interface:

```java
PaymentGateway proxy = (PaymentGateway) Proxy.newProxyInstance(
    PaymentGateway.class.getClassLoader(),
    new Class[]{PaymentGateway.class},
    (proxyObj, method, args) -> {
        log.info("Calling {} with args {}", method.getName(), args);
        Object result = method.invoke(realGateway, args);
        log.info("Result: {}", result);
        return result;
    }
);
```

This is how Spring AOP works internally. The `InvocationHandler` intercepts every method call. Spring uses this to inject transaction management, security, and caching behavior at runtime.

**Bytecode proxies (CGLIB):** For classes that don't implement an interface, Spring uses CGLIB to generate a subclass at runtime that overrides every method with proxy behavior. This is why Spring beans that use `@Transactional` without an interface must not be `final`.

---

## The Interview Answer

**Question:** What is Proxy Pattern and when would you use it?

**Weak answer:** *"A proxy acts as a placeholder for another object."*

**Strong answer:**

*"Proxy Pattern controls access to a real object by interposing a surrogate that implements the same interface. It addresses four distinct pressures: virtual proxy for deferred expensive creation (JPA lazy loading), remote proxy for transparent remote access (gRPC stubs, RPC clients), protection proxy for access control (Spring @PreAuthorize, ACL enforcement), and caching proxy for transparent result memoization. The fundamental invariant is that the caller can't distinguish the proxy from the real object. Spring's entire AOP model — @Transactional, @Cacheable, @PreAuthorize — is proxy-based. The common gotcha is @Transactional not working on self-calls, which happens because internal calls bypass the proxy."*

---

## Key Takeaways

- Proxy controls access to a real object — caller can't tell the difference.
- Four types: Virtual (lazy), Remote (RPC), Protection (auth), Caching (memoization).
- Spring AOP is proxies — @Transactional, @PreAuthorize, @Cacheable all intercept via proxy.
- Self-call gotcha: internal calls bypass the Spring proxy, @Transactional doesn't propagate.
- Dynamic Proxy (interfaces) vs CGLIB (concrete classes) — Spring uses both.
- Decorator vs Proxy: Decorator adds behavior explicitly; Proxy controls access transparently.

---

*All papers and runnable samples: [github.com/pramod0s007/architect-notes](https://github.com/pramod0s007/architect-notes)*

*Previous → Paper 17: Decorator Pattern | Next → Paper 19: Adapter Pattern*
