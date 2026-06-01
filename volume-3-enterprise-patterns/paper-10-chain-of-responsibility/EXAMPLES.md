# Chain of Responsibility Examples

## Example 1 - Authentication

```java
if(!authenticated(request))
    return unauthorized();
```

Handler stops chain or passes forward.

## Example 2 - Authorization

```java
if(!hasRole(request, "ADMIN"))
    return forbidden();
```

## Example 3 - Validation

```java
if(!validator.isValid(request))
    return badRequest();
```

## Example 4 - Rate Limiting

```java
if(limitExceeded(request))
    return tooManyRequests();
```

## Pipeline

```
Request
  → AuthHandler
  → AuthzHandler
  → ValidationHandler
  → RateLimitHandler
  → BusinessHandler
```

## Pressure

Sequential Decision Flow
