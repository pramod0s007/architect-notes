# Chain of Responsibility

*Every API gateway you've ever used is built on this pattern. Here's why it works.*

---

An API gateway service I once reviewed had grown from a clean 80-line handler to a 400-line method over eighteen months.

It wasn't a bad engineering team. Requirements arrived one at a time, each reasonable in isolation:

- Sprint 4: "Add JWT validation before processing requests"
- Sprint 7: "Rate limit unauthenticated callers separately from authenticated"
- Sprint 9: "Validate request body schema before touching business logic"
- Sprint 12: "Add IP blocklist check — security mandate"
- Sprint 15: "Log all requests with correlation IDs for debugging"
- Sprint 18: "Internal service requests bypass rate limiting"

Each addition made sense. The result didn't:

```java
void handleRequest(HttpRequest request) {
    // IP blocklist (added sprint 12, placed at top per security)
    if (ipBlocklist.contains(request.getClientIp())) {
        respond(403, "Forbidden");
        return;
    }

    // JWT validation (added sprint 4)
    User user = null;
    if (request.hasHeader("Authorization")) {
        try {
            user = jwtService.validate(request.getBearerToken());
        } catch (InvalidTokenException e) {
            respond(401, "Unauthorized");
            return;
        }
    }

    // Rate limiting (added sprint 7, modified sprint 18)
    String clientId = user != null ? user.getId() : request.getClientIp();
    boolean isInternalService = request.hasHeader("X-Internal-Service");
    if (!isInternalService) {
        RateLimitProfile profile = user != null
            ? RateLimitProfile.AUTHENTICATED
            : RateLimitProfile.ANONYMOUS;
        if (rateLimiter.isExceeded(clientId, profile)) {
            respond(429, "Too Many Requests");
            return;
        }
    }

    // Schema validation (added sprint 9)
    // ... 50 more lines
```

When the security team asked to add HMAC signature verification between IP blocklist and JWT validation, the estimate was three days. To add one stage to a pipeline.

**That's the signal.** Reordering or adding a stage shouldn't take three days.

Every HTTP request that hits a backend service passes through stages.

Authentication. Authorization. Rate limiting. Request validation. Logging. Business logic.

The first version of this code usually looks like this:

```java
void handleRequest(Request request) {
    authenticate(request);
    authorize(request);
    validateRequest(request);
    rateLimit(request);
    logRequest(request);
    processBusinessLogic(request);
}
```

Clean. Readable. Correct.

**Don't touch it.** Six sequential calls in one method is not a problem that needs a pattern.

---

## The Pressure

Then requirements arrive.

**Requirement 1:** Some endpoints skip authentication (health checks, public APIs).

**Requirement 2:** Rate limiting has three different profiles — authenticated users, anonymous users, internal services. The profile selection depends on the authentication result.

**Requirement 3:** A new compliance team wants to add request sanitization between validation and business logic. But only for requests from external partners.

**Requirement 4:** The security team wants to add IP blocklisting. It must run before authentication to prevent wasted compute.

The monolithic `handleRequest` method is now:
- A growing conditional tree
- Multiple teams touching the same file
- Stages that depend on results from previous stages
- Stages that conditionally skip based on request context

**This is sequential decision flow pressure.** Each stage can pass, modify, or terminate a request. The sequence is not fixed — it varies by endpoint, user type, and configuration.

---

## Chain of Responsibility

Each stage becomes an independent handler. Handlers form a linked list. Each handler decides: process and pass forward, or stop.

```java
abstract class Handler {

    private Handler next;

    Handler setNext(Handler next) {
        this.next = next;
        return next;
    }

    abstract void handle(Request request);

    protected void passToNext(Request request) {
        if (next != null) next.handle(request);
    }
}
```

Each stage is a concrete handler:

```java
class AuthenticationHandler extends Handler {

    @Override
    public void handle(Request request) {
        if (!request.hasValidToken()) {
            request.reject(401, "Unauthorized");
            return;  // Chain stops here
        }
        request.setAuthenticatedUser(tokenService.decode(request.getToken()));
        passToNext(request);
    }
}

class RateLimitHandler extends Handler {

    @Override
    public void handle(Request request) {
        RateLimitProfile profile = determineProfile(request);
        if (rateLimiter.isExceeded(request.getClientId(), profile)) {
            request.reject(429, "Too Many Requests");
            return;  // Chain stops here
        }
        passToNext(request);
    }
}

class ValidationHandler extends Handler {

    @Override
    public void handle(Request request) {
        List<String> errors = validator.validate(request.getBody());
        if (!errors.isEmpty()) {
            request.reject(400, "Validation failed: " + errors);
            return;  // Chain stops here
        }
        passToNext(request);
    }
}
```

The pipeline is assembled by wiring:

```java
Handler pipeline = new IpBlocklistHandler();
pipeline
    .setNext(new AuthenticationHandler())
    .setNext(new RateLimitHandler())
    .setNext(new ValidationHandler())
    .setNext(new LoggingHandler())
    .setNext(new BusinessLogicHandler());

pipeline.handle(incomingRequest);
```

---

## What This Buys You

**Reordering stages:** Move handlers in the assembly. The handlers themselves don't change.

**Adding stages:** New handler class, new `.setNext()` call. No modification to existing handlers.

**Conditional chains:** Build different pipelines for different endpoint profiles.

```java
// Public endpoint — skip auth and rate limiting
Handler publicPipeline = new ValidationHandler();
publicPipeline
    .setNext(new LoggingHandler())
    .setNext(new PublicBusinessLogicHandler());

// Authenticated endpoint
Handler authPipeline = new AuthenticationHandler();
authPipeline
    .setNext(new RateLimitHandler())
    .setNext(new ValidationHandler())
    .setNext(new LoggingHandler())
    .setNext(new AuthenticatedBusinessLogicHandler());
```

**Independent testing:** Each handler is testable in isolation. A `ValidationHandler` test doesn't need a live auth service. Pass a pre-built `Request`, assert the response.

---

## Where This Pattern Already Lives

You've been using Chain of Responsibility without the name:

**Spring `OncePerRequestFilter`:**
```java
// Every Spring filter is a chain link
public class JwtAuthFilter extends OncePerRequestFilter {
    protected void doFilterInternal(request, response, filterChain) {
        // validate token, set SecurityContext
        filterChain.doFilter(request, response); // pass to next
    }
}
```

**Express.js middleware:**
```javascript
app.use(helmet());          // security headers
app.use(rateLimit());       // rate limiting
app.use(authenticate);      // authentication
app.use('/api', router);    // business logic
```

**Servlet filters:**
```java
public void doFilter(request, response, FilterChain chain) {
    // pre-processing
    chain.doFilter(request, response); // pass to next
    // post-processing
}
```

**AWS API Gateway:** Authorizers, validators, throttling, Lambda integration — each is a chain stage with pass-or-stop semantics.

Every gateway, middleware stack, and filter chain in software is this pattern. The GoF name is optional. The structure is universal.

---

## Assembling Different Chains for Different Contexts

One of Chain of Responsibility's most powerful features is that the same handlers can be assembled into different pipelines for different contexts.

```java
class PipelineFactory {

    // Public API — full security + validation
    Handler buildPublicApiPipeline(BusinessLogicHandler businessLogic) {
        Handler ip       = new IpBlocklistHandler();
        Handler auth     = new AuthenticationHandler();
        Handler rateLimit = new RateLimitHandler(RateLimitProfile.PUBLIC);
        Handler validate = new ValidationHandler();
        Handler log      = new LoggingHandler();

        ip.setNext(auth).setNext(rateLimit).setNext(validate).setNext(log).setNext(businessLogic);
        return ip;
    }

    // Internal service — skip auth and rate limiting
    Handler buildInternalPipeline(BusinessLogicHandler businessLogic) {
        Handler validate = new ValidationHandler();
        Handler log      = new LoggingHandler();

        validate.setNext(log).setNext(businessLogic);
        return validate;
    }

    // Health check — pass through everything
    Handler buildHealthCheckPipeline(BusinessLogicHandler businessLogic) {
        return businessLogic;
    }
}
```

Same handlers. Three different pipeline configurations. Each handler knows nothing about which pipeline it's in.

This is how Spring Security's `HttpSecurity` DSL works under the hood — you're assembling a filter chain, choosing which handlers to include and in what order, for different URL patterns and security requirements.

## Short-Circuit Evaluation — Early Returns

A key distinction between Chain of Responsibility and a simple loop is that any handler can terminate the chain:

```java
class AuthenticationHandler extends Handler {

    @Override
    public void handle(Request request) {
        if (request.isHealthCheck()) {
            // Skip auth entirely for health checks
            passToNext(request);
            return;
        }

        if (!request.hasAuthHeader()) {
            request.reject(401, "Missing Authorization header");
            return;  // Chain terminates here — next handlers never run
        }

        try {
            User user = tokenService.validate(request.getBearerToken());
            request.setAuthenticatedUser(user);
            passToNext(request);
        } catch (InvalidTokenException e) {
            request.reject(401, "Invalid token: " + e.getMessage());
            // No passToNext — chain stops
        }
    }
}
```

This early termination is what makes the pattern efficient for security pipelines — an invalid IP is blocked at the first handler, never reaching authentication, validation, or business logic. Failed fast, minimum compute.

## Chain of Responsibility vs Similar Patterns

**vs Command Pattern:** Command encapsulates *what* an operation does so it can travel (be stored, undone, deferred). Chain routes a request through *stages* that may stop or pass it. Different pressures.

**vs Specification Pattern:** Specification composes rules (AND/OR/NOT) for eligibility decisions. Chain is an ordered pipeline where each stage may terminate flow. Rules don't have order. Pipeline stages do.

**vs Decorator:** Decorator wraps behavior unconditionally — all decorators always execute and the core always runs. Chain can terminate early — a handler stops the pipeline without calling the next link.

---

## The Interview Answer

**Question:** When should Chain of Responsibility be used?

**Weak answer:** *"For any multi-step process."*

**Strong answer:**

*"Chain of Responsibility addresses sequential decision flow — when a request passes through ordered stages and any stage may pass control forward or terminate the flow. The signal is a processing pipeline where stages need to be reorderable, independently testable, and conditionally assembled. It's distinct from Specification Pattern (which composes rules with AND/OR/NOT) and Command Pattern (which encapsulates operations for travel). In practice you see it in every HTTP middleware stack, servlet filter chain, and API gateway. I'd introduce it when a monolithic handler method starts accumulating conditionals that control which stages run — that's the pipeline becoming implicit, and Chain makes it explicit."*

---

## Key Takeaways

- Chain of Responsibility models **sequential decision flow** where any stage can stop.
- Stages (handlers) are independently testable and reorderable.
- The pipeline is assembled at wiring time — same handlers, different configurations.
- Every HTTP framework's middleware/filter system is this pattern.
- Distinct from Specification (rules), Command (encapsulated operations), Decorator (unconditional wrapping).

---

*All papers and runnable Java samples: [github.com/pramod0s007/architect-notes](https://github.com/pramod0s007/architect-notes)*

*Previous → Paper 09: Specification Pattern | Next → Paper 11: Builder Pattern*
