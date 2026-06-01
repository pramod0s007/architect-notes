# Adapter Pattern — Making Incompatible Interfaces Work Together

*Every time you wrap a third-party SDK, every time you integrate a legacy system, every time you write a "translation layer" — you're building an Adapter. Here's how to do it right.*

---

Here is a scenario every backend engineer has faced:

Your system uses a `NotificationService` interface that your team defined:

```java
interface NotificationService {
    void send(String userId, String title, String body);
    void sendBatch(List<String> userIds, String title, String body);
}
```

Six months in, a business decision switches your push notification provider from FCM to a new enterprise platform called "PushPro." PushPro's SDK looks like this:

```java
// PushPro SDK — you can't modify this
class PushProClient {
    PushProResponse deliver(PushProMessage message);
    BulkDeliveryResult bulkDeliver(List<PushProMessage> messages);
}

class PushProMessage {
    String recipient;
    PushProPayload payload;
    Map<String, String> metadata;
}
```

Two incompatible interfaces. Your system speaks `NotificationService`. PushPro speaks `PushProClient`.

**This is interface incompatibility pressure.** The adapter's job: translate one interface into the other without modifying either.

---

## The Adapter

```java
class PushProNotificationAdapter implements NotificationService {

    private final PushProClient client;

    PushProNotificationAdapter(PushProClient client) {
        this.client = client;
    }

    @Override
    public void send(String userId, String title, String body) {
        PushProMessage message = new PushProMessage();
        message.recipient = userId;
        message.payload = new PushProPayload(title, body);
        message.metadata = Map.of("source", "architect-notes-service");

        PushProResponse response = client.deliver(message);

        if (!response.isSuccess()) {
            throw new NotificationException("PushPro delivery failed: " + response.getError());
        }
    }

    @Override
    public void sendBatch(List<String> userIds, String title, String body) {
        List<PushProMessage> messages = userIds.stream()
            .map(uid -> {
                PushProMessage m = new PushProMessage();
                m.recipient = uid;
                m.payload = new PushProPayload(title, body);
                return m;
            })
            .collect(toList());

        BulkDeliveryResult result = client.bulkDeliver(messages);
        if (result.getFailureCount() > 0) {
            log.warn("Bulk delivery partial failure: {}/{} failed",
                result.getFailureCount(), userIds.size());
        }
    }
}
```

Your system keeps using `NotificationService`. PushPro keeps its SDK unchanged. The adapter translates between them.

Switch providers again next year: write a new adapter, swap the injection. Your callers are untouched.

---

## The Three Adapter Shapes

### Shape 1: Object Adapter (most common)

The adapter holds the adaptee as a field. This is what we built above.

```java
class PushProNotificationAdapter implements NotificationService {
    private final PushProClient client;  // holds the adaptee
}
```

Works with concrete classes. No inheritance required.

### Shape 2: Class Adapter (Java: rarely used)

The adapter extends the adaptee. Requires the adaptee to be a class (not an interface), and Java's single inheritance makes this impractical in most cases.

```java
class PushProNotificationAdapter
    extends PushProClient          // extends adaptee
    implements NotificationService { // implements target
    // override adaptee methods to implement target interface
}
```

Avoid in Java — use object adapter instead.

### Shape 3: Two-Way Adapter

When you need to adapt in both directions. System A can talk to System B, and System B can talk to System A.

Rarely needed. Mostly appears in plugin architectures and protocol bridges.

---

## Real Production Scenarios

### Legacy System Integration

```java
// Your modern interface
interface CustomerRepository {
    Customer findById(String customerId);
    void save(Customer customer);
}

// Legacy mainframe system — COBOL-era API, can't modify
class MainframeCustomerSystem {
    CustomerRecord fetchCustomer(int legacyId);
    void updateCustomer(CustomerRecord record);
    int resolveId(String externalId);
}

// Adapter bridges them
class LegacyCustomerAdapter implements CustomerRepository {

    private final MainframeCustomerSystem mainframe;

    public Customer findById(String customerId) {
        int legacyId = mainframe.resolveId(customerId);
        CustomerRecord record = mainframe.fetchCustomer(legacyId);
        return toCustomer(record);  // map legacy format to domain model
    }

    public void save(Customer customer) {
        CustomerRecord record = toRecord(customer);
        mainframe.updateCustomer(record);
    }

    private Customer toCustomer(CustomerRecord r) { /* mapping */ }
    private CustomerRecord toRecord(Customer c) { /* mapping */ }
}
```

The rest of your system never knows a mainframe exists.

### Cloud Provider Portability

```java
// Your storage interface
interface ObjectStorage {
    void upload(String key, byte[] data);
    byte[] download(String key);
    void delete(String key);
}

// AWS adapter
class S3StorageAdapter implements ObjectStorage {
    private final S3Client s3;
    private final String bucket;

    public void upload(String key, byte[] data) {
        s3.putObject(PutObjectRequest.builder().bucket(bucket).key(key).build(),
            RequestBody.fromBytes(data));
    }
    // ...
}

// Azure adapter
class AzureBlobStorageAdapter implements ObjectStorage {
    private final BlobServiceClient client;
    private final String container;

    public void upload(String key, byte[] data) {
        client.getBlobContainerClient(container)
              .getBlobClient(key)
              .upload(BinaryData.fromBytes(data));
    }
    // ...
}
```

Swap cloud providers by swapping the adapter. No service code changes.

### Third-Party Library Isolation

```java
// You don't want your code directly importing Jackson everywhere
interface JsonSerializer {
    String serialize(Object object);
    <T> T deserialize(String json, Class<T> type);
}

// Jackson adapter
class JacksonAdapter implements JsonSerializer {
    private final ObjectMapper mapper = new ObjectMapper();

    public String serialize(Object object) {
        return mapper.writeValueAsString(object);
    }

    public <T> T deserialize(String json, Class<T> type) {
        return mapper.readValue(json, type);
    }
}
```

When you want to switch to Gson or a custom serializer: one new adapter, zero service changes.

---

## The Anti-Corruption Layer — Adapters at Scale

When a system integrates with many external services, individual adapters evolve into something larger: an **anti-corruption layer (ACL)**.

The ACL is a boundary. Everything outside it speaks the external service's language. Everything inside it speaks your domain's language. The ACL handles the translation.

```
Your Domain Model          Anti-Corruption Layer       External Service
─────────────────────     ─────────────────────────    ─────────────────
Customer (your model)  ←  CustomerAdapter.toCustomer   CustomerRecord (mainframe)
Order (your model)     ←  OrderAdapter.toOrder         ORDR_HDR + ORDR_LINES (legacy)
Payment (your model)   ←  PaymentAdapter.toPayment     TransactionDTO (payment SDK)
```

Three principles for a healthy ACL:

**1. Translate, don't transform.** Adapters convert types and field names. Business rules live in the domain, not the adapter.

**2. One adapter per external system.** Don't mix payment SDK translations with mainframe translations. Each external system gets its own adapter class or package.

**3. Test adapters with contract tests.** The external system's API can change. An adapter test should fail loudly when the external format changes, before that change reaches domain logic.

```java
class MainframeCustomerAdapterTest {

    @Test
    void mapsLegacyStatusCodeToActiveDomainStatus() {
        CustomerRecord record = new CustomerRecord();
        record.setStatusCode("A");  // mainframe 'A' = active

        Customer customer = adapter.toCustomer(record);

        assertEquals(CustomerStatus.ACTIVE, customer.getStatus());
        // This test breaks if mainframe changes 'A' to '01' — surfaced early
    }
}
```

The ACL pattern is why large enterprise systems can evolve. New external integrations add new adapters at the boundary. The domain model stays clean. Teams working on core business logic never import `CustomerRecord` or `ORDR_HDR`.

---

## Adapter vs Related Patterns

| Pattern | When | Key distinction |
|---------|------|----------------|
| Adapter | Two interfaces must work together; can't change either | Translates existing interfaces |
| Facade | Simplify a complex subsystem for callers | Provides a new simplified interface (not translation) |
| Decorator | Add behavior to an object | Same interface in and out; adds capability |
| Proxy | Control access to an object | Same interface in and out; controls access |

**Adapter changes the interface.** Facade, Decorator, and Proxy preserve it. This is the clearest distinguishing signal.

---

## The Anti-Pattern — Adapter Sprawl

Adapter Pattern has one failure mode: adapters everywhere, with no clear boundary.

When every external system gets an adapter, but adapters start calling each other, or adapters begin containing business logic — you've lost the separation.

**Adapter rules:**
1. Adapters translate interfaces. They contain no business logic.
2. Business logic belongs in the domain layer, not the adapter.
3. Mapping/translation code is acceptable — transformation logic is not.
4. If the adapter has a test for a business rule, the rule is in the wrong place.

```java
// WRONG — business logic in adapter
class StripeAdapter implements PaymentGateway {
    public PaymentResult charge(PaymentRequest request) {
        // Business rule does not belong here
        if (request.getAmount() > 10000 && !request.hasApproval()) {
            throw new PolicyException("High-value charge requires approval");
        }
        return stripe.charge(...);
    }
}

// RIGHT — adapter translates only
class StripeAdapter implements PaymentGateway {
    public PaymentResult charge(PaymentRequest request) {
        StripeChargeRequest stripeReq = translate(request);  // only translation
        StripeChargeResponse resp = stripe.charge(stripeReq);
        return translate(resp);
    }
}
// Approval check belongs in PaymentService, not the adapter
```

---

## The Interview Answer

**Question:** When would you use Adapter Pattern?

**Weak answer:** *"When two classes have incompatible interfaces."*

**Strong answer:**

*"Adapter Pattern solves interface incompatibility — when your system has a target interface that callers depend on, and an existing adaptee (third-party SDK, legacy system, external API) with a different interface that you can't modify. The adapter wraps the adaptee and translates calls from the target interface to the adaptee's interface. The value is decoupling your system from external contracts: swap the provider by swapping the adapter, with no changes to callers. The critical discipline is keeping adapters thin — only interface translation, no business logic. When I integrate third-party systems at work, every integration gets an adapter, and the adapter's test suite covers translation correctness, not business rules."*

---

## Key Takeaways

- Adapter translates between two incompatible interfaces without modifying either.
- Object adapter (hold adaptee as field) is preferred over class adapter (extend adaptee) in Java.
- Three scenarios: legacy integration, cloud provider portability, third-party library isolation.
- Adapter changes the interface. Facade/Decorator/Proxy preserve it — clearest distinguishing signal.
- Anti-pattern: adapters containing business logic. Adapters translate. Business logic belongs elsewhere.

---

*All papers and runnable samples: [github.com/pramod0s007/architect-notes](https://github.com/pramod0s007/architect-notes)*

*Previous → Paper 18: Proxy Pattern | Next → Paper 20: Facade Pattern*
