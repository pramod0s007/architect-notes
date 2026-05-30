# Factory Pattern Examples

## Example 1 - NotificationFactory

**Before**

```java
if (type.equals("EMAIL")) return new EmailNotifier();
if (type.equals("SMS")) return new SmsNotifier();
```

**After**

```java
Notifier notifier = NotificationFactory.create(type);
```

## Example 2 - StorageFactory

**Before**

Environment-specific `new S3Client()` / `new BlobClient()` in services.

**After**

```java
Storage storage = StorageFactory.forProfile(profile);
```

## Example 3 - PaymentFactory

**Before**

Switch on payment method at checkout.

**After**

```java
PaymentGateway gateway = PaymentFactory.create(method);
```

## Pressure

Object Creation Variation
