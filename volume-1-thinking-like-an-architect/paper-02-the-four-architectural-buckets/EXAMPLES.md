# Bucket Classification Examples

## Example 1 - Report Generation

```java
generateReport("csv");
generateReport("json");
generateReport("xml");
```

**Classification:** Data Variation

**Reason:**

The algorithm remains the same.

Only the output data format changes.

**Typical Solutions:**

- Configuration
- Templates
- Parameterization

## Example 2 - Storage Providers

```java
Storage storage = new S3Storage();
Storage storage = new AzureBlobStorage();
Storage storage = new LocalFileStorage();
```

**Classification:** Object Variation

**Reason:**

The workflow remains the same.

The participating object changes.

**Typical Solutions:**

- Interfaces
- Composition
- Dependency Injection

## Example 3 - Encryption

```java
encrypt("AES");
encrypt("DES");
encrypt("BLOWFISH");
```

**Classification:** Behavior Variation

**Reason:**

The algorithm itself changes.

**Typical Solutions:**

- Strategy Pattern
- Command Pattern

## Example 4 - Product Search

```
price > 1000
AND category = Electronics
AND rating > 4
```

**Classification:** Rules Variation

**Reason:**

Business rules grow independently.

**Typical Solutions:**

- Specification Pattern
- Rule Engines
- Decision Tables

## Architect Exercise

For every design problem ask:

- Is data changing?
- Is the object changing?
- Is the behavior changing?
- Are the rules changing?

Correct classification usually reveals the correct abstraction.
