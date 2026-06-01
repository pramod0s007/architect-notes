# Builder Pattern Examples

## Example 1 - HTTP Request

**Before**

```java
new HttpRequest("POST", url, headers, body, 30, true, retry);
```

**After**

```java
HttpRequest.builder()
    .post(url)
    .headers(headers)
    .body(body)
    .timeoutSeconds(30)
    .build();
```

## Example 2 - Database Configuration

**Before**

Multiple overloaded constructors for SSL on/off, pool size, read replicas.

**After**

```java
DatabaseConfig.builder()
    .url(jdbcUrl)
    .poolSize(20)
    .sslEnabled(true)
    .build();
```

## Example 3 - Search Request

**Before**

```java
if (hasFilters && hasSort && hasPagination) { ... }
```

**After**

```java
SearchRequest.builder()
    .query("laptop")
    .filter("brand", "acme")
    .page(0, 20)
    .sort("price", DESC)
    .build();
```

## Pressure

Complex Object Construction
